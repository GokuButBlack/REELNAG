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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Radar
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.DefaultData
import com.example.data.SavedReelEntity
import com.example.data.TrendItemEntity
import com.example.data.UserSettings
import com.example.ui.WhatShouldIReelTodaySuggestion
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DangerRose
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.LavenderTint
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.MintEmeraldSurface
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiscoverScreen(
    settings: UserSettings,
    trends: List<TrendItemEntity>,
    dailyHelper: WhatShouldIReelTodaySuggestion,
    onToggleSaveTrend: (TrendItemEntity) -> Unit,
    onAddTrend: (
        name: String,
        description: String,
        category: String,
        sourceUrl: String,
        inspiration: String
    ) -> Unit,
    onDeleteTrend: (TrendItemEntity) -> Unit,
    onGenerateIdeasFromTrend: (TrendItemEntity) -> Unit,
    onGenerateIdeasFromPrompt: (String) -> Unit,
    onOpenOriginalUrl: (String) -> Unit,
    onGoToPlanner: () -> Unit,
    onMarkReelWatched: (SavedReelEntity) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddTrendDialog by remember { mutableStateOf(false) }

    val filteredTrends = remember(trends, selectedCategory) {
        if (selectedCategory == "All") trends
        else trends.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("discover_screen_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Discover & Radar",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "What Should I Reel Today + Curated Format Radar",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = { showAddTrendDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                    modifier = Modifier.testTag("add_custom_trend_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Trend")
                }
            }
        }

        // Section D: "WHAT SHOULD I REEL TODAY" Helper Card (100% real data)
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, ElectricPurple.copy(alpha = 0.6f)),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("what_should_i_reel_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(DeepViolet.copy(alpha = 0.65f), CardSurface)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = SoftLavender
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "What Should I Reel Today?",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Personalized from your real saved Reels, interests (${settings.interests.take(2).joinToString()}), & planner",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SoftLavender
                                )
                            }
                        }

                        // 1. Saved Reel to Watch / Revisit
                        val oldest = dailyHelper.oldestUnwatchedReel
                        Surface(
                            color = ElevatedSlate,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, CardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "🎬 1. Watch & Revisit From Your Locker",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = LavenderTint
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                if (oldest != null) {
                                    Text(
                                        text = "\"${oldest.title}\" (${oldest.categoryName})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = { onOpenOriginalUrl(oldest.rawUrl) },
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text("Watch Now", style = MaterialTheme.typography.labelSmall)
                                        }
                                        TextButton(
                                            onClick = { onMarkReelWatched(oldest) }
                                        ) {
                                            Text("Mark Watched", style = MaterialTheme.typography.labelSmall, color = MintEmerald)
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "All saved Reels are watched! Save a new Reel to get watch picks.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        // 2. Original Concept Idea in User's Niche
                        Surface(
                            color = ElevatedSlate,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, CardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "✨ 2. Original Concept Idea For Your Niche",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = LavenderTint
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dailyHelper.originalPromptIdea,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                FilledTonalButton(
                                    onClick = { onGenerateIdeasFromPrompt(dailyHelper.originalPromptIdea) },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Expand Into 3 Scripts in AI Studio", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        // 3. Unfinished Content in Planner
                        val draft = dailyHelper.activePlannerDraft
                        if (draft != null) {
                            Surface(
                                color = ElevatedSlate,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, CardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "📋 3. Resume Unfinished Planner Item",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = LavenderTint
                                        )
                                        Text(
                                            text = "${draft.title} (${draft.status})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                    TextButton(onClick = onGoToPlanner) {
                                        Text("Open Planner →", color = SoftLavender)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section A: TREND RADAR Header + Honesty Disclosure
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = null,
                        tint = SoftLavender
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Trend Radar",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Honesty Note: These are curated repeatable creative formats and your own saved trend links—never fabricated live Instagram engagement counts.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }

        // Trend Category Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(DefaultData.TREND_CATEGORIES) { cat ->
                    val isSelected = selectedCategory.equals(cat, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricPurple.copy(alpha = 0.28f),
                            selectedLabelColor = LavenderTint
                        ),
                        modifier = Modifier.testTag("trend_category_chip_${cat.lowercase().replace(" ", "_")}")
                    )
                }
            }
        }

        if (filteredTrends.isEmpty()) {
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
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = SoftLavender,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No trends in $selectedCategory yet",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = { showAddTrendDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)
                        ) {
                            Text("+ Add Your Own Trend Format")
                        }
                    }
                }
            }
        } else {
            items(filteredTrends, key = { it.id }) { trend ->
                TrendRadarCard(
                    trend = trend,
                    onToggleSave = { onToggleSaveTrend(trend) },
                    onGenerateIdeas = { onGenerateIdeasFromTrend(trend) },
                    onOpenSource = {
                        if (trend.sourceUrl.isNotBlank()) {
                            onOpenOriginalUrl(trend.sourceUrl)
                        }
                    },
                    onDelete = { onDeleteTrend(trend) }
                )
            }
        }
    }

    if (showAddTrendDialog) {
        AddCustomTrendDialog(
            onAdd = { name, desc, cat, url, insp ->
                onAddTrend(name, desc, cat, url, insp)
                showAddTrendDialog = false
            },
            onDismiss = { showAddTrendDialog = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TrendRadarCard(
    trend: TrendItemEntity,
    onToggleSave: () -> Unit,
    onGenerateIdeas: () -> Unit,
    onOpenSource: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("trend_card_${trend.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                            text = trend.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = LavenderTint,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = trend.dateVerified,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }

                Row {
                    IconButton(
                        onClick = onToggleSave,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (trend.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save Trend",
                            tint = if (trend.isSaved) MintEmerald else SoftLavender
                        )
                    }
                    if (!trend.isSample) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Trend",
                                tint = DangerRose
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = trend.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = trend.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = ElevatedSlate,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "💡 How to Use It: ${trend.creativeInspiration}",
                    style = MaterialTheme.typography.bodySmall,
                    color = LavenderTint,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onGenerateIdeas,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                    modifier = Modifier.testTag("trend_generate_ideas_button_${trend.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Generate Ideas")
                }

                OutlinedButton(
                    onClick = onToggleSave
                ) {
                    Text(if (trend.isSaved) "Saved ✓" else "Save Format")
                }

                if (trend.sourceUrl.isNotBlank()) {
                    TextButton(onClick = onOpenSource) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reference Link", color = SoftLavender)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddCustomTrendDialog(
    onAdd: (String, String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Editing Styles") }
    var sourceUrl by remember { mutableStateOf("") }
    var inspiration by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ElevatedSlate,
        title = { Text("Add Trend to Radar") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Trend / Format Name *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_trend_name_input")
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("What makes it work? *") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Category", style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DefaultData.TREND_CATEGORIES.filter { it != "All" }.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                OutlinedTextField(
                    value = inspiration,
                    onValueChange = { inspiration = it },
                    label = { Text("Creative Spin / Inspiration Note") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = sourceUrl,
                    onValueChange = { sourceUrl = it },
                    label = { Text("Reference URL (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(
                            name,
                            description.ifBlank { "Custom creator format added to Trend Radar." },
                            category,
                            sourceUrl,
                            inspiration
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                modifier = Modifier.testTag("confirm_add_trend_button")
            ) {
                Text("Save Trend")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
