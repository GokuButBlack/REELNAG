package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.NagPersonality
import com.example.data.NagReminderEntity
import com.example.data.PlannerItemEntity
import com.example.data.PlannerStatus
import com.example.data.ReelNagDatabase
import com.example.data.ReelNagRepository
import com.example.data.ReminderRecurrence
import com.example.data.SavedReelEntity
import com.example.data.UserRole
import com.example.domain.SmartNagMessageEngine
import com.example.domain.UrlNormalizer
import com.example.domain.UrlValidationResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var database: ReelNagDatabase
    private lateinit var repository: ReelNagRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, ReelNagDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ReelNagRepository(context, database.dao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read app_name from context matches ReelNag`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("ReelNag", appName)
    }

    @Test
    fun `url normalizer strips tracking parameters and detects invalid urls`() {
        val urlWithTracking = "https://www.instagram.com/reel/C8xSampleEdit01/?igsh=MzRlODBiNWFlZA==&utm_source=ig_web_copy_link"
        val res1 = UrlNormalizer.validateAndNormalize(urlWithTracking)
        assertTrue(res1 is UrlValidationResult.Valid)
        val valid1 = res1 as UrlValidationResult.Valid
        assertEquals("https://instagram.com/reel/C8xSampleEdit01", valid1.normalizedUrl)
        assertTrue(valid1.isInstagramReel)

        // Same reel with /reels/ path and trailing slash should normalize identically
        val urlVariant = "https://m.instagram.com/reels/C8xSampleEdit01?utm_medium=share_sheet"
        val res2 = UrlNormalizer.validateAndNormalize(urlVariant) as UrlValidationResult.Valid
        assertEquals(valid1.normalizedUrl, res2.normalizedUrl)

        // Invalid URLs
        val emptyRes = UrlNormalizer.validateAndNormalize("   ")
        assertTrue(emptyRes is UrlValidationResult.Invalid)

        val igHomeOnly = UrlNormalizer.validateAndNormalize("https://www.instagram.com/")
        assertTrue(igHomeOnly is UrlValidationResult.Invalid)
    }

    @Test
    fun `onboarding completion and skipping persist in settings`() = runBlocking {
        // Skip onboarding
        repository.updateSettings { it.copy(onboardingCompleted = true) }
        val afterSkip = repository.getSettingsOnce()
        assertTrue(afterSkip.onboardingCompleted)

        // Customize onboarding settings
        repository.updateSettings {
            it.copy(
                interests = listOf("Fitness", "Food"),
                role = UserRole.CREATOR,
                personality = NagPersonality.UNHINGED_FRIEND,
                quietHoursEnabled = true,
                quietHoursStart = "23:00",
                quietHoursEnd = "07:30"
            )
        }
        val updated = repository.getSettingsOnce()
        assertEquals(UserRole.CREATOR, updated.role)
        assertEquals(NagPersonality.UNHINGED_FRIEND, updated.personality)
        assertEquals(listOf("Fitness", "Food"), updated.interests)
        assertEquals("23:00", updated.quietHoursStart)
    }

    @Test
    fun `save reel, prevent duplicates, edit, mark watched, and delete`() = runBlocking {
        val norm = UrlNormalizer.validateAndNormalize("https://www.instagram.com/reel/AbCdEfG123/?igsh=xyz") as UrlValidationResult.Valid
        val id = repository.insertReel(
            SavedReelEntity(
                rawUrl = norm.rawUrl,
                normalizedUrl = norm.normalizedUrl,
                title = "High Protein Overnight Oats",
                description = "Easy 5 min breakfast",
                categoryName = "Learn Later",
                personalNotes = "Use chia seeds",
                creatorName = "@chef_test"
            )
        )
        assertTrue(id > 0)

        // Verify duplicate check finds existing item
        val duplicate = repository.findDuplicateReel("https://instagram.com/reel/AbCdEfG123")
        assertNotNull(duplicate)
        assertEquals("High Protein Overnight Oats", duplicate?.title)

        // Edit notes & mark watched
        val toUpdate = duplicate!!.copy(isWatched = true, personalNotes = "Tested and loved it!")
        repository.updateReel(toUpdate)

        val allReels = repository.reelsFlow.first()
        assertEquals(1, allReels.size)
        assertTrue(allReels.first().isWatched)
        assertEquals("Tested and loved it!", allReels.first().personalNotes)

        // Delete
        repository.deleteReel(allReels.first())
        assertTrue(repository.reelsFlow.first().isEmpty())
    }

    @Test
    fun `custom categories add, rename updates reels, and delete moves reels to Favorites`() = runBlocking {
        repository.seedDefaultCategoriesAndSamples()
        val added = repository.addCategory("Cinematography")
        assertTrue(added)

        val cats = repository.categoriesFlow.first()
        val customCat = cats.firstOrNull { it.name == "Cinematography" }
        assertNotNull(customCat)

        // Insert a reel in Cinematography
        repository.insertReel(
            SavedReelEntity(
                rawUrl = "https://instagram.com/reel/Cine01",
                normalizedUrl = "https://instagram.com/reel/Cine01",
                title = "Anamorphic Lens Flare",
                categoryName = "Cinematography"
            )
        )

        // Rename category to "Camera Tricks"
        repository.renameCategory(customCat!!, "Camera Tricks")
        val renamedReel = repository.findDuplicateReel("https://instagram.com/reel/Cine01")
        assertEquals("Camera Tricks", renamedReel?.categoryName)

        // Delete category -> moves reel to Favorites
        val updatedCat = repository.categoriesFlow.first().first { it.name == "Camera Tricks" }
        repository.deleteCategory(updatedCat)
        val movedReel = repository.findDuplicateReel("https://instagram.com/reel/Cine01")
        assertEquals("Favorites", movedReel?.categoryName)
    }

    @Test
    fun `smart nag message engine never repeats back to back and evaluates quiet hours`() {
        var lastId: String? = null
        NagPersonality.entries.forEach { personality ->
            repeat(12) { idx ->
                val (tplId, msg) = SmartNagMessageEngine.generateNagMessage(
                    personality = personality,
                    itemTitle = "Mobility Routine",
                    savedAtMillis = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(4),
                    unwatchedCount = 5,
                    snoozeCount = idx % 3,
                    categoryName = "Fitness",
                    lastTemplateId = lastId
                )
                assertNotEquals("Should not repeat template twice in a row", lastId, tplId)
                assertTrue(msg.isNotBlank())
                lastId = tplId
            }
        }

        // Overnight quiet hours 22:30 -> 08:00
        assertTrue(
            SmartNagMessageEngine.isWithinQuietHours(
                quietHoursEnabled = true,
                startStr = "22:30",
                endStr = "08:00",
                currentHour = 23,
                currentMinute = 15
            )
        )
        assertTrue(
            SmartNagMessageEngine.isWithinQuietHours(
                quietHoursEnabled = true,
                startStr = "22:30",
                endStr = "08:00",
                currentHour = 6,
                currentMinute = 45
            )
        )
        assertFalse(
            SmartNagMessageEngine.isWithinQuietHours(
                quietHoursEnabled = true,
                startStr = "22:30",
                endStr = "08:00",
                currentHour = 14,
                currentMinute = 0
            )
        )
    }

    @Test
    fun `reminders create, snooze, complete, and delete`() = runBlocking {
        val now = System.currentTimeMillis()
        val remId = repository.insertReminder(
            NagReminderEntity(
                reelId = null,
                itemTitle = "Watch Match-Cut Reel",
                categoryName = "Editing Tutorials",
                scheduledAt = now,
                recurrence = ReminderRecurrence.ONCE.name,
                messagePreview = "Bro, watch this now."
            )
        )
        val created = repository.remindersFlow.first().first { it.id == remId }
        assertEquals(0, created.snoozeCount)

        // Snooze 1 hour
        val snoozed = created.copy(
            scheduledAt = now + TimeUnit.MINUTES.toMillis(60),
            snoozeCount = created.snoozeCount + 1
        )
        repository.updateReminder(snoozed)
        val afterSnooze = repository.remindersFlow.first().first { it.id == remId }
        assertEquals(1, afterSnooze.snoozeCount)

        // Complete
        repository.updateReminder(afterSnooze.copy(isCompleted = true))
        val afterComplete = repository.remindersFlow.first().first { it.id == remId }
        assertTrue(afterComplete.isCompleted)

        // Delete
        repository.deleteReminder(afterComplete)
        assertNull(repository.remindersFlow.first().firstOrNull { it.id == remId })
    }

    @Test
    fun `export and import backup json restores full state`() = runBlocking {
        repository.seedDefaultCategoriesAndSamples()
        repository.insertPlannerItem(
            PlannerItemEntity(
                title = "My Custom Planned Reel",
                description = "Hook test",
                category = "Comedy",
                plannedDate = "2026-11-05",
                status = PlannerStatus.READY_TO_PUBLISH.name
            )
        )
        val exportedJson = repository.exportBackupJson()
        assertTrue(exportedJson.contains("My Custom Planned Reel"))

        // Wipe everything empty
        repository.resetAllData(keepSamples = false)
        assertTrue(repository.reelsFlow.first().isEmpty())
        assertTrue(repository.plannerItemsFlow.first().isEmpty())

        // Import backup
        val importResult = repository.importBackupJson(exportedJson)
        assertTrue(importResult.isSuccess)
        assertFalse(repository.reelsFlow.first().isEmpty())
        assertTrue(repository.plannerItemsFlow.first().any { it.title == "My Custom Planned Reel" })
    }
}
