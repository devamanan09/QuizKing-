package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val category: String, // e.g. "Science", "Technology", "History", "Geography", "Pop Culture", "Sports", "Literature", "Mathematics", "Space"
    val topic: String,
    val difficulty: Int, // 1 to 10 scale
    val qualityScore: Float = 0.95f,
    val sourceReference: String = "Curated Knowledge Corpus",
    val timesShown: Int = 0,
    val timesCorrect: Int = 0,
    val avgResponseTimeMs: Long = 4000L,
    val status: String = "APPROVED", // "APPROVED", "VALIDATING", "RETIRED"
    val language: String = "en",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey
    val id: Int = 1,
    val username: String = "QuizMaster",
    val avatarId: String = "avatar_cyber_king",
    val frameId: String = "frame_cyan_neon",
    val level: Int = 1,
    val xp: Long = 250L,
    val xpForNextLevel: Long = 500L,
    val rating: Int = 1250, // Elo rating
    val league: String = "Silver", // Bronze, Silver, Gold, Platinum, Diamond, Master
    val virtualCoins: Int = 450, // Non-cash progression currency
    val currentStreak: Int = 3,
    val bestStreak: Int = 8,
    val totalMatches: Int = 12,
    val wins: Int = 9,
    val losses: Int = 3,
    val perfectMatches: Int = 2,
    val lastPlayedDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "quests")
data class QuestEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val currentProgress: Int,
    val targetProgress: Int,
    val xpReward: Int,
    val coinReward: Int,
    val isClaimed: Boolean = false,
    val category: String = "Daily"
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val isUnlocked: Boolean = false,
    val progress: Int = 0,
    val maxProgress: Int = 1,
    val unlockedAt: Long = 0L,
    val rewardXp: Int = 100
)

@Entity(tableName = "match_history")
data class MatchHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mode: String, // "Quick Duel", "Ranked Match", "Tournament", "Practice", "Daily Challenge"
    val opponentName: String,
    val opponentAvatar: String,
    val playerScore: Int,
    val opponentScore: Int,
    val isWin: Boolean,
    val xpEarned: Int,
    val ratingDelta: Int,
    val category: String,
    val accuracy: Float,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "cosmetics")
data class CosmeticEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: String, // "AVATAR", "FRAME", "BADGE", "THEME"
    val priceCoins: Int,
    val isUnlocked: Boolean = false,
    val isEquipped: Boolean = false,
    val icon: String,
    val rarity: String = "RARE" // COMMON, RARE, EPIC, LEGENDARY
)

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.joinToString("|||") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        return if (value.isNullOrEmpty()) emptyList() else value.split("|||")
    }
}
