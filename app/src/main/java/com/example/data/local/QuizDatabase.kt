package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        QuestionEntity::class,
        UserStatsEntity::class,
        QuestEntity::class,
        AchievementEntity::class,
        MatchHistoryEntity::class,
        CosmeticEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class QuizDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun questDao(): QuestDao
    abstract fun achievementDao(): AchievementDao
    abstract fun matchHistoryDao(): MatchHistoryDao
    abstract fun cosmeticDao(): CosmeticDao

    companion object {
        @Volatile
        private var INSTANCE: QuizDatabase? = null

        fun getInstance(context: Context): QuizDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuizDatabase::class.java,
                    "quizking_database.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            database.questionDao().insertAll(SeedData.getDefaultQuestions())
                            database.userStatsDao().insertOrUpdate(UserStatsEntity())
                            database.questDao().insertAll(SeedData.getDefaultQuests())
                            database.achievementDao().insertAll(SeedData.getDefaultAchievements())
                            database.cosmeticDao().insertAll(SeedData.getDefaultCosmetics())
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
