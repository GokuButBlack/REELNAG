package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CreateNagReminderSheet
import com.example.ui.components.SaveReelBottomSheet
import com.example.ui.screens.CreateStudioAndPlannerScreen
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProfileSettingsScreen
import com.example.ui.screens.SavedLockerScreen
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.LavenderTint
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.TextSecondary

@Composable
fun ReelNagApp(viewModel: ReelNagViewModel) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val createSubTab by viewModel.createSubTab.collectAsStateWithLifecycle()
    val dailyGreeting by viewModel.dailyGreeting.collectAsStateWithLifecycle()
    val allReels by viewModel.reels.collectAsStateWithLifecycle()
    val filteredReels by viewModel.filteredReels.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val trends by viewModel.trends.collectAsStateWithLifecycle()
    val plannerItems by viewModel.plannerItems.collectAsStateWithLifecycle()
    val savedAiConcepts by viewModel.savedAiConcepts.collectAsStateWithLifecycle()
    val whatShouldIReelToday by viewModel.whatShouldIReelToday.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val watchFilter by viewModel.watchFilter.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()

    val saveDraft by viewModel.saveDraft.collectAsStateWithLifecycle()
    val showCreateReminderSheet by viewModel.showCreateReminderSheet.collectAsStateWithLifecycle()
    val preselectedReelForReminder by viewModel.preselectedReelForReminder.collectAsStateWithLifecycle()

    val aiState by viewModel.aiState.collectAsStateWithLifecycle()
    val studioInputType by viewModel.studioInputType.collectAsStateWithLifecycle()
    val studioInputText by viewModel.studioInputText.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    val openExternalUrl: (String) -> Unit = { url ->
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            viewModel.showMessage("Could not open link in external browser: $url")
        }
    }

    if (!settings.onboardingCompleted) {
        OnboardingScreen(
            initialSettings = settings,
            onComplete = { interests, role, personality, notif, prefTime, qEnabled, qStart, qEnd ->
                viewModel.completeOnboarding(
                    interests = interests,
                    role = role,
                    personality = personality,
                    notificationsEnabled = notif,
                    preferredTime = prefTime,
                    quietHoursEnabled = qEnabled,
                    quietStart = qStart,
                    quietEnd = qEnd
                )
            },
            onSkip = { viewModel.skipOnboarding() }
        )
        return
    }

    if (currentTab != AppTab.HOME) {
        BackHandler {
            viewModel.selectTab(AppTab.HOME)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            containerColor = CharcoalBackground,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            floatingActionButton = {
                if (currentTab == AppTab.HOME || currentTab == AppTab.SAVED) {
                    FloatingActionButton(
                        onClick = { viewModel.openSaveReelSheet() },
                        containerColor = ElectricPurple,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("fab_save_reel")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Save a Reel"
                        )
                    }
                }
            },
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar(
                        containerColor = ElevatedSlate,
                        tonalElevation = 8.dp
                    ) {
                        AppTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = tabIcon(tab, selected),
                                        contentDescription = tab.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = LavenderTint,
                                    selectedTextColor = SoftLavender,
                                    indicatorColor = DeepViolet,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("nav_tab_${tab.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWideScreen) {
                    NavigationRail(
                        containerColor = ElevatedSlate,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        AppTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = tabIcon(tab, selected),
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = LavenderTint,
                                    selectedTextColor = SoftLavender,
                                    indicatorColor = DeepViolet,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("rail_tab_${tab.route}")
                            )
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (currentTab) {
                        AppTab.HOME -> {
                            HomeScreen(
                                settings = settings,
                                dailyGreeting = dailyGreeting,
                                reels = allReels,
                                reminders = reminders,
                                dailySuggestion = whatShouldIReelToday,
                                onRotateGreeting = { viewModel.rotateDailyGreeting() },
                                onOpenSaveReel = { viewModel.openSaveReelSheet() },
                                onOpenCreateReminder = { reel -> viewModel.openCreateReminderSheet(reel) },
                                onViewAllSaved = { viewModel.selectTab(AppTab.SAVED) },
                                onOpenOriginalUrl = openExternalUrl,
                                onToggleReelWatched = { viewModel.toggleReelWatched(it) },
                                onSnoozeReminder = { rem, mins -> viewModel.snoozeReminder(rem, mins) },
                                onCompleteReminder = { viewModel.completeReminder(it) },
                                onTestNotification = { viewModel.testImmediateNagNotification(it) },
                                onJumpToStudioWithReel = { reel ->
                                    viewModel.jumpToAiStudioWithSeed(
                                        "Saved Reel",
                                        "${reel.title} — ${reel.description} ${reel.personalNotes}".trim()
                                    )
                                }
                            )
                        }

                        AppTab.DISCOVER -> {
                            DiscoverScreen(
                                settings = settings,
                                trends = trends,
                                dailyHelper = whatShouldIReelToday,
                                onToggleSaveTrend = { viewModel.toggleTrendSaved(it) },
                                onAddTrend = { name, desc, cat, url, insp ->
                                    viewModel.addCustomTrend(name, desc, cat, url, insp)
                                },
                                onDeleteTrend = { viewModel.deleteTrend(it) },
                                onGenerateIdeasFromTrend = { trend ->
                                    viewModel.jumpToAiStudioWithSeed(
                                        "Trend",
                                        "${trend.name}: ${trend.description} (${trend.creativeInspiration})"
                                    )
                                },
                                onGenerateIdeasFromPrompt = { prompt ->
                                    viewModel.jumpToAiStudioWithSeed("Own Idea", prompt)
                                },
                                onOpenOriginalUrl = openExternalUrl,
                                onGoToPlanner = {
                                    viewModel.selectTab(AppTab.CREATE)
                                    viewModel.selectCreateSubTab(1)
                                },
                                onMarkReelWatched = { viewModel.toggleReelWatched(it) }
                            )
                        }

                        AppTab.CREATE -> {
                            CreateStudioAndPlannerScreen(
                                activeSubTab = createSubTab,
                                onSubTabChange = { viewModel.selectCreateSubTab(it) },
                                settings = settings,
                                savedReels = allReels,
                                trends = trends,
                                categories = categories,
                                plannerItems = plannerItems,
                                savedAiConcepts = savedAiConcepts,
                                studioInputType = studioInputType,
                                studioInputText = studioInputText,
                                onUpdateStudioSeed = { t, s -> viewModel.updateStudioSeed(t, s) },
                                aiState = aiState,
                                onGenerateConcepts = { type, text, niche, aud, len, tone, goal ->
                                    viewModel.generateAiConcepts(type, text, niche, aud, len, tone, goal)
                                },
                                onSaveConceptToVault = { dto, niche ->
                                    viewModel.saveAiConceptToVault(dto, niche)
                                },
                                onDeleteSavedConcept = { viewModel.deleteSavedAiConcept(it) },
                                onSendConceptToPlanner = { dto, cat, date ->
                                    viewModel.sendConceptToPlanner(dto, cat, date)
                                },
                                onSavePlannerItem = { editing, title, desc, cat, date, cap, rel, notes, st, rem ->
                                    viewModel.savePlannerItem(editing, title, desc, cat, date, cap, rel, notes, st, rem)
                                },
                                onAdvancePlannerStatus = { item, st ->
                                    viewModel.advancePlannerStatus(item, st)
                                },
                                onDeletePlannerItem = { viewModel.deletePlannerItem(it) },
                                onUpdateWeeklyGoal = { newGoal ->
                                    viewModel.updateUserSettings { it.copy(weeklyContentGoal = newGoal) }
                                },
                                onShowMessage = { viewModel.showMessage(it) }
                            )
                        }

                        AppTab.SAVED -> {
                            SavedLockerScreen(
                                reels = filteredReels,
                                allReelsCount = allReels.size,
                                categories = categories,
                                reminders = reminders,
                                searchQuery = searchQuery,
                                selectedCategory = selectedCategoryFilter,
                                watchFilter = watchFilter,
                                sortOption = sortOption,
                                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                onCategorySelect = { viewModel.setCategoryFilter(it) },
                                onWatchFilterSelect = { viewModel.setWatchFilter(it) },
                                onSortOptionSelect = { viewModel.setSortOption(it) },
                                onOpenSaveReel = { reel -> viewModel.openSaveReelSheet(editingReel = reel) },
                                onToggleWatched = { viewModel.toggleReelWatched(it) },
                                onSaveNotes = { reel, notes -> viewModel.updateReelNotes(reel, notes) },
                                onDeleteReel = { viewModel.deleteReel(it) },
                                onScheduleNagForReel = { reel -> viewModel.openCreateReminderSheet(reel) },
                                onOpenOriginalUrl = openExternalUrl,
                                onGenerateAiIdeasForReel = { reel ->
                                    viewModel.jumpToAiStudioWithSeed(
                                        "Saved Reel",
                                        "${reel.title} — ${reel.description} ${reel.personalNotes}".trim()
                                    )
                                },
                                onAddCategory = { viewModel.addCustomCategory(it) },
                                onRenameCategory = { cat, name -> viewModel.renameCategory(cat, name) },
                                onDeleteCategory = { viewModel.deleteCategory(it) }
                            )
                        }

                        AppTab.PROFILE -> {
                            ProfileSettingsScreen(
                                settings = settings,
                                onUpdateSettings = { viewModel.updateUserSettings(it) },
                                onExportJson = { cb -> viewModel.exportDataToJson(cb) },
                                onImportJson = { jsonStr -> viewModel.importDataFromJson(jsonStr) },
                                onResetApp = { keepSamples -> viewModel.resetApp(keepSamples) },
                                onTestQuickSaveLink = { demoUrl ->
                                    viewModel.handleIncomingSharedUrl(demoUrl)
                                },
                                onShowMessage = { viewModel.showMessage(it) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (saveDraft.isOpen) {
        SaveReelBottomSheet(
            draft = saveDraft,
            categories = categories,
            personality = settings.personality,
            onUrlChange = { viewModel.onDraftUrlChanged(it) },
            onDraftChange = { viewModel.updateSaveDraft(it) },
            onSave = { viewModel.saveReelFromDraft() },
            onDismiss = { viewModel.closeSaveReelSheet() },
            onViewExistingDuplicate = { existingReel ->
                viewModel.selectTab(AppTab.SAVED)
                viewModel.setCategoryFilter("All")
                viewModel.setSearchQuery(existingReel.title)
            },
            generatePreviewMessage = { title, cat ->
                viewModel.generatePreviewMessage(title, cat)
            }
        )
    }

    if (showCreateReminderSheet) {
        val quietText = if (settings.quietHoursEnabled) {
            "Quiet Hours active (${settings.quietHoursStart}–${settings.quietHoursEnd}): system alerts pause overnight while in-app banners stay ready."
        } else {
            "Quiet Hours are currently turned off in Profile."
        }
        CreateNagReminderSheet(
            preselectedReel = preselectedReelForReminder,
            allReels = allReels,
            personality = settings.personality,
            quietHoursInfo = quietText,
            generatePreview = { title, cat -> viewModel.generatePreviewMessage(title, cat) },
            onSaveReminder = { reelId, title, cat, scheduledAt, rec, preview, fireNow ->
                viewModel.createNagReminder(reelId, title, cat, scheduledAt, rec, preview, fireNow)
            },
            onDismiss = { viewModel.closeCreateReminderSheet() }
        )
    }
}

private fun tabIcon(tab: AppTab, selected: Boolean): ImageVector {
    return when (tab) {
        AppTab.HOME -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
        AppTab.DISCOVER -> if (selected) Icons.Filled.Explore else Icons.Outlined.Explore
        AppTab.CREATE -> if (selected) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome
        AppTab.SAVED -> if (selected) Icons.Filled.VideoLibrary else Icons.Outlined.VideoLibrary
        AppTab.PROFILE -> if (selected) Icons.Filled.Person else Icons.Outlined.Person
    }
}
