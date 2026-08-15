package com.example.data.admin

import java.util.UUID

enum class AdminRole(val displayName: String, val level: Int) {
    SUPER_ADMIN("Super Admin", 100),
    ADMIN("Administrator", 80),
    CONTENT_MANAGER("Content Manager", 60),
    ANALYST("Analyst", 40),
    SUPPORT("Support Specialist", 20);

    companion object {
        fun fromString(role: String?): AdminRole {
            if (role == null) return SUPPORT
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
    val moderation: Boolean = false,
    val tournamentManagement: Boolean = false
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "questionManagement" to questionManagement,
            "ragManagement" to ragManagement,
            "analytics" to analytics,
            "userManagement" to userManagement,
            "systemConfiguration" to systemConfiguration,
            "moderation" to moderation,
            "tournamentManagement" to tournamentManagement
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>?): AdminPermissions {
            if (map == null) return AdminPermissions()
            return AdminPermissions(
                questionManagement = map["questionManagement"] as? Boolean ?: false,
                ragManagement = map["ragManagement"] as? Boolean ?: false,
                analytics = map["analytics"] as? Boolean ?: false,
                userManagement = map["userManagement"] as? Boolean ?: false,
                systemConfiguration = map["systemConfiguration"] as? Boolean ?: false,
                moderation = map["moderation"] as? Boolean ?: false,
                tournamentManagement = map["tournamentManagement"] as? Boolean ?: false
            )
        }

        fun forRole(role: AdminRole): AdminPermissions = when (role) {
            AdminRole.SUPER_ADMIN -> AdminPermissions(
                questionManagement = true,
                ragManagement = true,
                analytics = true,
                userManagement = true,
                systemConfiguration = true,
                moderation = true,
                tournamentManagement = true
            )
            AdminRole.ADMIN -> AdminPermissions(
                questionManagement = true,
                ragManagement = true,
                analytics = true,
                userManagement = false,
                systemConfiguration = false,
                moderation = true,
                tournamentManagement = true
            )
            AdminRole.CONTENT_MANAGER -> AdminPermissions(
                questionManagement = true,
                ragManagement = true,
                analytics = false,
                userManagement = false,
                systemConfiguration = false,
                moderation = false,
                tournamentManagement = false
            )
            AdminRole.ANALYST -> AdminPermissions(
                questionManagement = false,
                ragManagement = false,
                analytics = true,
                userManagement = false,
                systemConfiguration = false,
                moderation = false,
                tournamentManagement = false
            )
            AdminRole.SUPPORT -> AdminPermissions(
                questionManagement = false,
                ragManagement = false,
                analytics = false,
                userManagement = false,
                systemConfiguration = false,
                moderation = true,
                tournamentManagement = false
            )
        }
    }
}

data class AdminUser(
    val uid: String,
    val email: String,
    val displayName: String,
    val role: AdminRole,
    val status: String = "ACTIVE", // "ACTIVE", "DISABLED"
    val permissions: AdminPermissions = AdminPermissions.forRole(role),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis(),
    val createdBy: String = "SYSTEM_SERVER",
    val disabledAt: Long? = null
) {
    val isActive: Boolean get() = status.equals("ACTIVE", ignoreCase = true)

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "email" to email,
            "displayName" to displayName,
            "role" to role.name,
            "status" to status.uppercase(),
            "permissions" to permissions.toMap(),
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "lastLoginAt" to lastLoginAt,
            "createdBy" to createdBy,
            "disabledAt" to disabledAt
        )
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(uid: String, map: Map<String, Any?>): AdminUser {
            val roleStr = map["role"] as? String ?: "SUPPORT"
            val role = AdminRole.fromString(roleStr)
            val permissionsMap = map["permissions"] as? Map<String, Any?>
            val permissions = if (permissionsMap != null) {
                AdminPermissions.fromMap(permissionsMap)
            } else {
                AdminPermissions.forRole(role)
            }

            return AdminUser(
                uid = uid,
                email = map["email"] as? String ?: "",
                displayName = map["displayName"] as? String ?: (map["email"] as? String ?: "").substringBefore("@"),
                role = role,
                status = (map["status"] as? String ?: "ACTIVE").uppercase(),
                permissions = permissions,
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                lastLoginAt = (map["lastLoginAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                createdBy = map["createdBy"] as? String ?: "SYSTEM_SERVER",
                disabledAt = (map["disabledAt"] as? Number)?.toLong()
            )
        }
    }
}

data class AdminAuditLog(
    val id: String = UUID.randomUUID().toString(),
    val adminUid: String,
    val adminEmail: String,
    val action: String,
    val target: String,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: Map<String, String> = emptyMap()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "adminUid" to adminUid,
            "adminEmail" to adminEmail,
            "action" to action,
            "target" to target,
            "timestamp" to timestamp,
            "metadata" to metadata
        )
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(id: String, map: Map<String, Any?>): AdminAuditLog {
            val metadataRaw = map["metadata"] as? Map<String, Any?> ?: emptyMap()
            val metadata = metadataRaw.mapValues { it.value?.toString() ?: "" }
            return AdminAuditLog(
                id = id,
                adminUid = map["adminUid"] as? String ?: "",
                adminEmail = map["adminEmail"] as? String ?: "",
                action = map["action"] as? String ?: "",
                target = map["target"] as? String ?: "",
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                metadata = metadata
            )
        }
    }
}

sealed class AdminAuthResult {
    data class Success(val adminUser: AdminUser) : AdminAuthResult()
    data class AccessDenied(val reason: String) : AdminAuthResult()
    data class AuthFailed(val errorMessage: String) : AdminAuthResult()
    data class AccountDisabled(val reason: String) : AdminAuthResult()
    data class SessionExpired(val reason: String) : AdminAuthResult()
}
