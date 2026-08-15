package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.QuestionEntity
import com.example.data.local.UserStatsEntity
import com.example.ui.components.CyberCard
import com.example.ui.components.GlowCyanButton
import com.example.ui.components.LeagueBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameState
import com.example.ui.viewmodel.QuizUiState

@Composable
fun DuelScreen(
    uiState: QuizUiState,
    userStats: UserStatsEntity,
    onSelectOption: (Int) -> Unit,
    onPlayAgain: () -> Unit,
    onExitDuel: () -> Unit,
    onPracticeWeakCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        when (uiState.gameState) {
            GameState.MATCHMAKING -> {
                MatchmakingView(uiState = uiState, userStats = userStats, onCancel = onExitDuel)
            }
            GameState.PLAYING, GameState.ROUND_FEEDBACK -> {
                LiveBattleView(
                    uiState = uiState,
                    userStats = userStats,
                    onSelectOption = onSelectOption,
                    onExitDuel = onExitDuel
                )
            }
            GameState.MATCH_FINISHED -> {
                MatchResultView(
                    uiState = uiState,
                    userStats = userStats,
                    onPlayAgain = onPlayAgain,
                    onExit = onExitDuel,
                    onPracticeWeak = { cat -> onPracticeWeakCategory(cat) }
                )
            }
            else -> {
                MatchmakingView(uiState = uiState, userStats = userStats, onCancel = onExitDuel)
            }
        }
    }
}

@Composable
fun MatchmakingView(
    uiState: QuizUiState,
    userStats: UserStatsEntity,
    onCancel: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SEARCHING ARENA",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = NeonCyan
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Matching you with a rated ${userStats.league} opponent...",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Radar Pulse Orb
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(160.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(NeonCyan.copy(alpha = 0.08f))
                    .border(1.dp, NeonCyan.copy(alpha = 0.3f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceElevated)
                    .border(2.dp, NeonCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        CyberCard(
            modifier = Modifier.fillMaxWidth(0.9f),
            borderColor = DarkBorder
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "YOUR PROFILE",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                    )
                    Text(
                        text = userStats.username,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                LeagueBadge(league = userStats.league)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = onCancel,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, DarkBorder),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
            modifier = Modifier.testTag("cancel_matchmaking")
        ) {
            Text("Cancel Search")
        }
    }
}

@Composable
fun LiveBattleView(
    uiState: QuizUiState,
    userStats: UserStatsEntity,
    onSelectOption: (Int) -> Unit,
    onExitDuel: () -> Unit
) {
    val currentQuestion = uiState.activeQuestions.getOrNull(uiState.currentQuestionIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Battle Header: Player Score vs Opponent Score
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player side
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.2f))
                        .border(1.5.dp, NeonCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Player",
                        tint = NeonCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "YOU",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = NeonCyan
                        )
                    )
                    Text(
                        text = "${uiState.playerScore} pts",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                    )
                }
            }

            // Round & Question progress pill
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Q ${uiState.currentQuestionIndex + 1}/${uiState.activeQuestions.size}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                )
                if (uiState.currentCombo > 1) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentGold.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${uiState.currentCombo}x COMBO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = AccentGold
                            )
                        )
                    }
                }
            }

            // Opponent side
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = uiState.opponent?.name ?: "Opponent",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = TextSecondary
                        )
                    )
                    Text(
                        text = "${uiState.opponentScore} pts",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = TextSecondary
                        )
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceHighlight)
                        .border(1.5.dp, TextMuted, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonOutline,
                        contentDescription = "Opponent",
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Timer Bar with animated progress
        val timerFraction = (uiState.timeRemainingSeconds.toFloat() / 10f).coerceIn(0f, 1f)
        val timerColor = when {
            uiState.timeRemainingSeconds <= 3 -> ErrorRed
            uiState.timeRemainingSeconds <= 6 -> AccentGold
            else -> NeonCyan
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "TIME REMAINING",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "${uiState.timeRemainingSeconds}s",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = timerColor,
                        fontWeight = FontWeight.Black
                    )
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { timerFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = timerColor,
                trackColor = DarkSurfaceHighlight
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (currentQuestion != null) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Question Card
                item {
                    CyberCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = DarkBorder,
                        backgroundColor = DarkSurfaceElevated
                    ) {
                        // Category & Difficulty Tags
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NeonCyan.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = currentQuestion.category.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NeonCyan
                                    )
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Diff ${currentQuestion.difficulty}/10",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextMuted,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = AccentGold,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Question Body
                        Text(
                            text = currentQuestion.question,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                lineHeight = 26.sp
                            )
                        )
                    }
                }

                // 4 Answer Options
                items(currentQuestion.options.size) { index ->
                    val optionText = currentQuestion.options[index]
                    val isSelected = uiState.selectedOptionIndex == index
                    val isCorrect = index == currentQuestion.correctIndex

                    val (bgColor, borderColor, textColor) = when {
                        !uiState.isAnswerSubmitted -> {
                            if (isSelected) {
                                Triple(NeonCyan.copy(alpha = 0.15f), NeonCyan, TextPrimary)
                            } else {
                                Triple(DarkSurface, DarkBorder, TextPrimary)
                            }
                        }
                        isCorrect -> {
                            Triple(SuccessGreen.copy(alpha = 0.2f), SuccessGreen, TextPrimary)
                        }
                        isSelected && !isCorrect -> {
                            Triple(ErrorRed.copy(alpha = 0.2f), ErrorRed, TextPrimary)
                        }
                        else -> {
                            Triple(DarkSurface.copy(alpha = 0.5f), DarkBorder, TextMuted)
                        }
                    }

                    CyberCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("option_$index"),
                        backgroundColor = bgColor,
                        borderColor = borderColor,
                        onClick = if (!uiState.isAnswerSubmitted) {
                            { onSelectOption(index) }
                        } else null
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (uiState.isAnswerSubmitted && isCorrect) SuccessGreen
                                            else if (uiState.isAnswerSubmitted && isSelected && !isCorrect) ErrorRed
                                            else DarkSurfaceHighlight
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val letter = ('A' + index).toString()
                                    Text(
                                        text = letter,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (uiState.isAnswerSubmitted && (isCorrect || isSelected)) DarkBackground else TextSecondary
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = optionText,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (isSelected || (uiState.isAnswerSubmitted && isCorrect)) FontWeight.Bold else FontWeight.Normal,
                                        color = textColor
                                    )
                                )
                            }

                            if (uiState.isAnswerSubmitted) {
                                if (isCorrect) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Correct",
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(22.dp)
                                    )
                                } else if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = "Incorrect",
                                        tint = ErrorRed,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Post-Answer Explanation Card (revealed when answered)
                if (uiState.isAnswerSubmitted) {
                    item {
                        CyberCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = NeonCyan.copy(alpha = 0.3f),
                            backgroundColor = DarkSurfaceElevated
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "RAG Verified Fact Note",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NeonCyan
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentQuestion.explanation,
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Source: ${currentQuestion.sourceReference}",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MatchResultView(
    uiState: QuizUiState,
    userStats: UserStatsEntity,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit,
    onPracticeWeak: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Victory / Defeat Header
        item {
            Spacer(modifier = Modifier.height(16.dp))
            val headerColor = if (uiState.isWin) AccentGold else ElectricBlue
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(headerColor.copy(alpha = 0.15f))
                    .border(2.dp, headerColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (uiState.isWin) Icons.Default.EmojiEvents else Icons.Default.MilitaryTech,
                    contentDescription = null,
                    tint = headerColor,
                    modifier = Modifier.size(44.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (uiState.isWin) "VICTORY!" else "MATCH COMPLETED",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = headerColor,
                    textAlign = TextAlign.Center
                )
            )
            Text(
                text = if (uiState.isWin) "You outscored your opponent in the Arena!" else "Great effort! Review areas to climb the leaderboard.",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Score Comparison Card
        item {
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = DarkBorder,
                backgroundColor = DarkSurfaceElevated
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "YOU",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        )
                        Text(
                            text = "${uiState.playerScore}",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                        )
                    }

                    Text(
                        text = "VS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.opponent?.name ?: "Opponent",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        )
                        Text(
                            text = "${uiState.opponentScore}",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = TextSecondary
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Rewards & Elo Delta Card
        item {
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonCyan.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RewardItem(
                        icon = Icons.Default.Star,
                        iconColor = NeonCyan,
                        label = "XP Gained",
                        value = "+${uiState.earnedXp}"
                    )
                    RewardItem(
                        icon = Icons.Default.TrendingUp,
                        iconColor = if (uiState.ratingDelta >= 0) SuccessGreen else ErrorRed,
                        label = "Rating",
                        value = "${if (uiState.ratingDelta >= 0) "+" else ""}${uiState.ratingDelta} Elo"
                    )
                    RewardItem(
                        icon = Icons.Default.MonetizationOn,
                        iconColor = AccentGold,
                        label = "Coins",
                        value = "+${uiState.earnedCoins}"
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // AI Coach Diagnostic Card
        if (uiState.lastAiInsight != null) {
            val insight = uiState.lastAiInsight
            item {
                CyberCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = NeonCyan.copy(alpha = 0.5f),
                    backgroundColor = DarkSurfaceElevated
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "AI Coach",
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI COACH PERFORMANCE DIAGNOSTIC",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = NeonCyan
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = insight.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = insight.summary,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Strongest Area",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                            )
                            Text(
                                text = insight.strongCategory,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Growth Focus",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                            )
                            Text(
                                text = insight.weakCategory,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AccentGold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { onPracticeWeak(insight.weakCategory) },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, NeonCyan),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Practice ${insight.weakCategory} Now", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Action Buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GlowCyanButton(
                    text = "Play Another Duel",
                    onClick = onPlayAgain,
                    icon = Icons.Default.Replay,
                    testTag = "btn_play_again"
                )

                OutlinedButton(
                    onClick = onExit,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DarkBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_return_home")
                ) {
                    Text("Return to Hub", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun RewardItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Black,
                color = iconColor
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
        )
    }
}
