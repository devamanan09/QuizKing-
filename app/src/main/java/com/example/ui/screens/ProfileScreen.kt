package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.local.AchievementEntity
import com.example.data.local.CosmeticEntity
import com.example.data.local.MatchHistoryEntity
import com.example.data.local.UserStatsEntity
import com.example.ui.components.CyberCard
import com.example.ui.components.GlowCyanButton
import com.example.ui.components.LeagueBadge
import com.example.ui.components.TopStatHeader
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    userStats: UserStatsEntity,
    achievements: List<AchievementEntity>,
    matchHistory: List<MatchHistoryEntity>,
    cosmetics: List<CosmeticEntity>,
    onPurchaseOrEquipCosmetic: (CosmeticEntity) -> Unit,
    onNavigateAdminPortal: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableIntStateOf(0) } // 0: Stats & History, 1: Achievements, 2: Cosmetic Store

    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            TopStatHeader(stats = userStats, onStoreClick = { selectedSection = 2 })
        }

        // Profile Identity Header Card
        item {
            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                borderColor = NeonCyan.copy(alpha = 0.5f),
                backgroundColor = DarkSurfaceElevated
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.2f))
                            .border(2.dp, NeonCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Avatar",
                            tint = NeonCyan,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = userStats.username,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                            )
                            LeagueBadge(league = userStats.league)
                        }

                        Text(
                            text = "Level ${userStats.level} Challenger • ${userStats.rating} Elo Rating",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // XP Progress
                        val xpProgress = (userStats.xp.toFloat() / userStats.xpForNextLevel).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { xpProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = NeonCyan,
                            trackColor = DarkSurfaceHighlight
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Profile Tab Buttons
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileTabPill(
                    text = "Career & History",
                    isSelected = selectedSection == 0,
                    onClick = { selectedSection = 0 },
                    modifier = Modifier.weight(1f)
                )
                ProfileTabPill(
                    text = "Achievements",
                    isSelected = selectedSection == 1,
                    onClick = { selectedSection = 1 },
                    modifier = Modifier.weight(1f)
                )
                ProfileTabPill(
                    text = "Cosmetic Store",
                    isSelected = selectedSection == 2,
                    onClick = { selectedSection = 2 },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (selectedSection == 0) {
            // Career Stats Grid
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("Total Matches", "${userStats.totalMatches}", NeonCyan, Modifier.weight(1f))
                    StatCard("Matches Won", "${userStats.wins}", SuccessGreen, Modifier.weight(1f))
                    val winPct = if (userStats.totalMatches > 0) (userStats.wins * 100 / userStats.totalMatches) else 75
                    StatCard("Win Rate", "$winPct%", AccentGold, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("Current Streak", "${userStats.currentStreak} 🔥", AccentGold, Modifier.weight(1f))
                    StatCard("Best Streak", "${userStats.bestStreak} 🔥", AccentGold, Modifier.weight(1f))
                    StatCard("Perfect Battles", "${userStats.perfectMatches} ⚡", NeonCyan, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Recent Matches Header
            item {
                Text(
                    text = "RECENT MATCH HISTORY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(matchHistory) { match ->
                CyberCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    borderColor = if (match.isWin) SuccessGreen.copy(alpha = 0.3f) else DarkBorder
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (match.isWin) SuccessGreen.copy(alpha = 0.2f) else ErrorRed.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (match.isWin) "W" else "L",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = if (match.isWin) SuccessGreen else ErrorRed
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "vs ${match.opponentName}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "${match.mode} • ${(match.accuracy * 100).toInt()}% Accuracy",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${match.playerScore} - ${match.opponentScore}",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "+${match.xpEarned} XP",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            // Admin Command Center Portal Access Card
            item {
                Spacer(modifier = Modifier.height(16.dp))
                CyberCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { onNavigateAdminPortal() },
                    borderColor = Color(0x33FFFFFF),
                    backgroundColor = DarkSurface
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonCyan.copy(alpha = 0.12f))
                                .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Portal",
                                tint = NeonCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Staff & Admin Command Center",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0x22EF4444))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "RESTRICTED",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = ErrorRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                            Text(
                                text = "Authentication required for operations & RAG pipeline control.",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        } else if (selectedSection == 1) {
            // Achievements Gallery
            items(achievements) { ach ->
                CyberCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    borderColor = if (ach.isUnlocked) AccentGold.copy(alpha = 0.5f) else DarkBorder
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (ach.isUnlocked) AccentGold.copy(alpha = 0.2f) else DarkSurfaceHighlight)
                                .border(1.dp, if (ach.isUnlocked) AccentGold else TextMuted, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (ach.isUnlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (ach.isUnlocked) AccentGold else TextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ach.title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (ach.isUnlocked) TextPrimary else TextMuted
                                )
                            )
                            Text(
                                text = ach.description,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                        }

                        Text(
                            text = "+${ach.rewardXp} XP",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (ach.isUnlocked) NeonCyan else TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        } else {
            // Cosmetic Store & Locker (Virtual Non-cash cosmetics)
            item {
                CyberCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    borderColor = NeonCyan.copy(alpha = 0.4f),
                    backgroundColor = DarkSurfaceElevated
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "VIRTUAL COSMETICS VAULT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan
                                )
                            )
                            Text(
                                text = "Personalize Avatars & Glow Frames",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = AccentGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${userStats.virtualCoins} Coins",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AccentGold
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            items(cosmetics) { item ->
                CyberCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    borderColor = if (item.isEquipped) NeonCyan else DarkBorder
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(NeonCyan.copy(alpha = 0.15f))
                                    .border(1.5.dp, if (item.isEquipped) NeonCyan else DarkBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "${item.type} • ${item.rarity}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (item.rarity == "LEGENDARY") AccentGold else TextSecondary
                                    )
                                )
                            }
                        }

                        if (item.isEquipped) {
                            Text(
                                text = "Equipped ✓",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan
                                )
                            )
                        } else if (item.isUnlocked) {
                            OutlinedButton(
                                onClick = { onPurchaseOrEquipCosmetic(item) },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, NeonCyan),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
                            ) {
                                Text("Equip", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        } else {
                            Button(
                                onClick = { onPurchaseOrEquipCosmetic(item) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentGold,
                                    contentColor = DarkBackground
                                )
                            ) {
                                Text("${item.priceCoins} 🪙", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileTabPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) NeonCyan else DarkSurfaceElevated)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = if (isSelected) DarkBackground else TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        )
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    CyberCard(
        modifier = modifier,
        borderColor = DarkBorder,
        backgroundColor = DarkSurfaceElevated
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = valueColor
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted),
                maxLines = 1
            )
        }
    }
}
