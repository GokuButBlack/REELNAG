package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.DefaultData
import com.example.data.NagPersonality
import com.example.data.UserRole
import com.example.data.UserSettings
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.LavenderTint
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    initialSettings: UserSettings,
    onComplete: (
        interests: List<String>,
        role: UserRole,
        personality: NagPersonality,
        notificationsEnabled: Boolean,
        preferredTime: String,
        quietHoursEnabled: Boolean,
        quietStart: String,
        quietEnd: String
    ) -> Unit,
    onSkip: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    val selectedInterests = remember {
        mutableStateListOf<String>().apply { addAll(initialSettings.interests) }
    }
    var selectedRole by remember { mutableStateOf(initialSettings.role) }
    var selectedPersonality by remember { mutableStateOf(initialSettings.personality) }
    var notificationsEnabled by remember { mutableStateOf(initialSettings.notificationsEnabled) }
    var preferredTime by remember { mutableStateOf(initialSettings.preferredReminderTime) }
    var quietHoursEnabled by remember { mutableStateOf(initialSettings.quietHoursEnabled) }
    var quietStart by remember { mutableStateOf(initialSettings.quietHoursStart) }
    var quietEnd by remember { mutableStateOf(initialSettings.quietHoursEnd) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepViolet.copy(alpha = 0.45f), CharcoalBackground, CharcoalBackground)
                )
            )
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 560.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Top Bar with Step Indicator and Skip Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = ElectricPurple.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, ElectricPurple)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "ReelNag Logo",
                                tint = SoftLavender,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "ReelNag Setup",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Step ${step + 1} of 3 • Editable anytime",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }

                    TextButton(
                        onClick = onSkip,
                        modifier = Modifier.testTag("onboarding_skip_button")
                    ) {
                        Text(
                            text = "Skip for now",
                            color = SoftLavender,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress bar pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) { idx ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (idx <= step) ElectricPurple else CardBorder
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedContent(targetState = step, label = "onboarding_step") { currentStep ->
                    when (currentStep) {
                        0 -> {
                            Column {
                                Text(
                                    text = "Save it. Watch it. Create it.\nDon't get nagged about it.",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = LavenderTint
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Pick the Reel topics you save most and tell us how you use Reels.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Text(
                                    text = "1. What kind of Reels do you hoard?",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    DefaultData.ALL_INTERESTS.forEach { interest ->
                                        val isSelected = interest in selectedInterests
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                if (isSelected) selectedInterests.remove(interest)
                                                else selectedInterests.add(interest)
                                            },
                                            label = { Text(interest) },
                                            leadingIcon = if (isSelected) {
                                                {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            } else null,
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = ElectricPurple.copy(alpha = 0.25f),
                                                selectedLabelColor = LavenderTint
                                            ),
                                            modifier = Modifier.testTag("interest_chip_${interest.lowercase().replace(" ", "_")}")
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = "2. What's your role?",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                UserRole.entries.forEach { role ->
                                    val isSelected = selectedRole == role
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 5.dp)
                                            .clickable { selectedRole = role }
                                            .testTag("role_card_${role.name.lowercase()}"),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) DeepViolet.copy(alpha = 0.55f) else CardSurface
                                        ),
                                        border = BorderStroke(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) ElectricPurple else CardBorder
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = role.displayName,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = if (isSelected) LavenderTint else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = role.subtitle,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextSecondary
                                                )
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = SoftLavender
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            Column {
                                Text(
                                    text = "Pick Your Nag Personality",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = LavenderTint
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "How should ReelNag talk to you when a saved Reel has been sitting unwatched for 3 days?",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                NagPersonality.entries.forEach { p ->
                                    val isSelected = selectedPersonality == p
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 5.dp)
                                            .clickable { selectedPersonality = p }
                                            .testTag("personality_card_${p.name.lowercase()}"),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) DeepViolet.copy(alpha = 0.6f) else CardSurface
                                        ),
                                        border = BorderStroke(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) ElectricPurple else CardBorder
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = p.badgeEmoji,
                                                        style = MaterialTheme.typography.titleLarge
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(
                                                            text = p.displayName,
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Text(
                                                            text = p.tagline,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = SoftLavender
                                                        )
                                                    }
                                                }
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Selected",
                                                        tint = SoftLavender
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Surface(
                                                color = CharcoalBackground.copy(alpha = 0.65f),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text(
                                                    text = "\"${p.sampleLine}\"",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextSecondary,
                                                    modifier = Modifier.padding(10.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        else -> {
                            Column {
                                Text(
                                    text = "Reminder Timing & Quiet Hours",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = LavenderTint
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "We never bug you while you sleep. Set your preferred reminder window and quiet hours.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )

                                Spacer(modifier = Modifier.height(20.dp))

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
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.NotificationsActive,
                                                    contentDescription = null,
                                                    tint = SoftLavender
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = "Enable Smart Nag Alerts",
                                                        style = MaterialTheme.typography.titleMedium
                                                    )
                                                    Text(
                                                        text = "In-app due banners + optional system notifications",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = TextSecondary
                                                    )
                                                }
                                            }
                                            Switch(
                                                checked = notificationsEnabled,
                                                onCheckedChange = { notificationsEnabled = it }
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        OutlinedTextField(
                                            value = preferredTime,
                                            onValueChange = { preferredTime = it },
                                            label = { Text("Preferred Daily Nag Time (24h HH:MM)") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

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
                                                    text = "Respect Quiet Hours",
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                                Text(
                                                    text = "Silence system notifications overnight; keep them quietly in your Due Now list",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextSecondary
                                                )
                                            }
                                            Switch(
                                                checked = quietHoursEnabled,
                                                onCheckedChange = { quietHoursEnabled = it }
                                            )
                                        }

                                        if (quietHoursEnabled) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                OutlinedTextField(
                                                    value = quietStart,
                                                    onValueChange = { quietStart = it },
                                                    label = { Text("Quiet Start (HH:MM)") },
                                                    singleLine = true,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                OutlinedTextField(
                                                    value = quietEnd,
                                                    onValueChange = { quietEnd = it },
                                                    label = { Text("Quiet End (HH:MM)") },
                                                    singleLine = true,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (step > 0) {
                    OutlinedButton(
                        onClick = { step-- },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Back")
                    }
                }

                Button(
                    onClick = {
                        if (step < 2) {
                            step++
                        } else {
                            onComplete(
                                selectedInterests.toList(),
                                selectedRole,
                                selectedPersonality,
                                notificationsEnabled,
                                preferredTime,
                                quietHoursEnabled,
                                quietStart,
                                quietEnd
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(52.dp)
                        .testTag("onboarding_next_or_finish_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)
                ) {
                    Text(
                        text = if (step < 2) "Continue" else "Launch ReelNag",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (step < 2) Icons.AutoMirrored.Filled.ArrowForward else Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
