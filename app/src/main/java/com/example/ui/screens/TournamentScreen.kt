package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserStatsEntity
import com.example.ui.components.CyberCard
import com.example.ui.components.GlowCyanButton
import com.example.ui.components.TopStatHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.QuizUiState
import com.example.ui.viewmodel.TournamentMatch

@Composable
fun TournamentScreen(
    uiState: QuizUiState,
    userStats: UserStatsEntity,
    onEnterTournamentMatch: () -> Unit,
    onBack: () -> Unit,
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Apex Championship",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                    )
                }
                Text(
                    text = "8-Player Single Elimination • Silver Tier Bracket",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                    modifier = Modifier.padding(start = 48.dp)
                )
            }
        }

        // Tournament Prize Pool Banner (XP, League Points, Badges)
        item {
            Spacer(modifier = Modifier.height(8.dp))
            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                borderColor = AccentGold.copy(alpha = 0.6f),
                backgroundColor = DarkSurfaceElevated
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(AccentGold.copy(alpha = 0.2f))
                                .border(1.dp, AccentGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = AccentGold,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Grand Champion Reward",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AccentGold
                                )
                            )
                            Text(
                                text = "+500 XP • +250 Coins • Apex Trophy",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Tournament Action Button
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                GlowCyanButton(
                    text = "Enter Quarter-Final Match",
                    onClick = onEnterTournamentMatch,
                    icon = Icons.Default.PlayArrow
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Bracket Matches Header
        item {
            Text(
                text = "Tournament Bracket Matches",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(uiState.tournamentBracket) { match ->
            TournamentMatchItem(match = match, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
        }
    }
}

@Composable
fun TournamentMatchItem(
    match: TournamentMatch,
    modifier: Modifier = Modifier
) {
    CyberCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = if (match.player1.contains("You") || match.player2.contains("You")) NeonCyan.copy(alpha = 0.5f) else DarkBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = match.roundName.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            )
            if (match.winner != null) {
                Text(
                    text = "Winner: ${match.winner}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                )
            } else {
                Text(
                    text = "Ready to Play",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AccentGold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = match.player1,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (match.player1.contains("You")) FontWeight.Bold else FontWeight.Normal,
                        color = if (match.player1.contains("You")) NeonCyan else TextPrimary
                    )
                )
                Text(
                    text = match.player2,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (match.player2.contains("You")) FontWeight.Bold else FontWeight.Normal,
                        color = if (match.player2.contains("You")) NeonCyan else TextSecondary
                    )
                )
            }

            if (match.winner != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${match.score1} pts",
                        style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
                    )
                    Text(
                        text = "${match.score2} pts",
                        style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
                    )
                }
            } else {
                Text(
                    text = "VS",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                )
            }
        }
    }
}
