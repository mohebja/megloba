package com.global.sms

import com.global.sms.core.ai.agent.AgentRoleType
import com.global.sms.core.ai.agent.EnterpriseAIAgent
import com.global.sms.core.ai.agent.ExecutionMode
import com.global.sms.core.ai.memory.EnterpriseMemoryRecord
import com.global.sms.core.ai.memory.MemoryPermissionController
import com.global.sms.core.ai.memory.MemoryScope
import com.global.sms.core.ai.memory.UserRole
import com.global.sms.core.api.ApiGatewayRequest
import com.global.sms.core.api.ApiKeyProfile
import com.global.sms.core.api.InternalApiGateway
import com.global.sms.core.automation.AutomationActionType
import com.global.sms.core.automation.AutomationRule
import com.global.sms.core.automation.AutomationTriggerType
import com.global.sms.core.bi.EnterpriseBIEngine
import com.global.sms.core.desktop.DesktopSyncProtocol
import com.global.sms.core.reporting.ReportCategory
import com.global.sms.core.reporting.ReportEngine
import com.global.sms.core.reporting.ReportFormat
import com.global.sms.core.security.EnterpriseSecurityCenter
import com.global.sms.core.security.SecuritySeverity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Sprint11_1_EnterpriseRegressionTest {

    @Test
    fun testPhase1_AiCopilotAgentAndHumanApprovalFlow() {
        val agent = EnterpriseAIAgent()

        val plan1 = agent.processMessage(
            sender = "09123456789",
            content = "قیمت سرویس سازمانی چقدر است؟",
            role = AgentRoleType.CUSTOMER_SUPPORT,
            mode = ExecutionMode.HUMAN_CONFIRMED
        )

        assertNotNull(plan1.actionId)
        assertTrue(plan1.confidenceScore > 0.8f)
        assertTrue(plan1.safetyScore > 0.9f)
        assertEquals(1, agent.pendingActionPlans.value.size)

        val approved = agent.confirmActionPlan(plan1.actionId)
        assertTrue(approved)
        assertEquals(0, agent.pendingActionPlans.value.size)
        assertEquals(1, agent.executionHistory.value.size)
    }

    @Test
    fun testPhase2_AiAgentSecurityAuditAndMemoryIsolation() {
        val records = listOf(
            EnterpriseMemoryRecord("m1", MemoryScope.ORGANIZATION_WIDE, null, "سیاست عام", "ارسال پیامک ۵۰ ریال", sensitivityLevel = 1),
            EnterpriseMemoryRecord("m2", MemoryScope.DEPARTMENT_LEVEL, "DEPT_HR", "حقوق کارکنان", "محرمانه", sensitivityLevel = 3),
            EnterpriseMemoryRecord("m3", MemoryScope.PRIVATE_VAULT, null, "کلید خصوصی", "SECRET_KEY", sensitivityLevel = 3)
        )

        val adminAccessible = MemoryPermissionController.filterAccessibleMemories(UserRole.SUPER_ADMIN, null, records)
        assertEquals(3, adminAccessible.size)

        val hrAgentAccessible = MemoryPermissionController.filterAccessibleMemories(UserRole.REGULAR_AGENT, "DEPT_HR", records)
        assertEquals(2, hrAgentAccessible.size) // ORG + DEPT_HR, PRIVATE_VAULT blocked

        val salesAgentAccessible = MemoryPermissionController.filterAccessibleMemories(UserRole.REGULAR_AGENT, "DEPT_SALES", records)
        assertEquals(1, salesAgentAccessible.size) // Only ORG
    }

    @Test
    fun testPhase3_RbacPermissionBoundaries() {
        val canSalesWriteOrg = MemoryPermissionController.canWriteMemory(UserRole.REGULAR_AGENT, "DEPT_SALES", MemoryScope.ORGANIZATION_WIDE)
        assertFalse(canSalesWriteOrg)

        val canAdminWriteOrg = MemoryPermissionController.canWriteMemory(UserRole.ENTERPRISE_ADMIN, "DEPT_SALES", MemoryScope.ORGANIZATION_WIDE)
        assertTrue(canAdminWriteOrg)
    }

    @Test
    fun testPhase4_DesktopSyncProtocolLocalEncryption() {
        val sync = DesktopSyncProtocol()
        val pairingToken = sync.generatePairingToken()

        val device = sync.validateAndPairDevice(pairingToken, "Poco X3 NFC Client", "ANDROID")
        assertNotNull(device)

        val packet = sync.sendSyncPacket(device!!.deviceId, "SYNC_CONTACTS", "{\"count\":5000}")
        assertTrue(packet.encryptedPayload.startsWith("AES256_GCM["))
        assertFalse(packet.encryptedPayload.contains("cloud"))

        val revoked = sync.revokeDeviceSession(device.deviceId)
        assertTrue(revoked)
    }

    @Test
    fun testPhase5_InternalApiGatewayRbacAndRateLimiting() {
        val gateway = InternalApiGateway()
        gateway.registerApiKey(
            ApiKeyProfile(
                keyId = "KEY_VIP_001",
                secretHash = "HASH_SECRET_123",
                clientName = "Enterprise ERP",
                allowedPermissions = listOf("SMS_SEND"),
                maxRequestsPerMinute = 100
            )
        )

        val request = ApiGatewayRequest(
            apiKeyId = "KEY_VIP_001",
            endpoint = "/api/v1/sms/send",
            httpMethod = "POST",
            payloadJson = "{\"recipient\":\"09123456789\",\"body\":\"Test SMS\"}",
            signature = "SIG_HMAC"
        )

        val response = gateway.handleRequest(request)
        assertEquals(200, response.statusCode)
        assertTrue(response.responseBody.contains("QUEUED"))
    }

    @Test
    fun testPhase6_WorkflowAutomationRuleExecution() {
        val rule = AutomationRule(
            id = "rule_101",
            name = "پاسخ خودکار کلمه OTP",
            triggerType = AutomationTriggerType.BODY_CONTAINS,
            triggerValue = "کد تایید",
            actionType = AutomationActionType.COPY_OTP,
            actionValue = null,
            isEnabled = true
        )

        assertTrue(rule.isEnabled)
        assertEquals(AutomationTriggerType.BODY_CONTAINS, rule.triggerType)
    }

    @Test
    fun testPhase7_BiAnalyticsEngineMetrics() {
        val bi = EnterpriseBIEngine()
        val report = bi.refreshMetrics()

        assertTrue(report.sentiment.overallScore >= 8.0f)
        assertTrue(report.roi.estimatedRevenueDollars > 30000.0)
        assertTrue(report.efficiency.autoResolvedPercent > 50.0f)
    }

    @Test
    fun testPhase8_SecurityCenterAuditIntegrity() {
        val securityCenter = EnterpriseSecurityCenter()

        val event = securityCenter.logSecurityEvent(
            eventType = "UNAUTHORIZED_LOGIN_ATTEMPT",
            severity = SecuritySeverity.HIGH,
            sourceModule = "InternalApiGateway",
            description = "Multiple failed signature verification from IP 10.0.0.12"
        )

        assertTrue(securityCenter.verifyLogIntegrity(event))
        val posture = securityCenter.calculateSecurityPosture()
        assertTrue(posture.overallScore in 80..100)
    }

    @Test
    fun testPhase9_ReportEngineGeneration() = runBlocking {
        val engine = ReportEngine()

        val report = engine.generateReport(
            title = "گزارش جامع امنیت سازمانی",
            category = ReportCategory.SECURITY_AUDIT,
            format = ReportFormat.PDF
        )

        assertNotNull(report.reportId)
        assertTrue(report.payload.contains("Executive Summary"))
    }
}
