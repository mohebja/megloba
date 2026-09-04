package com.global.sms

import com.global.sms.core.automation.AutomationActionType
import com.global.sms.core.automation.AutomationEngine
import com.global.sms.core.automation.AutomationTriggerType
import com.global.sms.core.enterprise.EnterpriseOrganizationManager
import com.global.sms.core.enterprise.EnterprisePermission
import com.global.sms.core.enterprise.EnterpriseRole
import com.global.sms.core.enterprise.RolePermissionEngine
import com.global.sms.core.security.EnterpriseSecurityAudit
import com.global.sms.core.sync.CompanionDeviceType
import com.global.sms.core.sync.EnterpriseSyncEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Sprint10EnterpriseTest {

    @Test
    fun testOrganizationCreationAndDepartmentManagement() {
        val manager = EnterpriseOrganizationManager()
        val org = manager.createOrganization("شرکت فناوری پیشرو", "Software Enterprise")
        assertEquals("شرکت فناوری پیشرو", org.companyName)

        val dep = manager.addDepartment("دپارتمان هوش مصنوعی", "دکتر رضایی")
        assertNotNull(dep.id)
        assertEquals("دپارتمان هوش مصنوعی", dep.name)

        val emp = manager.addEmployee(dep.id, "امیرحسین کاظمی", "ADMIN", listOf("SEND_SMS", "ACCESS_AI"))
        assertNotNull(emp.id)
        assertEquals("امیرحسین کاظمی", emp.name)
    }

    @Test
    fun testRoleBasedAccessControlPermissions() {
        val rbac = RolePermissionEngine()
        assertTrue(rbac.hasPermission(EnterpriseRole.OWNER, EnterprisePermission.SEND_SMS))
        assertTrue(rbac.hasPermission(EnterpriseRole.OWNER, EnterprisePermission.DELETE_SMS))

        assertTrue(rbac.hasPermission(EnterpriseRole.EMPLOYEE, EnterprisePermission.SEND_SMS))
        assertTrue(!rbac.hasPermission(EnterpriseRole.EMPLOYEE, EnterprisePermission.DELETE_SMS))

        rbac.switchRole(EnterpriseRole.MANAGER)
        assertEquals(EnterpriseRole.MANAGER, rbac.activeUserRole.value)
        assertTrue(rbac.isActionAllowed(EnterprisePermission.MANAGE_CAMPAIGNS))
    }

    @Test
    fun testEnterpriseAutomationTriggersAndActions() {
        val engine = AutomationEngine()
        val results = engine.processIncomingMessage("VIP_SENDER", "سلام، درباره فاکتور و پرداخت بدهی سوال داشتم.")
        assertTrue(results.isNotEmpty())
    }

    @Test
    fun testSyncEnginePacketCreation() {
        val syncEngine = EnterpriseSyncEngine()
        val packet = syncEngine.createEncryptedSyncPacket(
            sourceDevice = CompanionDeviceType.PHONE,
            dataType = "CRM_METADATA",
            rawJsonData = "{\"contactId\":\"c100\",\"status\":\"VIP\"}"
        )
        assertNotNull(packet.packetId)
        assertTrue(packet.isE2eEncrypted)
        assertEquals(CompanionDeviceType.PHONE, packet.sourceDeviceType)
    }

    @Test
    fun testEnterpriseSecurityAudit() {
        val audit = EnterpriseSecurityAudit()
        val summary = audit.runFullSecurityAudit()
        assertEquals(100, summary.securityScore)
        assertEquals(0, summary.rbacViolationsCount)
        assertTrue(summary.isEncryptionActive)
    }
}
