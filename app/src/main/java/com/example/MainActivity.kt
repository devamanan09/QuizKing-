package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.admin.AdminAuthManager
import com.example.ui.components.frostedGlassCanvas
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.QuizUiState
import com.example.ui.viewmodel.QuizViewModel

class MainActivity : ComponentActivity() {

    private val quizViewModel: QuizViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                QuizKingApp(viewModel = quizViewModel)
            }
        }
    }
}

@Composable
fun QuizKingApp(viewModel: QuizViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userStats by viewModel.userStats.collectAsStateWithLifecycle()
    val quests by viewModel.quests.collectAsStateWithLifecycle()
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()
    val matchHistory by viewModel.matchHistory.collectAsStateWithLifecycle()
    val cosmetics by viewModel.cosmetics.collectAsStateWithLifecycle()
    val totalApprovedQuestions by viewModel.totalApprovedQuestions.collectAsStateWithLifecycle()
    val allQuestions by viewModel.allQuestions.collectAsStateWithLifecycle()
    val currentAdmin by AdminAuthManager.currentAdmin.collectAsStateWithLifecycle()

    val isStandaloneScreen = uiState.currentScreen in listOf("duel", "admin_login", "admin_dashboard")

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .frostedGlassCanvas(),
        bottomBar = {
            // Show bottom navigation on primary hub tabs only
            if (!isStandaloneScreen) {
                QuizKingBottomNav(
                    currentScreen = uiState.currentScreen,
                    onNavigate = { screen -> viewModel.navigateTo(screen) }
                )
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = uiState.currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    "home" -> {
                        HomeScreen(
                            stats = userStats,
                            quests = quests,
                            onStartDuel = { viewModel.startMatch(mode = "Quick Duel", category = "All") },
                            onStartTournament = {
                                viewModel.initTournament()
                                viewModel.navigateTo("tournament")
                            },
                            onStartRanked = { viewModel.startMatch(mode = "Ranked Match", category = "All") },
                            onStartDaily = { viewModel.startDailyChallenge() },
                            onStartPractice = { viewModel.navigateTo("practice") },
                            onClaimQuest = { id, xp, coins -> viewModel.claimQuest(id, xp, coins) },
                            onNavigateStore = { viewModel.navigateTo("profile") },
                            onNavigateAiCoach = { viewModel.navigateTo("aicoach") }
                        )
                    }
                    "duel" -> {
                        DuelScreen(
                            uiState = uiState,
                            userStats = userStats,
                            onSelectOption = { optIndex -> viewModel.submitAnswer(optIndex) },
                            onPlayAgain = { viewModel.startMatch(mode = uiState.matchMode, category = uiState.activeCategory) },
                            onExitDuel = { viewModel.navigateTo("home") },
                            onPracticeWeakCategory = { weakCat ->
                                viewModel.startPractice(category = weakCat, minDiff = 3, maxDiff = 8)
                            }
                        )
                    }
                    "tournament" -> {
                        TournamentScreen(
                            uiState = uiState,
                            userStats = userStats,
                            onEnterTournamentMatch = { viewModel.playTournamentMatch() },
                            onBack = { viewModel.navigateTo("home") },
                            onNavigateStore = { viewModel.navigateTo("profile") }
                        )
                    }
                    "practice" -> {
                        PracticeScreen(
                            userStats = userStats,
                            onStartPractice = { category, minDiff, maxDiff ->
                                viewModel.startPractice(category, minDiff, maxDiff)
                            },
                            onNavigateStore = { viewModel.navigateTo("profile") }
                        )
                    }
                    "leaderboard" -> {
                        LeaderboardScreen(
                            userStats = userStats,
                            onNavigateStore = { viewModel.navigateTo("profile") }
                        )
                    }
                    "aicoach" -> {
                        AiCoachScreen(
                            uiState = uiState,
                            userStats = userStats,
                            onNavigatePractice = { weakCat ->
                                viewModel.startPractice(category = weakCat, minDiff = 3, maxDiff = 8)
                            },
                            onNavigateStore = { viewModel.navigateTo("profile") }
                        )
                    }
                    "profile" -> {
                        ProfileScreen(
                            userStats = userStats,
                            achievements = achievements,
                            matchHistory = matchHistory,
                            cosmetics = cosmetics,
                            onPurchaseOrEquipCosmetic = { cosmetic ->
                                viewModel.purchaseOrEquipCosmetic(cosmetic)
                            },
                            onNavigateAdminPortal = { viewModel.navigateTo("admin_login") }
                        )
                    }
                    "admin_login" -> {
                        AdminLoginScreen(
                            onLoginSuccess = { viewModel.navigateTo("admin_dashboard") },
                            onBackToPlayerApp = { viewModel.navigateTo("home") }
                        )
                    }
                    "admin_dashboard" -> {
                        val admin = currentAdmin
                        if (admin != null && admin.isActive) {
                            AdminDashboardScreen(
                                adminUser = admin,
                                allQuestions = allQuestions,
                                knowledgeDocs = viewModel.ragKnowledgeDocs,
                                isGeneratingRag = uiState.isGeneratingRag,
                                onTriggerRagGeneration = { viewModel.triggerAutomatedRagGeneration() },
                                onLogout = {
                                    AdminAuthManager.logoutAdmin()
                                    viewModel.navigateTo("admin_login")
                                },
                                onBackToPlayer = { viewModel.navigateTo("home") }
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                viewModel.navigateTo("admin_login")
                            }
                            AdminLoginScreen(
                                onLoginSuccess = { viewModel.navigateTo("admin_dashboard") },
                                onBackToPlayerApp = { viewModel.navigateTo("home") }
                            )
                        }
                    }
                    else -> {
                        HomeScreen(
                            stats = userStats,
                            quests = quests,
                            onStartDuel = { viewModel.startMatch(mode = "Quick Duel") },
                            onStartTournament = { viewModel.initTournament() },
                            onStartRanked = { viewModel.startMatch(mode = "Ranked Match") },
                            onStartDaily = { viewModel.startDailyChallenge() },
                            onStartPractice = { viewModel.navigateTo("practice") },
                            onClaimQuest = { id, xp, coins -> viewModel.claimQuest(id, xp, coins) },
                            onNavigateStore = { viewModel.navigateTo("profile") },
                            onNavigateAiCoach = { viewModel.navigateTo("aicoach") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuizKingBottomNav(
    currentScreen: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .background(GlassSurfaceSolid)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0x30FFFFFF),
                        Color(0x0CFFFFFF)
                    )
                )
            )
            .border(
                androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(
                            Color(0x44FFFFFF),
                            Color(0x10FFFFFF)
                        )
                    )
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = currentScreen == "home",
                onClick = { onNavigate("home") },
                testTag = "nav_home"
            )
            NavItem(
                icon = Icons.Default.Bolt,
                label = "Arena",
                isSelected = currentScreen == "duel" || currentScreen == "practice",
                onClick = { onNavigate("practice") },
                testTag = "nav_arena"
            )
            NavItem(
                icon = Icons.Default.EmojiEvents,
                label = "Rankings",
                isSelected = currentScreen == "leaderboard" || currentScreen == "tournament",
                onClick = { onNavigate("leaderboard") },
                testTag = "nav_rankings"
            )
            NavItem(
                icon = Icons.Default.Psychology,
                label = "AI Coach",
                isSelected = currentScreen == "aicoach",
                onClick = { onNavigate("aicoach") },
                testTag = "nav_aicoach"
            )
            NavItem(
                icon = Icons.Default.Person,
                label = "Profile",
                isSelected = currentScreen == "profile",
                onClick = { onNavigate("profile") },
                testTag = "nav_profile"
            )
        }
    }
}

@Composable
fun NavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String = "nav_item"
) {
    val pillShape = RoundedCornerShape(14.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(pillShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(if (isSelected) NeonCyan.copy(alpha = 0.22f) else Color.Transparent)
                .border(
                    if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f))
                    else androidx.compose.foundation.BorderStroke(0.dp, Color.Transparent),
                    CircleShape
                )
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) NeonCyan else TextMuted,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) NeonCyan else TextMuted,
                fontSize = 11.sp
            )
        )
    }
}
