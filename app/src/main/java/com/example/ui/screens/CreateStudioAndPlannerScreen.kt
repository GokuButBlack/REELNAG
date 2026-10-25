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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ViewKanban
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ai.AiStudioConceptDto
import com.example.ai.GeminiStudioService
import com.example.data.CategoryEntity
import com.example.data.PlannerItemEntity
import com.example.data.PlannerStatus
import com.example.data.SavedAiConceptEntity
import com.example.data.SavedReelEntity
import com.example.data.TrendItemEntity
import com.example.data.UserSettings
import com.example.ui.AiGenerationState
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DangerRose
import com.example.ui.theme.DangerRoseSurface
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.LavenderTint
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.MintEmeraldSurface
import com.example.ui.theme.NagAmber
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@Composable
fun CreateStudioAndPlannerScreen(
    activeSubTab: Int,
    onSubTabChange: (Int) -> Unit,
    settings: UserSettings,
    savedReels: List<SavedReelEntity>,
    trends: List<TrendItemEntity>,
    categories: List<CategoryEntity>,
    plannerItems: List<PlannerItemEntity>,
    savedAiConcepts: List<SavedAiConceptEntity>,
    studioInputType: String,
    studioInputText: String,
    onUpdateStudioSeed: (String, String) -> Unit,
    aiState: AiGenerationState,
    onGenerateConcepts: (
        inputType: String,
        inputText: String,
        niche: String,
        audience: String,
        videoLength: String,
        tone: String,
        goal: String
    ) -> Unit,
    onSaveConceptToVault: (AiStudioConceptDto, String) -> Unit,
    onDeleteSavedConcept: (SavedAiConceptEntity) -> Unit,
    onSendConceptToPlanner: (AiStudioConceptDto, String, String) -> Unit,
    onSavePlannerItem: (
        editingItem: PlannerItemEntity?,
        title: String,
        description: String,
        category: String,
        plannedDate: String,
        caption: String,
        relatedSource: String,
        notes: String,
        status: PlannerStatus,
        scheduleReminderToo: Boolean
    ) -> Unit,
    onAdvancePlannerStatus: (PlannerItemEntity, PlannerStatus) -> Unit,
    onDeletePlannerItem: (PlannerItemEntity) -> Unit,
    onUpdateWeeklyGoal: (Int) -> Unit,
    onShowMessage: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = activeSubTab,
            containerColor = ElevatedSlate,
            contentColor = LavenderTint
        ) {
            Tab(
                selected = activeSubTab == 0,
                onClick = { onSubTabChange(0) },
                text = { Text("✨ AI Content Studio") },
                modifier = Modifier.testTag("subtab_ai_studio")
            )
            Tab(
                selected = activeSubTab == 1,
                onClick = { onSubTabChange(1) },
                text = { Text("📅 Content Planner (${plannerItems.size})") },
                modifier = Modifier.testTag("subtab_content_planner")
            )
        }

        if (activeSubTab == 0) {
            AiContentStudioPane(
                settings = settings,
                savedReels = savedReels,
                trends = trends,
                savedAiConcepts = savedAiConcepts,
                studioInputType = studioInputType,
                studioInputText = studioInputText,
                onUpdateStudioSeed = onUpdateStudioSeed,
                aiState = aiState,
                onGenerateConcepts = onGenerateConcepts,
                onSaveConceptToVault = onSaveConceptToVault,
                onDeleteSavedConcept = onDeleteSavedConcept,
                onSendConceptToPlanner = onSendConceptToPlanner,
                onShowMessage = onShowMessage
            )
        } else {
            ContentPlannerPane(
                settings = settings,
                categories = categories,
                plannerItems = plannerItems,
                onSavePlannerItem = onSavePlannerItem,
                onAdvancePlannerStatus = onAdvancePlannerStatus,
                onDeletePlannerItem = onDeletePlannerItem,
                onUpdateWeeklyGoal = onUpdateWeeklyGoal
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AiContentStudioPane(
    settings: UserSettings,
    savedReels: List<SavedReelEntity>,
    trends: List<TrendItemEntity>,
    savedAiConcepts: List<SavedAiConceptEntity>,
    studioInputType: String,
    studioInputText: String,
    onUpdateStudioSeed: (String, String) -> Unit,
    aiState: AiGenerationState,
    onGenerateConcepts: (String, String, String, String, String, String, String) -> Unit,
    onSaveConceptToVault: (AiStudioConceptDto, String) -> Unit,
    onDeleteSavedConcept: (SavedAiConceptEntity) -> Unit,
    onSendConceptToPlanner: (AiStudioConceptDto, String, String) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val inputTypes = listOf("Topic", "Trend", "Own Idea", "Saved Reel", "Reference URL")
    var selectedNiche by remember {
        mutableStateOf(settings.interests.firstOrNull() ?: "Lifestyle")
    }
    var selectedAudience by remember { mutableStateOf("Beginners & Enthusiasts") }
    var selectedLength by remember { mutableStateOf("15–30s") }
    var selectedTone by remember { mutableStateOf("Witty & Fast-Paced") }
    var selectedGoal by remember { mutableStateOf("High Watch-Time & Saves") }

    var editingConcept by remember { mutableStateOf<AiStudioConceptDto?>(null) }
    val clipboard = LocalClipboardManager.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ai_studio_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header & High Thinking Badge
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                border = BorderStroke(1.dp, ElectricPurple.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SoftLavender
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI Content Studio",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            color = DeepViolet,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, SoftLavender.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "🧠 High Thinking Mode",
                                style = MaterialTheme.typography.labelSmall,
                                color = LavenderTint,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Turn any topic, trend, or saved Reel into 3 distinct, scene-by-scene original concepts with hooks, shots, captions, and editing notes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    if (!GeminiStudioService.isApiKeyConfigured()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = ElevatedSlate,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, NagAmber.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "ℹ️ Note: To run live Gemini High Thinking generation, configure GEMINI_API_KEY in the AI Studio Secrets panel. You can still use the Content Planner, Trend Radar, and Reel Locker without a key.",
                                style = MaterialTheme.typography.labelSmall,
                                color = NagAmber,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }

        // Input Source Type Selector
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Pick Inspiration Source",
                        style = MaterialTheme.typography.titleSmall,
                        color = LavenderTint
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        inputTypes.forEach { type ->
                            FilterChip(
                                selected = studioInputType == type,
                                onClick = { onUpdateStudioSeed(type, studioInputText) },
                                label = { Text(type) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricPurple.copy(alpha = 0.28f),
                                    selectedLabelColor = LavenderTint
                                )
                            )
                        }
                    }

                    if (studioInputType == "Saved Reel" && savedReels.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Tap a Saved Reel to populate:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            savedReels.take(6).forEach { reel ->
                                FilterChip(
                                    selected = studioInputText.contains(reel.title),
                                    onClick = {
                                        onUpdateStudioSeed(
                                            "Saved Reel",
                                            "${reel.title} — ${reel.description} ${reel.personalNotes}".trim()
                                        )
                                    },
                                    label = { Text(reel.title.take(26)) }
                                )
                            }
                        }
                    } else if (studioInputType == "Trend" && trends.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Tap a Trend Format to populate:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            trends.take(6).forEach { trend ->
                                FilterChip(
                                    selected = studioInputText.contains(trend.name),
                                    onClick = {
                                        onUpdateStudioSeed(
                                            "Trend",
                                            "${trend.name}: ${trend.description} (${trend.creativeInspiration})"
                                        )
                                    },
                                    label = { Text(trend.name.removePrefix("[Curated Sample] ").take(24)) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = studioInputText,
                        onValueChange = { onUpdateStudioSeed(studioInputType, it) },
                        label = { Text("Your $studioInputType / Seed Idea *") },
                        placeholder = { Text("e.g., 3 mistakes people make when meal prepping high-protein lunches...") },
                        minLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("studio_seed_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "2. Tune Creative Parameters",
                        style = MaterialTheme.typography.titleSmall,
                        color = LavenderTint
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Niche
                    Text("Niche", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Comedy", "Fashion", "Fitness", "Food", "Lifestyle", "Tech & AI", "Education").forEach { n ->
                            FilterChip(
                                selected = selectedNiche == n,
                                onClick = { selectedNiche = n },
                                label = { Text(n, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Video Length", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Under 15s", "15–30s", "30–60s", "60–90s Deep Dive").forEach { len ->
                            FilterChip(
                                selected = selectedLength == len,
                                onClick = { selectedLength = len },
                                label = { Text(len, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Tone", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Witty & Fast-Paced", "Cozy & Aesthetic", "Bold & Contrarian", "Step-by-Step Practical").forEach { t ->
                            FilterChip(
                                selected = selectedTone == t,
                                onClick = { selectedTone = t },
                                label = { Text(t, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Goal & Target Audience", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("High Watch-Time & Saves", "Shares & Relatability", "Comments & Debate", "Teach a Skill").forEach { g ->
                            FilterChip(
                                selected = selectedGoal == g,
                                onClick = { selectedGoal = g },
                                label = { Text(g, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onGenerateConcepts(
                                studioInputType,
                                studioInputText,
                                selectedNiche,
                                selectedAudience,
                                selectedLength,
                                selectedTone,
                                selectedGoal
                            )
                        },
                        enabled = aiState !is AiGenerationState.Loading,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("generate_3_concepts_button")
                    ) {
                        if (aiState is AiGenerationState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = LavenderTint
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Thinking Deeply (3 Distinct Concepts)...")
                        } else {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Generate 3 Distinct Concepts",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }

        // AI Output States (Error / Success)
        when (aiState) {
            is AiGenerationState.Error -> {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DangerRoseSurface),
                        border = BorderStroke(1.dp, DangerRose),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_studio_error_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = DangerRose
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI Studio Notice",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = LavenderTint,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = aiState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = LavenderTint
                            )
                        }
                    }
                }
            }

            is AiGenerationState.Success -> {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Generated Concepts (${aiState.concepts.size})",
                            style = MaterialTheme.typography.titleLarge,
                            color = LavenderTint,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = {
                                onGenerateConcepts(
                                    studioInputType,
                                    studioInputText,
                                    selectedNiche,
                                    selectedAudience,
                                    selectedLength,
                                    selectedTone,
                                    selectedGoal
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Regenerate", color = SoftLavender)
                        }
                    }
                }

                itemsIndexed(aiState.concepts) { idx, concept ->
                    AiConceptCard(
                        index = idx + 1,
                        concept = concept,
                        onCopy = {
                            val formatted = buildString {
                                appendLine("TITLE: ${concept.title}")
                                appendLine("HOOK: ${concept.openingHook}")
                                appendLine("CONCEPT: ${concept.explanation}")
                                appendLine("SCENES:\n${concept.sceneOutline}")
                                appendLine("SHOTS:\n${concept.suggestedShots}")
                                appendLine("ON-SCREEN TEXT: ${concept.onScreenText}")
                                appendLine("CAPTION: ${concept.caption}")
                                appendLine("HASHTAGS: ${concept.hashtags}")
                                appendLine("CTA: ${concept.callToAction}")
                                appendLine("EDITING: ${concept.editingSuggestions}")
                            }
                            clipboard.setText(AnnotatedString(formatted))
                            onShowMessage("Copied full script for \"${concept.title}\" to clipboard!")
                        },
                        onEdit = { editingConcept = concept },
                        onSave = { onSaveConceptToVault(concept, selectedNiche) },
                        onSendToPlanner = {
                            onSendConceptToPlanner(concept, selectedNiche, "2026-10-31")
                        }
                    )
                }
            }

            else -> Unit
        }

        // Saved AI Concepts Vault
        if (savedAiConcepts.isNotEmpty()) {
            item {
                Text(
                    text = "Saved Concept Vault (${savedAiConcepts.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = LavenderTint
                )
            }
            items(savedAiConcepts, key = { "saved_ai_${it.id}" }) { saved ->
                val dto = AiStudioConceptDto(
                    title = saved.title,
                    openingHook = saved.openingHook,
                    explanation = saved.explanation,
                    sceneOutline = saved.sceneOutline,
                    suggestedShots = saved.suggestedShots,
                    onScreenText = saved.onScreenText,
                    caption = saved.caption,
                    hashtags = saved.hashtags,
                    callToAction = saved.callToAction,
                    editingSuggestions = saved.editingSuggestions
                )
                AiConceptCard(
                    index = null,
                    concept = dto,
                    onCopy = {
                        clipboard.setText(AnnotatedString("${saved.title}\nHook: ${saved.openingHook}\n${saved.sceneOutline}"))
                        onShowMessage("Copied \"${saved.title}\"!")
                    },
                    onEdit = { editingConcept = dto },
                    onSave = null,
                    onSendToPlanner = {
                        onSendConceptToPlanner(dto, saved.niche, "2026-11-01")
                    },
                    onDeleteSaved = { onDeleteSavedConcept(saved) }
                )
            }
        }
    }

    if (editingConcept != null) {
        val current = editingConcept!!
        var title by remember(current) { mutableStateOf(current.title) }
        var hook by remember(current) { mutableStateOf(current.openingHook) }
        var outline by remember(current) { mutableStateOf(current.sceneOutline) }
        var caption by remember(current) { mutableStateOf(current.caption) }

        AlertDialog(
            onDismissRequest = { editingConcept = null },
            containerColor = ElevatedSlate,
            title = { Text("Customize & Save Concept") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = hook,
                        onValueChange = { hook = it },
                        label = { Text("Opening Hook (0–3s)") },
                        maxLines = 2
                    )
                    OutlinedTextField(
                        value = outline,
                        onValueChange = { outline = it },
                        label = { Text("Scene-by-Scene Outline") },
                        minLines = 3
                    )
                    OutlinedTextField(
                        value = caption,
                        onValueChange = { caption = it },
                        label = { Text("Caption") },
                        maxLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = current.copy(
                            title = title,
                            openingHook = hook,
                            sceneOutline = outline,
                            caption = caption
                        )
                        onSaveConceptToVault(updated, selectedNiche)
                        editingConcept = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)
                ) {
                    Text("Save to Vault")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingConcept = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AiConceptCard(
    index: Int?,
    concept: AiStudioConceptDto,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onSave: (() -> Unit)?,
    onSendToPlanner: () -> Unit,
    onDeleteSaved: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
                        text = if (index != null) "Concept #$index" else "Saved Concept",
                        style = MaterialTheme.typography.labelSmall,
                        color = LavenderTint,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (onDeleteSaved != null) {
                    IconButton(onClick = onDeleteSaved, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete saved concept",
                            tint = DangerRose,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = concept.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            ConceptFieldBlock("🪝 Opening Hook (0–3s)", concept.openingHook)
            ConceptFieldBlock("💡 Why It Works", concept.explanation)
            ConceptFieldBlock("🎬 Scene-by-Scene Outline", concept.sceneOutline)
            ConceptFieldBlock("🎥 Suggested Camera Shots", concept.suggestedShots)
            ConceptFieldBlock("🔤 On-Screen Text", concept.onScreenText)
            ConceptFieldBlock("📝 Caption & Hashtags", "${concept.caption}\n${concept.hashtags}")
            ConceptFieldBlock("📣 Call To Action", concept.callToAction)
            ConceptFieldBlock("✂️ Editing Suggestions", concept.editingSuggestions)

            Spacer(modifier = Modifier.height(4.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSendToPlanner,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add to Planner")
                }

                if (onSave != null) {
                    FilledTonalButton(onClick = onSave) {
                        Icon(
                            imageVector = Icons.Default.BookmarkAdd,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Concept")
                    }
                }

                OutlinedButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }

                OutlinedButton(onClick = onCopy) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy")
                }
            }
        }
    }
}

@Composable
private fun ConceptFieldBlock(label: String, content: String) {
    if (content.isBlank()) return
    Surface(
        color = ElevatedSlate,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = SoftLavender,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = LavenderTint
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ContentPlannerPane(
    settings: UserSettings,
    categories: List<CategoryEntity>,
    plannerItems: List<PlannerItemEntity>,
    onSavePlannerItem: (
        editingItem: PlannerItemEntity?,
        title: String,
        description: String,
        category: String,
        plannedDate: String,
        caption: String,
        relatedSource: String,
        notes: String,
        status: PlannerStatus,
        scheduleReminderToo: Boolean
    ) -> Unit,
    onAdvancePlannerStatus: (PlannerItemEntity, PlannerStatus) -> Unit,
    onDeletePlannerItem: (PlannerItemEntity) -> Unit,
    onUpdateWeeklyGoal: (Int) -> Unit
) {
    var viewMode by remember { mutableIntStateOf(0) } // 0 = List View, 1 = Calendar / Schedule View
    var selectedStatusFilter by remember { mutableStateOf("All") }
    var showEditPlannerDialog by remember { mutableStateOf(false) }
    var editingPlannerItem by remember { mutableStateOf<PlannerItemEntity?>(null) }

    val publishedOrReadyCount = plannerItems.count {
        it.status == PlannerStatus.PUBLISHED.name || it.status == PlannerStatus.READY_TO_PUBLISH.name
    }
    val goal = settings.weeklyContentGoal.coerceAtLeast(1)
    val progress = (publishedOrReadyCount.toFloat() / goal.toFloat()).coerceIn(0f, 1f)

    val filteredItems = remember(plannerItems, selectedStatusFilter, viewMode) {
        val base = if (selectedStatusFilter == "All") {
            plannerItems
        } else {
            plannerItems.filter { it.status == selectedStatusFilter }
        }
        if (viewMode == 1) {
            base.sortedBy { it.plannedDate.ifBlank { "9999-99-99" } }
        } else {
            base
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("content_planner_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Weekly Content Goal Tracker + Honesty Note (No direct IG publishing)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Weekly Creator Goal: $publishedOrReadyCount / $goal Reels Ready",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Plan scripts, captions & dates. (Note: No direct Instagram publishing exists; export/copy your caption when ready to post.)",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                        Row {
                            TextButton(onClick = { if (goal > 1) onUpdateWeeklyGoal(goal - 1) }) {
                                Text("-1", color = SoftLavender)
                            }
                            TextButton(onClick = { onUpdateWeeklyGoal(goal + 1) }) {
                                Text("+1", color = SoftLavender)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        color = MintEmerald,
                        trackColor = CardBorder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    )
                }
            }
        }

        // View Switcher (List vs Calendar Schedule) + New Plan Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = viewMode == 0,
                        onClick = { viewMode = 0 },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.ViewList, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        label = { Text("Status List") },
                        modifier = Modifier.testTag("planner_list_view_chip")
                    )
                    FilterChip(
                        selected = viewMode == 1,
                        onClick = { viewMode = 1 },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        label = { Text("Calendar Schedule") },
                        modifier = Modifier.testTag("planner_calendar_view_chip")
                    )
                }

                Button(
                    onClick = {
                        editingPlannerItem = null
                        showEditPlannerDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                    modifier = Modifier.testTag("new_planner_item_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Plan")
                }
            }
        }

        // Status Filters
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedStatusFilter == "All",
                    onClick = { selectedStatusFilter = "All" },
                    label = { Text("All (${plannerItems.size})") }
                )
                PlannerStatus.entries.forEach { st ->
                    val count = plannerItems.count { it.status == st.name }
                    FilterChip(
                        selected = selectedStatusFilter == st.name,
                        onClick = { selectedStatusFilter = st.name },
                        label = { Text("${st.label} ($count)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricPurple.copy(alpha = 0.25f),
                            selectedLabelColor = LavenderTint
                        )
                    )
                }
            }
        }

        if (filteredItems.isEmpty()) {
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
                            imageVector = Icons.Default.ViewKanban,
                            contentDescription = null,
                            tint = SoftLavender,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Content Plans in This View",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Create a plan manually or send a generated script from the AI Content Studio.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(filteredItems, key = { it.id }) { item ->
                PlannerItemCard(
                    item = item,
                    showCalendarHeader = viewMode == 1,
                    onEdit = {
                        editingPlannerItem = item
                        showEditPlannerDialog = true
                    },
                    onDelete = { onDeletePlannerItem(item) },
                    onChangeStatus = { newStatus -> onAdvancePlannerStatus(item, newStatus) }
                )
            }
        }
    }

    if (showEditPlannerDialog) {
        EditPlannerItemDialog(
            editingItem = editingPlannerItem,
            categories = categories,
            onSave = { title, desc, cat, date, cap, rel, notes, st, rem ->
                onSavePlannerItem(editingPlannerItem, title, desc, cat, date, cap, rel, notes, st, rem)
                showEditPlannerDialog = false
            },
            onDismiss = { showEditPlannerDialog = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlannerItemCard(
    item: PlannerItemEntity,
    showCalendarHeader: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onChangeStatus: (PlannerStatus) -> Unit
) {
    val currentStatus = remember(item.status) {
        PlannerStatus.entries.firstOrNull { it.name == item.status } ?: PlannerStatus.IDEA
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("planner_card_${item.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (showCalendarHeader) {
                Surface(
                    color = DeepViolet,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "📅 Planned Date: ${item.plannedDate.ifBlank { "Unscheduled" }}",
                        style = MaterialTheme.typography.labelMedium,
                        color = LavenderTint,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = if (currentStatus == PlannerStatus.PUBLISHED) MintEmeraldSurface else DeepViolet,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = currentStatus.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (currentStatus == PlannerStatus.PUBLISHED) MintEmerald else LavenderTint,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        color = ElevatedSlate,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = SoftLavender,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Plan",
                            tint = SoftLavender,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Plan",
                            tint = DangerRose,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (!showCalendarHeader && item.plannedDate.isNotBlank()) {
                Text(
                    text = "Target Date: ${item.plannedDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = SoftLavender
                )
            }

            if (item.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            if (item.caption.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Caption Draft: ${item.caption}",
                    style = MaterialTheme.typography.bodySmall,
                    color = LavenderTint
                )
            }

            if (item.relatedSource.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Inspired by: ${item.relatedSource}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Move Stage:",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PlannerStatus.entries.forEach { st ->
                    FilterChip(
                        selected = currentStatus == st,
                        onClick = { onChangeStatus(st) },
                        label = { Text(st.label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditPlannerItemDialog(
    editingItem: PlannerItemEntity?,
    categories: List<CategoryEntity>,
    onSave: (
        title: String,
        description: String,
        category: String,
        plannedDate: String,
        caption: String,
        relatedSource: String,
        notes: String,
        status: PlannerStatus,
        scheduleReminderToo: Boolean
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember(editingItem) { mutableStateOf(editingItem?.title ?: "") }
    var description by remember(editingItem) { mutableStateOf(editingItem?.description ?: "") }
    var category by remember(editingItem) {
        mutableStateOf(editingItem?.category ?: categories.firstOrNull()?.name ?: "Content Ideas")
    }
    var plannedDate by remember(editingItem) { mutableStateOf(editingItem?.plannedDate ?: "2026-10-30") }
    var caption by remember(editingItem) { mutableStateOf(editingItem?.caption ?: "") }
    var relatedSource by remember(editingItem) { mutableStateOf(editingItem?.relatedSource ?: "") }
    var notes by remember(editingItem) { mutableStateOf(editingItem?.notes ?: "") }
    var status by remember(editingItem) {
        mutableStateOf(
            PlannerStatus.entries.firstOrNull { it.name == editingItem?.status } ?: PlannerStatus.IDEA
        )
    }
    var scheduleReminderToo by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ElevatedSlate,
        title = { Text(if (editingItem != null) "Edit Content Plan" else "New Content Plan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Reel Title / Concept *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("planner_title_input")
                )
                OutlinedTextField(
                    value = plannedDate,
                    onValueChange = { plannedDate = it },
                    label = { Text("Planned Date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Hook") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Draft Caption & Hashtags") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = relatedSource,
                    onValueChange = { relatedSource = it },
                    label = { Text("Related Saved Reel or Trend") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Filming / Editing Notes") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Status", style = MaterialTheme.typography.labelSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PlannerStatus.entries.forEach { st ->
                        FilterChip(
                            selected = status == st,
                            onClick = { status = st },
                            label = { Text(st.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                if (editingItem == null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Also create a Smart Nag reminder to film this",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = scheduleReminderToo,
                            onCheckedChange = { scheduleReminderToo = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            title,
                            description,
                            category,
                            plannedDate,
                            caption,
                            relatedSource,
                            notes,
                            status,
                            scheduleReminderToo
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                modifier = Modifier.testTag("confirm_save_planner_button")
            ) {
                Text("Save Plan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
