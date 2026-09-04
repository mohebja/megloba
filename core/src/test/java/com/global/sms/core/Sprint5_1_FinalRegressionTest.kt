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

class Sprint5_1_FinalRegressionTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var aiAssistant: AiCommunicationAssistantV2
    private lateinit var automationRepo: AutomationTemplateRepository
    private lateinit var translationEngine: OnDeviceTranslationEngine
    private lateinit var messageOps: AdvancedMessageOperationsEngine
    private lateinit var backupEngine: ProfessionalBackupEngine

    @Before
    fun setup() {
        aiAssistant = AiCommunicationAssistantV2()
        automationRepo = AutomationTemplateRepository()
        translationEngine = OnDeviceTranslationEngine()
        messageOps = AdvancedMessageOperationsEngine()
        backupEngine = ProfessionalBackupEngine()
    }

    @Test
    fun testFirstRunAndDefaultSmsScenario() {
        // Verify default SMS role initialization parameters
        val templates = automationRepo.getMarketplaceTemplates()
        assertNotNull(templates)
        assertTrue(templates.size >= 4)

        val otpTemplate = templates.find { it.id == "tpl_otp_copy" }
        assertNotNull(otpTemplate)
        assertEquals("امنیت و OTP", otpTemplate?.categoryName)
    }

    @Test
    fun testHistoricalImportAndAiClassificationScenario() {
        val importedBankSms = listOf(
            "بانک ملی: واریز به حساب ۱۲۳۴ بمبلغ ۵,۰۰۰,۰۰۰ ریال. موجودی: ۴۵,۰۰۰,۰۰۰ ریال."
        )
        val insight = aiAssistant.analyzeConversationV2("conv_bank_01", importedBankSms)

        assertEquals(DetectedEventType.PAYMENT_REMINDER, insight.detectedEvent)
        assertEquals("fa", insight.detectedLanguage)
        assertNotNull(insight.extractedAmount)
        assertTrue(insight.suggestedActions.isNotEmpty())
    }

    @Test
    fun testMessageOperationsPinStarNoteTaskExportScenario() {
        val msgId = "msg_reg_51_001"

        // Pin, Star, Bookmark
        assertTrue(messageOps.togglePin(msgId))
        assertTrue(messageOps.toggleStar(msgId))
        assertTrue(messageOps.toggleBookmark(msgId))

        // Note & Task Conversion
        val metaWithNote = messageOps.addUserNote(msgId, "یادداشت پیگیری مالی")
        assertEquals("یادداشت پیگیری مالی", metaWithNote.userNote)

        val metaWithTask = messageOps.convertToTask(msgId, "واریز تا پایان هفته")
        assertTrue(metaWithTask.isConvertedToTask)
        assertEquals("واریز تا پایان هفته", metaWithTask.taskTitle)

        // Text & PDF Export
        val exportDir = tempFolder.newFolder("reg_exports")
        val txtExport = messageOps.exportMessageToText("09121112233", "تست صادرات پیامک", exportDir)
        assertTrue(txtExport.file.exists())
        assertEquals("TXT", txtExport.format)

        val pdfExport = messageOps.exportMessageToPdfSimulated("09121112233", "تست صادرات PDF", exportDir)
        assertTrue(pdfExport.file.exists())
        assertEquals("PDF", pdfExport.format)
    }

    @Test
    fun testOfflineTranslationEngineScenario() {
        val engText = "your verification code is 987654"
        val translation = translationEngine.translateMessage(engText, SupportedLanguage.PERSIAN)

        assertEquals(SupportedLanguage.PERSIAN, translation.targetLanguage)
        assertTrue(translation.translatedText.contains("کد تایید"))
        assertTrue(translation.isOfflineProcessed)
    }

    @Test
    fun testEncryptedBackupAndRestoreInspectionScenario() {
        backupEngine.setSchedule(BackupScheduleInterval.DAILY)
        assertEquals(BackupScheduleInterval.DAILY, backupEngine.scheduleInterval)

        val payload = """
            {
                "version": "1.0",
                "messages": [
                    {"id": "msg_001", "body": "کد ورود: 8872"},
                    {"id": "msg_002", "body": "جلسه کاری فردا ساعت ۱۰"},
                    {"id": "msg_003", "body": "پیش‌فاکتور ارسال شد"}
                ],
                "contacts": [
                    {"contactId": "c_001", "fullName": "مریم حسینی", "phoneNumber": "09123334455"},
                    {"contactId": "c_002", "fullName": "حمید کاظمی", "phoneNumber": "09124445566"}
                ]
            }
        """.trimIndent()
        val password = "ReleaseCandidatePassword2026!"
        val backupFile = tempFolder.newFile("sprint5_1_backup.enc")

        val isEncrypted = backupEngine.createEncryptedBackupWithPassword(payload, password, backupFile)
        assertTrue(isEncrypted)

        val decryptedPayload = backupEngine.decryptBackupWithPassword(backupFile, password)
        assertEquals(payload, decryptedPayload)

        // 1. Password-verified restore inspection
        val verifiedPreview = backupEngine.inspectBackupForRestorePreview(backupFile, password)
        assertTrue(verifiedPreview.isValid)
        assertFalse(verifiedPreview.countsAreEstimated)
        assertEquals(3, verifiedPreview.messageCountToRestore)
        assertEquals(2, verifiedPreview.contactCountToRestore)

        // 2. Wrong-password inspection
        val failedPreview = backupEngine.inspectBackupForRestorePreview(backupFile, "BadPassword123!")
        assertFalse(failedPreview.isValid)
        assertFalse(failedPreview.countsAreEstimated)
        assertEquals(0, failedPreview.messageCountToRestore)
        assertEquals(0, failedPreview.contactCountToRestore)

        // 3. Fallback estimate without password
        val estimatedPreview = backupEngine.inspectBackupForRestorePreview(backupFile, password = null)
        assertTrue(estimatedPreview.isValid)
        assertTrue(estimatedPreview.countsAreEstimated)
        assertTrue(estimatedPreview.messageCountToRestore > 0)
    }
}
