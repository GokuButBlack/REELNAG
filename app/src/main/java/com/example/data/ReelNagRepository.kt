package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

private val Context.dataStore by preferencesDataStore(name = "reelnag_prefs")

class ReelNagRepository(
    private val context: Context,
    private val dao: ReelNagDao
) {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    private val KEY_SETTINGS_JSON = stringPreferencesKey("user_settings_json")
    private val KEY_INITIAL_SEED_DONE = booleanPreferencesKey("initial_seed_done")

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_SETTINGS_JSON]
        if (raw.isNullOrBlank()) {
            UserSettings()
        } else {
            try {
                json.decodeFromString<UserSettings>(raw)
            } catch (e: Exception) {
                UserSettings()
            }
        }
    }

    val reelsFlow: Flow<List<SavedReelEntity>> = dao.getAllReels()
    val categoriesFlow: Flow<List<CategoryEntity>> = dao.getAllCategories()
    val remindersFlow: Flow<List<NagReminderEntity>> = dao.getAllReminders()
    val trendsFlow: Flow<List<TrendItemEntity>> = dao.getAllTrends()
    val plannerItemsFlow: Flow<List<PlannerItemEntity>> = dao.getAllPlannerItems()
    val savedAiConceptsFlow: Flow<List<SavedAiConceptEntity>> = dao.getAllSavedAiConcepts()

    suspend fun getSettingsOnce(): UserSettings = settingsFlow.first()

    suspend fun updateSettings(transform: (UserSettings) -> UserSettings) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_SETTINGS_JSON]?.let {
                try {
                    json.decodeFromString<UserSettings>(it)
                } catch (e: Exception) {
                    UserSettings()
                }
            } ?: UserSettings()
            val updated = transform(current)
            prefs[KEY_SETTINGS_JSON] = json.encodeToString(updated)
        }
    }

    suspend fun ensureSeeded() {
        val prefs = context.dataStore.data.first()
        val alreadySeeded = prefs[KEY_INITIAL_SEED_DONE] ?: false
        if (!alreadySeeded) {
            seedDefaultCategoriesAndSamples()
            context.dataStore.edit { it[KEY_INITIAL_SEED_DONE] = true }
        }
    }

    suspend fun seedDefaultCategoriesAndSamples() {
        val existingCats = dao.getAllCategoriesOnce()
        if (existingCats.isEmpty()) {
            DefaultData.DEFAULT_CATEGORIES.forEachIndexed { index, name ->
                dao.insertCategory(
                    CategoryEntity(
                        name = name,
                        isDefault = true,
                        sortOrder = index
                    )
                )
            }
        }

        val now = System.currentTimeMillis()
        val existingReels = dao.getAllReelsOnce()
        if (existingReels.isEmpty()) {
            val reel1Id = dao.insertReel(
                SavedReelEntity(
                    rawUrl = "https://www.instagram.com/reel/C8xSampleEdit01/",
                    normalizedUrl = "https://instagram.com/reel/C8xSampleEdit01",
                    title = "[Sample] Seamless Match-Cut Transition Tutorial",
                    description = "Step-by-step phone camera whip transition using just natural lighting and a doorframe.",
                    categoryName = "Editing Tutorials",
                    personalNotes = "Try this transition for my weekend coffee routine video! Needs 60fps.",
                    creatorName = "@visualsbykai (Sample)",
                    savedAt = now - TimeUnit.DAYS.toMillis(5),
                    isWatched = false,
                    isSample = true
                )
            )

            val reel2Id = dao.insertReel(
                SavedReelEntity(
                    rawUrl = "https://www.instagram.com/reel/C9aSampleRecipe02/",
                    normalizedUrl = "https://instagram.com/reel/C9aSampleRecipe02",
                    title = "[Sample] 15-Minute Crispy Chili Garlic Noodles",
                    description = "High-protein late night noodles you promised yourself you would cook instead of ordering takeout.",
                    categoryName = "Learn Later",
                    personalNotes = "Buy scallions and chili crisp before Friday.",
                    creatorName = "@midnightwok (Sample)",
                    savedAt = now - TimeUnit.DAYS.toMillis(3),
                    isWatched = false,
                    isSample = true
                )
            )

            val reel3Id = dao.insertReel(
                SavedReelEntity(
                    rawUrl = "https://www.instagram.com/reel/C9bSampleMobility03/",
                    normalizedUrl = "https://instagram.com/reel/C9bSampleMobility03",
                    title = "[Sample] 3-Minute Desk Posture & Neck Reset",
                    description = "Quick thoracic spine opener after staring at screens all afternoon.",
                    categoryName = "Fitness",
                    personalNotes = "Actually felt amazing—do this between editing sessions.",
                    creatorName = "@movewithmaya (Sample)",
                    savedAt = now - TimeUnit.DAYS.toMillis(2),
                    isWatched = true,
                    watchedAt = now - TimeUnit.DAYS.toMillis(1),
                    isSample = true
                )
            )

            dao.insertReel(
                SavedReelEntity(
                    rawUrl = "https://www.instagram.com/reel/C9cSampleHook04/",
                    normalizedUrl = "https://instagram.com/reel/C9cSampleHook04",
                    title = "[Sample] POV: Your Saved Folder Has 800 Unwatched Videos",
                    description = "Relatable comedy sketch on digital hoarding.",
                    categoryName = "Funny",
                    personalNotes = "Great audio pacing—could adapt this joke for tech/coding niche.",
                    creatorName = "@sketchyben (Sample)",
                    savedAt = now - TimeUnit.DAYS.toMillis(1),
                    isWatched = false,
                    isSample = true
                )
            )

            // Sample overdue and upcoming reminders
            dao.insertReminder(
                NagReminderEntity(
                    reelId = reel1Id,
                    itemTitle = "[Sample] Seamless Match-Cut Transition Tutorial",
                    categoryName = "Editing Tutorials",
                    scheduledAt = now - TimeUnit.HOURS.toMillis(2), // Overdue so user sees Due Now immediately
                    recurrence = ReminderRecurrence.ONCE.name,
                    snoozeCount = 1,
                    isCompleted = false,
                    isEnabled = true,
                    messagePreview = "Bro, you saved \"[Sample] Seamless Match-Cut Transition Tutorial\" 5 days ago. Are you collecting links as a hobby?",
                    isSample = true
                )
            )

            dao.insertReminder(
                NagReminderEntity(
                    reelId = reel2Id,
                    itemTitle = "[Sample] 15-Minute Crispy Chili Garlic Noodles",
                    categoryName = "Learn Later",
                    scheduledAt = now + TimeUnit.HOURS.toMillis(4),
                    recurrence = ReminderRecurrence.ONCE.name,
                    snoozeCount = 0,
                    isCompleted = false,
                    isEnabled = true,
                    messagePreview = "You have 3 unwatched Reels rotting in your locker, starting with \"[Sample] 15-Minute Crispy Chili Garlic Noodles\". Museum curator behavior.",
                    isSample = true
                )
            )
        }

        val existingTrends = dao.getAllTrendsOnce()
        if (existingTrends.isEmpty()) {
            val sampleTrends = listOf(
                TrendItemEntity(
                    name = "[Curated Sample] Reverse Storytime Hook",
                    description = "Start with the chaotic final frame for 1.5 seconds ('Here is how this ended...'), then rewind to step 1.",
                    category = "Editing Styles",
                    sourceUrl = "https://www.instagram.com/reel/C8xSampleEdit01/",
                    dateVerified = "Curated Format • Oct 2026",
                    creativeInspiration = "Film the finished project/meal/outfit first, freeze-frame with bold text, then cut to the messy beginning.",
                    isSample = true
                ),
                TrendItemEntity(
                    name = "[Curated Sample] Silent ASMR Micro-Routine",
                    description = "Zero voiceover—just crisp foley sounds (keyboard clicks, coffee pour, outfit snaps) with minimal subtitle commentary.",
                    category = "Lifestyle",
                    sourceUrl = "",
                    dateVerified = "Curated Format • Oct 2026",
                    creativeInspiration = "Record close-up 2-second clips of your workspace setup and let tactile audio carry the retention.",
                    isSample = true
                ),
                TrendItemEntity(
                    name = "[Curated Sample] Split-Screen Expectation vs. Reality",
                    description = "Top half shows the polished tutorial you saved; bottom half shows your honest first attempt trying it.",
                    category = "Comedy",
                    sourceUrl = "",
                    dateVerified = "Curated Format • Oct 2026",
                    creativeInspiration = "Pick the oldest unwatched tutorial in your Reel Locker and record your real-time reaction trying it.",
                    isSample = true
                ),
                TrendItemEntity(
                    name = "[Curated Sample] 3 Rapid-Fire AI Workflow Myths",
                    description = "Debunking three common misconceptions in your niche in under 25 seconds using whip-pans between points.",
                    category = "AI Content",
                    sourceUrl = "",
                    dateVerified = "Curated Format • Oct 2026",
                    creativeInspiration = "Hold up 3 fingers at the start and drop one finger as you bust each myth with on-screen proof.",
                    isSample = true
                ),
                TrendItemEntity(
                    name = "[Curated Sample] 1 Ingredient, 3 Ways",
                    description = "Show how one staple ingredient or wardrobe piece transforms into three totally different vibes.",
                    category = "Food",
                    sourceUrl = "",
                    dateVerified = "Curated Format • Oct 2026",
                    creativeInspiration = "Use a snap transition on the beat to swap between Version 1 (5 min), Version 2 (15 min), and Version 3 (Pro).",
                    isSample = true
                ),
                TrendItemEntity(
                    name = "[Curated Sample] Mic'd Up Gym Inner Monologue",
                    description = "Deadpan voiceover recorded over a heavy set or mobility stretch saying what everyone secretly thinks.",
                    category = "Fitness",
                    sourceUrl = "",
                    dateVerified = "Curated Format • Oct 2026",
                    creativeInspiration = "Pair a serious workout clip with a totally relatable voiceover about wanting snacks.",
                    isSample = true
                )
            )
            sampleTrends.forEach { dao.insertTrend(it) }
        }

        val existingPlanner = dao.getAllPlannerItemsOnce()
        if (existingPlanner.isEmpty()) {
            dao.insertPlannerItem(
                PlannerItemEntity(
                    title = "[Sample] Weekend Coffee Match-Cut Reel",
                    description = "Test the doorframe match-cut transition from @visualsbykai's saved tutorial.",
                    category = "Editing Tutorials",
                    plannedDate = "2026-10-28",
                    caption = "Finally tried that match-cut tutorial that was sitting in my saved folder for 5 days ☕✨ #reelnag #editingtips",
                    relatedSource = "[Sample] Seamless Match-Cut Transition Tutorial",
                    notes = "Shoot clip A at 9 AM natural light, clip B right after.",
                    status = PlannerStatus.IN_PROGRESS.name,
                    isSample = true
                )
            )
            dao.insertPlannerItem(
                PlannerItemEntity(
                    title = "[Sample] Calling Out My Saved Folder Hoard",
                    description = "30-second comedy skit showing what happens when you finally open a Reel from 3 months ago.",
                    category = "Funny",
                    plannedDate = "2026-10-30",
                    caption = "My saved folder is basically a time capsule at this point 💀 How many unwatched Reels do you have?",
                    relatedSource = "[Sample] POV: Your Saved Folder Has 800 Unwatched Videos",
                    notes = "Needs punchy text hook in the first 2 seconds.",
                    status = PlannerStatus.IDEA.name,
                    isSample = true
                )
            )
        }
    }

    // --- Reel CRUD ---
    suspend fun findDuplicateReel(normalizedUrl: String): SavedReelEntity? {
        return dao.findReelByNormalizedUrl(normalizedUrl)
    }

    suspend fun insertReel(reel: SavedReelEntity): Long = dao.insertReel(reel)

    suspend fun updateReel(reel: SavedReelEntity) = dao.updateReel(reel)

    suspend fun deleteReel(reel: SavedReelEntity) {
        dao.deleteRemindersForReel(reel.id)
        dao.deleteReel(reel)
    }

    // --- Category CRUD ---
    suspend fun addCategory(name: String): Boolean {
        val clean = name.trim()
        if (clean.isEmpty()) return false
        val id = dao.insertCategory(CategoryEntity(name = clean, isDefault = false, sortOrder = 99))
        return id != -1L
    }

    suspend fun renameCategory(category: CategoryEntity, newName: String) {
        val clean = newName.trim()
        if (clean.isEmpty()) return
        dao.updateCategory(category.copy(name = clean))
        dao.renameCategoryInReels(category.name, clean)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        dao.renameCategoryInReels(category.name, "Favorites")
        dao.deleteCategory(category)
    }

    // --- Reminder CRUD ---
    suspend fun insertReminder(reminder: NagReminderEntity): Long = dao.insertReminder(reminder)

    suspend fun updateReminder(reminder: NagReminderEntity) = dao.updateReminder(reminder)

    suspend fun deleteReminder(reminder: NagReminderEntity) = dao.deleteReminder(reminder)

    // --- Trend CRUD ---
    suspend fun insertTrend(trend: TrendItemEntity): Long = dao.insertTrend(trend)

    suspend fun updateTrend(trend: TrendItemEntity) = dao.updateTrend(trend)

    suspend fun deleteTrend(trend: TrendItemEntity) = dao.deleteTrend(trend)

    // --- Planner CRUD ---
    suspend fun insertPlannerItem(item: PlannerItemEntity): Long = dao.insertPlannerItem(item)

    suspend fun updatePlannerItem(item: PlannerItemEntity) = dao.updatePlannerItem(item)

    suspend fun deletePlannerItem(item: PlannerItemEntity) = dao.deletePlannerItem(item)

    // --- Saved AI Concepts CRUD ---
    suspend fun insertSavedAiConcept(concept: SavedAiConceptEntity): Long = dao.insertSavedAiConcept(concept)

    suspend fun deleteSavedAiConcept(concept: SavedAiConceptEntity) = dao.deleteSavedAiConcept(concept)

    // --- Export / Import / Reset ---
    suspend fun exportBackupJson(): String {
        val payload = ReelNagBackupPayload(
            schemaVersion = 1,
            exportedAt = System.currentTimeMillis(),
            settings = getSettingsOnce(),
            categories = dao.getAllCategoriesOnce(),
            reels = dao.getAllReelsOnce(),
            reminders = dao.getAllRemindersOnce(),
            trends = dao.getAllTrendsOnce(),
            plannerItems = dao.getAllPlannerItemsOnce(),
            savedAiConcepts = dao.getAllSavedAiConceptsOnce()
        )
        return json.encodeToString(payload)
    }

    suspend fun importBackupJson(rawJson: String): Result<Int> {
        return try {
            val payload = json.decodeFromString<ReelNagBackupPayload>(rawJson)
            dao.deleteAllReels()
            dao.deleteAllCategories()
            dao.deleteAllReminders()
            dao.deleteAllTrends()
            dao.deleteAllPlannerItems()
            dao.deleteAllSavedAiConcepts()

            payload.categories.forEach { dao.insertCategory(it) }
            if (payload.categories.isEmpty()) {
                DefaultData.DEFAULT_CATEGORIES.forEachIndexed { idx, name ->
                    dao.insertCategory(CategoryEntity(name = name, isDefault = true, sortOrder = idx))
                }
            }
            payload.reels.forEach { dao.insertReel(it) }
            payload.reminders.forEach { dao.insertReminder(it) }
            payload.trends.forEach { dao.insertTrend(it) }
            payload.plannerItems.forEach { dao.insertPlannerItem(it) }
            payload.savedAiConcepts.forEach { dao.insertSavedAiConcept(it) }

            updateSettings { payload.settings }
            Result.success(payload.reels.size + payload.reminders.size + payload.plannerItems.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetAllData(keepSamples: Boolean = false) {
        dao.deleteAllReels()
        dao.deleteAllCategories()
        dao.deleteAllReminders()
        dao.deleteAllTrends()
        dao.deleteAllPlannerItems()
        dao.deleteAllSavedAiConcepts()

        DefaultData.DEFAULT_CATEGORIES.forEachIndexed { index, name ->
            dao.insertCategory(CategoryEntity(name = name, isDefault = true, sortOrder = index))
        }
        updateSettings { UserSettings(onboardingCompleted = false) }
        if (keepSamples) {
            seedDefaultCategoriesAndSamples()
        }
    }
}
