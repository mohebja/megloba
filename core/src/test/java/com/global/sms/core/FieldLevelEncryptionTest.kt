package com.global.sms.core

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.global.sms.core.security.FieldEncryptionManager
import com.global.sms.core.security.LegacyFieldDecryptionMigration
import com.global.sms.core.security.ZeroTrustSecurityLayer
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.data.db.crypto.DatabaseEncryption
import com.global.sms.data.entity.ConversationEntity
import com.global.sms.data.entity.MessageCategory
import com.global.sms.data.entity.MessageEntity
import com.global.sms.data.entity.MessageStatus
import com.global.sms.data.entity.MessageType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * The database file is encrypted as a whole (SQLCipher, verified by the instrumented
 * `DatabaseEncryptionMigrationTest`), so these host-JVM tests cover what does not need the native library:
 * new rows are plain text, legacy `enc:v1:` values stay readable, and the one-time conversion of legacy
 * rows works and restores full-text search.
 */
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

    private fun rawBody(id: Long): Pair<String, Int> {
        val cursor = database.openHelper.readableDatabase.query("SELECT body, isEncrypted FROM messages WHERE id = $id")
        try {
            assertTrue("Row must exist in SQLite", cursor.moveToFirst())
            return cursor.getString(0) to cursor.getInt(1)
        } finally {
            cursor.close()
        }
    }

    @Test
    fun testNewRowsAreStoredAsPlainTextBecauseTheDatabaseFileIsEncrypted() = runBlocking {
        val plainBody = "رمز یکبار مصرف شما: 948201. از اشتراک‌گذاری آن خودداری کنید."
        val message = MessageEntity(
            id = 101L,
            threadId = 1L,
            address = "+989123456789",
            body = plainBody,
            timestamp = 1700000000000L,
            category = MessageCategory.OTP,
            otpCode = "948201",
            deliveryStatus = MessageStatus.DELIVERED.code,
            type = MessageType.INBOX.code
        )

        // The legacy wrapper is a pass-through now: nothing is wrapped in enc:v1: any more.
        assertSame(message, FieldEncryptionManager.encryptMessage(message))
        database.messageDao().insertMessage(FieldEncryptionManager.encryptMessage(message))

        val (rawDbBody, rawIsEncrypted) = rawBody(101L)
        assertEquals(plainBody, rawDbBody)
        assertEquals("isEncrypted is reserved for Private Vault rows", 0, rawIsEncrypted)
        assertEquals(plainBody, database.messageDao().getMessageById(101L)!!.body)
    }

    @Test
    fun testLegacyCiphertextIsStillDecryptedAcrossProcessRestart() = runBlocking {
        val secret = "اطلاعات مالی بسیار محرمانه برای انتقال وجه ۱۰۰,۰۰۰,۰۰۰ ریال"
        val legacyBody = FieldEncryptionManager.encrypt(secret)
        assertTrue(legacyBody.startsWith("enc:v1:"))

        database.messageDao().insertMessage(
            MessageEntity(id = 777L, threadId = 10L, address = "09121112233", body = legacyBody, isEncrypted = true)
        )
        val initialKey = com.global.sms.security.keystore.KeyStoreManager.getOrCreateMasterKey()

        database.close()
        val restarted = Room.databaseBuilder(context, GlobalSmsDatabase::class.java, dbFile!!.absolutePath)
            .allowMainThreadQueries()
            .build()

        val reloadedKey = com.global.sms.security.keystore.KeyStoreManager.getOrCreateMasterKey()
        assertArrayEquals("Reloaded master key must match initial key", initialKey.encoded, reloadedKey.encoded)

        val stored = restarted.messageDao().getMessageById(777L)
        assertNotNull(stored)
        assertEquals(secret, FieldEncryptionManager.decryptMessage(stored!!).body)
        restarted.close()
    }

    @Test
    fun testTamperedCiphertextFailsClosedWithSecurityException() {
        val tamperedPayload = "enc:v1:dGFtcGVyZWRfYmFzZTY0X2ludmFsaWRfcGF5bG9hZA=="

        assertThrows(SecurityException::class.java) {
            FieldEncryptionManager.decrypt(tamperedPayload)
        }
    }

    @Test
    fun testPlainTextPassesThroughDecryptSafely() {
        val plain = "پیام معمولی بدون رمزنگاری فیلد"
        assertEquals(plain, FieldEncryptionManager.decrypt(plain))
    }

    @Test
    fun testConversationFieldsAreStoredAsPlainText() = runBlocking {
        val conversation = ConversationEntity(
            threadId = 55L,
            address = "+989351112233",
            contactName = "سردار احمدی",
            lastMessage = "سلام مهندس، جلسه فردا ساعت ۸ صبح است.",
            lastTimestamp = 1700000000000L,
            unreadCount = 2,
            category = MessageCategory.WORK
        )
        database.conversationDao().insertOrUpdateConversation(FieldEncryptionManager.encryptConversation(conversation))

        val retrieved = database.conversationDao().getConversationByThreadId(55L)
        assertNotNull(retrieved)
        assertEquals(conversation.contactName, retrieved!!.contactName)
        assertEquals(conversation.lastMessage, retrieved.lastMessage)
    }

    @Test
    fun testLegacyRowsAreConvertedInEveryTableAndFullTextSearchWorksAgain() = runBlocking {
        val plainBody = "Your invoice 4471 is ready"
        val legacyBody = FieldEncryptionManager.encrypt(plainBody)
        val sqlite = database.openHelper.writableDatabase

        database.messageDao().insertMessage(
            MessageEntity(id = 1L, threadId = 1L, address = "INVOICES", body = legacyBody, isEncrypted = true)
        )
        database.conversationDao().insertOrUpdateConversation(
            ConversationEntity(
                threadId = 1L,
                address = "INVOICES",
                contactName = FieldEncryptionManager.encrypt("Billing team"),
                lastMessage = legacyBody
            )
        )
        sqlite.execSQL(
            "INSERT INTO scheduled_messages (address, body, scheduledTimestamp, simSlot, status) VALUES (?, ?, ?, ?, ?)",
            arrayOf<Any>("INVOICES", legacyBody, 1700000000000L, 0, "PENDING")
        )
        sqlite.execSQL(
            "INSERT INTO quick_replies (title, content) VALUES (?, ?)",
            arrayOf<Any>("Thanks", FieldEncryptionManager.encrypt("Thanks, received!"))
        )

        // The search index still holds ciphertext, so the word cannot be found yet.
        assertTrue(database.messageDao().searchMessagesFts("invoice").first().isEmpty())

        val outcome = LegacyFieldDecryptionMigration.migrate(sqlite, timeBudgetMs = 60_000L)

        assertTrue(outcome.complete)
        assertEquals(4, outcome.converted)
        assertEquals(0, outcome.failed)

        val (body, isEncrypted) = rawBody(1L)
        assertEquals(plainBody, body)
        assertEquals(0, isEncrypted)

        val conversation = database.conversationDao().getConversationByThreadId(1L)!!
        assertEquals("Billing team", conversation.contactName)
        assertEquals(plainBody, conversation.lastMessage)

        sqlite.query("SELECT body FROM scheduled_messages").use {
            assertTrue(it.moveToFirst()); assertEquals(plainBody, it.getString(0))
        }
        sqlite.query("SELECT content FROM quick_replies").use {
            assertTrue(it.moveToFirst()); assertEquals("Thanks, received!", it.getString(0))
        }

        // The content-sync triggers re-indexed the updated row.
        assertEquals(1, database.messageDao().searchMessagesFts("invoice").first().size)

        // Running it again finds nothing left to do.
        val again = LegacyFieldDecryptionMigration.migrate(sqlite, timeBudgetMs = 60_000L)
        assertTrue(again.complete)
        assertEquals(0, again.converted)
    }

    @Test
    fun testUndecryptableRowsAreSkippedWithoutLoopingForever() = runBlocking {
        val tampered = "enc:v1:dGFtcGVyZWRfYmFzZTY0X2ludmFsaWRfcGF5bG9hZA=="
        database.messageDao().insertMessage(
            MessageEntity(id = 5L, threadId = 2L, address = "X", body = tampered, isEncrypted = true)
        )

        val outcome = LegacyFieldDecryptionMigration.migrate(database.openHelper.writableDatabase, timeBudgetMs = 60_000L)

        assertTrue(outcome.complete)
        assertEquals(0, outcome.converted)
        assertEquals(1, outcome.failed)
        assertEquals(tampered, rawBody(5L).first)
    }

    @Test
    fun testSecurityAuditReportsTheRealEncryptionState() {
        // SQLCipher's native library is never loaded on the host JVM, so the database is not encrypted here.
        assertNull(DatabaseEncryption.prepare(context, "host_jvm_probe.db"))
        assertEquals(DatabaseEncryption.Status.UNAVAILABLE_ON_HOST_JVM, DatabaseEncryption.status)

        val audit = ZeroTrustSecurityLayer().auditEncryptionState()
        assertFalse(audit.isDatabaseEncrypted)
        assertFalse(audit.isSensitiveFieldsEncrypted)
        assertTrue(audit.cipherSuite.contains("NOT encrypted"))
    }
}
