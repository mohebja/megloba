package com.global.sms

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.data.db.crypto.DatabaseEncryption
import com.global.sms.data.entity.MessageEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Runs on a device/emulator (SQLCipher's native library is not available on the host JVM):
 * `./gradlew :app:connectedDebugAndroidTest --tests "*DatabaseEncryptionMigrationTest"`
 */
@RunWith(AndroidJUnit4::class)
class DatabaseEncryptionMigrationTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val dbName = "encryption_migration_test_db"
    private lateinit var dbFile: File

    private val body = "Your invoice 4471 is ready"

    @Before
    fun setUp() {
        dbFile = context.getDatabasePath(dbName)
        wipe()
    }

    @After
    fun tearDown() = wipe()

    private fun wipe() {
        listOf("", "-wal", "-shm", "-journal", ".enc.tmp", ".plain.bak").forEach { File(dbFile.path + it).delete() }
    }

    private fun isPlaintext(file: File): Boolean = file.inputStream().use {
        val header = ByteArray(16)
        it.read(header)
        String(header, Charsets.US_ASCII) == "SQLite format 3\u0000"
    }

    /** Creates a database exactly like every previous app version did: plaintext, real schema. */
    private fun createLegacyPlaintextDatabase(): Long = runBlocking {
        val legacy = Room.databaseBuilder(context, GlobalSmsDatabase::class.java, dbName).build()
        val id = legacy.messageDao().insertMessage(MessageEntity(threadId = 1L, address = "INVOICES", body = body))
        legacy.close()
        id
    }

    private fun openEncrypted(factory: SupportSQLiteOpenHelper.Factory) =
        Room.databaseBuilder(context, GlobalSmsDatabase::class.java, dbName).openHelperFactory(factory).build()

    @Test
    fun legacyPlaintextDatabaseIsEncryptedWithoutLosingData() = runBlocking {
        val id = createLegacyPlaintextDatabase()
        assertTrue("precondition: the legacy file is plaintext", isPlaintext(dbFile))

        val factory = DatabaseEncryption.prepare(context, dbName)

        assertNotNull(factory)
        assertEquals(DatabaseEncryption.Status.ENCRYPTED, DatabaseEncryption.status)
        assertFalse("the file must no longer be readable as plain SQLite", isPlaintext(dbFile))
        assertFalse("the plaintext backup must be gone", File(dbFile.path + ".plain.bak").exists())
        assertFalse(File(dbFile.path + ".enc.tmp").exists())

        val db = openEncrypted(factory!!)
        assertEquals(body, db.messageDao().getMessageById(id)?.body)
        assertEquals("FTS index must survive the export", 1, db.messageDao().searchMessagesFts("invoice").first().size)
        db.close()

        // A second start finds an encrypted file and simply reuses the stored key.
        val again = DatabaseEncryption.prepare(context, dbName)
        assertNotNull(again)
        val reopened = openEncrypted(again!!)
        assertEquals(body, reopened.messageDao().getMessageById(id)?.body)
        reopened.close()
    }

    @Test
    fun interruptedMigrationIsRecoveredOnTheNextStart() = runBlocking {
        val id = createLegacyPlaintextDatabase()
        // Simulate a crash between the two renames: the plaintext DB sits in the backup slot, main is missing.
        assertTrue(dbFile.renameTo(File(dbFile.path + ".plain.bak")))
        assertFalse(dbFile.exists())

        val factory = DatabaseEncryption.prepare(context, dbName)

        assertNotNull(factory)
        assertTrue(dbFile.exists())
        assertFalse(isPlaintext(dbFile))
        val db = openEncrypted(factory!!)
        assertEquals(body, db.messageDao().getMessageById(id)?.body)
        db.close()
    }

    @Test
    fun freshInstallCreatesAnEncryptedDatabase() = runBlocking {
        val factory = DatabaseEncryption.prepare(context, dbName)
        assertNotNull(factory)

        val db = openEncrypted(factory!!)
        val id = db.messageDao().insertMessage(MessageEntity(threadId = 2L, address = "BANK", body = body))
        db.close()

        assertFalse(isPlaintext(dbFile))
        val reopened = openEncrypted(DatabaseEncryption.prepare(context, dbName)!!)
        assertEquals(body, reopened.messageDao().getMessageById(id)?.body)
        reopened.close()
    }
}
