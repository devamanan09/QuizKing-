package com.example.data.repository

import com.example.data.engine.RagKnowledgeCorpus
import com.example.data.engine.RagKnowledgeDoc
import com.example.data.engine.RagQuestionEngine
import com.example.data.engine.ValidationResult
import com.example.data.local.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.roundToInt

data class AiCoachInsight(
    val title: String,
    val summary: String,
    val strongCategory: String,
    val weakCategory: String,
    val recommendation: String,
    val estimatedAccuracy: Float,
    val speedRating: String
)

class QuizRepository(private val database: QuizDatabase) {

    private val ragEngine = RagQuestionEngine()

    val userStatsFlow: Flow<UserStatsEntity?> = database.userStatsDao().getUserStatsFlow()
    val questsFlow: Flow<List<QuestEntity>> = database.questDao().getQuestsFlow()
    val achievementsFlow: Flow<List<AchievementEntity>> = database.achievementDao().getAchievementsFlow()
    val matchHistoryFlow: Flow<List<MatchHistoryEntity>> = database.matchHistoryDao().getMatchHistoryFlow()
    val cosmeticsFlow: Flow<List<CosmeticEntity>> = database.cosmeticDao().getCosmeticsFlow()
    val allQuestionsFlow: Flow<List<QuestionEntity>> = database.questionDao().getAllQuestionsFlow()
    val totalApprovedCountFlow: Flow<Int> = database.questionDao().getApprovedQuestionCountFlow()

    suspend fun getQuestionsForDuel(count: Int = 5, category: String? = null): List<QuestionEntity> = withContext(Dispatchers.IO) {
        val questions = if (category != null && category != "All") {
            database.questionDao().getQuestionsByCategory(category, count)
        } else {
            database.questionDao().getRandomQuestions(count)
        }
        if (questions.isEmpty()) {
            SeedData.getDefaultQuestions().shuffled().take(count)
        } else {
            questions
        }
    }

    suspend fun getPracticeQuestions(category: String, minDiff: Int, maxDiff: Int, count: Int = 5): List<QuestionEntity> = withContext(Dispatchers.IO) {
        val list = if (category == "All") {
            database.questionDao().getQuestionsByDifficultyRange(minDiff, maxDiff, count)
        } else {
            database.questionDao().getQuestionsByCategory(category, count)
        }
        if (list.isEmpty()) {
            SeedData.getDefaultQuestions().filter { it.difficulty in minDiff..maxDiff }.take(count)
        } else {
            list
        }
    }

    /**
     * Completes a duel/match: Updates Elo rating, XP, level, coins, streak, quest progress, and records history.
     */
    suspend fun recordMatchCompletion(
        mode: String,
        opponentName: String,
        opponentAvatar: String,
        playerScore: Int,
        opponentScore: Int,
        category: String,
        accuracy: Float,
        questionAnswers: List<Pair<QuestionEntity, Boolean>>
    ): Pair<UserStatsEntity, AiCoachInsight> = withContext(Dispatchers.IO) {
        val currentStats = database.userStatsDao().getUserStats() ?: UserStatsEntity()
        val isWin = playerScore > opponentScore

        // Calculate XP, Coins, and Elo Rating Delta
        val baseScore = playerScore
        val winBonus = if (isWin) 120 else 40
        val streakBonus = currentStats.currentStreak * 10
        val earnedXp = (baseScore * 1.5f).toInt() + winBonus + streakBonus
        val earnedCoins = if (isWin) 50 + (accuracy * 20).toInt() else 15

        // Elo Rating calculation
        val expectedScore = 0.5f
        val kFactor = 32
        val actualResult = if (playerScore > opponentScore) 1.0f else if (playerScore == opponentScore) 0.5f else 0.0f
        val ratingDelta = (kFactor * (actualResult - expectedScore) + (accuracy * 10 - 5)).roundToInt()
        val newRating = max(100, currentStats.rating + ratingDelta)

        // League calculation
        val newLeague = when {
            newRating >= 2500 -> "Master"
            newRating >= 2100 -> "Diamond"
            newRating >= 1800 -> "Platinum"
            newRating >= 1500 -> "Gold"
            newRating >= 1200 -> "Silver"
            else -> "Bronze"
        }

        // Level & XP calculation
        var newXp = currentStats.xp + earnedXp
        var newLevel = currentStats.level
        var xpRequired = currentStats.xpForNextLevel
        while (newXp >= xpRequired) {
            newXp -= xpRequired
            newLevel += 1
            xpRequired = (xpRequired * 1.35f).toLong()
        }

        val newStreak = if (isWin) currentStats.currentStreak + 1 else 0
        val bestStreak = max(currentStats.bestStreak, newStreak)

        val updatedStats = currentStats.copy(
            level = newLevel,
            xp = newXp,
            xpForNextLevel = xpRequired,
            rating = newRating,
            league = newLeague,
            virtualCoins = currentStats.virtualCoins + earnedCoins,
            currentStreak = newStreak,
            bestStreak = bestStreak,
            totalMatches = currentStats.totalMatches + 1,
            wins = currentStats.wins + (if (isWin) 1 else 0),
            losses = currentStats.losses + (if (!isWin && playerScore != opponentScore) 1 else 0),
            perfectMatches = currentStats.perfectMatches + (if (accuracy >= 1.0f) 1 else 0),
            lastPlayedDate = System.currentTimeMillis()
        )

        database.userStatsDao().insertOrUpdate(updatedStats)

        // Record match history
        database.matchHistoryDao().insertMatch(
            MatchHistoryEntity(
                mode = mode,
                opponentName = opponentName,
                opponentAvatar = opponentAvatar,
                playerScore = playerScore,
                opponentScore = opponentScore,
                isWin = isWin,
                xpEarned = earnedXp,
                ratingDelta = ratingDelta,
                category = category,
                accuracy = accuracy
            )
        )

        // Increment quest progress
        database.questDao().incrementQuestProgress("quest_duel_3", 1)
        if (isWin && accuracy >= 0.7f) {
            database.questDao().incrementQuestProgress("quest_win_2", 1)
        }
        val scienceCorrect = questionAnswers.count { it.first.category == "Science" && it.second }
        if (scienceCorrect > 0) {
            database.questDao().incrementQuestProgress("quest_science_5", scienceCorrect)
        }
        if (accuracy >= 0.8f) {
            database.questDao().incrementQuestProgress("quest_streak_perfect", 1)
        }

        // Record individual question analytics to improve dynamic difficulty
        questionAnswers.forEach { (q, correct) ->
            database.questionDao().recordQuestionAnalytics(
                id = q.id,
                isCorrect = if (correct) 1 else 0,
                responseTimeMs = 3500L
            )
        }

        // Generate AI Coach post-match insight
        val correctCategories = questionAnswers.filter { it.second }.map { it.first.category }
        val incorrectCategories = questionAnswers.filter { !it.second }.map { it.first.category }

        val strong = correctCategories.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: "Science & Technology"
        val weak = incorrectCategories.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: "Geography & History"

        val insight = AiCoachInsight(
            title = if (isWin) "Dominant Tactical Performance!" else "Valuable Combat Learning Experience",
            summary = "You demonstrated ${(accuracy * 100).toInt()}% precision. Your fastest answers scored high multiplier streaks.",
            strongCategory = strong,
            weakCategory = weak,
            recommendation = "Focus 3 practice drills on $weak concepts to boost your win rate in $newLeague league matches.",
            estimatedAccuracy = accuracy,
            speedRating = if (accuracy >= 0.8f) "S-Tier (Lightning)" else "A-Tier (Solid)"
        )

        Pair(updatedStats, insight)
    }

    suspend fun claimQuest(questId: String, xp: Int, coins: Int) = withContext(Dispatchers.IO) {
        database.questDao().claimQuestReward(questId)
        val currentStats = database.userStatsDao().getUserStats() ?: UserStatsEntity()
        var newXp = currentStats.xp + xp
        var newLevel = currentStats.level
        var xpRequired = currentStats.xpForNextLevel
        while (newXp >= xpRequired) {
            newXp -= xpRequired
            newLevel += 1
            xpRequired = (xpRequired * 1.35f).toLong()
        }
        val updated = currentStats.copy(
            level = newLevel,
            xp = newXp,
            xpForNextLevel = xpRequired,
            virtualCoins = currentStats.virtualCoins + coins
        )
        database.userStatsDao().insertOrUpdate(updated)
    }

    suspend fun equipOrUnlockCosmetic(cosmetic: CosmeticEntity): Boolean = withContext(Dispatchers.IO) {
        val currentStats = database.userStatsDao().getUserStats() ?: UserStatsEntity()
        if (!cosmetic.isUnlocked) {
            if (currentStats.virtualCoins >= cosmetic.priceCoins) {
                val newCoins = currentStats.virtualCoins - cosmetic.priceCoins
                database.userStatsDao().insertOrUpdate(currentStats.copy(virtualCoins = newCoins))
                database.cosmeticDao().updateCosmetic(cosmetic.copy(isUnlocked = true, isEquipped = true))
                if (cosmetic.type == "AVATAR") {
                    database.userStatsDao().insertOrUpdate(currentStats.copy(virtualCoins = newCoins, avatarId = cosmetic.id))
                } else if (cosmetic.type == "FRAME") {
                    database.userStatsDao().insertOrUpdate(currentStats.copy(virtualCoins = newCoins, frameId = cosmetic.id))
                }
                return@withContext true
            }
            return@withContext false
        } else {
            database.cosmeticDao().updateCosmetic(cosmetic.copy(isEquipped = true))
            if (cosmetic.type == "AVATAR") {
                database.userStatsDao().insertOrUpdate(currentStats.copy(avatarId = cosmetic.id))
            } else if (cosmetic.type == "FRAME") {
                database.userStatsDao().insertOrUpdate(currentStats.copy(frameId = cosmetic.id))
            }
            return@withContext true
        }
    }

    /**
     * Background/Admin Automated Question Generation & Validation Trigger.
     */
    suspend fun runAutomatedRagGeneration(): Int = withContext(Dispatchers.IO) {
        val currentQuestions = database.questionDao().getAllQuestionsFlow().firstOrNull() ?: emptyList()
        val newApproved = ragEngine.generateQuestionsFromRag(currentQuestions)
        if (newApproved.isNotEmpty()) {
            database.questionDao().insertAll(newApproved)
        }
        newApproved.size
    }

    fun getKnowledgeDocs(): List<RagKnowledgeDoc> = RagKnowledgeCorpus.documents
}
