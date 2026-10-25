package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AlarmAdd
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.CategoryEntity
import com.example.data.NagReminderEntity
import com.example.data.SavedReelEntity
import com.example.ui.ReelSortOption
import com.example.ui.WatchFilter
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DangerRose
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.LavenderTint
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.MintEmeraldSurface
import com.example.ui.theme.NagAmber
import com.example.ui.theme.NagAmberSurface
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SavedLockerScreen(
    reels: List<SavedReelEntity>,
    allReelsCount: Int,
    categories: List<CategoryEntity>,
    reminders: List<NagReminderEntity>,
    searchQuery: String,
    selectedCategory: String,
    watchFilter: WatchFilter,
    sortOption: ReelSortOption,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onWatchFilterSelect: (WatchFilter) -> Unit,
    onSortOptionSelect: (ReelSortOption) -> Unit,
    onOpenSaveReel: (SavedReelEntity?) -> Unit,
    onToggleWatched: (SavedReelEntity) -> Unit,
    onSaveNotes: (SavedReelEntity, String) -> Unit,
    onDeleteReel: (SavedReelEntity) -> Unit,
    onScheduleNagForReel: (SavedReelEntity) -> Unit,
    onOpenOriginalUrl: (String) -> Unit,
    onGenerateAiIdeasForReel: (SavedReelEntity) -> Unit,
    onAddCategory: (String) -> Unit,
    onRenameCategory: (CategoryEntity, String) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit
) {
    var showCategoryManagerDialog by remember { mutableStateOf(false) }
    var editingNotesReel by remember { mutableStateOf<SavedReelEntity?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("saved_locker_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header + Save Button + Manage Categories Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Reel Locker",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Your saved Instagram Reels, notes & reminders",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showCategoryManagerDialog = true },
                        modifier = Modifier.testTag("manage_categories_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = "Manage Categories",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Folders")
                    }

                    Button(
                        onClick = { onOpenSaveReel(null) },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                        modifier = Modifier.testTag("locker_save_reel_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Reel")
                    }
                }
            }
        }

        // Search Field
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search title, description, creator, or notes...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                } else null,
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("locker_search_input")
            )
        }

        // Category Filter Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == "All",
                        onClick = { onCategorySelect("All") },
                        label = { Text("All ($allReelsCount)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricPurple.copy(alpha = 0.28f),
                            selectedLabelColor = LavenderTint
                        ),
                        modifier = Modifier.testTag("filter_category_all")
                    )
                }
                items(categories, key = { it.id }) { cat ->
                    val isSelected = selectedCategory.equals(cat.name, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelect(cat.name) },
                        label = { Text(cat.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricPurple.copy(alpha = 0.28f),
                            selectedLabelColor = LavenderTint
                        ),
                        modifier = Modifier.testTag("filter_category_${cat.name.lowercase().replace(" ", "_")}")
                    )
                }
            }
        }

        // Watch Status Filter & Sort Bar
        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    WatchFilter.entries.forEach { wf ->
                        FilterChip(
                            selected = watchFilter == wf,
                            onClick = { onWatchFilterSelect(wf) },
                            label = { Text(wf.label) },
                            modifier = Modifier.testTag("watch_filter_${wf.name.lowercase()}")
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = null,
                        tint = SoftLavender,
                        modifier = Modifier.size(18.dp)
                    )
                    ReelSortOption.entries.forEach { option ->
                        FilterChip(
                            selected = sortOption == option,
                            onClick = { onSortOptionSelect(option) },
                            label = { Text(option.label, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("sort_option_${option.name.lowercase()}")
                        )
                    }
                }
            }
        }

        // Reel Cards List or Friendly Empty State
        if (reels.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("locker_empty_state")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (allReelsCount == 0) "Your Reel Locker is Empty" else "No Reels Match Your Filters",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (allReelsCount == 0) {
                                "Paste an Instagram Reel link or share straight from the Instagram app to start organizing inspiration."
                            } else {
                                "Try clearing your search query or switching back to the 'All' category tab."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        if (allReelsCount == 0) {
                            Button(
                                onClick = { onOpenSaveReel(null) },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)
                            ) {
                                Text("+ Save Your First Reel")
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    onSearchQueryChange("")
                                    onCategorySelect("All")
                                    onWatchFilterSelect(WatchFilter.ALL)
                                }
                            ) {
                                Text("Reset Search & Filters")
                            }
                        }
                    }
                }
            }
        } else {
            items(reels, key = { it.id }) { reel ->
                val activeNag = reminders.firstOrNull { it.reelId == reel.id && !it.isCompleted }
                SavedReelCard(
                    reel = reel,
                    activeNag = activeNag,
                    onToggleWatched = { onToggleWatched(reel) },
                    onEdit = { onOpenSaveReel(reel) },
                    onEditNotes = { editingNotesReel = reel },
                    onDelete = { onDeleteReel(reel) },
                    onScheduleNag = { onScheduleNagForReel(reel) },
                    onOpenOriginal = { onOpenOriginalUrl(reel.rawUrl) },
                    onGenerateIdeas = { onGenerateAiIdeasForReel(reel) }
                )
            }
        }
    }

    if (showCategoryManagerDialog) {
        CategoryManagerDialog(
            categories = categories,
            onAddCategory = onAddCategory,
            onRenameCategory = onRenameCategory,
            onDeleteCategory = onDeleteCategory,
            onDismiss = { showCategoryManagerDialog = false }
        )
    }

    if (editingNotesReel != null) {
        val reel = editingNotesReel!!
        var notesDraft by remember(reel) { mutableStateOf(reel.personalNotes) }
        AlertDialog(
            onDismissRequest = { editingNotesReel = null },
            containerColor = ElevatedSlate,
            title = { Text("Notes for \"${reel.title}\"") },
            text = {
                OutlinedTextField(
                    value = notesDraft,
                    onValueChange = { notesDraft = it },
                    label = { Text("Personal Notes & Takeaways") },
                    placeholder = { Text("What hook, audio, recipe step, or idea stood out?") },
                    minLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick_notes_dialog_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveNotes(reel, notesDraft)
                        editingNotesReel = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)
                ) {
                    Text("Save Notes")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingNotesReel = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SavedReelCard(
    reel: SavedReelEntity,
    activeNag: NagReminderEntity?,
    onToggleWatched: () -> Unit,
    onEdit: () -> Unit,
    onEditNotes: () -> Unit,
    onDelete: () -> Unit,
    onScheduleNag: () -> Unit,
    onOpenOriginal: () -> Unit,
    onGenerateIdeas: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val savedDateStr = remember(reel.savedAt) { dateFormat.format(Date(reel.savedAt)) }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_reel_card_${reel.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                            text = if (reel.isWatched) "✓ Watched" else "Unwatched",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (reel.isWatched) MintEmerald else NagAmber,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (reel.isSample) {
                        Surface(
                            color = CardBorder,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Sample",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("edit_reel_button_${reel.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Reel",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("delete_reel_button_${reel.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Reel",
                            tint = DangerRose,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Clean Fallback Visual Header (User's own title + Creator + Saved Date)
            Text(
                text = reel.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = buildString {
                    if (reel.creatorName.isNotBlank()) {
                        append("By ${reel.creatorName} • ")
                    }
                    append("Saved $savedDateStr")
                },
                style = MaterialTheme.typography.labelMedium,
                color = SoftLavender
            )

            if (reel.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = reel.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            if (reel.personalNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = ElevatedSlate,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📝 Notes: ${reel.personalNotes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = LavenderTint,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            if (activeNag != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⏰ Active Nag: \"${activeNag.messagePreview.take(80)}...\"",
                    style = MaterialTheme.typography.labelSmall,
                    color = NagAmber,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary Action Row: Open Original + Toggle Watched
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onOpenOriginal,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                    modifier = Modifier.testTag("open_original_button_${reel.id}")
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
                    onClick = onToggleWatched,
                    modifier = Modifier.testTag("toggle_watched_button_${reel.id}")
                ) {
                    Icon(
                        imageVector = if (reel.isWatched) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (reel.isWatched) "Watched" else "Mark Watched")
                }

                OutlinedButton(
                    onClick = onEditNotes,
                    modifier = Modifier.testTag("notes_button_${reel.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Notes")
                }

                OutlinedButton(
                    onClick = onScheduleNag,
                    modifier = Modifier.testTag("nag_button_${reel.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.AlarmAdd,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Set Nag")
                }

                TextButton(
                    onClick = onGenerateIdeas
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = SoftLavender,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AI Remix", color = SoftLavender)
                }
            }
        }
    }
}

@Composable
private fun CategoryManagerDialog(
    categories: List<CategoryEntity>,
    onAddCategory: (String) -> Unit,
    onRenameCategory: (CategoryEntity, String) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var newCategoryName by remember { mutableStateOf("") }
    var renamingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var renameText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ElevatedSlate,
        title = { Text("Manage Categories") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("New Custom Category") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("new_category_input")
                    )
                    Button(
                        onClick = {
                            if (newCategoryName.isNotBlank()) {
                                onAddCategory(newCategoryName)
                                newCategoryName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                        modifier = Modifier.testTag("add_category_confirm_button")
                    ) {
                        Text("Add")
                    }
                }

                if (renamingCategory != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        border = BorderStroke(1.dp, ElectricPurple)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Rename \"${renamingCategory!!.name}\":",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = renameText,
                                onValueChange = { renameText = it },
                                singleLine = true
                            )
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                TextButton(onClick = { renamingCategory = null }) {
                                    Text("Cancel")
                                }
                                TextButton(
                                    onClick = {
                                        onRenameCategory(renamingCategory!!, renameText)
                                        renamingCategory = null
                                    }
                                ) {
                                    Text("Save", color = SoftLavender)
                                }
                            }
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.height(240.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categories, key = { it.id }) { cat ->
                        Surface(
                            color = CardSurface,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Row {
                                    IconButton(
                                        onClick = {
                                            renamingCategory = cat
                                            renameText = cat.name
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Rename ${cat.name}",
                                            tint = SoftLavender,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    if (cat.name != "Favorites") {
                                        IconButton(
                                            onClick = { onDeleteCategory(cat) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Delete ${cat.name}",
                                                tint = DangerRose,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
