package com.global.sms.core

import com.global.sms.core.ai.assistant.AiCommunicationAssistant
import com.global.sms.core.ai.assistant.CustomerIntent
import com.global.sms.core.backup.BackupProvider
import com.global.sms.core.campaign.CampaignRepository
import com.global.sms.core.contact.crm.ContactCRMRepository
import com.global.sms.core.contact.crm.ContactProfile
import com.global.sms.core.contact.crm.ContactTag
import com.global.sms.core.contact.crm.ContactTagType
import com.global.sms.core.group.GroupCampaignManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class Sprint3EnterpriseTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var crmRepository: ContactCRMRepository
    private lateinit var campaignRepository: CampaignRepository
    private lateinit var aiAssistant: AiCommunicationAssistant
    private lateinit var backupProvider: BackupProvider

    @Before
    fun setup() {
        crmRepository = ContactCRMRepository()
        campaignRepository = CampaignRepository()
        aiAssistant = AiCommunicationAssistant()
        val testKey = ByteArray(32) { (it + 1).toByte() }
        backupProvider = BackupProvider(testKey)
    }

    @Test
    fun testContactCRMProfileManagement() = runBlocking {
        val newProfile = ContactProfile(
            id = "test_crm_1",
            fullName = "سار احمدی",
            phoneNumbers = listOf("09129999999"),
            primaryPhoneNumber = "09129999999",
            company = "شرکت پارت",
            tags = listOf(ContactTag("t9", "VIP", ContactTagType.VIP, "#A855F7")),
            isVip = true
        )

        crmRepository.saveContactProfile(newProfile)

        val retrieved = crmRepository.getContactById("test_crm_1")
        assertNotNull(retrieved)
        assertEquals("سار احمدی", retrieved?.fullName)
        assertTrue(retrieved?.isVip == true)

        val foundByPhone = crmRepository.findContactByPhone("09129999999")
        assertNotNull(foundByPhone)
        assertEquals("test_crm_1", foundByPhone?.id)
    }

    @Test
    fun testCampaignCreationAndExecution() = runBlocking {
        val campaign = campaignRepository.createCampaign(
            name = "کمپین تست unit",
            recipients = listOf("09121111111", "09122222222"),
            templateBody = "سلام {name} عزیز",
            scheduledTime = System.currentTimeMillis(),
            simSlot = 0
        )

        assertNotNull(campaign.id)
        assertEquals("QUEUED", campaign.status)

        val report = campaignRepository.runCampaign(campaign.id)
        assertEquals(2, report.totalRecipients)
        assertEquals(2, report.deliveredCount)
        assertEquals(100f, report.successRatePercentage, 0.01f)
    }

    @Test
    fun testAiCommunicationAssistantOnDeviceProcessing() {
        val messages = listOf(
            "سلام، قیمت فاکتور جدید چقدر است؟",
            "لطفاً هزینه ارسال پیش‌فاکتور را بررسی بفرمایید."
        )

        val (insight, suggestions) = aiAssistant.processConversation("conv_test_101", messages)

        assertEquals(CustomerIntent.PRICE_INQUIRY, insight.detectedIntent)
        assertTrue(suggestions.isNotEmpty())
        assertTrue(suggestions.any { it.language == "fa" })
        assertTrue(suggestions.any { it.language == "en" })
    }

    @Test
    fun testEncryptedBackupAndIntegrityVerification() {
        val backupFile = tempFolder.newFile("test_backup.enc")
        val jsonPayload = "{\"messages\":[{\"id\":101,\"body\":\"تست بکاپ\"}]}"

        val header = backupProvider.createEncryptedBackup(backupFile, jsonPayload)
        assertNotNull(header.integrityHashSha256)
        assertTrue(backupFile.length() > 0)

        val restoredJson = backupProvider.restoreEncryptedBackup(
            backupFile = backupFile,
            expectedHash = header.integrityHashSha256
        )

        assertEquals(jsonPayload, restoredJson)
    }
}
