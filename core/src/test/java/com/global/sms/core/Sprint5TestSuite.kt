package com.global.sms.core

import com.global.sms.core.ai.translation.OnDeviceTranslationEngine
import com.global.sms.core.ai.translation.SupportedLanguage
import com.global.sms.core.ai.v2.AiCommunicationAssistantV2
import com.global.sms.core.ai.v2.DetectedEventType
import com.global.sms.core.automation.AutomationTemplateRepository
import com.global.sms.core.backup.BackupScheduleInterval
import com.global.sms.core.backup.ProfessionalBackupEngine
import com.global.sms.core.message.AdvancedMessageOperationsEngine
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class Sprint5TestSuite {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var aiV2Assistant: AiCommunicationAssistantV2
    private lateinit var templateRepo: AutomationTemplateRepository
    private lateinit var translationEngine: OnDeviceTranslationEngine
    private lateinit var messageOpsEngine: AdvancedMessageOperationsEngine
    private lateinit var backupEngine: ProfessionalBackupEngine

    @Before
    fun setup() {
        aiV2Assistant = AiCommunicationAssistantV2()
        templateRepo = AutomationTemplateRepository()
        translationEngine = OnDeviceTranslationEngine()
        messageOpsEngine = AdvancedMessageOperationsEngine()
        backupEngine = ProfessionalBackupEngine()
    }

    @Test
    fun testAiAssistantV2PaymentDetection() {
        val messages = listOf(
            "سلام، فاکتور خرید دوره به مبلغ ۳,۵۰۰,۰۰۰ تومان صادر گردید.",
            "لطفاً تا فردا واریز کنید."
        )
        val insight = aiV2Assistant.analyzeConversationV2("conv_s5_1", messages)

        assertEquals(DetectedEventType.PAYMENT_REMINDER, insight.detectedEvent)
        assertTrue(insight.isUrgent)
        assertEquals("fa", insight.detectedLanguage)
        assertNotNull(insight.extractedAmount)
        assertTrue(insight.suggestedActions.isNotEmpty())
    }

    @Test
    fun testAiAssistantV2AppointmentMultilingual() {
        val messages = listOf("Meeting scheduled for tomorrow at 10 AM at office.")
        val insight = aiV2Assistant.analyzeConversationV2("conv_s5_2", messages)

        assertEquals(DetectedEventType.APPOINTMENT, insight.detectedEvent)
        assertEquals("en", insight.detectedLanguage)
    }

    @Test
    fun testAutomationMarketplaceTemplates() {
        val templates = templateRepo.getMarketplaceTemplates()
        assertTrue(templates.isNotEmpty())
        assertTrue(templates.any { it.id == "tpl_bank_auto" })
        assertTrue(templates.any { it.isPopular })
    }

    @Test
    fun testOnDeviceTranslationEngine() {
        val text = "your verification code is 123456"
        val result = translationEngine.translateMessage(text, SupportedLanguage.PERSIAN)

        assertEquals(SupportedLanguage.PERSIAN, result.targetLanguage)
        assertTrue(result.translatedText.contains("کد تایید"))
        assertTrue(result.isOfflineProcessed)
    }

    @Test
    fun testAdvancedMessageOperations() {
        val msgId = "msg_001"
        assertTrue(messageOpsEngine.togglePin(msgId))
        assertTrue(messageOpsEngine.toggleStar(msgId))
        assertTrue(messageOpsEngine.toggleBookmark(msgId))

        val noteMeta = messageOpsEngine.addUserNote(msgId, "پیگیری چک خرداد")
        assertEquals("پیگیری چک خرداد", noteMeta.userNote)

        val taskMeta = messageOpsEngine.convertToTask(msgId, "تماس با پشتیبانی")
        assertTrue(taskMeta.isConvertedToTask)

        val exportDir = tempFolder.newFolder("exports")
        val exportResult = messageOpsEngine.exportMessageToText("09120000000", "متن آزمایش", exportDir)
        assertTrue(exportResult.file.exists())
        assertEquals("TXT", exportResult.format)
    }

    @Test
    fun testProfessionalBackupEngineWithPassword() {
        backupEngine.setSchedule(BackupScheduleInterval.WEEKLY)
        assertEquals(BackupScheduleInterval.WEEKLY, backupEngine.scheduleInterval)

        val jsonBackupPayload = """
            {
                "messages": [
                    {"id": "msg_001", "body": "کد ورود شما: 123456"},
                    {"id": "msg_002", "body": "واریز مبلغ ۵۰۰,۰۰۰ ریال"}
                ],
                "contacts": [
                    {"contactId": "c_001", "fullName": "علی رضایی", "phoneNumber": "09121112233"}
                ]
            }
        """.trimIndent()
        val password = "StrongPassword2026!"
        val backupFile = tempFolder.newFile("backup_enc.dat")

        val success = backupEngine.createEncryptedBackupWithPassword(jsonBackupPayload, password, backupFile)
        assertTrue(success)

        val decrypted = backupEngine.decryptBackupWithPassword(backupFile, password)
        assertEquals(jsonBackupPayload, decrypted)

        val wrongDecrypted = backupEngine.decryptBackupWithPassword(backupFile, "WrongPassword")
        assertNull(wrongDecrypted)

        // 1. Inspect with correct password: real decryption and verified counts
        val verifiedPreview = backupEngine.inspectBackupForRestorePreview(backupFile, password)
        assertTrue(verifiedPreview.isValid)
        assertFalse(verifiedPreview.countsAreEstimated)
        assertEquals(2, verifiedPreview.messageCountToRestore)
        assertEquals(1, verifiedPreview.contactCountToRestore)

        // 2. Inspect with wrong password: fails verification with zero restore counts
        val failedPreview = backupEngine.inspectBackupForRestorePreview(backupFile, "WrongPassword")
        assertFalse(failedPreview.isValid)
        assertFalse(failedPreview.countsAreEstimated)
        assertEquals(0, failedPreview.messageCountToRestore)
        assertEquals(0, failedPreview.contactCountToRestore)

        // 3. Inspect without password: size-based estimate path marked with countsAreEstimated = true
        val estimatedPreview = backupEngine.inspectBackupForRestorePreview(backupFile, password = null)
        assertTrue(estimatedPreview.isValid)
        assertTrue(estimatedPreview.countsAreEstimated)
        assertTrue(estimatedPreview.messageCountToRestore > 0)
    }
}
