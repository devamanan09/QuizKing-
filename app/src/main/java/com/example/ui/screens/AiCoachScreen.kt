package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserStatsEntity
import com.example.ui.components.CyberCard
import com.example.ui.components.TopStatHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.QuizUiState

@Composable
fun AiCoachScreen(
    uiState: QuizUiState,
    userStats: UserStatsEntity,
    onNavigatePractice: (String) -> Unit,
    onNavigateStore: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                    text = "AI Tactical Coach",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Personalized gameplay insights, response speed analytics, and category precision training.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }
        }

        // Tactical Rating Card
        item {
            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                borderColor = NeonCyan.copy(alpha = 0.5f),
                backgroundColor = DarkSurfaceElevated
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.15f))
                            .border(1.5.dp, NeonCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "AI Coach",
                            tint = NeonCyan,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "PLAYER TACTICAL RATING",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        )
                        Text(
                            text = "A-Tier Precision (78% Overall)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Top 12% in Silver League",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Category Accuracy & Speed",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                CategoryAccuracyBar("Science & Physics", 0.92f, "1.4s avg", SuccessGreen)
                CategoryAccuracyBar("Technology & AI", 0.88f, "1.8s avg", SuccessGreen)
                CategoryAccuracyBar("Space & Astronomy", 0.85f, "2.1s avg", NeonCyan)
                CategoryAccuracyBar("World History", 0.70f, "3.4s avg", AccentGold)
                CategoryAccuracyBar("Geography & Capitals", 0.60f, "4.2s avg", AccentGold)
                CategoryAccuracyBar("Literature & Arts", 0.55f, "5.1s avg", ErrorRed)

                Spacer(modifier = Modifier.height(16.dp))

                CyberCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = DarkBorder,
                    backgroundColor = DarkSurface
                ) {
                    Text(
                        text = "💡 AI Tactical Assessment",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your response times in STEM are in the top 5% of players. Focus your sparring drills on Literature and Geography to increase your win rate against Gold league contenders.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }
        }

        // Recommended Training Drills
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "RECOMMENDED DRILLS FOR YOU",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                DrillCard(
                    title = "Literature & Humanities Sparring",
                    category = "Literature",
                    description = "Target your lowest accuracy category with fast-paced 5-question drills.",
                    icon = Icons.Default.MenuBook,
                    accentColor = ErrorRed,
                    onClick = { onNavigatePractice("Literature") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                DrillCard(
                    title = "World Geography & Capitals Blitz",
                    category = "Geography",
                    description = "Boost recall speed for geographical boundaries and capitals.",
                    icon = Icons.Default.Public,
                    accentColor = AccentGold,
                    onClick = { onNavigatePractice("Geography") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                DrillCard(
                    title = "Speed Mastery: Science & Space",
                    category = "Science",
                    description = "Sharpen your highest tier skills to achieve streak combo milestones.",
                    icon = Icons.Default.RocketLaunch,
                    accentColor = NeonCyan,
                    onClick = { onNavigatePractice("Science") }
                )
            }
        }
    }
}

@Composable
private fun DrillCard(
    title: String,
    category: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    CyberCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        borderColor = accentColor.copy(alpha = 0.3f),
        backgroundColor = DarkSurfaceElevated
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp),
                    maxLines = 2
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Start Drill",
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun CategoryAccuracyBar(
    category: String,
    accuracy: Float,
    speedNote: String,
    barColor: Color
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = speedNote,
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                )
                Text(
                    text = "${(accuracy * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = barColor
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { accuracy },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = DarkSurfaceHighlight
        )
    }
}
