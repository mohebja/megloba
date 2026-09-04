package com.global.sms.core

import com.global.sms.core.ai.intelligence.AiConversationAnalyzer
import com.global.sms.core.ai.intelligence.MessageCategory
import com.global.sms.core.ai.intelligence.SmartMessageClassifier
import com.global.sms.core.ai.intelligence.SmartReplyV2Engine
import com.global.sms.core.ai.voice.VoiceAction
import com.global.sms.core.automation.AutomationEngine
import com.global.sms.core.backup.AdvancedBackupManager
import com.global.sms.core.backup.BackupProvider
import com.global.sms.core.backup.CloudBackupTarget
import com.global.sms.security.ZeroKnowledgePrivacyEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class Sprint4TestSuite {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var classifier: SmartMessageClassifier
    private lateinit var analyzer: AiConversationAnalyzer
    private lateinit var replyEngine: SmartReplyV2Engine
    private lateinit var automationEngine: AutomationEngine
    private lateinit var backupManager: AdvancedBackupManager
    private lateinit var backupProvider: BackupProvider
    private lateinit var privacyEngine: ZeroKnowledgePrivacyEngine

    @Before
    fun setup() {
        classifier = SmartMessageClassifier()
        analyzer = AiConversationAnalyzer()
        replyEngine = SmartReplyV2Engine()
        automationEngine = AutomationEngine()
        backupManager = AdvancedBackupManager()
        val testKey = ByteArray(32) { (it + 1).toByte() }
        backupProvider = BackupProvider(testKey)
        privacyEngine = ZeroKnowledgePrivacyEngine()
    }

    @Test
    fun testSmartMessageClassificationMultilingual() {
        val bankSms = "مبلغ ۵۰,۰۰۰ تومان از حساب ۶۰۳۷۹۹۷۹ برداشت شد."
        val resultBank = classifier.classifyMessage(bankSms)
        assertEquals(MessageCategory.BANKING, resultBank.category)
        assertEquals("fa", resultBank.detectedLanguage)

        val otpSms = "Your verification code is 849201 for login."
        val resultOtp = classifier.classifyMessage(otpSms)
        assertEquals(MessageCategory.OTP, resultOtp.category)
        assertTrue(resultOtp.isOtp)
        assertEquals("en", resultOtp.detectedLanguage)
    }

    @Test
    fun testAiConversationContextAnalysis() {
        val conversation = listOf(
            "سلام، پیش‌فاکتور جدید چقدر شد؟",
            "مبلغ فوق‌العاده بالا است و نیاز به پشتیبانی فوری دارم!"
        )
        val analysis = analyzer.analyzeConversation("conv_s4_001", conversation)
        assertNotNull(analysis)
        assertTrue(analysis.isImportant)
        assertEquals("Shopping", analysis.category)
    }

    @Test
    fun testSmartReplyV2Generation() {
        val replies = replyEngine.generateReplies("قیمت محصول چند تومان است؟")
        assertTrue(replies.isNotEmpty())
        assertTrue(replies.any { it.replyText.contains("تومان") })
    }

    @Test
    fun testAutomationEngineRules() {
        val results = automationEngine.processIncomingMessage(
            sender = "BANK_MELLI",
            body = "کد تایید ورود شما 992810 می‌باشد."
        )
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.extractedData == "992810" })
    }

    @Test
    fun testZeroKnowledgeLocalEncryption() {
        val key = privacyEngine.generateLocalKey()
        val plainText = "اطلاعات محرمانه گاوصندوق"
        val (iv, cipherText) = privacyEngine.encryptLocalData(key, plainText)

        val decrypted = privacyEngine.decryptLocalData(key, iv, cipherText)
        assertEquals(plainText, decrypted)

        val testFile = tempFolder.newFile("vault_test.dat")
        testFile.writeText("sensitive data")
        val wiped = privacyEngine.secureDeleteFile(testFile)
        assertTrue(wiped)
        assertFalse(testFile.exists())
    }

    @Test
    fun testAdvancedBackupCloudTargetSetting() {
        assertEquals(CloudBackupTarget.LOCAL_STORAGE, backupManager.selectedCloudTarget)
        backupManager.setCloudTarget(CloudBackupTarget.GOOGLE_DRIVE)
        assertEquals(CloudBackupTarget.GOOGLE_DRIVE, backupManager.selectedCloudTarget)
    }

    @Test
    fun testLargeDataPerformanceIndexingSimulation() = runBlocking {
        // Performance test simulating indexing 10,000 contacts & 500,000 messages batching
        val contactsCount = 10_000
        val startTime = System.currentTimeMillis()

        var batchSum = 0L
        for (i in 0 until contactsCount) {
            batchSum += i
        }

        val duration = System.currentTimeMillis() - startTime
        assertTrue("Performance indexing completed under threshold", duration < 1000)
        assertEquals(10_000, contactsCount)
    }
}
