package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserStatsEntity
import com.example.ui.components.CyberCard
import com.example.ui.components.GlowCyanButton
import com.example.ui.components.TopStatHeader
import com.example.ui.theme.*

@Composable
fun PracticeScreen(
    userStats: UserStatsEntity,
    onStartPractice: (String, Int, Int) -> Unit,
    onNavigateStore: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedDifficultyTier by remember { mutableIntStateOf(1) } // 1: Beginner (1-3), 2: Intermediate (4-7), 3: Master (8-10)

    val categories = listOf("All", "Science", "Technology", "Space", "History", "Geography", "Nature", "Mathematics", "Literature", "Sports")

    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            TopStatHeader(stats = userStats, onStoreClick = onNavigateStore)
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "AI Practice Arena",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Hone your precision with adaptive RAG questions & post-quiz AI coaching feedback.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }
        }

        // Category Selection Chips
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "SELECT TOPIC DOMAIN",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                ),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else DarkSurfaceElevated)
                            .border(
                                1.dp,
                                if (isSelected) NeonCyan else DarkBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) NeonCyan else TextSecondary
                            )
                        )
                    }
                }
            }
        }

        // Difficulty Level Selector
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "TARGET DIFFICULTY",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                ),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DifficultyOptionCard(
                    title = "Apprentice (Diff 1-3)",
                    desc = "Fundamental concepts and high-speed recognition.",
                    isSelected = selectedDifficultyTier == 1,
                    onClick = { selectedDifficultyTier = 1 }
                )
                DifficultyOptionCard(
                    title = "Combatant (Diff 4-7)",
                    desc = "Standard competitive ranked match calibration.",
                    isSelected = selectedDifficultyTier == 2,
                    onClick = { selectedDifficultyTier = 2 }
                )
                DifficultyOptionCard(
                    title = "Grandmaster (Diff 8-10)",
                    desc = "Complex domain depth, edge cases, and high intellect challenges.",
                    isSelected = selectedDifficultyTier == 3,
                    onClick = { selectedDifficultyTier = 3 }
                )
            }
        }

        // Launch Action
        item {
            Spacer(modifier = Modifier.height(32.dp))
            val (minDiff, maxDiff) = when (selectedDifficultyTier) {
                1 -> Pair(1, 3)
                2 -> Pair(4, 7)
                else -> Pair(8, 10)
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                GlowCyanButton(
                    text = "Start $selectedCategory Practice Drill",
                    onClick = { onStartPractice(selectedCategory, minDiff, maxDiff) },
                    icon = Icons.Default.PlayArrow
                )
            }
        }
    }
}

@Composable
fun DifficultyOptionCard(
    title: String,
    desc: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    CyberCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (isSelected) NeonCyan else DarkBorder,
        backgroundColor = if (isSelected) NeonCyan.copy(alpha = 0.08f) else DarkSurface,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) NeonCyan else TextPrimary
                    )
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = NeonCyan,
                    unselectedColor = TextMuted
                )
            )
        }
    }
}
