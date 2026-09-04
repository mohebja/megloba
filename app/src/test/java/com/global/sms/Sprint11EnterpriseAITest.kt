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
import com.global.sms.core.bi.EnterpriseBIEngine
import com.global.sms.core.desktop.DesktopSyncProtocol
import com.global.sms.core.reporting.ReportCategory
import com.global.sms.core.reporting.ReportEngine
import com.global.sms.core.reporting.ReportFormat
import com.global.sms.core.security.EnterpriseSecurityCenter
import com.global.sms.core.security.SecuritySeverity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Sprint11EnterpriseAITest {

    @Test
    fun testEnterpriseAiAgentProcessingAndHumanApproval() {
        val agent = EnterpriseAIAgent()

        val plan = agent.processMessage(
            sender = "09123456789",
            content = "از پشتیبانی راضی نیستم و میخوام شکایت ثبت کنم",
            role = AgentRoleType.COMPLAINT_HANDLER,
            mode = ExecutionMode.HUMAN_CONFIRMED
        )

        assertNotNull(plan.actionId)
        assertTrue(plan.requiresUserConfirmation)
        assertEquals(1, agent.pendingActionPlans.value.size)

        val approved = agent.confirmActionPlan(plan.actionId)
        assertTrue(approved)
        assertEquals(0, agent.pendingActionPlans.value.size)
        assertEquals(1, agent.executionHistory.value.size)
    }

    @Test
    fun testMemoryPermissionControllerScopeAndRbac() {
        val recordOrg = EnterpriseMemoryRecord(
            memoryId = "m1",
            scope = MemoryScope.ORGANIZATION_WIDE,
            departmentId = null,
            key = "سیاست فروش",
            value = "تخفیف ۱۰ درصدی زمستانه"
        )

        val recordDept = EnterpriseMemoryRecord(
            memoryId = "m2",
            scope = MemoryScope.DEPARTMENT_LEVEL,
            departmentId = "DEPT_SALES",
            key = "پروپوزال خاص",
            value = "مشتری VIP"
        )

        assertTrue(MemoryPermissionController.canReadMemory(UserRole.SUPER_ADMIN, null, recordOrg))
        assertTrue(MemoryPermissionController.canReadMemory(UserRole.REGULAR_AGENT, "DEPT_SALES", recordDept))
        assertTrue(!MemoryPermissionController.canReadMemory(UserRole.REGULAR_AGENT, "DEPT_SUPPORT", recordDept))
    }

    @Test
    fun testDesktopSyncProtocolPairingAndHeartbeat() {
        val syncProtocol = DesktopSyncProtocol()

        val token = syncProtocol.generatePairingToken()
        assertTrue(token.startsWith("G-SMS-PAIR-"))

        val device = syncProtocol.validateAndPairDevice(token, "Windows Desktop App", "WINDOWS")
        assertNotNull(device)
        assertEquals("Windows Desktop App", device?.deviceName)

        val packet = syncProtocol.sendSyncPacket(device!!.deviceId, "SMS_RECEIVED", "{\"text\":\"Hello\"}")
        assertNotNull(packet.packetId)

        val heartbeatOk = syncProtocol.handleHeartbeat(device.deviceId)
        assertTrue(heartbeatOk)
    }

    @Test
    fun testInternalApiGatewayRateLimitingAndRbac() {
        val gateway = InternalApiGateway()

        gateway.registerApiKey(
            ApiKeyProfile(
                keyId = "KEY_ENTERPRISE_1",
                secretHash = "HASH_SECRET",
                clientName = "ERP System",
                allowedPermissions = listOf("SMS_SEND"),
                maxRequestsPerMinute = 2
            )
        )

        val request1 = ApiGatewayRequest(
            apiKeyId = "KEY_ENTERPRISE_1",
            endpoint = "/api/v1/sms/send",
            httpMethod = "POST",
            payloadJson = "{\"to\":\"09123456789\",\"body\":\"Test\"}",
            signature = "SIG1"
        )

        val resp1 = gateway.handleRequest(request1)
        assertEquals(200, resp1.statusCode)

        val request2 = ApiGatewayRequest(
            apiKeyId = "KEY_ENTERPRISE_1",
            endpoint = "/api/v1/sms/send",
            httpMethod = "POST",
            payloadJson = "{\"to\":\"09123456789\",\"body\":\"Test 2\"}",
            signature = "SIG2"
        )
        val resp2 = gateway.handleRequest(request2)
        assertEquals(200, resp2.statusCode)

        // 3rd request exceeds limit of 2
        val request3 = ApiGatewayRequest(
            apiKeyId = "KEY_ENTERPRISE_1",
            endpoint = "/api/v1/sms/send",
            httpMethod = "POST",
            payloadJson = "{\"to\":\"09123456789\",\"body\":\"Test 3\"}",
            signature = "SIG3"
        )
        val resp3 = gateway.handleRequest(request3)
        assertEquals(429, resp3.statusCode)
    }

    @Test
    fun testEnterpriseBiEngineMetricsRefresh() {
        val biEngine = EnterpriseBIEngine()
        val report = biEngine.refreshMetrics()

        assertTrue(report.sentiment.overallScore > 0f)
        assertTrue(report.roi.estimatedRevenueDollars > 0)
        assertTrue(report.efficiency.avgResponseTimeMinutes < 60)
    }

    @Test
    fun testReportEngineGeneration() = runBlocking {
        val reportEngine = ReportEngine()

        val pdfResult = reportEngine.generateReport(
            title = "گزارش بازدهی کمپین‌های زمستانه",
            category = ReportCategory.CAMPAIGN_ROI,
            format = ReportFormat.PDF
        )

        assertNotNull(pdfResult.reportId)
        assertTrue(pdfResult.payload.contains("PDF DOCUMENT"))

        val csvResult = reportEngine.generateReport(
            title = "تحلیل ریزش مشتریان",
            category = ReportCategory.CHURN_RISK,
            format = ReportFormat.EXCEL_CSV
        )
        assertTrue(csvResult.payload.contains("Category"))
    }

    @Test
    fun testEnterpriseSecurityCenterAuditAndPosture() {
        val securityCenter = EnterpriseSecurityCenter()

        val event = securityCenter.logSecurityEvent(
            eventType = "UNAUTHORIZED_API_ATTEMPT",
            severity = SecuritySeverity.HIGH,
            sourceModule = "InternalApiGateway",
            description = "Rate limit threshold breached from IP 192.168.1.50"
        )

        assertNotNull(event.eventId)
        assertTrue(securityCenter.verifyLogIntegrity(event))

        val posture = securityCenter.calculateSecurityPosture()
        assertTrue(posture.overallScore in 0..100)
        assertTrue(posture.aesGcmEncryptionActive)
    }
}
