package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmAdd
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CategoryEntity
import com.example.data.NagPersonality
import com.example.data.ReminderRecurrence
import com.example.data.SavedReelEntity
import com.example.ui.SaveReelDraftState
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DangerRose
import com.example.ui.theme.DangerRoseSurface
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.LavenderTint
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SaveReelBottomSheet(
    draft: SaveReelDraftState,
    categories: List<CategoryEntity>,
    personality: NagPersonality,
    onUrlChange: (String) -> Unit,
    onDraftChange: ((SaveReelDraftState) -> SaveReelDraftState) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    onViewExistingDuplicate: (SavedReelEntity) -> Unit,
    generatePreviewMessage: (String, String) -> String
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val titleFocusRequester = remember { FocusRequester() }

    LaunchedEffect(draft.isOpen) {
        if (draft.isOpen) {
            try {
                titleFocusRequester.requestFocus()
            } catch (_: Exception) {
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ElevatedSlate
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BookmarkAdd,
                        contentDescription = null,
                        tint = SoftLavender
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (draft.editingReelId != null) "Edit Saved Reel" else "Quick-Save Reel",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            if (draft.prefilledFromShareOrQuery) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = DeepViolet.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, ElectricPurple)
                ) {
                    Text(
                        text = "✨ Link pre-filled from Share / Quick-Save! Give it a quick title below.",
                        style = MaterialTheme.typography.labelMedium,
                        color = LavenderTint,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Auto-focused Title field for super fast saving
            OutlinedTextField(
                value = draft.title,
                onValueChange = { text -> onDraftChange { it.copy(title = text) } },
                label = { Text("Custom Title (auto-focused, optional)") },
                placeholder = { Text("e.g., 10-Min Garlic Butter Pasta or Whip Pan Transition") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(titleFocusRequester)
                    .testTag("save_reel_title_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = draft.rawUrl,
                onValueChange = onUrlChange,
                label = { Text("Instagram Reel URL *") },
                placeholder = { Text("https://www.instagram.com/reel/...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Link, contentDescription = null)
                },
                isError = draft.urlError != null,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_reel_url_input")
            )

            if (draft.urlError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = DangerRoseSurface),
                    border = BorderStroke(1.dp, DangerRose),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_reel_error_banner")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = DangerRose,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = draft.urlError,
                                style = MaterialTheme.typography.bodySmall,
                                color = LavenderTint
                            )
                        }
                        if (draft.duplicateReel != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    onDismiss()
                                    onViewExistingDuplicate(draft.duplicateReel)
                                },
                                border = BorderStroke(1.dp, SoftLavender),
                                modifier = Modifier.testTag("view_duplicate_reel_button")
                            ) {
                                Text("Show Existing Item: \"${draft.duplicateReel.title}\"")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "One-Tap Category",
                style = MaterialTheme.typography.titleSmall,
                color = LavenderTint
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val selected = draft.categoryName.equals(cat.name, ignoreCase = true)
                    FilterChip(
                        selected = selected,
                        onClick = { onDraftChange { it.copy(categoryName = cat.name) } },
                        label = { Text(cat.name) },
                        leadingIcon = if (selected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricPurple.copy(alpha = 0.28f),
                            selectedLabelColor = LavenderTint
                        ),
                        modifier = Modifier.testTag("save_category_chip_${cat.name.lowercase().replace(" ", "_")}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = draft.creatorName,
                    onValueChange = { text -> onDraftChange { it.copy(creatorName = text) } },
                    label = { Text("Creator / Source (optional)") },
                    placeholder = { Text("@creatorhandle") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_reel_creator_input")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = draft.description,
                onValueChange = { text -> onDraftChange { it.copy(description = text) } },
                label = { Text("Short Description (optional)") },
                placeholder = { Text("Why did you save this?") },
                maxLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_reel_description_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = draft.personalNotes,
                onValueChange = { text -> onDraftChange { it.copy(personalNotes = text) } },
                label = { Text("Personal Notes / Takeaways (optional)") },
                placeholder = { Text("Timestamps, lighting notes, ingredients...") },
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_reel_notes_input")
            )

            if (draft.editingReelId == null) {
                Spacer(modifier = Modifier.height(16.dp))
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-Attach Smart Nag Reminder",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "Get a ${personality.displayName} nudge if still unwatched",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Switch(
                                checked = draft.createNagReminder,
                                onCheckedChange = { checked ->
                                    onDraftChange { it.copy(createNagReminder = checked) }
                                }
                            )
                        }

                        if (draft.createNagReminder) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(2 to "In 2h", 24 to "Tomorrow", 72 to "In 3 Days").forEach { (hours, label) ->
                                    FilterChip(
                                        selected = draft.reminderDelayHours == hours,
                                        onClick = { onDraftChange { it.copy(reminderDelayHours = hours) } },
                                        label = { Text(label) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            val livePreview = remember(draft.title, draft.categoryName, personality) {
                                generatePreviewMessage(
                                    draft.title.ifBlank { "this saved Reel" },
                                    draft.categoryName
                                )
                            }
                            Text(
                                text = "${personality.badgeEmoji} Preview: \"$livePreview\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftLavender
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Honesty Note: ReelNag never scrapes private Instagram metadata or downloads copyrighted video files. Your saved card uses your custom title and opens the original link directly in Instagram or your browser.",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = onSave,
                    modifier = Modifier
                        .weight(1.6f)
                        .height(50.dp)
                        .testTag("save_reel_submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (draft.editingReelId != null) "Save Changes" else "Save to Locker",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateNagReminderSheet(
    preselectedReel: SavedReelEntity?,
    allReels: List<SavedReelEntity>,
    personality: NagPersonality,
    quietHoursInfo: String,
    generatePreview: (String, String) -> String,
    onSaveReminder: (
        reelId: Long?,
        itemTitle: String,
        categoryName: String,
        scheduledAtMillis: Long,
        recurrence: ReminderRecurrence,
        previewMessage: String,
        fireNow: Boolean
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedReel by remember { mutableStateOf(preselectedReel) }
    var customTitle by remember {
        mutableStateOf(preselectedReel?.title ?: "")
    }
    var categoryName by remember {
        mutableStateOf(preselectedReel?.categoryName ?: "Favorites")
    }
    var delayOptionMinutes by remember { mutableIntStateOf(60) } // 0 = Due Now, 10 = 10m, 60 = 1h, 1440 = Tomorrow
    var recurrence by remember { mutableStateOf(ReminderRecurrence.ONCE) }
    var previewMessage by remember(customTitle, categoryName, personality) {
        mutableStateOf(
            generatePreview(
                customTitle.ifBlank { "that Reel you saved" },
                categoryName
            )
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ElevatedSlate
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AlarmAdd,
                        contentDescription = null,
                        tint = SoftLavender
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Schedule a Smart Nag",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (allReels.isNotEmpty() && preselectedReel == null) {
                Text(
                    text = "Pick an Unwatched Reel (or type any reminder below):",
                    style = MaterialTheme.typography.labelLarge,
                    color = LavenderTint
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allReels.filter { !it.isWatched }.take(5).forEach { reel ->
                        val isChosen = selectedReel?.id == reel.id
                        FilterChip(
                            selected = isChosen,
                            onClick = {
                                selectedReel = reel
                                customTitle = reel.title
                                categoryName = reel.categoryName
                                previewMessage = generatePreview(reel.title, reel.categoryName)
                            },
                            label = { Text(reel.title.take(28)) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = customTitle,
                onValueChange = {
                    customTitle = it
                    previewMessage = generatePreview(it.ifBlank { "that Reel you saved" }, categoryName)
                },
                label = { Text("What should we nag you about?") },
                placeholder = { Text("e.g., Watch Seamless Match-Cut Tutorial") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("nag_title_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "When should this Nag trigger?",
                style = MaterialTheme.typography.titleSmall,
                color = LavenderTint
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    0 to "Due Right Now (Test)",
                    10 to "In 10 Min",
                    60 to "In 1 Hour",
                    240 to "Tonight (4h)",
                    1440 to "Tomorrow"
                ).forEach { (mins, label) ->
                    FilterChip(
                        selected = delayOptionMinutes == mins,
                        onClick = { delayOptionMinutes = mins },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Recurrence",
                style = MaterialTheme.typography.titleSmall,
                color = LavenderTint
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReminderRecurrence.entries.forEach { rec ->
                    FilterChip(
                        selected = recurrence == rec,
                        onClick = { recurrence = rec },
                        label = { Text(rec.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Personality Message Preview Box with Shuffle Button
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepViolet.copy(alpha = 0.45f)),
                border = BorderStroke(1.dp, ElectricPurple),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SoftLavender,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${personality.badgeEmoji} ${personality.displayName} Preview",
                                style = MaterialTheme.typography.titleSmall,
                                color = LavenderTint
                            )
                        }
                        IconButton(
                            onClick = {
                                previewMessage = generatePreview(
                                    customTitle.ifBlank { "that Reel you saved" },
                                    categoryName
                                )
                            },
                            modifier = Modifier.testTag("shuffle_nag_preview_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Roll another message",
                                tint = SoftLavender
                            )
                        }
                    }
                    Text(
                        text = "\"$previewMessage\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LavenderTint
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = quietHoursInfo,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val scheduledTime = if (delayOptionMinutes == 0) {
                        System.currentTimeMillis() - 1000L
                    } else {
                        System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(delayOptionMinutes.toLong())
                    }
                    onSaveReminder(
                        selectedReel?.id,
                        customTitle.ifBlank { "Watch Saved Reel" },
                        categoryName,
                        scheduledTime,
                        recurrence,
                        previewMessage,
                        delayOptionMinutes == 0
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_create_nag_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (delayOptionMinutes == 0) "Trigger Nag Now" else "Save Smart Nag",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
