package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.QuestEntity
import com.example.data.local.UserStatsEntity
import com.example.ui.components.CyberCard
import com.example.ui.components.GlowCyanButton
import com.example.ui.components.LeagueBadge
import com.example.ui.components.TopStatHeader
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    stats: UserStatsEntity,
    quests: List<QuestEntity>,
    onStartDuel: () -> Unit,
    onStartTournament: () -> Unit,
    onStartRanked: () -> Unit,
    onStartDaily: () -> Unit,
    onStartPractice: () -> Unit,
    onClaimQuest: (String, Int, Int) -> Unit,
    onNavigateStore: () -> Unit,
    onNavigateAiCoach: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Top Brand & Stats Bar
        item {
            TopStatHeader(stats = stats, onStoreClick = onNavigateStore)
        }

        // Hero Title & Subtitle matching the uploaded image design
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Choose Your Challenge",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.testTag("home_title")
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "From fast-paced duels to high-stakes tournaments, your next victory awaits.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // 3 Mode Challenge Cards (Quick Duel, Tournaments, Ranked / Predict Events)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Quick Duel Card
                ChallengeCard(
                    icon = Icons.Default.Bolt,
                    iconColor = NeonCyan,
                    title = "Quick Duel",
                    description = "Face off against a random opponent in a fast-paced 1v1 match. Quick XP, quick glory!",
                    buttonText = "Find a Match",
                    onButtonClick = onStartDuel,
                    testTag = "duel_card"
                )

                // Tournaments Card
                ChallengeCard(
                    icon = Icons.Default.EmojiEvents,
                    iconColor = NeonCyan,
                    title = "Tournaments",
                    description = "Compete in scheduled events with bigger trophy rewards. Climb the leaderboard and prove your mettle.",
                    buttonText = "Browse Tournaments",
                    onButtonClick = onStartTournament,
                    testTag = "tournament_card"
                )

                // Ranked / Competitive Mode Card
                ChallengeCard(
                    icon = Icons.Default.TrendingUp,
                    iconColor = NeonCyan,
                    title = "Ranked Match",
                    description = "Test your skills in rated Elo leagues against players worldwide. Climb to Master tier!",
                    buttonText = "Play Ranked",
                    onButtonClick = onStartRanked,
                    testTag = "ranked_card"
                )
            }
        }

        // Daily Challenge Banner
        item {
            Spacer(modifier = Modifier.height(20.dp))
            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                borderColor = AccentGold.copy(alpha = 0.5f),
                backgroundColor = DarkSurfaceElevated
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = AccentGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DAILY CHALLENGE",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AccentGold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Daily Knowledge Gauntlet",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "10 curated questions • 2x XP • +80 Coins",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary
                            )
                        )
                    }

                    Button(
                        onClick = onStartDaily,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentGold,
                            contentColor = DarkBackground
                        ),
                        modifier = Modifier.testTag("daily_challenge_button")
                    ) {
                        Text("Play", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Player League & Rating Progress Card
        item {
            Spacer(modifier = Modifier.height(16.dp))
            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                borderColor = DarkBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CURRENT LEAGUE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LeagueBadge(league = stats.league)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${stats.rating} Elo",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Win Rate",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                        )
                        val winPct = if (stats.totalMatches > 0) (stats.wins * 100 / stats.totalMatches) else 75
                        Text(
                            text = "$winPct% (${stats.wins}W/${stats.losses}L)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                // Level & XP Bar
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Level ${stats.level}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "${stats.xp} / ${stats.xpForNextLevel} XP",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    val progress = (stats.xp.toFloat() / stats.xpForNextLevel).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = NeonCyan,
                        trackColor = DarkSurfaceHighlight
                    )
                }
            }
        }

        // Daily Quests Section
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Quests",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "Resets in 18h",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(quests) { quest ->
            QuestItemRow(
                quest = quest,
                onClaim = { onClaimQuest(quest.id, quest.xpReward, quest.coinReward) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        // Quick AI Practice Arena Prompt
        item {
            Spacer(modifier = Modifier.height(16.dp))
            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                borderColor = NeonCyan.copy(alpha = 0.3f),
                onClick = onStartPractice
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NeonCyan.copy(alpha = 0.15f))
                            .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI Practice & Coaching",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Target weak topics with adaptive RAG questions",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun ChallengeCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "challenge_card"
) {
    CyberCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        borderColor = DarkBorder,
        backgroundColor = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Action Button
            GlowCyanButton(
                text = buttonText,
                onClick = onButtonClick,
                testTag = "btn_$title"
            )
        }
    }
}

@Composable
fun QuestItemRow(
    quest: QuestEntity,
    onClaim: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isComplete = quest.currentProgress >= quest.targetProgress
    CyberCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = if (isComplete && !quest.isClaimed) AccentGold.copy(alpha = 0.5f) else DarkBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quest.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = quest.description,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val progress = (quest.currentProgress.toFloat() / quest.targetProgress).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .width(100.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (isComplete) SuccessGreen else NeonCyan,
                        trackColor = DarkSurfaceHighlight
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${quest.currentProgress}/${quest.targetProgress}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "+${quest.xpReward} XP",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            if (quest.isClaimed) {
                Text(
                    text = "Claimed",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                )
            } else if (isComplete) {
                Button(
                    onClick = onClaim,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGold,
                        contentColor = DarkBackground
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Claim", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            } else {
                Text(
                    text = "+${quest.coinReward} 🪙",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = AccentGold,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
