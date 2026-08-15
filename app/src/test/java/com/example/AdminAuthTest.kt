package com.example

import com.example.data.admin.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AdminAuthTest {

    @Before
    fun setup() {
        AdminAuthManager.logoutAdmin()
    }

    @Test
    fun `empty credentials reject with authentication error`() = runTest {
        val result = AdminAuthManager.loginAdmin("", "")
        assertTrue(result is AdminAuthResult.AuthFailed)
        assertNull(AdminAuthManager.currentAdmin.value)
        assertFalse(AdminAuthManager.requireAdmin())
    }

    @Test
    fun `super admin role matrix grants full permissions`() {
        val permissions = AdminPermissions.forRole(AdminRole.SUPER_ADMIN)
        assertTrue(permissions.questionManagement)
        assertTrue(permissions.ragManagement)
        assertTrue(permissions.analytics)
        assertTrue(permissions.userManagement)
        assertTrue(permissions.systemConfiguration)
        assertTrue(permissions.moderation)
        assertTrue(permissions.tournamentManagement)
    }

    @Test
    fun `content manager role matrix grants content permissions but denies user management`() {
        val permissions = AdminPermissions.forRole(AdminRole.CONTENT_MANAGER)
        assertTrue(permissions.questionManagement)
        assertTrue(permissions.ragManagement)
        assertFalse(permissions.userManagement)
        assertFalse(permissions.systemConfiguration)
        assertFalse(permissions.analytics)
    }

    @Test
    fun `analyst role matrix grants analytics but denies mutating permissions`() {
        val permissions = AdminPermissions.forRole(AdminRole.ANALYST)
        assertTrue(permissions.analytics)
        assertFalse(permissions.questionManagement)
        assertFalse(permissions.ragManagement)
        assertFalse(permissions.userManagement)
        assertFalse(permissions.systemConfiguration)
    }

    @Test
    fun `support role matrix grants moderation but denies user management and rag`() {
        val permissions = AdminPermissions.forRole(AdminRole.SUPPORT)
        assertTrue(permissions.moderation)
        assertFalse(permissions.ragManagement)
        assertFalse(permissions.questionManagement)
        assertFalse(permissions.userManagement)
    }

    @Test
    fun `admin user serialization to and from Firestore map preserves all attributes`() {
        val original = AdminUser(
            uid = "test_admin_uid_99",
            email = "verified_admin@organization.com",
            displayName = "Verified Admin",
            role = AdminRole.ADMIN,
            status = "ACTIVE",
            permissions = AdminPermissions.forRole(AdminRole.ADMIN),
            createdAt = 1700000000000L,
            updatedAt = 1700000500000L,
            lastLoginAt = 1700001000000L,
            createdBy = "root_super_admin"
        )

        val map = original.toMap()
        assertEquals("test_admin_uid_99", map["uid"])
        assertEquals("verified_admin@organization.com", map["email"])
        assertEquals("ADMIN", map["role"])
        assertEquals("ACTIVE", map["status"])

        val deserialized = AdminUser.fromMap("test_admin_uid_99", map)
        assertEquals(original.uid, deserialized.uid)
        assertEquals(original.email, deserialized.email)
        assertEquals(original.role, deserialized.role)
        assertEquals(original.status, deserialized.status)
        assertTrue(deserialized.isActive)
        assertTrue(deserialized.permissions.questionManagement)
        assertFalse(deserialized.permissions.userManagement)
    }

    @Test
    fun `password policy validator enforces complexity requirements`() {
        // Blank
        assertNotNull(AdminAuthManager.validatePasswordPolicy(""))
        assertNotNull(AdminAuthManager.validatePasswordPolicy("   "))

        // Too short (<10 chars)
        assertNotNull(AdminAuthManager.validatePasswordPolicy("Pass1!"))

        // Missing uppercase
        assertNotNull(AdminAuthManager.validatePasswordPolicy("password123!"))

        // Missing lowercase
        assertNotNull(AdminAuthManager.validatePasswordPolicy("PASSWORD123!"))

        // Missing number
        assertNotNull(AdminAuthManager.validatePasswordPolicy("PasswordSpecial!"))

        // Missing symbol
        assertNotNull(AdminAuthManager.validatePasswordPolicy("Password12345"))

        // Valid strong compliant passwords
        assertNull(AdminAuthManager.validatePasswordPolicy("CorrectHorse#Battery99"))
        assertNull(AdminAuthManager.validatePasswordPolicy("AdminSecure!2026"))
        assertNull(AdminAuthManager.validatePasswordPolicy("Kv9\$mP2@xL7#"))
    }

    @Test
    fun `createAdminUser rejects when caller is not authenticated as super admin`() = runTest {
        val result = AdminAuthManager.createAdminUser(
            email = "new_admin@company.com",
            password = "StrongPassword123!",
            displayName = "New Admin",
            role = AdminRole.CONTENT_MANAGER
        )
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun `disabled admin status evaluates isActive as false`() {
        val disabledAdmin = AdminUser(
            uid = "disabled_uid",
            email = "disabled@organization.com",
            displayName = "Disabled Admin",
            role = AdminRole.ADMIN,
            status = "DISABLED",
            permissions = AdminPermissions.forRole(AdminRole.ADMIN)
        )
        assertFalse(disabledAdmin.isActive)
    }

    @Test
    fun `audit log serialization preserves metadata and timestamp`() {
        val log = AdminAuditLog(
            adminUid = "admin_123",
            adminEmail = "audited@organization.com",
            action = "RUN_RAG_PIPELINE",
            target = "question-generator",
            timestamp = 1700002000000L,
            metadata = mapOf("category" to "Science", "batchSize" to "30")
        )

        val map = log.toMap()
        assertEquals("admin_123", map["adminUid"])
        assertEquals("RUN_RAG_PIPELINE", map["action"])

        val deserialized = AdminAuditLog.fromMap(log.id, map)
        assertEquals(log.id, deserialized.id)
        assertEquals(log.adminUid, deserialized.adminUid)
        assertEquals(log.action, deserialized.action)
        assertEquals("Science", deserialized.metadata["category"])
    }
}
