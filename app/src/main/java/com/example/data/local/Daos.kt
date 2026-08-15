package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions WHERE status = 'APPROVED' ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuestions(limit: Int): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE status = 'APPROVED' AND category = :category ORDER BY RANDOM() LIMIT :limit")
    suspend fun getQuestionsByCategory(category: String, limit: Int): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE status = 'APPROVED' AND difficulty BETWEEN :minDiff AND :maxDiff ORDER BY RANDOM() LIMIT :limit")
    suspend fun getQuestionsByDifficultyRange(minDiff: Int, maxDiff: Int, limit: Int): List<QuestionEntity>

    @Query("SELECT * FROM questions ORDER BY createdAt DESC")
    fun getAllQuestionsFlow(): Flow<List<QuestionEntity>>

    @Query("SELECT COUNT(*) FROM questions WHERE status = 'APPROVED'")
    fun getApprovedQuestionCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getTotalQuestionCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuestionEntity>)

    @Update
    suspend fun updateQuestion(question: QuestionEntity)

    @Query("UPDATE questions SET timesShown = timesShown + 1, timesCorrect = timesCorrect + :isCorrect, avgResponseTimeMs = (:responseTimeMs + avgResponseTimeMs) / 2 WHERE id = :id")
    suspend fun recordQuestionAnalytics(id: Long, isCorrect: Int, responseTimeMs: Long)
}

@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getUserStatsFlow(): Flow<UserStatsEntity?>

    @Query("SELECT * FROM user_stats WHERE id = 1")
    suspend fun getUserStats(): UserStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: UserStatsEntity)
}

@Dao
interface QuestDao {
    @Query("SELECT * FROM quests ORDER BY id ASC")
    fun getQuestsFlow(): Flow<List<QuestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(quests: List<QuestEntity>)

    @Update
    suspend fun updateQuest(quest: QuestEntity)

    @Query("UPDATE quests SET currentProgress = MIN(targetProgress, currentProgress + :amount) WHERE id = :id")
    suspend fun incrementQuestProgress(id: String, amount: Int = 1)

    @Query("UPDATE quests SET isClaimed = 1 WHERE id = :id")
    suspend fun claimQuestReward(id: String)
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY isUnlocked DESC, id ASC")
    fun getAchievementsFlow(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(achievements: List<AchievementEntity>)

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)
}

@Dao
interface MatchHistoryDao {
    @Query("SELECT * FROM match_history ORDER BY timestamp DESC LIMIT 20")
    fun getMatchHistoryFlow(): Flow<List<MatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchHistoryEntity)
}

@Dao
interface CosmeticDao {
    @Query("SELECT * FROM cosmetics ORDER BY rarity DESC, priceCoins ASC")
    fun getCosmeticsFlow(): Flow<List<CosmeticEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cosmetics: List<CosmeticEntity>)

    @Update
    suspend fun updateCosmetic(cosmetic: CosmeticEntity)
}
