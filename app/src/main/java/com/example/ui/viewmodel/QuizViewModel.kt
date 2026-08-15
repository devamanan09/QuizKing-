package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.engine.RagKnowledgeDoc
import com.example.data.local.*
import com.example.data.repository.AiCoachInsight
import com.example.data.repository.QuizRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class GameState {
    IDLE,
    MATCHMAKING,
    PLAYING,
    ROUND_FEEDBACK,
    MATCH_FINISHED
}

data class OpponentProfile(
    val name: String,
    val avatar: String,
    val rating: Int,
    val league: String,
    val winRate: String
)

data class TournamentMatch(
    val id: String,
    val player1: String,
    val player2: String,
    val score1: Int = 0,
    val score2: Int = 0,
    val winner: String? = null,
    val roundName: String // "Quarter-Final", "Semi-Final", "Grand Final"
)

data class QuizUiState(
    val currentScreen: String = "home", // home, duel, tournament, practice, daily, leaderboard, aicoach, profile, store
    val gameState: GameState = GameState.IDLE,
    val matchMode: String = "Quick Duel",
    val activeCategory: String = "All",
    val opponent: OpponentProfile? = null,
    val activeQuestions: List<QuestionEntity> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val timeRemainingSeconds: Int = 10,
    val selectedOptionIndex: Int? = null,
    val isAnswerSubmitted: Boolean = false,
    val playerScore: Int = 0,
    val opponentScore: Int = 0,
    val currentCombo: Int = 0,
    val answeredQuestions: List<Pair<QuestionEntity, Boolean>> = emptyList(),
    val lastAiInsight: AiCoachInsight? = null,
    val earnedXp: Int = 0,
    val earnedCoins: Int = 0,
    val ratingDelta: Int = 0,
    val isWin: Boolean = false,
    val tournamentBracket: List<TournamentMatch> = emptyList(),
    val tournamentRound: Int = 1,
    val isGeneratingRag: Boolean = false,
    val ragGeneratedCount: Int = 0,
    val showConfetti: Boolean = false
)

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val database = QuizDatabase.getInstance(application)
    private val repository = QuizRepository(database)

    val userStats: StateFlow<UserStatsEntity> = repository.userStatsFlow
        .map { it ?: UserStatsEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserStatsEntity())

    val quests: StateFlow<List<QuestEntity>> = repository.questsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievements: StateFlow<List<AchievementEntity>> = repository.achievementsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val matchHistory: StateFlow<List<MatchHistoryEntity>> = repository.matchHistoryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cosmetics: StateFlow<List<CosmeticEntity>> = repository.cosmeticsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalApprovedQuestions: StateFlow<Int> = repository.totalApprovedCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 30)

    val allQuestions: StateFlow<List<QuestionEntity>> = repository.allQuestionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    val ragKnowledgeDocs: List<RagKnowledgeDoc> = repository.getKnowledgeDocs()

    fun navigateTo(screen: String) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    /**
     * Matchmaking and starting a Quick Duel or Ranked match.
     */
    fun startMatch(mode: String = "Quick Duel", category: String = "All") {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    currentScreen = "duel",
                    gameState = GameState.MATCHMAKING,
                    matchMode = mode,
                    activeCategory = category,
                    playerScore = 0,
                    opponentScore = 0,
                    currentCombo = 0,
                    currentQuestionIndex = 0,
                    selectedOptionIndex = null,
                    isAnswerSubmitted = false,
                    answeredQuestions = emptyList(),
                    lastAiInsight = null,
                    showConfetti = false
                )
            }

            // Fetch questions from Room repository
            val questions = repository.getQuestionsForDuel(count = 5, category = category)

            // Simulate opponent matchmaking
            delay(1600)
            val opponents = listOf(
                OpponentProfile("CyberPulse", "avatar_neon_falcon", 1280, "Silver", "64%"),
                OpponentProfile("VortexRider", "avatar_cosmic_sage", 1340, "Silver", "68%"),
                OpponentProfile("NovaQueen", "avatar_cyber_king", 1420, "Gold", "72%"),
                OpponentProfile("QuantumAce", "avatar_esports_titan", 1210, "Silver", "58%"),
                OpponentProfile("HelixMind", "avatar_neon_falcon", 1390, "Gold", "70%")
            )
            val selectedOpponent = opponents.random()

            _uiState.update {
                it.copy(
                    gameState = GameState.PLAYING,
                    opponent = selectedOpponent,
                    activeQuestions = questions,
                    timeRemainingSeconds = 10
                )
            }

            startQuestionTimer()
        }
    }

    private fun startQuestionTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            _uiState.update { it.copy(timeRemainingSeconds = 10) }
            for (sec in 9 downTo 0) {
                delay(1000)
                if (_uiState.value.isAnswerSubmitted) break
                _uiState.update { it.copy(timeRemainingSeconds = sec) }
            }
            if (!_uiState.value.isAnswerSubmitted) {
                // Time expired -> treat as timeout
                submitAnswer(-1)
            }
        }
    }

    fun submitAnswer(optionIndex: Int) {
        if (_uiState.value.isAnswerSubmitted) return
        timerJob?.cancel()

        val state = _uiState.value
        val currentQ = state.activeQuestions.getOrNull(state.currentQuestionIndex) ?: return
        val isCorrect = optionIndex == currentQ.correctIndex

        // Calculate score with speed bonus & combo
        val speedMultiplier = maxOf(1, state.timeRemainingSeconds)
        val comboMultiplier = if (isCorrect) state.currentCombo + 1 else 0
        val points = if (isCorrect) (100 + speedMultiplier * 10) * (1 + comboMultiplier * 0.2f).toInt() else 0

        // Simulate realistic opponent behavior (70% accuracy with slight delay)
        val opponentCorrect = Random.nextFloat() < 0.72f
        val opponentPoints = if (opponentCorrect) (90 + Random.nextInt(20, 90)) else 0

        val updatedAnswered = state.answeredQuestions + Pair(currentQ, isCorrect)

        _uiState.update {
            it.copy(
                selectedOptionIndex = optionIndex,
                isAnswerSubmitted = true,
                gameState = GameState.ROUND_FEEDBACK,
                playerScore = state.playerScore + points,
                opponentScore = state.opponentScore + opponentPoints,
                currentCombo = comboMultiplier,
                answeredQuestions = updatedAnswered
            )
        }

        viewModelScope.launch {
            delay(1800)
            if (state.currentQuestionIndex + 1 < state.activeQuestions.size) {
                // Next question
                _uiState.update {
                    it.copy(
                        currentQuestionIndex = it.currentQuestionIndex + 1,
                        selectedOptionIndex = null,
                        isAnswerSubmitted = false,
                        gameState = GameState.PLAYING
                    )
                }
                startQuestionTimer()
            } else {
                // Match finished!
                finishMatch()
            }
        }
    }

    private suspend fun finishMatch() {
        val state = _uiState.value
        val isWin = state.playerScore > state.opponentScore
        val accuracy = if (state.answeredQuestions.isNotEmpty()) {
            state.answeredQuestions.count { it.second }.toFloat() / state.answeredQuestions.size
        } else 0f

        val (updatedStats, insight) = repository.recordMatchCompletion(
            mode = state.matchMode,
            opponentName = state.opponent?.name ?: "Opponent",
            opponentAvatar = state.opponent?.avatar ?: "avatar_neon_falcon",
            playerScore = state.playerScore,
            opponentScore = state.opponentScore,
            category = state.activeCategory,
            accuracy = accuracy,
            questionAnswers = state.answeredQuestions
        )

        val winBonus = if (isWin) 120 else 40
        val earnedXp = (state.playerScore * 1.5f).toInt() + winBonus
        val earnedCoins = if (isWin) 50 + (accuracy * 20).toInt() else 15
        val ratingDelta = if (isWin) 24 else -16

        _uiState.update {
            it.copy(
                gameState = GameState.MATCH_FINISHED,
                lastAiInsight = insight,
                earnedXp = earnedXp,
                earnedCoins = earnedCoins,
                ratingDelta = ratingDelta,
                isWin = isWin,
                showConfetti = isWin
            )
        }
    }

    fun startPractice(category: String, minDiff: Int, maxDiff: Int) {
        viewModelScope.launch {
            val questions = repository.getPracticeQuestions(category, minDiff, maxDiff, count = 5)
            _uiState.update {
                it.copy(
                    currentScreen = "duel",
                    gameState = GameState.PLAYING,
                    matchMode = "Practice Arena",
                    activeCategory = category,
                    opponent = OpponentProfile("AI Sparring Bot", "avatar_cyber_king", 1200, "Silver", "N/A"),
                    activeQuestions = questions,
                    playerScore = 0,
                    opponentScore = 0,
                    currentCombo = 0,
                    currentQuestionIndex = 0,
                    selectedOptionIndex = null,
                    isAnswerSubmitted = false,
                    answeredQuestions = emptyList(),
                    lastAiInsight = null,
                    showConfetti = false
                )
            }
            startQuestionTimer()
        }
    }

    fun startDailyChallenge() {
        startMatch(mode = "Daily Challenge", category = "All")
    }

    fun initTournament() {
        val bracket = listOf(
            TournamentMatch("QF_1", "You (QuizMaster)", "VortexRider", 0, 0, null, "Quarter-Final"),
            TournamentMatch("QF_2", "NovaQueen", "CyberPulse", 240, 180, "NovaQueen", "Quarter-Final"),
            TournamentMatch("QF_3", "QuantumAce", "HelixMind", 310, 290, "QuantumAce", "Quarter-Final"),
            TournamentMatch("QF_4", "ApexPredator", "SolarFlare", 290, 210, "ApexPredator", "Quarter-Final"),
            TournamentMatch("SF_1", "TBD", "NovaQueen", 0, 0, null, "Semi-Final"),
            TournamentMatch("SF_2", "QuantumAce", "ApexPredator", 0, 0, null, "Semi-Final"),
            TournamentMatch("FINAL", "TBD", "TBD", 0, 0, null, "Grand Final")
        )
        _uiState.update {
            it.copy(
                tournamentBracket = bracket,
                tournamentRound = 1,
                currentScreen = "tournament"
            )
        }
    }

    fun playTournamentMatch() {
        startMatch(mode = "Tournament Arena", category = "All")
    }

    fun claimQuest(questId: String, xp: Int, coins: Int) {
        viewModelScope.launch {
            repository.claimQuest(questId, xp, coins)
        }
    }

    fun purchaseOrEquipCosmetic(cosmetic: CosmeticEntity) {
        viewModelScope.launch {
            repository.equipOrUnlockCosmetic(cosmetic)
        }
    }

    fun triggerAutomatedRagGeneration() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingRag = true) }
            delay(1200) // Visual pipeline progression
            val added = repository.runAutomatedRagGeneration()
            _uiState.update {
                it.copy(
                    isGeneratingRag = false,
                    ragGeneratedCount = it.ragGeneratedCount + added
                )
            }
        }
    }
}
