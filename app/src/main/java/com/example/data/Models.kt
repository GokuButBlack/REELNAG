package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
enum class UserRole(val displayName: String, val subtitle: String) {
    VIEWER("Reel Viewer", "Save & actually watch the good stuff"),
    CREATOR("Content Creator", "Turn saved links into original videos"),
    BOTH("Both Viewer & Creator", "Watch for fun and build my own channel")
}

@Serializable
enum class NagPersonality(
    val displayName: String,
    val badgeEmoji: String,
    val tagline: String,
    val sampleLine: String
) {
    SARCASTIC_BESTIE(
        "Sarcastic Bestie",
        "💅",
        "Witty roasts with tough love",
        "Bro, you saved this Reel three days ago. Are you collecting links as a hobby?"
    ),
    CHILL_FRIEND(
        "Chill Friend",
        "☕",
        "Zero pressure, gentle nudges",
        "That Reel you saved is still waiting whenever you have a minute."
    ),
    DRAMA_QUEEN(
        "Drama Queen",
        "🎭",
        "Theatrical, urgent, over-the-top",
        "Breaking news: a legendary Reel remains unwatched."
    ),
    MOTIVATOR(
        "Motivator",
        "🚀",
        "High-energy creator hype",
        "Your next great idea might be hiding in that saved Reel."
    ),
    UNHINGED_FRIEND(
        "Unhinged Friend",
        "🤪",
        "Chaotic, brutally funny reality checks",
        "Your saved folder has more commitment issues than your entire friend group."
    )
}

@Serializable
enum class ReminderRecurrence(val label: String) {
    ONCE("Once"),
    DAILY("Daily"),
    WEEKLY("Weekly")
}

@Serializable
enum class PlannerStatus(val label: String) {
    IDEA("Idea"),
    PLANNED("Planned"),
    IN_PROGRESS("In Progress"),
    READY_TO_PUBLISH("Ready to Publish"),
    PUBLISHED("Published")
}

@Serializable
@Entity(
    tableName = "saved_reels",
    indices = [Index(value = ["normalizedUrl"], unique = true)]
)
data class SavedReelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rawUrl: String,
    val normalizedUrl: String,
    val title: String,
    val description: String = "",
    val categoryName: String = "Favorites",
    val personalNotes: String = "",
    val creatorName: String = "",
    val savedAt: Long = System.currentTimeMillis(),
    val isWatched: Boolean = false,
    val watchedAt: Long? = null,
    val isSample: Boolean = false
)

@Serializable
@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], unique = true)]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isDefault: Boolean = false,
    val sortOrder: Int = 0
)

@Serializable
@Entity(tableName = "nag_reminders")
data class NagReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reelId: Long? = null,
    val itemTitle: String,
    val categoryName: String = "Favorites",
    val scheduledAt: Long,
    val recurrence: String = ReminderRecurrence.ONCE.name,
    val snoozeCount: Int = 0,
    val isCompleted: Boolean = false,
    val isEnabled: Boolean = true,
    val messagePreview: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isSample: Boolean = false
)

@Serializable
@Entity(tableName = "trend_items")
data class TrendItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val category: String,
    val sourceUrl: String = "",
    val dateVerified: String,
    val creativeInspiration: String,
    val isSaved: Boolean = false,
    val isSample: Boolean = true
)

@Serializable
@Entity(tableName = "planner_items")
data class PlannerItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val category: String,
    val plannedDate: String,
    val caption: String = "",
    val relatedSource: String = "",
    val notes: String = "",
    val status: String = PlannerStatus.IDEA.name,
    val createdAt: Long = System.currentTimeMillis(),
    val isSample: Boolean = false
)

@Serializable
@Entity(tableName = "saved_ai_concepts")
data class SavedAiConceptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val openingHook: String,
    val explanation: String,
    val sceneOutline: String,
    val suggestedShots: String,
    val onScreenText: String,
    val caption: String,
    val hashtags: String,
    val callToAction: String,
    val editingSuggestions: String,
    val niche: String = "Lifestyle",
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class UserSettings(
    val schemaVersion: Int = 1,
    val onboardingCompleted: Boolean = false,
    val userName: String = "Creator",
    val interests: List<String> = listOf("Comedy and memes", "Editing Tutorials", "Technology and AI"),
    val role: UserRole = UserRole.BOTH,
    val personality: NagPersonality = NagPersonality.SARCASTIC_BESTIE,
    val notificationsEnabled: Boolean = true,
    val preferredReminderTime: String = "19:30",
    val quietHoursEnabled: Boolean = true,
    val quietHoursStart: String = "22:30",
    val quietHoursEnd: String = "08:00",
    val weeklyContentGoal: Int = 3,
    val lastGreetingIndex: Int = -1,
    val lastNagTemplateId: String = ""
)

@Serializable
data class ReelNagBackupPayload(
    val schemaVersion: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val settings: UserSettings,
    val categories: List<CategoryEntity>,
    val reels: List<SavedReelEntity>,
    val reminders: List<NagReminderEntity>,
    val trends: List<TrendItemEntity>,
    val plannerItems: List<PlannerItemEntity>,
    val savedAiConcepts: List<SavedAiConceptEntity>
)

object DefaultData {
    val ALL_INTERESTS = listOf(
        "Comedy and memes",
        "Fashion and outfits",
        "Fitness",
        "Gaming",
        "Education",
        "Food",
        "Lifestyle",
        "Technology and AI",
        "Other"
    )

    val DEFAULT_CATEGORIES = listOf(
        "Funny",
        "Fashion",
        "Fitness",
        "Learn Later",
        "Inspiration",
        "Editing Tutorials",
        "Content Ideas",
        "Favorites"
    )

    val TREND_CATEGORIES = listOf(
        "All",
        "Comedy",
        "Fashion",
        "Fitness",
        "Gaming",
        "Food",
        "Lifestyle",
        "Editing Styles",
        "AI Content"
    )
}
