package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AlarmAdd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.NagReminderEntity
import com.example.data.SavedReelEntity
import com.example.data.UserSettings
import com.example.ui.WhatShouldIReelTodaySuggestion
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.LavenderTint
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.MintEmeraldSurface
import com.example.ui.theme.NagAmber
import com.example.ui.theme.NagAmberSurface
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import java.util.concurrent.TimeUnit
import kotlin.math.max

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    settings: UserSettings,
    dailyGreeting: String,
    reels: List<SavedReelEntity>,
    reminders: List<NagReminderEntity>,
    dailySuggestion: WhatShouldIReelTodaySuggestion,
    onRotateGreeting: () -> Unit,
    onOpenSaveReel: () -> Unit,
    onOpenCreateReminder: (SavedReelEntity?) -> Unit,
    onViewAllSaved: () -> Unit,
    onOpenOriginalUrl: (String) -> Unit,
    onToggleReelWatched: (SavedReelEntity) -> Unit,
    onSnoozeReminder: (NagReminderEntity, Long) -> Unit,
    onCompleteReminder: (NagReminderEntity) -> Unit,
    onTestNotification: (NagReminderEntity) -> Unit,
    onJumpToStudioWithReel: (SavedReelEntity) -> Unit
) {
    val totalSaved = reels.size
    val watchedCount = reels.count { it.isWatched }
    val unwatchedCount = reels.count { !it.isWatched }
    val completedRemindersCount = reminders.count { it.isCompleted }

    val now = System.currentTimeMillis()
    val activeReminders = reminders.filter { !it.isCompleted && it.isEnabled }
    val overdueOrDueNow = activeReminders.filter { it.scheduledAt <= now + TimeUnit.MINUTES.toMillis(5) }
    val upcomingReminders = activeReminders.filter { it.scheduledAt > now + TimeUnit.MINUTES.toMillis(5) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Personality Greeting Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, ElectricPurple.copy(alpha = 0.6f)),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    DeepViolet.copy(alpha = 0.75f),
                                    CardSurface
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = ElectricPurple.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, SoftLavender.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "${settings.personality.badgeEmoji} ${settings.personality.displayName} Mode",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = LavenderTint,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }

                            IconButton(
                                onClick = onRotateGreeting,
                                modifier = Modifier.testTag("rotate_greeting_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "New daily message",
                                    tint = SoftLavender
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Hey ${settings.userName.ifBlank { "Creator" }} 👋",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "\"$dailyGreeting\"",
                            style = MaterialTheme.typography.bodyLarge,
                            color = LavenderTint
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick Actions Row
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onOpenSaveReel,
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                                modifier = Modifier.testTag("home_quick_save_reel_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save a Reel")
                            }

                            FilledTonalButton(
                                onClick = { onOpenCreateReminder(null) },
                                modifier = Modifier.testTag("home_quick_create_nag_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AlarmAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Create Nag")
                            }

                            OutlinedButton(
                                onClick = onViewAllSaved,
                                border = BorderStroke(1.dp, SoftLavender.copy(alpha = 0.5f)),
                                modifier = Modifier.testTag("home_quick_view_saved_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideoLibrary,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("View Saved")
                            }
                        }
                    }
                }
            }
        }

        // 2. Real Usage Stats Grid (100% honest counts from Room DB)
        item {
            Column {
                Text(
                    text = "Real Locker Stats",
                    style = MaterialTheme.typography.titleMedium,
                    color = LavenderTint
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        value = totalSaved.toString(),
                        label = "Saved",
                        accentColor = SoftLavender,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value = unwatchedCount.toString(),
                        label = "Unwatched",
                        accentColor = NagAmber,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value = watchedCount.toString(),
                        label = "Watched",
                        accentColor = MintEmerald,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value = completedRemindersCount.toString(),
                        label = "Nags Done",
                        accentColor = ElectricPurple,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Due Now / Overdue Smart Nags Banner
        if (overdueOrDueNow.isNotEmpty()) {
            item {
                Text(
                    text = "🔥 Due Now / Overdue Nags (${overdueOrDueNow.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = NagAmber,
                    fontWeight = FontWeight.Bold
                )
            }
            items(overdueOrDueNow, key = { "due_${it.id}" }) { reminder ->
                ReminderCard(
                    reminder = reminder,
                    isOverdue = true,
                    personalityEmoji = settings.personality.badgeEmoji,
                    onSnooze = { mins -> onSnoozeReminder(reminder, mins) },
                    onComplete = { onCompleteReminder(reminder) },
                    onTestNotify = { onTestNotification(reminder) }
                )
            }
        }

        // 4. Real Data Suggestion ("Oldest Unwatched Reel")
        item {
            val oldest = dailySuggestion.oldestUnwatchedReel
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = SoftLavender
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Suggested From Your Real Saved Vault",
                            style = MaterialTheme.typography.titleMedium,
                            color = LavenderTint
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (oldest != null) {
                        val daysAgo = max(
                            1L,
                            TimeUnit.MILLISECONDS.toDays(now - oldest.savedAt)
                        )
                        Text(
                            text = "Your oldest unwatched Reel has been waiting $daysAgo day(s) in ${oldest.categoryName}:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = oldest.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (oldest.personalNotes.isNotBlank()) {
                            Text(
                                text = "Note: ${oldest.personalNotes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftLavender
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onOpenOriginalUrl(oldest.rawUrl) },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Original")
                            }
                            OutlinedButton(
                                onClick = { onToggleReelWatched(oldest) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Mark Watched")
                            }
                            TextButton(
                                onClick = { onJumpToStudioWithReel(oldest) }
                            ) {
                                Text("Turn Into AI Concept →", color = SoftLavender)
                            }
                        }
                    } else {
                        Text(
                            text = "Zero unwatched Reels right now! You've actually watched everything you saved—legendary behavior.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MintEmerald
                        )
                    }
                }
            }
        }

        // 5. Upcoming Reminders Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upcoming Smart Nags",
                    style = MaterialTheme.typography.titleMedium,
                    color = LavenderTint
                )
                TextButton(onClick = { onOpenCreateReminder(null) }) {
                    Text("+ New Nag", color = SoftLavender)
                }
            }
        }

        if (upcomingReminders.isEmpty() && overdueOrDueNow.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No active nags scheduled",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Schedule a Smart Nag on any saved Reel so you don't forget it exists.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(upcomingReminders.take(4), key = { "up_${it.id}" }) { reminder ->
                ReminderCard(
                    reminder = reminder,
                    isOverdue = false,
                    personalityEmoji = settings.personality.badgeEmoji,
                    onSnooze = { mins -> onSnoozeReminder(reminder, mins) },
                    onComplete = { onCompleteReminder(reminder) },
                    onTestNotify = { onTestNotification(reminder) }
                )
            }
        }

        // 6. Recently Saved Reels
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recently Saved Reels",
                    style = MaterialTheme.typography.titleMedium,
                    color = LavenderTint
                )
                TextButton(onClick = onViewAllSaved) {
                    Text("See All (${reels.size}) →", color = SoftLavender)
                }
            }
        }

        if (reels.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircleOutline,
                            contentDescription = null,
                            tint = SoftLavender,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your Reel Locker is empty",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap 'Save a Reel' or share any link directly from Instagram.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(reels.take(4), key = { "recent_${it.id}" }) { reel ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = DeepViolet,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = reel.categoryName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LavenderTint,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Surface(
                                color = if (reel.isWatched) MintEmeraldSurface else NagAmberSurface,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (reel.isWatched) "Watched" else "Unwatched",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (reel.isWatched) MintEmerald else NagAmber,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = reel.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (reel.creatorName.isNotBlank()) {
                            Text(
                                text = "Creator: ${reel.creatorName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onOpenOriginalUrl(reel.rawUrl) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open original")
                            }
                            FilledTonalButton(
                                onClick = { onToggleReelWatched(reel) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (reel.isWatched) "Mark Unwatched" else "Mark Watched")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, CardBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReminderCard(
    reminder: NagReminderEntity,
    isOverdue: Boolean,
    personalityEmoji: String,
    onSnooze: (Long) -> Unit,
    onComplete: () -> Unit,
    onTestNotify: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue) NagAmberSurface.copy(alpha = 0.65f) else CardSurface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isOverdue) NagAmber else CardBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reminder_card_${reminder.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isOverdue) NagAmber.copy(alpha = 0.2f) else DeepViolet
                    ) {
                        Text(
                            text = personalityEmoji,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = reminder.itemTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val statusLabel = if (isOverdue) {
                            "Due Now • Snoozed ${reminder.snoozeCount}x • ${reminder.recurrence}"
                        } else {
                            val diffMin = max(1L, TimeUnit.MILLISECONDS.toMinutes(reminder.scheduledAt - System.currentTimeMillis()))
                            val timeStr = if (diffMin >= 60) "${diffMin / 60}h ${diffMin % 60}m" else "${diffMin}m"
                            "In $timeStr • ${reminder.recurrence}"
                        }
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOverdue) NagAmber else SoftLavender
                        )
                    }
                }

                IconButton(
                    onClick = onTestNotify,
                    modifier = Modifier.testTag("test_notify_button_${reminder.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Test notification delivery",
                        tint = SoftLavender,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (reminder.messagePreview.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"${reminder.messagePreview}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = LavenderTint
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onComplete,
                    colors = ButtonDefaults.buttonColors(containerColor = MintEmerald),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("complete_reminder_button_${reminder.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Done", color = Color.Black, style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = { onSnooze(10L) },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("snooze_10m_button_${reminder.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Snooze,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("10m", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = { onSnooze(60L) },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("snooze_1h_button_${reminder.id}")
                ) {
                    Text("1h", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = { onSnooze(1440L) },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("snooze_tomorrow_button_${reminder.id}")
                ) {
                    Text("Tomorrow", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
