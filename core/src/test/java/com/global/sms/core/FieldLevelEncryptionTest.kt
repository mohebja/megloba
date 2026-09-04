package com.global.sms.core

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.global.sms.core.security.FieldEncryptionManager
import com.global.sms.core.security.ZeroTrustSecurityLayer
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.data.entity.ConversationEntity
import com.global.sms.data.entity.MessageCategory
import com.global.sms.data.entity.MessageEntity
import com.global.sms.data.entity.MessageStatus
import com.global.sms.data.entity.MessageType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class FieldLevelEncryptionTest {

    private lateinit var database: GlobalSmsDatabase
    private lateinit var context: Context
    private var dbFile: File? = null

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        dbFile = File(context.filesDir, "test_field_enc.db")
        if (dbFile!!.exists()) dbFile!!.delete()

        database = Room.databaseBuilder(context, GlobalSmsDatabase::class.java, dbFile!!.absolutePath)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
        dbFile?.delete()
    }

    @Test
    fun testFieldEncryptionPlaintextIsNotStoredInRawSqliteRow() = runBlocking {
        val plainBody = "رمز یکبار مصرف شما: 948201. از اشتراک‌گذاری آن خودداری کنید."
        val address = "+989123456789"

        // 1. Prepare message entity encrypted with FieldEncryptionManager
        val rawMessage = MessageEntity(
            id = 101L,
            threadId = 1L,
            address = address,
            body = plainBody,
            timestamp = 1700000000000L,
            category = MessageCategory.OTP,
            otpCode = "948201",
            deliveryStatus = MessageStatus.DELIVERED.code,
            type = MessageType.INBOX.code
        )
        val encryptedMessage = FieldEncryptionManager.encryptMessage(rawMessage)

        // 2. Insert into Room DAO
        database.messageDao().insertMessage(encryptedMessage)

        // 3. Query raw underlying SQLite row directly via SupportSQLiteDatabase to bypass DAO/Application layer
        val cursor = database.openHelper.readableDatabase.query(
            "SELECT id, address, body, isEncrypted, category, otpCode FROM messages WHERE id = 101"
        )
        assertTrue("Row must exist in SQLite", cursor.moveToFirst())

        val rawDbBody = cursor.getString(cursor.getColumnIndexOrThrow("body"))
        val rawDbIsEncrypted = cursor.getInt(cursor.getColumnIndexOrThrow("isEncrypted"))
        val rawDbAddress = cursor.getString(cursor.getColumnIndexOrThrow("address"))
        val rawDbCategory = cursor.getString(cursor.getColumnIndexOrThrow("category"))
        val rawDbOtpCode = cursor.getString(cursor.getColumnIndexOrThrow("otpCode"))
        cursor.close()

        // 4. Assertions on raw SQLite disk representation:
        // Plaintext must NOT appear in the database column
        assertFalse("Raw SQLite body column must NOT contain plaintext message", rawDbBody.contains("رمز یکبار مصرف"))
        assertFalse("Raw SQLite body column must NOT contain OTP code plaintext", rawDbBody.contains("948201"))
        assertTrue("Raw SQLite body column must start with enc:v1: prefix", rawDbBody.startsWith("enc:v1:"))
        assertEquals(1, rawDbIsEncrypted)

        // Metadata needed for queries/indexing remains queryable
        assertEquals(address, rawDbAddress)
        assertEquals("OTP", rawDbCategory)
        assertEquals("948201", rawDbOtpCode)

        // 5. Assertions on DAO read path:
        val readFromDao = database.messageDao().getMessageById(101L)
        assertNotNull(readFromDao)
        val decryptedMessage = FieldEncryptionManager.decryptMessage(readFromDao!!)
        assertEquals(plainBody, decryptedMessage.body)
    }

    @Test
    fun testKeyPersistenceSurvivesProcessRestartSimulation() = runBlocking {
        val originalSecretMessage = "اطلاعات مالی بسیار محرمانه برای انتقال وجه ۱۰۰,۰۰۰,۰۰۰ ریال"
        val messageId = 777L

        // 1. Encrypt and insert into database
        val originalMessage = MessageEntity(
            id = messageId,
            threadId = 10L,
            address = "09121112233",
            body = originalSecretMessage,
            timestamp = 1700000000000L
        )
        val encryptedEntity = FieldEncryptionManager.encryptMessage(originalMessage)
        database.messageDao().insertMessage(encryptedEntity)

        // Verify the persistent master key is non-null
        val initialKey = com.global.sms.security.keystore.KeyStoreManager.getOrCreateMasterKey()
        assertNotNull(initialKey)

        // 2. Simulate complete process restart: close database instance and re-open from disk
        database.close()

        val restartedDatabase = Room.databaseBuilder(context, GlobalSmsDatabase::class.java, dbFile!!.absolutePath)
            .allowMainThreadQueries()
            .build()

        // 3. Verify that re-reading master key from storage yields the exact same secret key bytes
        val reloadedKey = com.global.sms.security.keystore.KeyStoreManager.getOrCreateMasterKey()
        assertNotNull(reloadedKey)
        assertArrayEquals("Reloaded master key must match initial key", initialKey.encoded, reloadedKey.encoded)

        // 4. Retrieve from new DB connection and decrypt using persistent Android KeyStore master key
        val retrievedFromDisk = restartedDatabase.messageDao().getMessageById(messageId)
        assertNotNull("Record must persist across restart", retrievedFromDisk)
        assertTrue("Stored payload on disk is still encrypted", retrievedFromDisk!!.body.startsWith("enc:v1:"))

        // KeyStore master key decrypts it perfectly after restart
        val decrypted = FieldEncryptionManager.decryptMessage(retrievedFromDisk)
        assertEquals(originalSecretMessage, decrypted.body)

        restartedDatabase.close()
    }

    @Test
    fun testTamperedCiphertextFailsClosedWithSecurityException() {
        val tamperedPayload = "enc:v1:dGFtcGVyZWRfYmFzZTY0X2ludmFsaWRfcGF5bG9hZA=="

        assertThrows(SecurityException::class.java) {
            FieldEncryptionManager.decrypt(tamperedPayload)
        }
    }

    @Test
    fun testLegacyPlaintextPassesThroughSafely() {
        val legacyPlaintext = "پیام قدیمی کاربر قبل از فعال‌سازی رمزنگاری"
        val decrypted = FieldEncryptionManager.decrypt(legacyPlaintext)
        assertEquals(legacyPlaintext, decrypted)
    }

    @Test
    fun testConversationContactNameAndSnippetEncryption() = runBlocking {
        val rawContactName = "سردار احمدی"
        val rawSnippet = "سلام مهندس، جلسه فردا ساعت ۸ صبح است."
        val threadId = 55L

        val conversation = ConversationEntity(
            threadId = threadId,
            address = "+989351112233",
            contactName = rawContactName,
            lastMessage = rawSnippet,
            lastTimestamp = 1700000000000L,
            unreadCount = 2,
            category = MessageCategory.WORK
        )

        val encryptedConv = FieldEncryptionManager.encryptConversation(conversation)
        database.conversationDao().insertOrUpdateConversation(encryptedConv)

        // Raw SQLite inspection
        val cursor = database.openHelper.readableDatabase.query(
            "SELECT threadId, address, contactName, lastMessage, category FROM conversations WHERE threadId = 55"
        )
        assertTrue(cursor.moveToFirst())
        val dbContactName = cursor.getString(cursor.getColumnIndexOrThrow("contactName"))
        val dbLastMessage = cursor.getString(cursor.getColumnIndexOrThrow("lastMessage"))
        cursor.close()

        assertFalse("Raw contact name must not be stored as plaintext", dbContactName.contains("سردار احمدی"))
        assertFalse("Raw last message must not be stored as plaintext", dbLastMessage.contains("جلسه فردا"))
        assertTrue(dbContactName.startsWith("enc:v1:"))
        assertTrue(dbLastMessage.startsWith("enc:v1:"))

        // Decrypted retrieval
        val retrieved = database.conversationDao().getConversationByThreadId(threadId)
        assertNotNull(retrieved)
        val decryptedConv = FieldEncryptionManager.decryptConversation(retrieved!!)
        assertEquals(rawContactName, decryptedConv.contactName)
        assertEquals(rawSnippet, decryptedConv.lastMessage)
    }

    @Test
    fun testSecurityAuditReflectsFieldLevelEncryptionAccurately() {
        val securityLayer = ZeroTrustSecurityLayer()
        val audit = securityLayer.auditEncryptionState()

        // Database full-page encryption is false (no SQLCipher)
        assertFalse(audit.isDatabaseEncrypted)
        // Sensitive fields are encrypted via Hardware KeyStore
        assertTrue(audit.isSensitiveFieldsEncrypted)
        // Description explicitly names fields, cipher, and explicitly notes SQLite container is unencrypted
        assertTrue(audit.cipherSuite.contains("SQLite database container is unencrypted"))
        assertTrue(audit.cipherSuite.contains("field-level") || audit.cipherSuite.contains("AES-256-GCM"))
        assertTrue(audit.zeroDataLeakVerified)
    }
}
