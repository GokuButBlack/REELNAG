package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.AiStudioConceptDto
import com.example.ai.GeminiStudioService
import com.example.ai.StudioRequestParams
import com.example.data.CategoryEntity
import com.example.data.NagPersonality
import com.example.data.NagReminderEntity
import com.example.data.PlannerItemEntity
import com.example.data.PlannerStatus
import com.example.data.ReelNagDatabase
import com.example.data.ReelNagRepository
import com.example.data.ReminderRecurrence
import com.example.data.SavedAiConceptEntity
import com.example.data.SavedReelEntity
import com.example.data.TrendItemEntity
import com.example.data.UserRole
import com.example.data.UserSettings
import com.example.domain.NagNotificationHelper
import com.example.domain.NotificationDeliveryStatus
import com.example.domain.SmartNagMessageEngine
import com.example.domain.UrlNormalizer
import com.example.domain.UrlValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

enum class AppTab(val route: String, val label: String) {
    HOME("home", "Home"),
    DISCOVER("discover", "Discover"),
    CREATE("create", "Create"),
    SAVED("saved", "Saved"),
    PROFILE("profile", "Profile")
}

enum class WatchFilter(val label: String) {
    ALL("All"),
    UNWATCHED("Unwatched"),
    WATCHED("Watched")
}

enum class ReelSortOption(val label: String) {
    DATE_SAVED_DESC("Newest Saved"),
    DATE_SAVED_ASC("Oldest Saved"),
    REMINDER_SOONEST("Reminder Date")
}

sealed class AiGenerationState {
    object Idle : AiGenerationState()
    object Loading : AiGenerationState()
    data class Success(val concepts: List<AiStudioConceptDto>) : AiGenerationState()
    data class Error(val message: String) : AiGenerationState()
}

data class SaveReelDraftState(
    val isOpen: Boolean = false,
    val editingReelId: Long? = null,
    val rawUrl: String = "",
    val title: String = "",
    val description: String = "",
    val categoryName: String = "Favorites",
    val personalNotes: String = "",
    val creatorName: String = "",
    val createNagReminder: Boolean = true,
    val reminderDelayHours: Int = 24,
    val reminderRecurrence: ReminderRecurrence = ReminderRecurrence.ONCE,
    val urlError: String? = null,
    val duplicateReel: SavedReelEntity? = null,
    val prefilledFromShareOrQuery: Boolean = false
)

data class WhatShouldIReelTodaySuggestion(
    val oldestUnwatchedReel: SavedReelEntity?,
    val recentSameNicheReel: SavedReelEntity?,
    val activePlannerDraft: PlannerItemEntity?,
    val matchingTrend: TrendItemEntity?,
    val originalPromptIdea: String
)

class ReelNagViewModel(
    private val appContext: Context,
    private val repository: ReelNagRepository
) : ViewModel() {

    val settings: StateFlow<UserSettings> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    val reels: StateFlow<List<SavedReelEntity>> = repository.reelsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = repository.categoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<NagReminderEntity>> = repository.remindersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trends: StateFlow<List<TrendItemEntity>> = repository.trendsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val plannerItems: StateFlow<List<PlannerItemEntity>> = repository.plannerItemsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedAiConcepts: StateFlow<List<SavedAiConceptEntity>> = repository.savedAiConceptsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _dailyGreeting = MutableStateFlow("Welcome to ReelNag! Let's rescue those saved Reels.")
    val dailyGreeting: StateFlow<String> = _dailyGreeting.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Reel Locker Filter & Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow("All")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    private val _watchFilter = MutableStateFlow(WatchFilter.ALL)
    val watchFilter: StateFlow<WatchFilter> = _watchFilter.asStateFlow()

    private val _sortOption = MutableStateFlow(ReelSortOption.DATE_SAVED_DESC)
    val sortOption: StateFlow<ReelSortOption> = _sortOption.asStateFlow()

    // Save / Edit Reel Modal State
    private val _saveDraft = MutableStateFlow(SaveReelDraftState())
    val saveDraft: StateFlow<SaveReelDraftState> = _saveDraft.asStateFlow()

    // Create / Edit Reminder Modal State
    private val _showCreateReminderSheet = MutableStateFlow(false)
    val showCreateReminderSheet: StateFlow<Boolean> = _showCreateReminderSheet.asStateFlow()

    private val _preselectedReelForReminder = MutableStateFlow<SavedReelEntity?>(null)
    val preselectedReelForReminder: StateFlow<SavedReelEntity?> = _preselectedReelForReminder.asStateFlow()

    // AI Studio State
    private val _aiState = MutableStateFlow<AiGenerationState>(AiGenerationState.Idle)
    val aiState: StateFlow<AiGenerationState> = _aiState.asStateFlow()

    private val _studioInputType = MutableStateFlow("Topic")
    val studioInputType: StateFlow<String> = _studioInputType.asStateFlow()

    private val _studioInputText = MutableStateFlow("")
    val studioInputText: StateFlow<String> = _studioInputText.asStateFlow()

    private val _createSubTab = MutableStateFlow(0) // 0 = AI Content Studio, 1 = Content Planner
    val createSubTab: StateFlow<Int> = _createSubTab.asStateFlow()

    val filteredReels: StateFlow<List<SavedReelEntity>> = combine(
        reels,
        reminders,
        _searchQuery,
        _selectedCategoryFilter,
        _watchFilter,
        _sortOption
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val allReels = args[0] as List<SavedReelEntity>
        @Suppress("UNCHECKED_CAST")
        val allReminders = args[1] as List<NagReminderEntity>
        val query = (args[2] as String).trim().lowercase()
        val catFilter = args[3] as String
        val watch = args[4] as WatchFilter
        val sort = args[5] as ReelSortOption

        val reminderMap = allReminders
            .filter { !it.isCompleted && it.reelId != null }
            .groupBy { it.reelId!! }
            .mapValues { entry -> entry.value.minOf { it.scheduledAt } }

        allReels.filter { reel ->
            val matchesQuery = query.isEmpty() ||
                reel.title.lowercase().contains(query) ||
                reel.description.lowercase().contains(query) ||
                reel.personalNotes.lowercase().contains(query) ||
                reel.creatorName.lowercase().contains(query)

            val matchesCategory = catFilter == "All" || reel.categoryName.equals(catFilter, ignoreCase = true)

            val matchesWatch = when (watch) {
                WatchFilter.ALL -> true
                WatchFilter.UNWATCHED -> !reel.isWatched
                WatchFilter.WATCHED -> reel.isWatched
            }

            matchesQuery && matchesCategory && matchesWatch
        }.let { list ->
            when (sort) {
                ReelSortOption.DATE_SAVED_DESC -> list.sortedByDescending { it.savedAt }
                ReelSortOption.DATE_SAVED_ASC -> list.sortedBy { it.savedAt }
                ReelSortOption.REMINDER_SOONEST -> list.sortedWith(
                    compareBy<SavedReelEntity> { reminderMap[it.id] ?: Long.MAX_VALUE }
                        .thenByDescending { it.savedAt }
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val whatShouldIReelToday: StateFlow<WhatShouldIReelTodaySuggestion> = combine(
        reels,
        plannerItems,
        trends,
        settings
    ) { allReels, allPlanner, allTrends, userSettings ->
        val unwatched = allReels.filter { !it.isWatched }
        val oldestUnwatched = unwatched.minByOrNull { it.savedAt }
        val recentUnwatched = unwatched.maxByOrNull { it.savedAt }
        val unfinishedPlan = allPlanner.firstOrNull {
            it.status != PlannerStatus.PUBLISHED.name
        }
        val preferredTrend = allTrends.firstOrNull { trend ->
            userSettings.interests.any { interest ->
                interest.contains(trend.category, ignoreCase = true) ||
                    trend.category.contains(interest.split(" ").first(), ignoreCase = true)
            }
        } ?: allTrends.firstOrNull()

        val niche = userSettings.interests.firstOrNull() ?: "Lifestyle"
        val promptIdea = if (oldestUnwatched != null) {
            "Record a 20-second 'Expectation vs. Reality' trying out \"${oldestUnwatched.title}\" from your ${oldestUnwatched.categoryName} folder."
        } else {
            "Film a 3-part quick tip Reel in your $niche niche starting with a contrarian 2-second hook."
        }

        WhatShouldIReelTodaySuggestion(
            oldestUnwatchedReel = oldestUnwatched,
            recentSameNicheReel = recentUnwatched,
            activePlannerDraft = unfinishedPlan,
            matchingTrend = preferredTrend,
            originalPromptIdea = promptIdea
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        WhatShouldIReelTodaySuggestion(null, null, null, null, "")
    )

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            val s = repository.getSettingsOnce()
            refreshDailyGreeting(s.personality, s.lastGreetingIndex)
        }
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun selectCreateSubTab(index: Int) {
        _createSubTab.value = index
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    fun rotateDailyGreeting() {
        viewModelScope.launch {
            val s = settings.value
            refreshDailyGreeting(s.personality, s.lastGreetingIndex)
        }
    }

    private suspend fun refreshDailyGreeting(personality: NagPersonality, previousIndex: Int) {
        val (nextIdx, text) = SmartNagMessageEngine.getDailyGreeting(personality, previousIndex)
        _dailyGreeting.value = text
        repository.updateSettings { it.copy(lastGreetingIndex = nextIdx) }
    }

    // --- Onboarding & Profile Settings ---
    fun completeOnboarding(
        interests: List<String>,
        role: UserRole,
        personality: NagPersonality,
        notificationsEnabled: Boolean,
        preferredTime: String,
        quietHoursEnabled: Boolean,
        quietStart: String,
        quietEnd: String
    ) {
        viewModelScope.launch {
            repository.updateSettings {
                it.copy(
                    onboardingCompleted = true,
                    interests = interests.ifEmpty { listOf("Comedy and memes") },
                    role = role,
                    personality = personality,
                    notificationsEnabled = notificationsEnabled,
                    preferredReminderTime = preferredTime,
                    quietHoursEnabled = quietHoursEnabled,
                    quietHoursStart = quietStart,
                    quietHoursEnd = quietEnd
                )
            }
            refreshDailyGreeting(personality, -1)
            showMessage("Welcome! ${personality.badgeEmoji} ${personality.displayName} is now on duty.")
        }
    }

    fun skipOnboarding() {
        viewModelScope.launch {
            repository.updateSettings {
                it.copy(onboardingCompleted = true)
            }
            refreshDailyGreeting(settings.value.personality, -1)
            showMessage("Onboarding skipped with smart defaults. You can customize anytime in Profile!")
        }
    }

    fun updateUserSettings(transform: (UserSettings) -> UserSettings) {
        viewModelScope.launch {
            val oldPersonality = settings.value.personality
            repository.updateSettings(transform)
            val updated = repository.getSettingsOnce()
            if (updated.personality != oldPersonality) {
                refreshDailyGreeting(updated.personality, -1)
            }
        }
    }

    // --- Quick-Save & Share Sheet Handling ---
    fun handleIncomingSharedUrl(sharedTextOrUrl: String) {
        val extracted = UrlNormalizer.extractUrlFromSharedText(sharedTextOrUrl)
        if (extracted.isBlank()) return
        _currentTab.value = AppTab.SAVED
        openSaveReelSheet(prefilledUrl = extracted, fromShare = true)
    }

    fun openSaveReelSheet(
        editingReel: SavedReelEntity? = null,
        prefilledUrl: String = "",
        fromShare: Boolean = false
    ) {
        if (editingReel != null) {
            _saveDraft.value = SaveReelDraftState(
                isOpen = true,
                editingReelId = editingReel.id,
                rawUrl = editingReel.rawUrl,
                title = editingReel.title,
                description = editingReel.description,
                categoryName = editingReel.categoryName,
                personalNotes = editingReel.personalNotes,
                creatorName = editingReel.creatorName,
                createNagReminder = false
            )
        } else {
            _saveDraft.value = SaveReelDraftState(
                isOpen = true,
                rawUrl = prefilledUrl,
                categoryName = categories.value.firstOrNull()?.name ?: "Favorites",
                prefilledFromShareOrQuery = fromShare && prefilledUrl.isNotBlank()
            )
            if (prefilledUrl.isNotBlank()) {
                onDraftUrlChanged(prefilledUrl)
            }
        }
    }

    fun closeSaveReelSheet() {
        _saveDraft.update { it.copy(isOpen = false) }
    }

    fun onDraftUrlChanged(newUrl: String) {
        _saveDraft.update { it.copy(rawUrl = newUrl, urlError = null, duplicateReel = null) }
        if (newUrl.isBlank()) return

        viewModelScope.launch {
            when (val res = UrlNormalizer.validateAndNormalize(newUrl)) {
                is UrlValidationResult.Invalid -> {
                    _saveDraft.update { it.copy(urlError = res.errorMessage, duplicateReel = null) }
                }
                is UrlValidationResult.Valid -> {
                    val existing = repository.findDuplicateReel(res.normalizedUrl)
                    val editingId = _saveDraft.value.editingReelId
                    if (existing != null && existing.id != editingId) {
                        _saveDraft.update {
                            it.copy(
                                urlError = "Duplicate detected! This URL matches \"${existing.title}\" in ${existing.categoryName}.",
                                duplicateReel = existing
                            )
                        }
                    } else {
                        _saveDraft.update { it.copy(urlError = null, duplicateReel = null) }
                    }
                }
            }
        }
    }

    fun updateSaveDraft(transform: (SaveReelDraftState) -> SaveReelDraftState) {
        _saveDraft.update(transform)
    }

    fun saveReelFromDraft(onSuccess: () -> Unit = {}) {
        val draft = _saveDraft.value
        val validation = UrlNormalizer.validateAndNormalize(draft.rawUrl)
        if (validation is UrlValidationResult.Invalid) {
            _saveDraft.update { it.copy(urlError = validation.errorMessage) }
            return
        }
        val valid = validation as UrlValidationResult.Valid

        viewModelScope.launch {
            val existing = repository.findDuplicateReel(valid.normalizedUrl)
            if (existing != null && existing.id != draft.editingReelId) {
                _saveDraft.update {
                    it.copy(
                        urlError = "Already saved as \"${existing.title}\" (${existing.categoryName}).",
                        duplicateReel = existing
                    )
                }
                return@launch
            }

            val finalTitle = draft.title.trim().ifEmpty {
                "Saved Reel (${draft.categoryName})"
            }

            if (draft.editingReelId != null) {
                val original = reels.value.firstOrNull { it.id == draft.editingReelId } ?: return@launch
                repository.updateReel(
                    original.copy(
                        rawUrl = valid.rawUrl,
                        normalizedUrl = valid.normalizedUrl,
                        title = finalTitle,
                        description = draft.description.trim(),
                        categoryName = draft.categoryName,
                        personalNotes = draft.personalNotes.trim(),
                        creatorName = draft.creatorName.trim()
                    )
                )
                showMessage("Updated \"$finalTitle\"!")
            } else {
                val now = System.currentTimeMillis()
                val newId = repository.insertReel(
                    SavedReelEntity(
                        rawUrl = valid.rawUrl,
                        normalizedUrl = valid.normalizedUrl,
                        title = finalTitle,
                        description = draft.description.trim(),
                        categoryName = draft.categoryName,
                        personalNotes = draft.personalNotes.trim(),
                        creatorName = draft.creatorName.trim(),
                        savedAt = now,
                        isWatched = false,
                        isSample = false
                    )
                )

                if (draft.createNagReminder) {
                    val currentSettings = settings.value
                    val unwatchedCount = reels.value.count { !it.isWatched } + 1
                    val (tplId, previewMsg) = SmartNagMessageEngine.generateNagMessage(
                        personality = currentSettings.personality,
                        itemTitle = finalTitle,
                        savedAtMillis = now,
                        unwatchedCount = unwatchedCount,
                        snoozeCount = 0,
                        categoryName = draft.categoryName,
                        lastTemplateId = currentSettings.lastNagTemplateId
                    )
                    repository.updateSettings { it.copy(lastNagTemplateId = tplId) }

                    val scheduledAt = now + TimeUnit.HOURS.toMillis(draft.reminderDelayHours.toLong())
                    repository.insertReminder(
                        NagReminderEntity(
                            reelId = newId,
                            itemTitle = finalTitle,
                            categoryName = draft.categoryName,
                            scheduledAt = scheduledAt,
                            recurrence = draft.reminderRecurrence.name,
                            messagePreview = previewMsg,
                            isSample = false
                        )
                    )
                }
                showMessage("Saved \"$finalTitle\" to ${draft.categoryName}!")
            }

            _saveDraft.value = SaveReelDraftState(isOpen = false)
            onSuccess()
        }
    }

    fun toggleReelWatched(reel: SavedReelEntity) {
        viewModelScope.launch {
            val nowWatched = !reel.isWatched
            repository.updateReel(
                reel.copy(
                    isWatched = nowWatched,
                    watchedAt = if (nowWatched) System.currentTimeMillis() else null
                )
            )
            if (nowWatched) {
                // Also mark associated active reminders completed
                reminders.value.filter { it.reelId == reel.id && !it.isCompleted }.forEach { rem ->
                    repository.updateReminder(rem.copy(isCompleted = true))
                }
                showMessage("Marked \"${reel.title}\" as watched! One less thing to get nagged about.")
            } else {
                showMessage("Marked \"${reel.title}\" as unwatched.")
            }
        }
    }

    fun updateReelNotes(reel: SavedReelEntity, newNotes: String) {
        viewModelScope.launch {
            repository.updateReel(reel.copy(personalNotes = newNotes.trim()))
            showMessage("Notes saved for \"${reel.title}\".")
        }
    }

    fun deleteReel(reel: SavedReelEntity) {
        viewModelScope.launch {
            repository.deleteReel(reel)
            showMessage("Deleted \"${reel.title}\" from your locker.")
        }
    }

    // --- Search, Filter, Sort & Categories ---
    fun setSearchQuery(q: String) {
        _searchQuery.value = q
    }

    fun setCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun setWatchFilter(filter: WatchFilter) {
        _watchFilter.value = filter
    }

    fun setSortOption(option: ReelSortOption) {
        _sortOption.value = option
    }

    fun addCustomCategory(name: String) {
        viewModelScope.launch {
            val added = repository.addCategory(name)
            if (added) {
                showMessage("Category \"${name.trim()}\" added!")
            } else {
                showMessage("Category already exists or is invalid.")
            }
        }
    }

    fun renameCategory(category: CategoryEntity, newName: String) {
        viewModelScope.launch {
            repository.renameCategory(category, newName)
            if (_selectedCategoryFilter.value == category.name) {
                _selectedCategoryFilter.value = newName.trim()
            }
            showMessage("Renamed category to \"${newName.trim()}\".")
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            if (_selectedCategoryFilter.value == category.name) {
                _selectedCategoryFilter.value = "All"
            }
            showMessage("Deleted category \"${category.name}\". Items moved to Favorites.")
        }
    }

    // --- Smart Nag Reminders ---
    fun openCreateReminderSheet(forReel: SavedReelEntity? = null) {
        _preselectedReelForReminder.value = forReel
        _showCreateReminderSheet.value = true
    }

    fun closeCreateReminderSheet() {
        _showCreateReminderSheet.value = false
        _preselectedReelForReminder.value = null
    }

    fun generatePreviewMessage(
        itemTitle: String,
        categoryName: String,
        savedAtMillis: Long = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2),
        snoozeCount: Int = 0
    ): String {
        val currentSettings = settings.value
        val unwatchedCount = reels.value.count { !it.isWatched }.coerceAtLeast(1)
        val (_, msg) = SmartNagMessageEngine.generateNagMessage(
            personality = currentSettings.personality,
            itemTitle = itemTitle,
            savedAtMillis = savedAtMillis,
            unwatchedCount = unwatchedCount,
            snoozeCount = snoozeCount,
            categoryName = categoryName,
            lastTemplateId = currentSettings.lastNagTemplateId
        )
        return msg
    }

    fun createNagReminder(
        reelId: Long?,
        itemTitle: String,
        categoryName: String,
        scheduledAtMillis: Long,
        recurrence: ReminderRecurrence,
        previewMessage: String,
        fireNotificationNowIfDue: Boolean = false
    ) {
        viewModelScope.launch {
            val cleanTitle = itemTitle.trim().ifEmpty { "Watch saved Reel" }
            val id = repository.insertReminder(
                NagReminderEntity(
                    reelId = reelId,
                    itemTitle = cleanTitle,
                    categoryName = categoryName,
                    scheduledAt = scheduledAtMillis,
                    recurrence = recurrence.name,
                    messagePreview = previewMessage,
                    isSample = false
                )
            )
            _showCreateReminderSheet.value = false
            _preselectedReelForReminder.value = null

            if (fireNotificationNowIfDue || scheduledAtMillis <= System.currentTimeMillis() + 60_000L) {
                val delivery = NagNotificationHelper.trySendNagNotification(
                    context = appContext,
                    notificationId = id.toInt(),
                    title = cleanTitle,
                    message = previewMessage,
                    settings = settings.value
                )
                val statusMsg = when (delivery) {
                    is NotificationDeliveryStatus.Delivered -> delivery.message
                    is NotificationDeliveryStatus.SuppressedQuietHours -> delivery.reason
                    is NotificationDeliveryStatus.PermissionMissing -> delivery.reason
                    is NotificationDeliveryStatus.DisabledInSettings -> delivery.reason
                }
                showMessage(statusMsg)
            } else {
                showMessage("Smart Nag scheduled for \"$cleanTitle\"!")
            }
        }
    }

    fun snoozeReminder(reminder: NagReminderEntity, durationMinutes: Long) {
        viewModelScope.launch {
            val newSnoozeCount = reminder.snoozeCount + 1
            val currentSettings = settings.value
            val unwatchedCount = reels.value.count { !it.isWatched }.coerceAtLeast(1)
            val (tplId, newMessage) = SmartNagMessageEngine.generateNagMessage(
                personality = currentSettings.personality,
                itemTitle = reminder.itemTitle,
                savedAtMillis = reminder.createdAt,
                unwatchedCount = unwatchedCount,
                snoozeCount = newSnoozeCount,
                categoryName = reminder.categoryName,
                lastTemplateId = currentSettings.lastNagTemplateId
            )
            repository.updateSettings { it.copy(lastNagTemplateId = tplId) }

            val nextTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(durationMinutes)
            repository.updateReminder(
                reminder.copy(
                    scheduledAt = nextTime,
                    snoozeCount = newSnoozeCount,
                    messagePreview = newMessage
                )
            )
            val label = when {
                durationMinutes < 60 -> "$durationMinutes min"
                durationMinutes == 60L -> "1 hour"
                else -> "tomorrow"
            }
            showMessage("Snoozed for $label (Snooze #$newSnoozeCount). Expect a sassier nag next time!")
        }
    }

    fun completeReminder(reminder: NagReminderEntity) {
        viewModelScope.launch {
            if (reminder.recurrence == ReminderRecurrence.DAILY.name) {
                repository.updateReminder(
                    reminder.copy(
                        scheduledAt = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1),
                        snoozeCount = 0
                    )
                )
                showMessage("Completed today's nag! Rescheduled for tomorrow (Daily).")
            } else if (reminder.recurrence == ReminderRecurrence.WEEKLY.name) {
                repository.updateReminder(
                    reminder.copy(
                        scheduledAt = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7),
                        snoozeCount = 0
                    )
                )
                showMessage("Completed this week's nag! Rescheduled for next week (Weekly).")
            } else {
                repository.updateReminder(reminder.copy(isCompleted = true))
                showMessage("Nag completed! Look at you actually following through.")
            }
        }
    }

    fun toggleReminderEnabled(reminder: NagReminderEntity) {
        viewModelScope.launch {
            repository.updateReminder(reminder.copy(isEnabled = !reminder.isEnabled))
        }
    }

    fun deleteReminder(reminder: NagReminderEntity) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            showMessage("Reminder deleted.")
        }
    }

    fun testImmediateNagNotification(reminder: NagReminderEntity) {
        val status = NagNotificationHelper.trySendNagNotification(
            context = appContext,
            notificationId = reminder.id.toInt(),
            title = reminder.itemTitle,
            message = reminder.messagePreview,
            settings = settings.value
        )
        val text = when (status) {
            is NotificationDeliveryStatus.Delivered -> status.message
            is NotificationDeliveryStatus.SuppressedQuietHours -> status.reason
            is NotificationDeliveryStatus.PermissionMissing -> status.reason
            is NotificationDeliveryStatus.DisabledInSettings -> status.reason
        }
        showMessage(text)
    }

    // --- Discover: Trend Radar ---
    fun toggleTrendSaved(trend: TrendItemEntity) {
        viewModelScope.launch {
            repository.updateTrend(trend.copy(isSaved = !trend.isSaved))
            showMessage(
                if (!trend.isSaved) "Saved trend \"${trend.name}\"!"
                else "Removed \"${trend.name}\" from saved trends."
            )
        }
    }

    fun addCustomTrend(
        name: String,
        description: String,
        category: String,
        sourceUrl: String,
        inspiration: String
    ) {
        viewModelScope.launch {
            repository.insertTrend(
                TrendItemEntity(
                    name = name.trim(),
                    description = description.trim(),
                    category = category,
                    sourceUrl = sourceUrl.trim(),
                    dateVerified = "Added by You • Today",
                    creativeInspiration = inspiration.trim().ifEmpty { "Create your own spin on this trend." },
                    isSaved = true,
                    isSample = false
                )
            )
            showMessage("Added \"${name.trim()}\" to Trend Radar!")
        }
    }

    fun deleteTrend(trend: TrendItemEntity) {
        viewModelScope.launch {
            repository.deleteTrend(trend)
            showMessage("Removed trend \"${trend.name}\".")
        }
    }

    fun jumpToAiStudioWithSeed(inputType: String, seedText: String) {
        _studioInputType.value = inputType
        _studioInputText.value = seedText
        _currentTab.value = AppTab.CREATE
        _createSubTab.value = 0
    }

    // --- Create: AI Content Studio & Content Planner ---
    fun updateStudioSeed(inputType: String, text: String) {
        _studioInputType.value = inputType
        _studioInputText.value = text
    }

    fun generateAiConcepts(
        inputType: String,
        inputText: String,
        niche: String,
        audience: String,
        videoLength: String,
        tone: String,
        goal: String
    ) {
        if (inputText.isBlank()) {
            showMessage("Please enter a topic, trend, or select a saved Reel first.")
            return
        }
        _aiState.value = AiGenerationState.Loading
        viewModelScope.launch {
            val result = GeminiStudioService.generateThreeConcepts(
                StudioRequestParams(
                    inputType = inputType,
                    inputText = inputText.trim(),
                    niche = niche,
                    audience = audience,
                    videoLength = videoLength,
                    tone = tone,
                    goal = goal,
                    personalityName = settings.value.personality.displayName
                )
            )
            result.fold(
                onSuccess = { list ->
                    _aiState.value = AiGenerationState.Success(list)
                },
                onFailure = { err ->
                    _aiState.value = AiGenerationState.Error(
                        err.message ?: "Could not reach Gemini API."
                    )
                }
            )
        }
    }

    fun saveAiConceptToVault(dto: AiStudioConceptDto, niche: String) {
        viewModelScope.launch {
            repository.insertSavedAiConcept(
                SavedAiConceptEntity(
                    title = dto.title,
                    openingHook = dto.openingHook,
                    explanation = dto.explanation,
                    sceneOutline = dto.sceneOutline,
                    suggestedShots = dto.suggestedShots,
                    onScreenText = dto.onScreenText,
                    caption = dto.caption,
                    hashtags = dto.hashtags,
                    callToAction = dto.callToAction,
                    editingSuggestions = dto.editingSuggestions,
                    niche = niche
                )
            )
            showMessage("Saved \"${dto.title}\" to your Concept Vault!")
        }
    }

    fun deleteSavedAiConcept(concept: SavedAiConceptEntity) {
        viewModelScope.launch {
            repository.deleteSavedAiConcept(concept)
            showMessage("Removed saved concept.")
        }
    }

    fun sendConceptToPlanner(dto: AiStudioConceptDto, category: String, plannedDate: String) {
        viewModelScope.launch {
            repository.insertPlannerItem(
                PlannerItemEntity(
                    title = dto.title,
                    description = "${dto.openingHook}\n\n${dto.explanation}",
                    category = category,
                    plannedDate = plannedDate,
                    caption = "${dto.caption}\n\n${dto.hashtags}",
                    relatedSource = "AI Content Studio",
                    notes = "Scene Outline:\n${dto.sceneOutline}\n\nShots:\n${dto.suggestedShots}\n\nEditing:\n${dto.editingSuggestions}",
                    status = PlannerStatus.PLANNED.name,
                    isSample = false
                )
            )
            showMessage("Added \"${dto.title}\" to Content Planner!")
            _createSubTab.value = 1
        }
    }

    fun savePlannerItem(
        editingItem: PlannerItemEntity?,
        title: String,
        description: String,
        category: String,
        plannedDate: String,
        caption: String,
        relatedSource: String,
        notes: String,
        status: PlannerStatus,
        scheduleReminderToo: Boolean = false
    ) {
        viewModelScope.launch {
            val cleanTitle = title.trim().ifEmpty { "Untitled Reel Concept" }
            if (editingItem != null) {
                repository.updatePlannerItem(
                    editingItem.copy(
                        title = cleanTitle,
                        description = description.trim(),
                        category = category,
                        plannedDate = plannedDate.trim(),
                        caption = caption.trim(),
                        relatedSource = relatedSource.trim(),
                        notes = notes.trim(),
                        status = status.name
                    )
                )
                showMessage("Updated planner item \"$cleanTitle\".")
            } else {
                repository.insertPlannerItem(
                    PlannerItemEntity(
                        title = cleanTitle,
                        description = description.trim(),
                        category = category,
                        plannedDate = plannedDate.trim(),
                        caption = caption.trim(),
                        relatedSource = relatedSource.trim(),
                        notes = notes.trim(),
                        status = status.name,
                        isSample = false
                    )
                )
                if (scheduleReminderToo) {
                    val preview = generatePreviewMessage(cleanTitle, category)
                    repository.insertReminder(
                        NagReminderEntity(
                            reelId = null,
                            itemTitle = "Film/Edit: $cleanTitle",
                            categoryName = category,
                            scheduledAt = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24),
                            recurrence = ReminderRecurrence.ONCE.name,
                            messagePreview = preview,
                            isSample = false
                        )
                    )
                }
                showMessage("Added \"$cleanTitle\" to your Content Planner!")
            }
        }
    }

    fun advancePlannerStatus(item: PlannerItemEntity, newStatus: PlannerStatus) {
        viewModelScope.launch {
            repository.updatePlannerItem(item.copy(status = newStatus.name))
            showMessage("Moved \"${item.title}\" to ${newStatus.label}!")
        }
    }

    fun deletePlannerItem(item: PlannerItemEntity) {
        viewModelScope.launch {
            repository.deletePlannerItem(item)
            showMessage("Deleted \"${item.title}\" from planner.")
        }
    }

    // --- Backup, Import & Reset ---
    fun exportDataToJson(onExportReady: (String) -> Unit) {
        viewModelScope.launch {
            val jsonStr = repository.exportBackupJson()
            onExportReady(jsonStr)
        }
    }

    fun importDataFromJson(rawJson: String) {
        viewModelScope.launch {
            val res = repository.importBackupJson(rawJson)
            res.fold(
                onSuccess = { count ->
                    showMessage("Imported backup successfully ($count items restored)!")
                },
                onFailure = { err ->
                    showMessage("Import failed: Invalid ReelNag JSON format (${err.localizedMessage}).")
                }
            )
        }
    }

    fun resetApp(keepSamples: Boolean) {
        viewModelScope.launch {
            repository.resetAllData(keepSamples = keepSamples)
            showMessage(
                if (keepSamples) "App reset to fresh sample data!"
                else "All local data wiped clean."
            )
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = ReelNagDatabase.getInstance(context.applicationContext)
            val repo = ReelNagRepository(context.applicationContext, db.dao())
            return ReelNagViewModel(context.applicationContext, repo) as T
        }
    }
}
