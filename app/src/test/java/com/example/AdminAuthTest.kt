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
    fun `super admin login succeeds with full permissions`() = runTest {
        val result = AdminAuthManager.loginAdmin("admin@quizking.internal", "AdminSecurePass123!")
        assertTrue(result is AdminAuthResult.Success)

        val currentAdmin = AdminAuthManager.currentAdmin.value
        assertNotNull(currentAdmin)
        assertEquals(AdminRole.SUPER_ADMIN, currentAdmin?.role)
        assertTrue(AdminAuthManager.requireAdmin())
        assertTrue(AdminAuthManager.requireRole(AdminRole.SUPER_ADMIN))
        assertTrue(AdminAuthManager.requirePermission { it.userManagement })
        assertTrue(AdminAuthManager.requirePermission { it.ragManagement })
    }

    @Test
    fun `non-admin user is rejected with access denied`() = runTest {
        val result = AdminAuthManager.loginAdmin("unauthorized_player@quizking.com", "random_password")
        assertTrue(result is AdminAuthResult.AccessDenied)
        assertNull(AdminAuthManager.currentAdmin.value)
        assertFalse(AdminAuthManager.requireAdmin())
    }

    @Test
    fun `content manager has rag permissions but cannot manage users`() = runTest {
        val result = AdminAuthManager.loginAdmin("curator@quizking.internal", "CuratorPass123!")
        assertTrue(result is AdminAuthResult.Success)

        val current = AdminAuthManager.currentAdmin.value
        assertEquals(AdminRole.CONTENT_MANAGER, current?.role)
        assertTrue(AdminAuthManager.requirePermission { it.questionManagement })
        assertTrue(AdminAuthManager.requirePermission { it.ragManagement })
        assertFalse(AdminAuthManager.requirePermission { it.userManagement })
    }

    @Test
    fun `audit log records login and admin actions`() = runTest {
        AdminAuthManager.loginAdmin("admin@quizking.internal", "AdminSecurePass123!")
        val logs = AdminAuthManager.auditLogs.value
        assertTrue(logs.any { it.action == "ADMIN_LOGIN" && it.adminEmail == "admin@quizking.internal" })
    }
}
