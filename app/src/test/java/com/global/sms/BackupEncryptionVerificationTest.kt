package com.global.sms

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.global.sms.core.backup.BackupProvider
import com.global.sms.core.backup.ProfessionalBackupEngine
import com.global.sms.data.entity.MessageEntity
import com.global.sms.security.backup.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackupEncryptionVerificationTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
    }

    @Test
    fun testRealBackupEncryptionAndDecryptionRoundTrip() {
        val backupFile = tempFolder.newFile("test_secure_backup.gsms")
        val password = "SuperSecretEnterprisePass123!"

        val sampleMessages = listOf(
            BackupMessageItem(
                id = 101L,
                threadId = 1L,
                address = "09123456789",
                body = "پیام محرمانه سازمانی شماره یک",
                date = 1700000000000L,
                type = 1,
                read = 1,
                status = 0
            ),
            BackupMessageItem(
                id = 102L,
                threadId = 1L,
                address = "09123456789",
                body = "کد تأیید تراکنش مالی: 984120",
                date = 1700000010000L,
                type = 2,
                read = 1,
                status = 0
            )
        )

        val originalModel = EnterpriseBackupModel(
            version = 1,
            timestamp = System.currentTimeMillis(),
            appVersion = "2.0.0",
            messages = sampleMessages,
            categories = listOf("BANKING", "ENTERPRISE"),
            settings = mapOf("security_strict" to "true", "biometric_enabled" to "true")
        )

        // 1. Create encrypted backup
        val generatedFile = EncryptedBackupManager.createEncryptedBackup(
            context = context,
            model = originalModel,
            password = password,
            targetFile = backupFile
        )

        assertTrue(generatedFile.exists())
        assertTrue("Backup file size should be substantial", generatedFile.length() > 64)

        // 2. Verify file is NOT literal placeholder or plaintext
        val rawFileBytes = generatedFile.readBytes()
        val rawFileString = String(rawFileBytes, Charsets.ISO_8859_1)

        assertFalse("Backup file must NEVER be literal EXPORT_OK", rawFileString.contains("EXPORT_OK"))
        assertFalse("Backup file must NEVER contain raw plaintext message body", rawFileString.contains("پیام محرمانه سازمانی"))
        assertFalse("Backup file must NEVER contain raw plaintext OTP", rawFileString.contains("984120"))

        // Verify GSMS magic header is at the start
        val magic = String(rawFileBytes.sliceArray(0 until 4), Charsets.UTF_8)
        assertEquals("GSMS", magic)

        // 3. Verify successful decryption with correct password
        val restoredModel = EncryptedBackupManager.restoreEncryptedBackup(generatedFile, password)
        assertEquals(2, restoredModel.messages.size)
        assertEquals("پیام محرمانه سازمانی شماره یک", restoredModel.messages[0].body)
        assertEquals("کد تأیید تراکنش مالی: 984120", restoredModel.messages[1].body)
        assertEquals(listOf("BANKING", "ENTERPRISE"), restoredModel.categories)
        assertEquals("true", restoredModel.settings["security_strict"])

        // 4. Verify decryption fails with incorrect password
        var failedAsExpected = false
        try {
            EncryptedBackupManager.restoreEncryptedBackup(generatedFile, "WrongPassword!")
        } catch (e: Exception) {
            failedAsExpected = true
        }
        assertTrue("Decryption must fail when an incorrect password is provided", failedAsExpected)
    }

    @Test
    fun testBackupManagerEntitiesConversionAndRoundTrip() {
        val testEntities = listOf(
            MessageEntity(
                id = 201L,
                threadId = 5L,
                address = "09987654321",
                body = "تست پشتیبان‌گیری چندگانه",
                timestamp = 1700000050000L,
                type = 1,
                isRead = true,
                deliveryStatus = 0
            )
        )

        val pass = "StrongPassword456$"
        val file = BackupManager.exportBackup(context, testEntities, pass)
        assertTrue(file.exists())

        val rawBytes = file.readBytes()
        assertFalse(String(rawBytes, Charsets.ISO_8859_1).contains("EXPORT_OK"))

        val restoredEntities = BackupManager.importBackup(file, pass)
        assertNotNull(restoredEntities)
        assertEquals(1, restoredEntities?.size)
        assertEquals("تست پشتیبان‌گیری چندگانه", restoredEntities?.first()?.body)
        assertEquals(true, restoredEntities?.first()?.isRead)

        // Null returned on wrong password
        val failedImport = BackupManager.importBackup(file, "BadPassword")
        assertNull(failedImport)
    }

    @Test
    fun testProfessionalBackupEngineInspection() {
        val engine = ProfessionalBackupEngine()
        val backupFile = tempFolder.newFile("prof_backup.gsms")
        val pass = "EnterprisePass789#"
        val payload = "{\"body\":\"پیام اول\",\"phoneNumber\":\"09121111111\"}"

        val header = engine.exportEncryptedBackup(backupFile, payload, pass)
        assertNotNull(header.integrityHashSha256)

        val inspection = engine.inspectBackup(backupFile, pass)
        assertTrue(inspection.isValid)
        assertEquals(1, inspection.messageCount)
        assertEquals(1, inspection.contactCount)

        val badInspection = engine.inspectBackup(backupFile, "WrongPassword")
        assertFalse(badInspection.isValid)
    }
}
