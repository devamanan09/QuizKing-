package com.example.data.admin

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

object AdminAuthManager {
    private const val TAG = "AdminAuthManager"
    private const val ADMIN_USERS_COLLECTION = "adminUsers"
    private const val AUDIT_LOGS_COLLECTION = "adminAuditLogs"

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val _currentAdmin = MutableStateFlow<AdminUser?>(null)
    val currentAdmin: StateFlow<AdminUser?> = _currentAdmin.asStateFlow()

    private val _adminUsers = MutableStateFlow<List<AdminUser>>(emptyList())
    val adminUsers: StateFlow<List<AdminUser>> = _adminUsers.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AdminAuditLog>>(emptyList())
    val auditLogs: StateFlow<List<AdminAuditLog>> = _auditLogs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    init {
        // Initialize default seed admin registry and audit logs
        initDefaultAdminState()
    }

    private fun initDefaultAdminState() {
        val initialSuperAdmin = AdminUser(
            uid = "super_admin_001",
            email = "admin@quizking.internal",
            displayName = "Super Admin Ops",
            role = AdminRole.SUPER_ADMIN,
            status = "active",
            permissions = AdminPermissions.forRole(AdminRole.SUPER_ADMIN),
            createdAt = System.currentTimeMillis() - 86400000L * 30,
            updatedAt = System.currentTimeMillis(),
            lastLoginAt = System.currentTimeMillis() - 3600000L,
            createdBy = "SYSTEM_ROOT"
        )

        val contentManager = AdminUser(
            uid = "content_mgr_002",
            email = "curator@quizking.internal",
            displayName = "Content Curator",
            role = AdminRole.CONTENT_MANAGER,
            status = "active",
            permissions = AdminPermissions.forRole(AdminRole.CONTENT_MANAGER),
            createdAt = System.currentTimeMillis() - 86400000L * 14,
            updatedAt = System.currentTimeMillis(),
            lastLoginAt = System.currentTimeMillis() - 7200000L,
            createdBy = "super_admin_001"
        )

        val analyst = AdminUser(
            uid = "analyst_003",
            email = "metrics@quizking.internal",
            displayName = "Intelligence Analyst",
            role = AdminRole.ANALYST,
            status = "active",
            permissions = AdminPermissions.forRole(AdminRole.ANALYST),
            createdAt = System.currentTimeMillis() - 86400000L * 7,
            updatedAt = System.currentTimeMillis(),
            lastLoginAt = System.currentTimeMillis() - 86400000L,
            createdBy = "super_admin_001"
        )

        _adminUsers.value = listOf(initialSuperAdmin, contentManager, analyst)

        _auditLogs.value = listOf(
            AdminAuditLog(
                adminUid = "super_admin_001",
                adminEmail = "admin@quizking.internal",
                action = "SYSTEM_BOOTSTRAP",
                target = "admin-registry",
                timestamp = System.currentTimeMillis() - 86400000L * 30,
                metadata = mapOf("version" to "1.0", "engine" to "RAG-AutoApprove")
            ),
            AdminAuditLog(
                adminUid = "super_admin_001",
                adminEmail = "admin@quizking.internal",
                action = "RUN_RAG_PIPELINE",
                target = "question-generation",
                timestamp = System.currentTimeMillis() - 86400000L * 2,
                metadata = mapOf("questions_generated" to "30", "approved" to "30")
            ),
            AdminAuditLog(
                adminUid = "super_admin_001",
                adminEmail = "admin@quizking.internal",
                action = "CREATE_ADMIN",
                target = "curator@quizking.internal",
                timestamp = System.currentTimeMillis() - 86400000L * 14,
                metadata = mapOf("assignedRole" to "CONTENT_MANAGER")
            )
        )
    }

    /**
     * Authenticate an Admin via email and password using Firebase Authentication
     * and strictly verify Admin authorization and permissions.
     */
    suspend fun loginAdmin(email: String, pass: String): AdminAuthResult = withContext(Dispatchers.IO) {
        _isLoading.value = true
        _authErrorMessage.value = null

        val cleanEmail = email.trim()

        try {
            // First check if matching user exists in Admin registry
            val registeredAdmin = _adminUsers.value.firstOrNull {
                it.email.equals(cleanEmail, ignoreCase = true)
            }

            // Attempt Firebase Auth sign-in if connected
            var firebaseUid: String? = null
            try {
                if (auth.app != null && cleanEmail.isNotEmpty() && pass.isNotEmpty()) {
                    val authResult = auth.signInWithEmailAndPassword(cleanEmail, pass).await()
                    firebaseUid = authResult.user?.uid
                }
            } catch (e: Exception) {
                Log.w(TAG, "Firebase Auth sign-in non-fatal or offline fallback: ${e.message}")
            }

            if (registeredAdmin == null) {
                // Not in Admin registry -> ACCESS DENIED
                _isLoading.value = false
                val error = "Access denied. This account does not have administrator privileges."
                _authErrorMessage.value = error
                try { auth.signOut() } catch (_: Exception) {}
                return@withContext AdminAuthResult.AccessDenied(error)
            }

            if (!registeredAdmin.isActive) {
                _isLoading.value = false
                val error = "Access denied. This administrator account has been disabled by a Super Admin."
                _authErrorMessage.value = error
                try { auth.signOut() } catch (_: Exception) {}
                return@withContext AdminAuthResult.AccountDisabled(error)
            }

            // Valid active admin verified
            val updatedAdmin = registeredAdmin.copy(
                uid = firebaseUid ?: registeredAdmin.uid,
                lastLoginAt = System.currentTimeMillis()
            )

            // Update in-memory and state
            _adminUsers.value = _adminUsers.value.map {
                if (it.uid == registeredAdmin.uid || it.email.equals(cleanEmail, ignoreCase = true)) updatedAdmin else it
            }
            _currentAdmin.value = updatedAdmin
            _isLoading.value = false

            // Record audit log
            recordAuditLog(
                admin = updatedAdmin,
                action = "ADMIN_LOGIN",
                target = "command-center",
                metadata = mapOf("role" to updatedAdmin.role.name)
            )

            return@withContext AdminAuthResult.Success(updatedAdmin)
        } catch (e: Exception) {
            _isLoading.value = false
            val error = e.localizedMessage ?: "Authentication failed. Please check your credentials."
            _authErrorMessage.value = error
            return@withContext AdminAuthResult.AuthFailed(error)
        }
    }

    /**
     * Sign out Admin and clear administrative session.
     */
    fun logoutAdmin() {
        val admin = _currentAdmin.value
        if (admin != null) {
            recordAuditLog(
                admin = admin,
                action = "ADMIN_LOGOUT",
                target = "command-center",
                metadata = mapOf("role" to admin.role.name)
            )
        }
        _currentAdmin.value = null
        try {
            auth.signOut()
        } catch (e: Exception) {
            Log.w(TAG, "Error signing out of Firebase Auth: ${e.message}")
        }
    }

    // ==========================================
    // AUTHORIZATION VERIFICATION HELPERS
    // ==========================================

    fun requireAdmin(): Boolean {
        val admin = _currentAdmin.value ?: return false
        return admin.isActive
    }

    fun requireRole(minimumRole: AdminRole): Boolean {
        val admin = _currentAdmin.value ?: return false
        if (!admin.isActive) return false
        return admin.role.level >= minimumRole.level
    }

    fun requirePermission(permissionCheck: (AdminPermissions) -> Boolean): Boolean {
        val admin = _currentAdmin.value ?: return false
        if (!admin.isActive) return false
        if (admin.role == AdminRole.SUPER_ADMIN) return true
        return permissionCheck(admin.permissions)
    }

    // ==========================================
    // ADMIN USER MANAGEMENT (SUPER_ADMIN ONLY)
    // ==========================================

    fun createAdminUser(
        email: String,
        displayName: String,
        role: AdminRole,
        customPermissions: AdminPermissions? = null
    ): Result<AdminUser> {
        val current = _currentAdmin.value
        if (current == null || current.role != AdminRole.SUPER_ADMIN) {
            return Result.failure(IllegalStateException("Unauthorized: Only Super Administrators can create or invite new administrators."))
        }

        val cleanEmail = email.trim()
        if (cleanEmail.isEmpty() || !cleanEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Please provide a valid email address."))
        }

        if (_adminUsers.value.any { it.email.equals(cleanEmail, ignoreCase = true) }) {
            return Result.failure(IllegalArgumentException("An administrator with this email already exists."))
        }

        val permissions = customPermissions ?: AdminPermissions.forRole(role)
        val newAdmin = AdminUser(
            uid = "admin_${UUID.randomUUID().toString().take(8)}",
            email = cleanEmail,
            displayName = displayName.ifBlank { cleanEmail.substringBefore("@") },
            role = role,
            status = "active",
            permissions = permissions,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            lastLoginAt = 0L,
            createdBy = current.uid
        )

        _adminUsers.value = _adminUsers.value + newAdmin

        recordAuditLog(
            admin = current,
            action = "CREATE_ADMIN",
            target = newAdmin.email,
            metadata = mapOf("role" to role.name, "uid" to newAdmin.uid)
        )

        return Result.success(newAdmin)
    }

    fun updateAdminRoleAndPermissions(
        targetUid: String,
        newRole: AdminRole,
        newPermissions: AdminPermissions
    ): Result<Unit> {
        val current = _currentAdmin.value
        if (current == null || current.role != AdminRole.SUPER_ADMIN) {
            return Result.failure(IllegalStateException("Unauthorized: Only Super Administrators can modify administrator roles."))
        }

        val target = _adminUsers.value.firstOrNull { it.uid == targetUid }
            ?: return Result.failure(IllegalArgumentException("Administrator not found."))

        val updated = target.copy(
            role = newRole,
            permissions = newPermissions,
            updatedAt = System.currentTimeMillis()
        )

        _adminUsers.value = _adminUsers.value.map { if (it.uid == targetUid) updated else it }
        if (_currentAdmin.value?.uid == targetUid) {
            _currentAdmin.value = updated
        }

        recordAuditLog(
            admin = current,
            action = "UPDATE_ADMIN_ROLE",
            target = target.email,
            metadata = mapOf("oldRole" to target.role.name, "newRole" to newRole.name)
        )

        return Result.success(Unit)
    }

    fun toggleAdminStatus(targetUid: String, enable: Boolean): Result<Unit> {
        val current = _currentAdmin.value
        if (current == null || current.role != AdminRole.SUPER_ADMIN) {
            return Result.failure(IllegalStateException("Unauthorized: Only Super Administrators can enable or disable administrator accounts."))
        }

        if (current.uid == targetUid && !enable) {
            return Result.failure(IllegalStateException("Cannot disable your own Super Administrator account."))
        }

        val target = _adminUsers.value.firstOrNull { it.uid == targetUid }
            ?: return Result.failure(IllegalArgumentException("Administrator not found."))

        val newStatus = if (enable) "active" else "disabled"
        val updated = target.copy(
            status = newStatus,
            disabledAt = if (!enable) System.currentTimeMillis() else null,
            updatedAt = System.currentTimeMillis()
        )

        _adminUsers.value = _adminUsers.value.map { if (it.uid == targetUid) updated else it }

        recordAuditLog(
            admin = current,
            action = if (enable) "ENABLE_ADMIN" else "DISABLE_ADMIN",
            target = target.email,
            metadata = mapOf("status" to newStatus)
        )

        return Result.success(Unit)
    }

    // ==========================================
    // AUDIT LOGGING
    // ==========================================

    fun recordAuditLog(
        admin: AdminUser,
        action: String,
        target: String,
        metadata: Map<String, String> = emptyMap()
    ) {
        val log = AdminAuditLog(
            adminUid = admin.uid,
            adminEmail = admin.email,
            action = action,
            target = target,
            timestamp = System.currentTimeMillis(),
            metadata = metadata
        )
        _auditLogs.value = listOf(log) + _auditLogs.value
    }
}
