package com.example.data.admin

import java.util.UUID

enum class AdminRole(val displayName: String, val level: Int) {
    SUPER_ADMIN("Super Admin", 100),
    ADMIN("Administrator", 80),
    CONTENT_MANAGER("Content Manager", 60),
    ANALYST("Analyst", 40),
    SUPPORT("Support Specialist", 20);

    companion object {
        fun fromString(role: String): AdminRole {
            return entries.firstOrNull { it.name.equals(role, ignoreCase = true) } ?: SUPPORT
        }
    }
}

data class AdminPermissions(
    val questionManagement: Boolean = false,
    val ragManagement: Boolean = false,
    val analytics: Boolean = false,
    val userManagement: Boolean = false,
    val systemConfiguration: Boolean = false,
    val moderation: Boolean = false
) {
    companion object {
        fun forRole(role: AdminRole): AdminPermissions = when (role) {
            AdminRole.SUPER_ADMIN -> AdminPermissions(
                questionManagement = true,
                ragManagement = true,
                analytics = true,
                userManagement = true,
                systemConfiguration = true,
                moderation = true
            )
            AdminRole.ADMIN -> AdminPermissions(
                questionManagement = true,
                ragManagement = true,
                analytics = true,
                userManagement = false,
                systemConfiguration = false,
                moderation = true
            )
            AdminRole.CONTENT_MANAGER -> AdminPermissions(
                questionManagement = true,
                ragManagement = true,
                analytics = false,
                userManagement = false,
                systemConfiguration = false,
                moderation = false
            )
            AdminRole.ANALYST -> AdminPermissions(
                questionManagement = false,
                ragManagement = false,
                analytics = true,
                userManagement = false,
                systemConfiguration = false,
                moderation = false
            )
            AdminRole.SUPPORT -> AdminPermissions(
                questionManagement = false,
                ragManagement = false,
                analytics = false,
                userManagement = false,
                systemConfiguration = false,
                moderation = true
            )
        }
    }
}

data class AdminUser(
    val uid: String,
    val email: String,
    val displayName: String,
    val role: AdminRole,
    val status: String = "active", // "active", "disabled"
    val permissions: AdminPermissions = AdminPermissions.forRole(role),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis(),
    val createdBy: String = "SYSTEM_BOOTSTRAP",
    val disabledAt: Long? = null
) {
    val isActive: Boolean get() = status.equals("active", ignoreCase = true)
}

data class AdminAuditLog(
    val id: String = UUID.randomUUID().toString(),
    val adminUid: String,
    val adminEmail: String,
    val action: String,
    val target: String,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: Map<String, String> = emptyMap()
)

sealed class AdminAuthResult {
    data class Success(val adminUser: AdminUser) : AdminAuthResult()
    data class AccessDenied(val reason: String) : AdminAuthResult()
    data class AuthFailed(val errorMessage: String) : AdminAuthResult()
    data class AccountDisabled(val reason: String) : AdminAuthResult()
}
