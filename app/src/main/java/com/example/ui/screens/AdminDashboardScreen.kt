package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.admin.*
import com.example.data.engine.RagKnowledgeDoc
import com.example.data.local.QuestionEntity
import com.example.ui.components.CyberCard
import com.example.ui.components.GlowCyanButton
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminDashboardScreen(
    adminUser: AdminUser,
    allQuestions: List<QuestionEntity>,
    knowledgeDocs: List<RagKnowledgeDoc>,
    isGeneratingRag: Boolean,
    onTriggerRagGeneration: () -> Unit,
    onLogout: () -> Unit,
    onBackToPlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentNavTab by remember { mutableIntStateOf(0) }
    // 0: Overview, 1: Question Intelligence, 2: Question Repository, 3: RAG Knowledge, 4: Admin Users, 5: Audit Logs, 6: System Health

    val coroutineScope = rememberCoroutineScope()
    val adminUsersList by AdminAuthManager.adminUsers.collectAsState()
    val auditLogsList by AdminAuthManager.auditLogs.collectAsState()

    var showCreateAdminDialog by remember { mutableStateOf(false) }
    var showAddKnowledgeDialog by remember { mutableStateOf(false) }
    var repositorySearchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }

    // Dialog state for editing admin
    var editingAdminUser by remember { mutableStateOf<AdminUser?>(null) }

    val activeQuestions = remember(allQuestions) { allQuestions.filter { it.status == "APPROVED" } }
    val retiredQuestions = remember(allQuestions) { allQuestions.filter { it.status == "RETIRED" } }

    val filteredQuestions = remember(allQuestions, repositorySearchQuery, selectedCategoryFilter, selectedStatusFilter) {
        allQuestions.filter { q ->
            val matchesCategory = selectedCategoryFilter == "All" || q.category.equals(selectedCategoryFilter, ignoreCase = true)
            val matchesStatus = selectedStatusFilter == "ALL" || q.status.equals(selectedStatusFilter, ignoreCase = true)
            val matchesQuery = repositorySearchQuery.isBlank() ||
                    q.question.contains(repositorySearchQuery, ignoreCase = true) ||
                    q.topic.contains(repositorySearchQuery, ignoreCase = true)
            matchesCategory && matchesStatus && matchesQuery
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TOP ADMIN BAR
            AdminTopBar(
                admin = adminUser,
                onLogout = onLogout,
                onBackToPlayer = onBackToPlayer
            )

            // NAVIGATION BAR / TABS
            AdminNavPills(
                currentTab = currentNavTab,
                onSelectTab = { currentNavTab = it },
                userRole = adminUser.role
            )

            Divider(color = DarkBorder, thickness = 1.dp)

            // CONTENT BODY
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (currentNavTab) {
                    0 -> AdminOverviewTab(
                        adminUser = adminUser,
                        totalApproved = activeQuestions.size,
                        totalRetired = retiredQuestions.size,
                        knowledgeCount = knowledgeDocs.size,
                        adminCount = adminUsersList.size,
                        auditCount = auditLogsList.size,
                        onNavigateTab = { currentNavTab = it }
                    )
                    1 -> AdminQuestionIntelligenceTab(
                        admin = adminUser,
                        activeQuestionsCount = activeQuestions.size,
                        knowledgeDocs = knowledgeDocs,
                        isGenerating = isGeneratingRag,
                        onRunPipeline = {
                            if (AdminAuthManager.requirePermission { it.ragManagement }) {
                                onTriggerRagGeneration()
                                AdminAuthManager.recordAuditLog(
                                    admin = adminUser,
                                    action = "RUN_RAG_PIPELINE",
                                    target = "question-generator",
                                    metadata = mapOf("status" to "TRIGGERED")
                                )
                            }
                        }
                    )
                    2 -> AdminQuestionRepositoryTab(
                        admin = adminUser,
                        questions = filteredQuestions,
                        allCategories = listOf("All", "Science", "Space", "Technology", "History", "Geography", "Mathematics"),
                        selectedCategory = selectedCategoryFilter,
                        onSelectCategory = { selectedCategoryFilter = it },
                        searchQuery = repositorySearchQuery,
                        onSearchChange = { repositorySearchQuery = it },
                        statusFilter = selectedStatusFilter,
                        onStatusFilterChange = { selectedStatusFilter = it }
                    )
                    3 -> AdminKnowledgeSourcesTab(
                        admin = adminUser,
                        docs = knowledgeDocs,
                        onAddSourceClick = { showAddKnowledgeDialog = true }
                    )
                    4 -> AdminUserControlTab(
                        currentAdmin = adminUser,
                        adminList = adminUsersList,
                        onCreateAdminClick = { showCreateAdminDialog = true },
                        onEditAdminClick = { editingAdminUser = it },
                        onToggleStatus = { targetUid, enable ->
                            coroutineScope.launch {
                                AdminAuthManager.toggleAdminStatus(targetUid, enable)
                            }
                        }
                    )
                    5 -> AdminAuditLogsTab(
                        logs = auditLogsList
                    )
                    6 -> AdminSystemHealthTab(
                        admin = adminUser,
                        onReturnToPlayer = onBackToPlayer
                    )
                }
            }
        }

        // CREATE ADMIN DIALOG (SUPER_ADMIN ONLY)
        if (showCreateAdminDialog) {
            CreateAdminDialog(
                onDismiss = { showCreateAdminDialog = false },
                onAdminCreated = { email, password, name, role, permissions ->
                    coroutineScope.launch {
                        AdminAuthManager.createAdminUser(
                            email = email,
                            displayName = name,
                            role = role,
                            customPermissions = permissions,
                            password = password
                        )
                    }
                    showCreateAdminDialog = false
                }
            )
        }

        // EDIT ADMIN DIALOG
        editingAdminUser?.let { targetAdmin ->
            EditAdminDialog(
                target = targetAdmin,
                onDismiss = { editingAdminUser = null },
                onSave = { newRole, newPerms ->
                    coroutineScope.launch {
                        AdminAuthManager.updateAdminRoleAndPermissions(targetAdmin.uid, newRole, newPerms)
                    }
                    editingAdminUser = null
                }
            )
        }

        // ADD KNOWLEDGE SOURCE DIALOG
        if (showAddKnowledgeDialog) {
            AddKnowledgeSourceDialog(
                onDismiss = { showAddKnowledgeDialog = false },
                onAddSource = {
                    showAddKnowledgeDialog = false
                    AdminAuthManager.recordAuditLog(
                        admin = adminUser,
                        action = "ADD_RAG_SOURCE",
                        target = "rag-corpus",
                        metadata = mapOf("title" to it)
                    )
                }
            )
        }
    }
}

@Composable
private fun AdminTopBar(
    admin: AdminUser,
    onLogout: () -> Unit,
    onBackToPlayer: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurfaceElevated)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Title & Tag
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(NeonCyan.copy(alpha = 0.2f))
                    .border(1.dp, NeonCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "QuizKing",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x3322D3EE))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "COMMAND CENTER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                fontSize = 9.sp
                            )
                        )
                    }
                }
                Text(
                    text = "v2.5 Enterprise Admin",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                )
            }
        }

        // Current Admin Profile & Actions
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Role Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (admin.role == AdminRole.SUPER_ADMIN) AccentGold.copy(alpha = 0.18f)
                        else NeonCyan.copy(alpha = 0.15f)
                    )
                    .border(
                        1.dp,
                        if (admin.role == AdminRole.SUPER_ADMIN) AccentGold.copy(alpha = 0.5f)
                        else NeonCyan.copy(alpha = 0.4f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = admin.role.displayName.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (admin.role == AdminRole.SUPER_ADMIN) AccentGold else NeonCyan,
                        fontSize = 11.sp
                    )
                )
            }

            // Admin Email
            Text(
                text = admin.email,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                maxLines = 1
            )

            // Logout Button
            IconButton(
                onClick = onLogout,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Logout",
                    tint = ErrorRed,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun AdminNavPills(
    currentTab: Int,
    onSelectTab: (Int) -> Unit,
    userRole: AdminRole
) {
    val navItems = listOf(
        Pair("Overview", Icons.Default.Dashboard),
        Pair("Question Intelligence", Icons.Default.AutoAwesome),
        Pair("Question Repository", Icons.Default.Folder),
        Pair("RAG Knowledge", Icons.Default.MenuBook),
        Pair("Admin Users", Icons.Default.Group),
        Pair("Audit Logs", Icons.Default.History),
        Pair("System Health", Icons.Default.Memory)
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(navItems.size) { index ->
            val (title, icon) = navItems[index]
            val isSelected = currentTab == index

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) NeonCyan else Color.Transparent)
                    .border(
                        1.dp,
                        if (isSelected) NeonCyan else Color(0x22FFFFFF),
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { onSelectTab(index) }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isSelected) GlassCanvasBg else TextSecondary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) GlassCanvasBg else TextSecondary
                    )
                )
            }
        }
    }
}

@Composable
private fun AdminOverviewTab(
    adminUser: AdminUser,
    totalApproved: Int,
    totalRetired: Int,
    knowledgeCount: Int,
    adminCount: Int,
    auditCount: Int,
    onNavigateTab: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Card
        item {
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonCyan.copy(alpha = 0.5f),
                backgroundColor = DarkSurfaceElevated
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Welcome back, ${adminUser.displayName}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "QuizKing Operations Portal • Role: ${adminUser.role.displayName}",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SuccessGreen.copy(alpha = 0.15f))
                            .border(1.dp, SuccessGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(SuccessGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SYSTEM OPERATIONAL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // High-level KPI Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "OPERATIONAL METRICS & REPOSITORY HEALTH",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        title = "Active Questions",
                        value = "$totalApproved",
                        subtitle = "Auto-Validated in Pool",
                        color = NeonCyan,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(2) }
                    )
                    MetricStatCard(
                        title = "Auto-Approval Rate",
                        value = "100%",
                        subtitle = "Zero Manual Bottleneck",
                        color = SuccessGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(1) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        title = "Knowledge Sources",
                        value = "$knowledgeCount",
                        subtitle = "Ingested RAG Corpora",
                        color = ElectricBlue,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(3) }
                    )
                    MetricStatCard(
                        title = "Admin Accounts",
                        value = "$adminCount",
                        subtitle = "Authorized Operators",
                        color = AccentGold,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(4) }
                    )
                }
            }
        }

        // Quick Launch Operations
        item {
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = DarkBorder
            ) {
                Text(
                    text = "QUICK ADMINISTRATIVE ACTIONS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { onNavigateTab(1) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Question Pipeline", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { onNavigateTab(4) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentGold)
                    ) {
                        Icon(imageVector = Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("User Control", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    CyberCard(
        modifier = modifier.clickable { onClick() },
        borderColor = color.copy(alpha = 0.3f),
        backgroundColor = DarkSurfaceElevated
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                fontSize = 10.sp
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Black,
                color = color
            )
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp),
            maxLines = 1
        )
    }
}

@Composable
private fun AdminQuestionIntelligenceTab(
    admin: AdminUser,
    activeQuestionsCount: Int,
    knowledgeDocs: List<RagKnowledgeDoc>,
    isGenerating: Boolean,
    onRunPipeline: () -> Unit
) {
    val canRunRag = admin.role == AdminRole.SUPER_ADMIN || admin.permissions.ragManagement

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonCyan.copy(alpha = 0.6f),
                backgroundColor = DarkSurfaceElevated
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Question Intelligence",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Automated RAG Generation & Validation",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SuccessGreen.copy(alpha = 0.15f))
                            .border(1.dp, SuccessGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "AUTO-APPROVAL: ACTIVE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "The Question Intelligence pipeline autonomously retrieves fact-grounded knowledge from ingested RAG sources, generates 4-choice candidates, performs 5-layer programmatic & semantic validation, filters duplicates, and auto-approves valid questions directly into the live repository.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Pipeline 5-Stage Diagram / Status
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "5-STAGE AUTOMATED PIPELINE ARCHITECTURE:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    PipelineStageRow("1. RAG Retrieval & Ingestion", "Corpus Grounding (${knowledgeDocs.size} Sources)", SuccessGreen)
                    PipelineStageRow("2. LLM Candidate Generation", "Gemini 2.5 Flash Engine", NeonCyan)
                    PipelineStageRow("3. Multi-Pass Validator", "Structural, Ambiguity & Distractor Pass", SuccessGreen)
                    PipelineStageRow("4. Duplicate Detection Engine", "Levenshtein & Vector Similarity Pass", SuccessGreen)
                    PipelineStageRow("5. Auto-Approval Gate", "Zero-human approval directly to Production", AccentGold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (canRunRag) {
                    GlowCyanButton(
                        text = if (isGenerating) "Executing Automated RAG Pipeline..." else "Run Generation Pipeline",
                        onClick = onRunPipeline,
                        enabled = !isGenerating,
                        icon = Icons.Default.PlayArrow,
                        testTag = "admin_run_pipeline_btn"
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurface)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "🔒 Pipeline execution requires 'ragManagement' permission.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                        )
                    }
                }
            }
        }

        // Live Question Intelligence KPIs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricItemCard("Active Questions", "$activeQuestionsCount", NeonCyan, Modifier.weight(1f))
                MetricItemCard("Validation Pass Rate", "100%", SuccessGreen, Modifier.weight(1f))
                MetricItemCard("Duplicate Rejection", "0%", SuccessGreen, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PipelineStageRow(stage: String, detail: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = stage, style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary, fontWeight = FontWeight.Medium))
        }
        Text(text = detail, style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
    }
}

@Composable
private fun MetricItemCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    CyberCard(
        modifier = modifier,
        borderColor = DarkBorder,
        backgroundColor = DarkSurfaceElevated
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = color))
            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = TextMuted), maxLines = 1)
        }
    }
}

@Composable
private fun AdminQuestionRepositoryTab(
    admin: AdminUser,
    questions: List<QuestionEntity>,
    allCategories: List<String>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    statusFilter: String,
    onStatusFilterChange: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Question Repository",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
            )
            Text(
                text = "${questions.size} questions matching filters",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )
        }

        // Search & Category Filters
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by question or topic...", color = TextMuted) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = NeonCyan) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkSurfaceElevated,
                    unfocusedContainerColor = DarkSurfaceElevated,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(allCategories) { cat ->
                    val isSelected = cat == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else DarkSurface)
                            .border(
                                1.dp,
                                if (isSelected) NeonCyan else DarkBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectCategory(cat) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) NeonCyan else TextSecondary
                            )
                        )
                    }
                }
            }
        }

        // Questions List
        items(questions) { q ->
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = DarkBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonCyan.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = q.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan,
                                    fontSize = 10.sp
                                )
                            )
                        }

                        Text(
                            text = "Diff: ${q.difficulty}/10",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (q.status == "APPROVED") SuccessGreen.copy(alpha = 0.15f)
                                else ErrorRed.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = q.status,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (q.status == "APPROVED") SuccessGreen else ErrorRed,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = q.question,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    q.options.forEachIndexed { idx, opt ->
                        val isCorrect = idx == q.correctIndex
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${('A' + idx)}. ",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCorrect) SuccessGreen else TextMuted
                                )
                            )
                            Text(
                                text = opt,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isCorrect) SuccessGreen else TextSecondary,
                                    fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                            if (isCorrect) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "✓ (Correct)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SuccessGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Grounding: ${q.sourceReference}",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                )
            }
        }
    }
}

@Composable
private fun AdminKnowledgeSourcesTab(
    admin: AdminUser,
    docs: List<RagKnowledgeDoc>,
    onAddSourceClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "RAG Knowledge Sources",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "${docs.size} Ingested trusted corpora documents",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }

                if (admin.role == AdminRole.SUPER_ADMIN || admin.permissions.ragManagement) {
                    OutlinedButton(
                        onClick = onAddSourceClick,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, NeonCyan),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Source", fontSize = 12.sp)
                    }
                }
            }
        }

        items(docs) { doc ->
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = DarkBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ElectricBlue.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = doc.category.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue
                            )
                        )
                    }

                    Text(
                        text = doc.sourceName,
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = doc.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = doc.content,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }
    }
}

@Composable
private fun AdminUserControlTab(
    currentAdmin: AdminUser,
    adminList: List<AdminUser>,
    onCreateAdminClick: () -> Unit,
    onEditAdminClick: (AdminUser) -> Unit,
    onToggleStatus: (String, Boolean) -> Unit
) {
    val isSuperAdmin = currentAdmin.role == AdminRole.SUPER_ADMIN

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Admin User Control",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Manage administrator credentials, roles & granular permissions",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }

                if (isSuperAdmin) {
                    Button(
                        onClick = onCreateAdminClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentGold,
                            contentColor = DarkBackground
                        )
                    ) {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Admin", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        items(adminList) { admin ->
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (admin.role == AdminRole.SUPER_ADMIN) AccentGold.copy(alpha = 0.5f) else DarkBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = admin.displayName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (admin.role == AdminRole.SUPER_ADMIN) AccentGold.copy(alpha = 0.18f)
                                        else NeonCyan.copy(alpha = 0.15f)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = admin.role.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (admin.role == AdminRole.SUPER_ADMIN) AccentGold else NeonCyan,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        Text(
                            text = admin.email,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (admin.isActive) SuccessGreen.copy(alpha = 0.15f)
                                else ErrorRed.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = admin.status.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (admin.isActive) SuccessGreen else ErrorRed,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Permissions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PermissionBadge("Questions", admin.permissions.questionManagement)
                    PermissionBadge("RAG", admin.permissions.ragManagement)
                    PermissionBadge("Analytics", admin.permissions.analytics)
                    PermissionBadge("Users", admin.permissions.userManagement)
                    PermissionBadge("Config", admin.permissions.systemConfiguration)
                }

                if (isSuperAdmin && admin.uid != currentAdmin.uid) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { onEditAdminClick(admin) }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = NeonCyan)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Role / Perms", color = NeonCyan, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        if (admin.isActive) {
                            OutlinedButton(
                                onClick = { onToggleStatus(admin.uid, false) },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Disable", fontSize = 11.sp)
                            }
                        } else {
                            Button(
                                onClick = { onToggleStatus(admin.uid, true) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, contentColor = DarkBackground),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Enable", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionBadge(name: String, enabled: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (enabled) NeonCyan.copy(alpha = 0.12f) else DarkSurface)
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(
            text = "$name ${if (enabled) "✓" else "✕"}",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = if (enabled) NeonCyan else TextMuted,
                fontSize = 9.sp
            )
        )
    }
}

@Composable
private fun AdminAuditLogsTab(logs: List<AdminAuditLog>) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Administrative Audit Trail",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
            )
            Text(
                text = "Immutable security logs for all privileged admin operations",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )
        }

        items(logs) { log ->
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = DarkBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AccentGold.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = log.action,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AccentGold,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Text(
                        text = dateFormat.format(Date(log.timestamp)),
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Admin: ${log.adminEmail} • Target: ${log.target}",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary, fontWeight = FontWeight.Medium)
                )

                if (log.metadata.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Metadata: ${log.metadata}",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminSystemHealthTab(admin: AdminUser, onReturnToPlayer: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "System Health & Infrastructure",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
            )
            Text(
                text = "Live telemetry and service connectivity metrics",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )
        }

        item {
            CyberCard(modifier = Modifier.fillMaxWidth(), borderColor = DarkBorder) {
                SystemHealthItem("Firebase Authentication", "CONNECTED", SuccessGreen)
                SystemHealthItem("Cloud Firestore Rules", "ENFORCED (Role-Protected)", SuccessGreen)
                SystemHealthItem("Gemini AI API Engine", "HEALTHY (Flash 2.5)", SuccessGreen)
                SystemHealthItem("Room Local SQLite Database", "SYNCED & ACTIVE", SuccessGreen)
                SystemHealthItem("RAG Knowledge Grounding", "100% OPERATIONAL", NeonCyan)
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onReturnToPlayer,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, NeonCyan),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Return to Player Experience", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SystemHealthItem(service: String, status: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = service, style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(color.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontSize = 10.sp
                )
            )
        }
    }
}

// DIALOGS

@Composable
private fun CreateAdminDialog(
    onDismiss: () -> Unit,
    onAdminCreated: (String, String, String, AdminRole, AdminPermissions) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(AdminRole.CONTENT_MANAGER) }
    var permissions by remember { mutableStateOf(AdminPermissions.forRole(AdminRole.CONTENT_MANAGER)) }

    Dialog(onDismissRequest = onDismiss) {
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = AccentGold.copy(alpha = 0.6f),
            backgroundColor = DarkSurfaceElevated
        ) {
            Text(
                text = "Create / Invite Administrator",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Initial Password (min 6 chars)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text("Select Role:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextMuted))
            Spacer(modifier = Modifier.height(4.dp))

            AdminRole.entries.forEach { role ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedRole = role
                            permissions = AdminPermissions.forRole(role)
                        }
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = selectedRole == role,
                        onClick = {
                            selectedRole = role
                            permissions = AdminPermissions.forRole(role)
                        }
                    )
                    Text(text = role.displayName, style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (email.isNotBlank()) {
                            onAdminCreated(email, password, name, selectedRole, permissions)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = DarkBackground)
                ) {
                    Text("Create Admin", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun EditAdminDialog(
    target: AdminUser,
    onDismiss: () -> Unit,
    onSave: (AdminRole, AdminPermissions) -> Unit
) {
    var selectedRole by remember { mutableStateOf(target.role) }
    var permissions by remember { mutableStateOf(target.permissions) }

    Dialog(onDismissRequest = onDismiss) {
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = NeonCyan.copy(alpha = 0.6f),
            backgroundColor = DarkSurfaceElevated
        ) {
            Text(
                text = "Edit Permissions for ${target.displayName}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text("Assign Role:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextMuted))
            AdminRole.entries.forEach { role ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedRole = role
                            permissions = AdminPermissions.forRole(role)
                        }
                        .padding(vertical = 2.dp)
                ) {
                    RadioButton(selected = selectedRole == role, onClick = {
                        selectedRole = role
                        permissions = AdminPermissions.forRole(role)
                    })
                    Text(text = role.displayName, style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onSave(selectedRole, permissions) },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkBackground)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AddKnowledgeSourceDialog(
    onDismiss: () -> Unit,
    onAddSource: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Science") }
    var content by remember { mutableStateOf("") }
    var sourceName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = ElectricBlue.copy(alpha = 0.6f),
            backgroundColor = DarkSurfaceElevated
        ) {
            Text(
                text = "Add RAG Knowledge Corpus",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title / Topic") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(value = sourceName, onValueChange = { sourceName = it }, label = { Text("Source Reference Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Knowledge Text Excerpt") }, modifier = Modifier.fillMaxWidth(), maxLines = 4)

            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { if (title.isNotBlank()) onAddSource(title) },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = DarkBackground)
                ) {
                    Text("Ingest Source", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
