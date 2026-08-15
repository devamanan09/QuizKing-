package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.data.local.UserStatsEntity
import com.example.ui.components.CyberCard
import com.example.ui.components.LeagueBadge
import com.example.ui.components.TopStatHeader
import com.example.ui.theme.*

data class LeaderboardPlayer(
    val rank: Int,
    val name: String,
    val rating: Int,
    val league: String,
    val winRate: String,
    val isUser: Boolean = false
)

@Composable
fun LeaderboardScreen(
    userStats: UserStatsEntity,
    onNavigateStore: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Global Elo, 1: Silver League, 2: Daily Challenge

    val dummyLeaderboard = listOf(
        LeaderboardPlayer(1, "ValkyriePrime", 2740, "Master", "88%"),
        LeaderboardPlayer(2, "AeroStrike", 2610, "Master", "84%"),
        LeaderboardPlayer(3, "OmniBrain", 2490, "Diamond", "81%"),
        LeaderboardPlayer(4, "CyberPulse", 2320, "Diamond", "79%"),
        LeaderboardPlayer(5, "SolarFlare", 2180, "Diamond", "76%"),
        LeaderboardPlayer(6, "QuantumAce", 1950, "Platinum", "73%"),
        LeaderboardPlayer(7, "NovaQueen", 1780, "Gold", "71%"),
        LeaderboardPlayer(8, "QuizMaster (You)", userStats.rating, userStats.league, "75%", isUser = true),
        LeaderboardPlayer(9, "HelixMind", 1230, "Silver", "65%"),
        LeaderboardPlayer(10, "VortexRider", 1190, "Bronze", "59%")
    )

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
                    text = "Competitive Rankings",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Season 4 Standings • Top performers promote to Gold League",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }
        }

        // Leaderboard Tabs (Global, League, Daily)
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LeaderboardTabButton(
                    text = "Global Tier",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                LeaderboardTabButton(
                    text = "${userStats.league} League",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f)
                )
                LeaderboardTabButton(
                    text = "Daily Gauntlet",
                    isSelected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Top 3 Podium
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                PodiumCard(player = dummyLeaderboard[1], rank = 2, color = LeagueSilver, height = 130.dp, modifier = Modifier.weight(1f))
                PodiumCard(player = dummyLeaderboard[0], rank = 1, color = AccentGold, height = 150.dp, modifier = Modifier.weight(1.1f))
                PodiumCard(player = dummyLeaderboard[2], rank = 3, color = LeagueBronze, height = 120.dp, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Full List Header
        item {
            Text(
                text = "STANDINGS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                ),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        itemsIndexed(dummyLeaderboard) { index, player ->
            LeaderboardRow(player = player, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }
    }
}

@Composable
fun LeaderboardTabButton(
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
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (isSelected) DarkBackground else TextSecondary
            )
        )
    }
}

@Composable
fun PodiumCard(
    player: LeaderboardPlayer,
    rank: Int,
    color: Color,
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    CyberCard(
        modifier = modifier.height(height),
        borderColor = color.copy(alpha = 0.6f),
        backgroundColor = DarkSurfaceElevated
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f))
                    .border(1.dp, color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rank",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = color
                    )
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = player.name,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                ),
                maxLines = 1
            )
            Text(
                text = "${player.rating} Elo",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = color,
                    fontWeight = FontWeight.Black
                )
            )
        }
    }
}

@Composable
fun LeaderboardRow(
    player: LeaderboardPlayer,
    modifier: Modifier = Modifier
) {
    CyberCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = if (player.isUser) NeonCyan else DarkBorder,
        backgroundColor = if (player.isUser) NeonCyan.copy(alpha = 0.08f) else DarkSurface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "#${player.rank}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = if (player.isUser) NeonCyan else TextMuted
                    ),
                    modifier = Modifier.width(36.dp)
                )
                Column {
                    Text(
                        text = player.name,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (player.isUser) FontWeight.Bold else FontWeight.Medium,
                            color = if (player.isUser) NeonCyan else TextPrimary
                        )
                    )
                    Text(
                        text = "${player.league} League • ${player.winRate} Win Rate",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                }
            }

            Text(
                text = "${player.rating} Elo",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = if (player.isUser) NeonCyan else TextPrimary
                )
            )
        }
    }
}
