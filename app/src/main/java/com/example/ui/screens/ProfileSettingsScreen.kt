package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.DefaultData
import com.example.data.NagPersonality
import com.example.data.UserRole
import com.example.data.UserSettings
import com.example.domain.NagNotificationHelper
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DangerRose
import com.example.ui.theme.DangerRoseSurface
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.LavenderTint
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.NagAmber
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSettingsScreen(
    settings: UserSettings,
    onUpdateSettings: ((UserSettings) -> UserSettings) -> Unit,
    onExportJson: ((String) -> Unit) -> Unit,
    onImportJson: (String) -> Unit,
    onResetApp: (keepSamples: Boolean) -> Unit,
    onTestQuickSaveLink: (String) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    var hasSystemNotificationPermission by remember {
        mutableStateOf(NagNotificationHelper.hasNotificationPermission(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasSystemNotificationPermission = granted
        if (granted) {
            onShowMessage("Android notification permission granted!")
        } else {
            onShowMessage("Permission denied. Reminders will still appear in your in-app Due Now banner.")
        }
    }

    var exportedJsonText by remember { mutableStateOf<String?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonInput by remember { mutableStateOf("") }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("profile_settings_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Profile & Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Customize your nag personality, quiet hours, shortcuts, and local data backup",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // 1. Reminder Personality Selector
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Reminder Personality",
                        style = MaterialTheme.typography.titleMedium,
                        color = LavenderTint,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Changes your daily greeting and all Smart Nag messages immediately.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    NagPersonality.entries.forEach { p ->
                        val selected = settings.personality == p
                        Surface(
                            color = if (selected) DeepViolet.copy(alpha = 0.6f) else ElevatedSlate,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) ElectricPurple else CardBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    onUpdateSettings { it.copy(personality = p) }
                                    onShowMessage("Switched to ${p.badgeEmoji} ${p.displayName}!")
                                }
                                .testTag("profile_personality_${p.name.lowercase()}")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${p.badgeEmoji} ${p.displayName} — ${p.tagline}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (selected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = SoftLavender,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\"${p.sampleLine}\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Role & Interests
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Your Role & Interests",
                        style = MaterialTheme.typography.titleMedium,
                        color = LavenderTint,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = settings.userName,
                        onValueChange = { name -> onUpdateSettings { it.copy(userName = name) } },
                        label = { Text("Display Name / Nickname") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Role", style = MaterialTheme.typography.labelLarge, color = SoftLavender)
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        UserRole.entries.forEach { role ->
                            FilterChip(
                                selected = settings.role == role,
                                onClick = { onUpdateSettings { it.copy(role = role) } },
                                label = { Text(role.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Interests", style = MaterialTheme.typography.labelLarge, color = SoftLavender)
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DefaultData.ALL_INTERESTS.forEach { interest ->
                            val isSelected = interest in settings.interests
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    onUpdateSettings { current ->
                                        val next = if (isSelected) {
                                            current.interests - interest
                                        } else {
                                            current.interests + interest
                                        }
                                        current.copy(interests = next)
                                    }
                                },
                                label = { Text(interest) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricPurple.copy(alpha = 0.25f),
                                    selectedLabelColor = LavenderTint
                                )
                            )
                        }
                    }
                }
            }
        }

        // 3. Notifications, Preferred Reminder Times & Quiet Hours
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = SoftLavender
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Notifications & Quiet Hours",
                            style = MaterialTheme.typography.titleMedium,
                            color = LavenderTint,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = ElevatedSlate,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (hasSystemNotificationPermission) {
                                    "✓ System Notification Permission: Granted"
                                } else {
                                    "⚠️ System Notification Permission: Not Granted Yet"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = if (hasSystemNotificationPermission) MintEmerald else NagAmber
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Plain-English Honesty Note: Due and overdue reminders are always shown inside ReelNag's 'Due Now' banner so you never lose a reminder. Granting Android notification permission allows ReelNag to also post alerts to your system notification shade (respecting your Quiet Hours). Full cloud push across multiple devices is a Phase 3 future upgrade.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            if (!hasSystemNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                                    modifier = Modifier.testTag("request_notification_permission_button")
                                ) {
                                    Text("Grant Notification Permission")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Smart Nag Reminders", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = settings.notificationsEnabled,
                            onCheckedChange = { enabled ->
                                onUpdateSettings { it.copy(notificationsEnabled = enabled) }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = settings.preferredReminderTime,
                        onValueChange = { t -> onUpdateSettings { it.copy(preferredReminderTime = t) } },
                        label = { Text("Preferred Daily Nag Time (24h HH:MM)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Quiet Hours", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Never buzz system notifications during this window",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = settings.quietHoursEnabled,
                            onCheckedChange = { q ->
                                onUpdateSettings { it.copy(quietHoursEnabled = q) }
                            },
                            modifier = Modifier.testTag("quiet_hours_switch")
                        )
                    }

                    if (settings.quietHoursEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = settings.quietHoursStart,
                                onValueChange = { s -> onUpdateSettings { it.copy(quietHoursStart = s) } },
                                label = { Text("Quiet Start (HH:MM)") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("quiet_start_input")
                            )
                            OutlinedTextField(
                                value = settings.quietHoursEnd,
                                onValueChange = { e -> onUpdateSettings { it.copy(quietHoursEnd = e) } },
                                label = { Text("Quiet End (HH:MM)") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("quiet_end_input")
                            )
                        }
                    }
                }
            }
        }

        // 4. Plain-English Phone Share Shortcut & Quick-Save Guide
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = SoftLavender
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "How to Quick-Save From Your Phone",
                            style = MaterialTheme.typography.titleMedium,
                            color = LavenderTint,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Direct Android Share Sheet: While watching a Reel in Instagram, tap the paper-airplane Share icon → 'Share to...' → choose ReelNag. The Save form will pop open with your link pre-filled and tracking parameters automatically stripped!\n\n2. URL Shortcut (`?url=...`): ReelNag also supports opening deep links in the format `https://reelnag.app/save?url=<ReelLink>` or `reelnag://save?url=<ReelLink>` for automation apps and phone shortcuts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            onTestQuickSaveLink("https://www.instagram.com/reel/C9QuickSaveDemo99/?igsh=tracking123")
                        },
                        modifier = Modifier.testTag("simulate_quick_save_button")
                    ) {
                        Text("Simulate `?url=` Quick-Save Now")
                    }
                }
            }
        }

        // 5. Privacy Explanation, JSON Export, JSON Import & Reset App
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PrivacyTip,
                            contentDescription = null,
                            tint = MintEmerald
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Privacy & Your Local Data",
                            style = MaterialTheme.typography.titleMedium,
                            color = LavenderTint,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Plain-English Privacy Promise: All your saved Reels, categories, notes, reminders, and content plans are stored strictly on this device in a local database. Your data does not sync across devices or to external servers, and clearing app storage or resetting the app erases it. Use Export Data below anytime to keep a JSON backup!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                onExportJson { jsonStr ->
                                    exportedJsonText = jsonStr
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                            modifier = Modifier.testTag("export_data_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export My Data (JSON)")
                        }

                        FilledTonalButton(
                            onClick = { showImportDialog = true },
                            modifier = Modifier.testTag("import_data_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import Data (JSON)")
                        }

                        OutlinedButton(
                            onClick = { showResetConfirmDialog = true },
                            border = BorderStroke(1.dp, DangerRose),
                            modifier = Modifier.testTag("reset_app_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = null,
                                tint = DangerRose,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset App", color = DangerRose)
                        }
                    }
                }
            }
        }
    }

    // Exported JSON Preview & Copy Modal
    if (exportedJsonText != null) {
        AlertDialog(
            onDismissRequest = { exportedJsonText = null },
            containerColor = ElevatedSlate,
            title = { Text("Exported ReelNag Backup (JSON)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Copy this JSON to save your backup or paste it into Import Data later:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = exportedJsonText!!,
                        onValueChange = {},
                        readOnly = true,
                        maxLines = 10,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("exported_json_text_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboard.setText(AnnotatedString(exportedJsonText!!))
                        onShowMessage("Backup JSON copied to clipboard!")
                        exportedJsonText = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy JSON")
                }
            },
            dismissButton = {
                TextButton(onClick = { exportedJsonText = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Import JSON Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = ElevatedSlate,
            title = { Text("Import ReelNag Backup (JSON)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Paste a previously exported ReelNag JSON backup below:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = importJsonInput,
                        onValueChange = { importJsonInput = it },
                        placeholder = { Text("{\"schemaVersion\": 1, ...}") },
                        minLines = 5,
                        maxLines = 10,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("import_json_input_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importJsonInput.isNotBlank()) {
                            onImportJson(importJsonInput)
                            showImportDialog = false
                            importJsonInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                    modifier = Modifier.testTag("confirm_import_json_button")
                ) {
                    Text("Restore Backup")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset Confirmation Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            containerColor = ElevatedSlate,
            title = { Text("Reset ReelNag?") },
            text = {
                Text(
                    text = "This will erase your saved Reels, custom categories, reminders, and planner items from this device. Would you like to wipe everything completely empty, or restore the starter sample Reels?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            onResetApp(true)
                            showResetConfirmDialog = false
                        },
                        modifier = Modifier.testTag("confirm_reset_with_samples_button")
                    ) {
                        Text("Reset to Samples")
                    }
                    Button(
                        onClick = {
                            onResetApp(false)
                            showResetConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRose),
                        modifier = Modifier.testTag("confirm_reset_empty_button")
                    ) {
                        Text("Wipe Empty")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
