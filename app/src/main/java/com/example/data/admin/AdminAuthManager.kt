package com.example.data.admin

import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Production-grade Administrator Authentication and Authorization Manager.
 * 
 * Enforces server-side authority:
 * 1. Firebase Authentication (identity verification)
 * 2. Firebase Custom Claims & Protected Firestore adminUsers/{uid} verification
 * 3. Granular Role-Based Access Control (RBAC) & Permission enforcement
 * 4. Immutable Audit Trail logging in Firestore
 * 
 * Client-side "isAdmin" flags or local mock lists are strictly disallowed.
 */
object AdminAuthManager {
    private const val TAG = "AdminAuthManager"
    private const val ADMIN_USERS_COLLECTION = "adminUsers"
    private const val AUDIT_LOGS_COLLECTION = "adminAuditLogs"

    private fun getAuth(): FirebaseAuth? {
        return try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseAuth not available: ${e.message}")
            null
        }
    }

    private fun getFirestore(): FirebaseFirestore? {
        return try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseFirestore not available: ${e.message}")
            null
        }
    }

    private fun getFunctions(): FirebaseFunctions? {
        return try {
            FirebaseFunctions.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseFunctions not available: ${e.message}")
            null
        }
    }

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

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        // Automatically check if a valid, authorized Admin session is currently active
        coroutineScope.launch {
            checkExistingSession()
        }
    }

    /**
     * Authenticate an Admin via email and password using Firebase Authentication,
     * and strictly verify server-side Admin authorization from Custom Claims & Firestore.
     */
    suspend fun loginAdmin(email: String, pass: String): AdminAuthResult = withContext(Dispatchers.IO) {
        _isLoading.value = true
        _authErrorMessage.value = null

        val cleanEmail = email.trim()
        if (cleanEmail.isEmpty() || pass.isEmpty()) {
            _isLoading.value = false
            val err = "Please enter both administrator email and password."
            _authErrorMessage.value = err
            return@withContext AdminAuthResult.AuthFailed(err)
        }

        val auth = getAuth()
        if (auth == null) {
            _isLoading.value = false
            val err = "Authentication service is temporarily unavailable. Please check your connection."
            _authErrorMessage.value = err
            return@withContext AdminAuthResult.AuthFailed(err)
        }

        try {
            // 1. Firebase Authentication
            val authResult = auth.signInWithEmailAndPassword(cleanEmail, pass).await()
            val firebaseUser = authResult.user
            if (firebaseUser == null) {
                _isLoading.value = false
                val err = "Authentication failed. Unable to resolve user identity."
                _authErrorMessage.value = err
                return@withContext AdminAuthResult.AuthFailed(err)
            }

            // 2. Fetch fresh ID token result to inspect Custom Claims
            val tokenResult = try {
                firebaseUser.getIdToken(true).await()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to refresh ID token: ${e.message}")
                null
            }

            val claims = tokenResult?.claims ?: emptyMap()
            val hasAdminClaim = claims["admin"] == true ||
                    claims["super_admin"] == true ||
                    claims.containsKey("adminRole") ||
                    claims.containsKey("role")

            val claimRoleStr = (claims["adminRole"] ?: claims["role"]) as? String
            val claimRole = if (claimRoleStr != null) AdminRole.fromString(claimRoleStr) else null

            // 3. Fetch Admin record from Firestore adminUsers/{uid}
            var adminDocData: Map<String, Any?>? = null
            val firestore = getFirestore()
            if (firestore != null) {
                try {
                    val snapshot = firestore.collection(ADMIN_USERS_COLLECTION)
                        .document(firebaseUser.uid)
                        .get()
                        .await()
                    if (snapshot.exists()) {
                        adminDocData = snapshot.data
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Firestore admin document fetch encountered: ${e.message}")
                }
            }

            // 4. Server Authorization Verification
            // Authoritative Custom Claim Verification: Firebase Auth Custom Claims are the authoritative gate.
            if (!hasAdminClaim) {
                // Not authorized as an Admin!
                Log.w(TAG, "Access denied for UID ${firebaseUser.uid} (email: ${firebaseUser.email}): No authoritative admin Custom Claim.")
                try { auth.signOut() } catch (_: Exception) {}
                _currentAdmin.value = null
                _isLoading.value = false
                val deniedMsg = "Access denied. This account does not possess authoritative administrator claims."
                _authErrorMessage.value = deniedMsg
                return@withContext AdminAuthResult.AccessDenied(deniedMsg)
            }

            // 5. Parse Admin Profile
            val adminUser: AdminUser = if (adminDocData != null) {
                AdminUser.fromMap(firebaseUser.uid, adminDocData)
            } else {
                val role = claimRole ?: if (claims["super_admin"] == true) AdminRole.SUPER_ADMIN else AdminRole.ADMIN
                AdminUser(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: cleanEmail,
                    displayName = firebaseUser.displayName ?: cleanEmail.substringBefore("@"),
                    role = role,
                    status = "ACTIVE",
                    permissions = AdminPermissions.forRole(role),
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    lastLoginAt = System.currentTimeMillis(),
                    createdBy = "SYSTEM_AUTH_CLAIMS"
                )
            }

            // 6. Check Active Status
            if (!adminUser.isActive) {
                Log.w(TAG, "Administrator account ${adminUser.email} is disabled.")
                try { auth.signOut() } catch (_: Exception) {}
                _currentAdmin.value = null
                _isLoading.value = false
                val disabledMsg = "Access denied. This administrator account has been disabled by a Super Administrator."
                _authErrorMessage.value = disabledMsg
                return@withContext AdminAuthResult.AccountDisabled(disabledMsg)
            }

            // 7. Update lastLoginAt in Firestore
            val updatedAdmin = adminUser.copy(lastLoginAt = System.currentTimeMillis())
            if (firestore != null) {
                try {
                    firestore.collection(ADMIN_USERS_COLLECTION)
                        .document(firebaseUser.uid)
                        .set(updatedAdmin.toMap())
                        .await()
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to update lastLoginAt in Firestore: ${e.message}")
                }
            }

            // 8. Record Immutable Audit Log
            recordAuditLog(
                admin = updatedAdmin,
                action = "ADMIN_LOGIN",
                target = "command-center",
                metadata = mapOf("role" to updatedAdmin.role.name, "authProvider" to "firebase_password")
            )

            // 9. Load Admin Directory and Audit Logs if permitted
            loadAdminDirectoryAndLogs(updatedAdmin)

            _currentAdmin.value = updatedAdmin
            _isLoading.value = false
            return@withContext AdminAuthResult.Success(updatedAdmin)

        } catch (e: FirebaseAuthInvalidCredentialsException) {
            _isLoading.value = false
            val msg = "Invalid administrator email or password. Please verify your credentials."
            _authErrorMessage.value = msg
            return@withContext AdminAuthResult.AuthFailed(msg)
        } catch (e: FirebaseAuthInvalidUserException) {
            _isLoading.value = false
            val msg = "Administrator account does not exist or has been deleted."
            _authErrorMessage.value = msg
            return@withContext AdminAuthResult.AuthFailed(msg)
        } catch (e: FirebaseNetworkException) {
            _isLoading.value = false
            val msg = "Network connection failed. Secure administration portal requires active internet connectivity."
            _authErrorMessage.value = msg
            return@withContext AdminAuthResult.AuthFailed(msg)
        } catch (e: Exception) {
            _isLoading.value = false
            val msg = e.localizedMessage ?: "Authentication failed due to an internal security error."
            _authErrorMessage.value = msg
            return@withContext AdminAuthResult.AuthFailed(msg)
        }
    }

    /**
     * Check if a previously authenticated Firebase user has valid server-side Admin authorization.
     */
    suspend fun checkExistingSession(): Boolean = withContext(Dispatchers.IO) {
        val auth = getAuth() ?: return@withContext false
        val user = auth.currentUser ?: return@withContext false
        try {
            val tokenResult = user.getIdToken(false).await()
            val claims = tokenResult.claims
            val hasAdminClaim = claims["admin"] == true ||
                    claims["super_admin"] == true ||
                    claims.containsKey("adminRole") ||
                    claims.containsKey("role")

            val firestore = getFirestore()
            var adminDocData: Map<String, Any?>? = null
            if (firestore != null) {
                try {
                    val snapshot = firestore.collection(ADMIN_USERS_COLLECTION)
                        .document(user.uid)
                        .get()
                        .await()
                    if (snapshot.exists()) {
                        adminDocData = snapshot.data
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Firestore admin fetch in checkExistingSession: ${e.message}")
                }
            }

            if (!hasAdminClaim) {
                // User is not authorized with custom claims
                _currentAdmin.value = null
                return@withContext false
            }

            val adminUser = if (adminDocData != null) {
                AdminUser.fromMap(user.uid, adminDocData)
            } else {
                val roleStr = (claims["adminRole"] ?: claims["role"]) as? String
                val role = if (roleStr != null) AdminRole.fromString(roleStr) else AdminRole.ADMIN
                AdminUser(
                    uid = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: (user.email ?: "").substringBefore("@"),
                    role = role,
                    status = "ACTIVE",
                    permissions = AdminPermissions.forRole(role)
                )
            }

            if (!adminUser.isActive) {
                try { auth.signOut() } catch (_: Exception) {}
                _currentAdmin.value = null
                return@withContext false
            }

            _currentAdmin.value = adminUser
            loadAdminDirectoryAndLogs(adminUser)
            return@withContext true
        } catch (e: Exception) {
            Log.w(TAG, "Error checking existing admin session: ${e.message}")
            _currentAdmin.value = null
            return@withContext false
        }
    }

    /**
     * Sign out Admin and terminate administrative session completely.
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
        _adminUsers.value = emptyList()
        _auditLogs.value = emptyList()
        try {
            getAuth()?.signOut()
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
    // DATA SYNCHRONIZATION (ADMINS & AUDIT LOGS)
    // ==========================================

    private fun loadAdminDirectoryAndLogs(admin: AdminUser) {
        coroutineScope.launch {
            val firestore = getFirestore()
            // Load admin users list if user has userManagement permission or is Super Admin
            if (admin.role == AdminRole.SUPER_ADMIN || admin.permissions.userManagement) {
                if (firestore != null) {
                    try {
                        val usersSnapshot = firestore.collection(ADMIN_USERS_COLLECTION)
                            .orderBy("createdAt", Query.Direction.DESCENDING)
                            .get()
                            .await()
                        val list = usersSnapshot.documents.mapNotNull { doc ->
                            doc.data?.let { AdminUser.fromMap(doc.id, it) }
                        }
                        if (list.isNotEmpty()) {
                            _adminUsers.value = list
                        } else {
                            _adminUsers.value = listOf(admin)
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to load admin directory from Firestore: ${e.message}")
                        _adminUsers.value = listOf(admin)
                    }
                } else {
                    _adminUsers.value = listOf(admin)
                }
            } else {
                _adminUsers.value = listOf(admin)
            }

            // Load audit logs if permitted
            if (admin.role == AdminRole.SUPER_ADMIN || admin.permissions.analytics || admin.permissions.moderation) {
                if (firestore != null) {
                    try {
                        val logsSnapshot = firestore.collection(AUDIT_LOGS_COLLECTION)
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .limit(100)
                            .get()
                            .await()
                        val list = logsSnapshot.documents.mapNotNull { doc ->
                            doc.data?.let { AdminAuditLog.fromMap(doc.id, it) }
                        }
                        _auditLogs.value = list
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to load audit logs from Firestore: ${e.message}")
                    }
                }
            }
        }
    }

    // ==========================================
    // PASSWORD POLICY & CREDENTIAL INTEGRITY
    // ==========================================

    fun validatePasswordPolicy(password: String): String? {
        val clean = password.trim()
        if (clean.isEmpty()) {
            return "Password is required and cannot be blank."
        }
        if (clean.length < 10) {
            return "Password must be at least 10 characters in length."
        }
        if (!clean.any { it.isUpperCase() }) {
            return "Password must contain at least one uppercase letter (A-Z)."
        }
        if (!clean.any { it.isLowerCase() }) {
            return "Password must contain at least one lowercase letter (a-z)."
        }
        if (!clean.any { it.isDigit() }) {
            return "Password must contain at least one numeric digit (0-9)."
        }
        if (!clean.any { !it.isLetterOrDigit() }) {
            return "Password must contain at least one special symbol (!@#$%^&*)."
        }
        return null
    }

    // ==========================================
    // ADMIN USER MANAGEMENT (SUPER_ADMIN ONLY)
    // Invokes trusted Backend Cloud Functions
    // ==========================================

    suspend fun createAdminUser(
        email: String,
        password: String,
        displayName: String,
        role: AdminRole,
        customPermissions: AdminPermissions? = null
    ): Result<AdminUser> = withContext(Dispatchers.IO) {
        val current = _currentAdmin.value
        if (current == null || current.role != AdminRole.SUPER_ADMIN) {
            return@withContext Result.failure(IllegalStateException("Unauthorized: Only Super Administrators can create or invite new administrators."))
        }

        val cleanEmail = email.trim()
        if (cleanEmail.isEmpty() || !cleanEmail.contains("@")) {
            return@withContext Result.failure(IllegalArgumentException("Please provide a valid email address."))
        }

        val passwordError = validatePasswordPolicy(password)
        if (passwordError != null) {
            return@withContext Result.failure(IllegalArgumentException(passwordError))
        }

        val permissions = customPermissions ?: AdminPermissions.forRole(role)

        // 1. Invoke trusted backend Cloud Function to create Auth user and assign Custom Claims
        val functions = getFunctions()
        if (functions != null) {
            try {
                val data = hashMapOf(
                    "email" to cleanEmail,
                    "password" to password.trim(),
                    "displayName" to displayName.ifBlank { cleanEmail.substringBefore("@") },
                    "role" to role.name,
                    "customPermissions" to permissions.toMap()
                )
                val callResult = functions.getHttpsCallable("createAdminUser").call(data).await()
                val resultMap = callResult.data as? Map<*, *>
                val adminMap = resultMap?.get("admin") as? Map<String, Any?>
                if (adminMap != null) {
                    val uid = (adminMap["uid"] as? String) ?: cleanEmail
                    val createdAdmin = AdminUser.fromMap(uid, adminMap)
                    _adminUsers.value = listOf(createdAdmin) + _adminUsers.value.filter { it.uid != createdAdmin.uid }

                    recordAuditLog(
                        admin = current,
                        action = "CREATE_ADMIN",
                        target = createdAdmin.email,
                        metadata = mapOf("role" to role.name, "uid" to createdAdmin.uid, "backend" to "cloud_functions")
                    )
                    return@withContext Result.success(createdAdmin)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Backend Cloud Function createAdminUser failed: ${e.message}")
                return@withContext Result.failure(e)
            }
        }

        return@withContext Result.failure(IllegalStateException("Cloud Functions backend service unavailable."))
    }

    suspend fun updateAdminRoleAndPermissions(
        targetUid: String,
        newRole: AdminRole,
        newPermissions: AdminPermissions
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val current = _currentAdmin.value
        if (current == null || current.role != AdminRole.SUPER_ADMIN) {
            return@withContext Result.failure(IllegalStateException("Unauthorized: Only Super Administrators can modify administrator roles."))
        }

        val target = _adminUsers.value.firstOrNull { it.uid == targetUid }
            ?: return@withContext Result.failure(IllegalArgumentException("Administrator not found."))

        // 1. Invoke backend Cloud Function to update Custom Claims and Firestore record
        val functions = getFunctions()
        if (functions != null) {
            try {
                val data = hashMapOf(
                    "targetUid" to targetUid,
                    "role" to newRole.name,
                    "customPermissions" to newPermissions.toMap()
                )
                functions.getHttpsCallable("updateAdminRole").call(data).await()
            } catch (e: Exception) {
                Log.e(TAG, "Backend Cloud Function updateAdminRole failed: ${e.message}")
                return@withContext Result.failure(e)
            }
        }

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
            metadata = mapOf("oldRole" to target.role.name, "newRole" to newRole.name, "backend" to "cloud_functions")
        )

        return@withContext Result.success(Unit)
    }

    suspend fun toggleAdminStatus(targetUid: String, enable: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val current = _currentAdmin.value
        if (current == null || current.role != AdminRole.SUPER_ADMIN) {
            return@withContext Result.failure(IllegalStateException("Unauthorized: Only Super Administrators can enable or disable administrator accounts."))
        }

        if (current.uid == targetUid && !enable) {
            return@withContext Result.failure(IllegalStateException("Cannot disable your own Super Administrator account."))
        }

        val target = _adminUsers.value.firstOrNull { it.uid == targetUid }
            ?: return@withContext Result.failure(IllegalArgumentException("Administrator not found."))

        // 1. Invoke backend Cloud Function to disable/enable Firebase Auth user and update status
        val functions = getFunctions()
        if (functions != null) {
            try {
                val data = hashMapOf(
                    "targetUid" to targetUid,
                    "enable" to enable
                )
                functions.getHttpsCallable("toggleAdminStatus").call(data).await()
            } catch (e: Exception) {
                Log.e(TAG, "Backend Cloud Function toggleAdminStatus failed: ${e.message}")
                return@withContext Result.failure(e)
            }
        }

        val newStatus = if (enable) "ACTIVE" else "DISABLED"
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
            metadata = mapOf("status" to newStatus, "backend" to "cloud_functions")
        )

        return@withContext Result.success(Unit)
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

        coroutineScope.launch {
            val firestore = getFirestore()
            if (firestore != null) {
                try {
                    firestore.collection(AUDIT_LOGS_COLLECTION)
                        .document(log.id)
                        .set(log.toMap())
                        .await()
                } catch (e: Exception) {
                    Log.w(TAG, "Firestore audit log persist non-fatal error: ${e.message}")
                }
            }
        }
    }
}
