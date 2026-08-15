package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserStatsEntity
import com.example.ui.theme.*

/**
 * Frosted Glass Ambient Backdrop Modifier
 * Draws subtle glowing aurora mesh gradients behind the dark canvas to give the frosted glass panels authentic depth & refraction.
 */
fun Modifier.frostedGlassCanvas(): Modifier = this.then(
    Modifier
        .background(GlassCanvasBg)
        .drawBehind {
            // Ambient Ice Cyan top-left orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NeonCyan.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(size.width * 0.15f, size.height * 0.12f),
                    radius = size.width * 0.7f
                )
            )
            // Ambient Violet/Indigo center-right orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(FrostViolet.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.45f),
                    radius = size.width * 0.8f
                )
            )
            // Ambient Blue bottom-left orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ElectricBlue.copy(alpha = 0.10f), Color.Transparent),
                    center = Offset(size.width * 0.25f, size.height * 0.85f),
                    radius = size.width * 0.75f
                )
            )
        }
)

@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    borderColor: Color = DarkBorder,
    backgroundColor: Color = DarkSurface,
    cornerRadius: Dp = 18.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(color = NeonCyan),
            onClick = onClick
        )
    } else {
        modifier
    }

    val shape = RoundedCornerShape(cornerRadius)

    // Frosted glass border gradient: bright specular refraction on top, subtle at bottom
    val borderBrush = if (borderColor == DarkBorder) {
        Brush.verticalGradient(
            listOf(
                Color(0x55FFFFFF),
                Color(0x1FFFFFFF),
                Color(0x0FFFFFFF)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                borderColor.copy(alpha = (borderColor.alpha * 1.3f).coerceAtMost(1f)),
                borderColor.copy(alpha = (borderColor.alpha * 0.6f).coerceAtMost(1f))
            )
        )
    }

    Box(
        modifier = clickableModifier
            .clip(shape)
            .background(GlassSurfaceSolid) // Dark base
            .background(
                // Frosted glass translucent sheen gradient
                Brush.verticalGradient(
                    listOf(
                        Color(0x28FFFFFF),
                        Color(0x12FFFFFF),
                        Color(0x06FFFFFF)
                    )
                )
            )
            .border(
                BorderStroke(1.dp, borderBrush),
                shape
            )
    ) {
        // Subtle top specular highlight bar inside card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color(0x40FFFFFF),
                            Color(0x60FFFFFF),
                            Color(0x40FFFFFF),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun GlowCyanButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    testTag: String = "glow_button"
) {
    val shape = RoundedCornerShape(14.dp)
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = NeonCyanGlow,
                spotColor = NeonCyan
            )
            .border(
                BorderStroke(1.dp, Color(0x66FFFFFF)),
                shape
            )
            .testTag(testTag),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = NeonCyan,
            contentColor = GlassCanvasBg,
            disabledContainerColor = GlassSurfaceSolid,
            disabledContentColor = TextMuted
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = GlassCanvasBg
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = GlassCanvasBg
                )
            )
        }
    }
}

@Composable
fun OutlineCyanButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    testTag: String = "outline_button"
) {
    val shape = RoundedCornerShape(14.dp)
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(NeonCyan.copy(alpha = 0.10f), shape)
            .testTag(testTag),
        shape = shape,
        border = BorderStroke(1.2.dp, NeonCyan.copy(alpha = 0.8f)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = NeonCyan
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = NeonCyan
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            )
        }
    }
}

@Composable
fun LeagueBadge(
    league: String,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp
) {
    val (color, name) = when (league.lowercase()) {
        "master" -> Pair(LeagueMaster, "Master")
        "diamond" -> Pair(LeagueDiamond, "Diamond")
        "platinum" -> Pair(LeaguePlatinum, "Platinum")
        "gold" -> Pair(LeagueGold, "Gold")
        "silver" -> Pair(LeagueSilver, "Silver")
        else -> Pair(LeagueBronze, "Bronze")
    }

    val shape = RoundedCornerShape(20.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(shape)
            .background(GlassSurfaceSolid)
            .background(color.copy(alpha = 0.18f))
            .border(
                BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(
                            color.copy(alpha = 0.8f),
                            color.copy(alpha = 0.3f)
                        )
                    )
                ),
                shape
            )
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Icon(
            imageVector = Icons.Default.MilitaryTech,
            contentDescription = "League",
            tint = color,
            modifier = Modifier.size(size)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
    }
}

@Composable
fun TopStatHeader(
    stats: UserStatsEntity,
    onStoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Title & Logo with Frosted Glass capsule
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable { /* Brand tap */ }
                .padding(vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(NeonCyan.copy(alpha = 0.18f))
                    .border(
                        BorderStroke(
                            1.dp,
                            Brush.verticalGradient(
                                listOf(Color(0x80FFFFFF), NeonCyan.copy(alpha = 0.4f))
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "QuizKing Trophy",
                    tint = NeonCyan,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "QuizKing",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    color = TextPrimary
                )
            )
        }

        // Stats items: Streaks, Coins, Level/League with Frosted Glass pills
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val pillShape = RoundedCornerShape(14.dp)

            // Streak Flame Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(pillShape)
                    .background(GlassSurfaceSolid)
                    .background(AccentGold.copy(alpha = 0.14f))
                    .border(
                        BorderStroke(
                            1.dp,
                            Brush.verticalGradient(
                                listOf(Color(0x55FFFFFF), AccentGold.copy(alpha = 0.4f))
                            )
                        ),
                        pillShape
                    )
                    .padding(horizontal = 9.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = AccentGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${stats.currentStreak}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AccentGold
                    )
                )
            }

            // Virtual Progression Coins Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(pillShape)
                    .background(GlassSurfaceSolid)
                    .background(NeonCyan.copy(alpha = 0.14f))
                    .border(
                        BorderStroke(
                            1.dp,
                            Brush.verticalGradient(
                                listOf(Color(0x55FFFFFF), NeonCyan.copy(alpha = 0.4f))
                            )
                        ),
                        pillShape
                    )
                    .clickable { onStoreClick() }
                    .padding(horizontal = 9.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = "Virtual Coins",
                    tint = NeonCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${stats.virtualCoins}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                )
            }

            // Level Badge Pill
            Box(
                modifier = Modifier
                    .clip(pillShape)
                    .background(GlassSurfaceSolid)
                    .background(Color(0x18FFFFFF))
                    .border(
                        BorderStroke(
                            1.dp,
                            Brush.verticalGradient(
                                listOf(Color(0x44FFFFFF), Color(0x10FFFFFF))
                            )
                        ),
                        pillShape
                    )
                    .padding(horizontal = 9.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Lv.${stats.level}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }
        }
    }
}
