package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReelNagDao {
    // --- Saved Reels ---
    @Query("SELECT * FROM saved_reels ORDER BY savedAt DESC")
    fun getAllReels(): Flow<List<SavedReelEntity>>

    @Query("SELECT * FROM saved_reels ORDER BY savedAt DESC")
    suspend fun getAllReelsOnce(): List<SavedReelEntity>

    @Query("SELECT * FROM saved_reels WHERE normalizedUrl = :normalizedUrl LIMIT 1")
    suspend fun findReelByNormalizedUrl(normalizedUrl: String): SavedReelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReel(reel: SavedReelEntity): Long

    @Update
    suspend fun updateReel(reel: SavedReelEntity)

    @Delete
    suspend fun deleteReel(reel: SavedReelEntity)

    @Query("DELETE FROM saved_reels")
    suspend fun deleteAllReels()

    // --- Categories ---
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, name ASC")
    suspend fun getAllCategoriesOnce(): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("UPDATE saved_reels SET categoryName = :newName WHERE categoryName = :oldName")
    suspend fun renameCategoryInReels(oldName: String, newName: String)

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()

    // --- Nag Reminders ---
    @Query("SELECT * FROM nag_reminders ORDER BY scheduledAt ASC")
    fun getAllReminders(): Flow<List<NagReminderEntity>>

    @Query("SELECT * FROM nag_reminders ORDER BY scheduledAt ASC")
    suspend fun getAllRemindersOnce(): List<NagReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: NagReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: NagReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: NagReminderEntity)

    @Query("DELETE FROM nag_reminders WHERE reelId = :reelId")
    suspend fun deleteRemindersForReel(reelId: Long)

    @Query("DELETE FROM nag_reminders")
    suspend fun deleteAllReminders()

    // --- Trend Radar ---
    @Query("SELECT * FROM trend_items ORDER BY id DESC")
    fun getAllTrends(): Flow<List<TrendItemEntity>>

    @Query("SELECT * FROM trend_items ORDER BY id DESC")
    suspend fun getAllTrendsOnce(): List<TrendItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrend(trend: TrendItemEntity): Long

    @Update
    suspend fun updateTrend(trend: TrendItemEntity)

    @Delete
    suspend fun deleteTrend(trend: TrendItemEntity)

    @Query("DELETE FROM trend_items")
    suspend fun deleteAllTrends()

    // --- Content Planner ---
    @Query("SELECT * FROM planner_items ORDER BY createdAt DESC")
    fun getAllPlannerItems(): Flow<List<PlannerItemEntity>>

    @Query("SELECT * FROM planner_items ORDER BY createdAt DESC")
    suspend fun getAllPlannerItemsOnce(): List<PlannerItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlannerItem(item: PlannerItemEntity): Long

    @Update
    suspend fun updatePlannerItem(item: PlannerItemEntity)

    @Delete
    suspend fun deletePlannerItem(item: PlannerItemEntity)

    @Query("DELETE FROM planner_items")
    suspend fun deleteAllPlannerItems()

    // --- Saved AI Concepts ---
    @Query("SELECT * FROM saved_ai_concepts ORDER BY createdAt DESC")
    fun getAllSavedAiConcepts(): Flow<List<SavedAiConceptEntity>>

    @Query("SELECT * FROM saved_ai_concepts ORDER BY createdAt DESC")
    suspend fun getAllSavedAiConceptsOnce(): List<SavedAiConceptEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedAiConcept(concept: SavedAiConceptEntity): Long

    @Delete
    suspend fun deleteSavedAiConcept(concept: SavedAiConceptEntity)

    @Query("DELETE FROM saved_ai_concepts")
    suspend fun deleteAllSavedAiConcepts()
}

@Database(
    entities = [
        SavedReelEntity::class,
        CategoryEntity::class,
        NagReminderEntity::class,
        TrendItemEntity::class,
        PlannerItemEntity::class,
        SavedAiConceptEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ReelNagDatabase : RoomDatabase() {
    abstract fun dao(): ReelNagDao

    companion object {
        @Volatile
        private var INSTANCE: ReelNagDatabase? = null

        fun getInstance(context: Context): ReelNagDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReelNagDatabase::class.java,
                    "reelnag_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
