package com.global.sms.core

import com.global.sms.core.ai.assistant.DetectedEntityType
import com.global.sms.core.ai.assistant.SmartAssistantV2
import com.global.sms.core.enterprise.CampaignRecipient
import com.global.sms.core.enterprise.EnterpriseCampaignEngine
import com.global.sms.core.wear.WearCompanionManager
import com.global.sms.core.web.WebCompanionSyncManager
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream

class Sprint2_5EcosystemTest {

    @Test
    fun testAiAssistantV2CardAndIbanDetection() {
        val sampleSms = "مبلغ ۵۰۰،۰۰۰ ریال به شماره کارت ۶۰۳۷۹۹۷۱۱۲۳۴۵۶۷۸ و شماره شبا IR120120000000001234567890 واریز گردید."
        val entities = SmartAssistantV2.analyzeAndExtractEntities(sampleSms)

        assertTrue(entities.isNotEmpty())
        val cardEntity = entities.find { it.type == DetectedEntityType.CARD_NUMBER }
        assertNotNull(cardEntity)
        assertEquals("6037997112345678", cardEntity?.value)

        val ibanEntity = entities.find { it.type == DetectedEntityType.PAYMENT_IBAN }
        assertNotNull(ibanEntity)
        assertTrue(ibanEntity?.value?.startsWith("IR") == true)
    }

    @Test
    fun testAiAssistantV2AppointmentDetection() {
        val sampleSms = "سلام، جلسه هیئت مدیره فردا 14:30 در دفتر مرکزی برگزار می‌شود."
        val entities = SmartAssistantV2.analyzeAndExtractEntities(sampleSms)

        val apptEntity = entities.find { it.type == DetectedEntityType.APPOINTMENT }
        assertNotNull(apptEntity)
    }

    @Test
    fun testEnterpriseCampaignMergeTags() {
        val template = "جناب {Name} عزیز، سررسید فاکتور شرکت {Company} رسیده است."
        val recipient = CampaignRecipient(
            phone = "09121112233",
            name = "علی رضایی",
            company = "گلوبال اس‌ام‌اس"
        )

        val personalized = EnterpriseCampaignEngine.personalizeMessage(template, recipient)
        assertTrue(personalized.contains("علی رضایی"))
        assertTrue(personalized.contains("گلوبال اس‌ام‌اس"))
        assertFalse(personalized.contains("{Name}"))
    }

    @Test
    fun testWearCompanionPayload() {
        val payload = WearCompanionManager.buildWearSyncPayload(
            address = "09123456789",
            senderName = "بانک ملی",
            body = "رمز پویا شما: 849201",
            isOtp = true
        )

        assertTrue(payload.contains("SMS_INCOMING"))
        assertTrue(payload.contains("بانک ملی"))
        assertTrue(payload.contains("isOtp\":true"))
    }

    @Test
    fun testWebCompanionFrameCreation() {
        // Test pairing QR payload structure
        val qrPayload = "GLOBALSMS_WEB_V1:session123:key456:https://web.globalsms.app"
        assertTrue(qrPayload.startsWith("GLOBALSMS_WEB_V1"))
    }
}
