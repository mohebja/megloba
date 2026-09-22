package com.global.sms.data.db.crypto

import android.content.Context
import android.database.Cursor
import android.util.Log
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import java.io.File
import java.io.RandomAccessFile

/**
 * Full-database encryption (SQLCipher) for the Room database.
 *
 * [prepare] is called once from `GlobalSmsDatabase.getInstance` and returns the open-helper factory
 * Room must use. It also takes care of the three situations an installed app can be in:
 *
 *  1. **No database yet** (fresh install): a new random key is created and the database is born encrypted.
 *  2. **Legacy plaintext database** (every version before this one): the file is exported to an
 *     encrypted copy, the copy is verified (schema + row counts + integrity), swapped in atomically,
 *     and the plaintext file is overwritten and deleted. The plaintext file is never removed before
 *     the encrypted copy is proven good, and an interrupted run is recovered on the next launch.
 *  3. **Already encrypted**: the stored key is unwrapped and returned.
 *
 * If migrating a legacy database fails, the app keeps working on the plaintext file (a security
 * downgrade is better than losing every message) and retries on the next launches, up to
 * [MAX_MIGRATION_ATTEMPTS] times. [status] reports what happened; surface it in diagnostics.
 */
object DatabaseEncryption {

    enum class Status {
        NOT_INITIALIZED,

        /** The database file is encrypted with SQLCipher. */
        ENCRYPTED,

        /** Running on a host JVM (unit tests): SQLCipher's native library is unavailable by design. */
        UNAVAILABLE_ON_HOST_JVM,

        /** A legacy plaintext database could not be encrypted; it is still in use unencrypted. */
        MIGRATION_FAILED_UNENCRYPTED
    }

    @Volatile
    var status: Status = Status.NOT_INITIALIZED
        private set

    private const val TAG = "DatabaseEncryption"
    private const val PREFS = "global_sms_db_state"
    private const val PREF_ATTEMPTS = "encrypt_attempts"
    private const val PREF_KEY_LOST = "key_lost_reset_pending"
    private const val MAX_MIGRATION_ATTEMPTS = 3
    private const val ENCRYPTING_SUFFIX = ".enc.tmp"
    private const val PLAINTEXT_BACKUP_SUFFIX = ".plain.bak"
    private const val MIN_FREE_SPACE_MARGIN = 16L * 1024 * 1024
    private val SQLITE_MAGIC = "SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)

    /**
     * @return the factory Room must use, or `null` when the database has to be opened without
     *   SQLCipher (host JVM, or a legacy database that could not be migrated yet). See [status].
     * @throws IllegalStateException when SQLCipher cannot be used and there is no legacy database to fall back to.
     */
    @Synchronized
    fun prepare(context: Context, databaseName: String): SupportSQLiteOpenHelper.Factory? {
        if (!isAndroidRuntime()) {
            status = Status.UNAVAILABLE_ON_HOST_JVM
            return null
        }

        val appContext = context.applicationContext
        val dbFile = appContext.getDatabasePath(databaseName)
        dbFile.parentFile?.mkdirs()

        recoverInterruptedMigration(dbFile)
        if (dbFile.exists() && dbFile.length() == 0L) deleteDatabaseFiles(dbFile) // empty leftover, nothing to lose

        val plaintextExists = dbFile.exists() && isPlaintextSqlite(dbFile)

        if (!loadNativeLibrary()) {
            if (plaintextExists) {
                status = Status.MIGRATION_FAILED_UNENCRYPTED
                return null
            }
            throw IllegalStateException("SQLCipher native library could not be loaded; refusing to create an unencrypted database")
        }

        val key: ByteArray = when {
            plaintextExists -> {
                val newKey = when (val load = DatabaseKeyStore.load(appContext)) {
                    is DatabaseKeyStore.Load.Ok -> load.key
                    else -> DatabaseKeyStore.createNew(appContext)
                }
                if (!encryptExistingDatabase(appContext, dbFile, newKey)) {
                    status = Status.MIGRATION_FAILED_UNENCRYPTED
                    return null
                }
                newKey
            }

            dbFile.exists() -> when (val load = DatabaseKeyStore.load(appContext)) {
                is DatabaseKeyStore.Load.Ok -> load.key
                else -> {
                    // The database is encrypted but its key is gone for good: it can never be read again.
                    // Start clean; the SMS import re-populates from the system provider when the DB is empty.
                    Log.e(TAG, "Database key is unrecoverable; discarding the unreadable database")
                    deleteDatabaseFiles(dbFile)
                    prefs(appContext).edit().putBoolean(PREF_KEY_LOST, true).apply()
                    DatabaseKeyStore.createNew(appContext)
                }
            }

            else -> when (val load = DatabaseKeyStore.load(appContext)) {
                is DatabaseKeyStore.Load.Ok -> load.key
                else -> DatabaseKeyStore.createNew(appContext)
            }
        }

        status = Status.ENCRYPTED
        return SupportOpenHelperFactory(passphraseFor(key))
    }

    /** True once after the database had to be recreated because its key was lost. */
    fun consumeKeyLostNotice(context: Context): Boolean {
        val prefs = prefs(context.applicationContext)
        val pending = prefs.getBoolean(PREF_KEY_LOST, false)
        if (pending) prefs.edit().putBoolean(PREF_KEY_LOST, false).apply()
        return pending
    }

    // ---------------------------------------------------------------------------------------------
    // Plaintext -> encrypted migration
    // ---------------------------------------------------------------------------------------------

    private class PlaintextFacts(val version: Int, val snapshot: Snapshot)

    private class Snapshot(val objects: Set<String>, val rowCounts: Map<String, Long>)

    private fun encryptExistingDatabase(context: Context, dbFile: File, key: ByteArray): Boolean {
        val prefs = prefs(context)
        val attempts = prefs.getInt(PREF_ATTEMPTS, 0)
        if (attempts >= MAX_MIGRATION_ATTEMPTS) {
            Log.e(TAG, "Giving up on encrypting the legacy database after $attempts attempts")
            return false
        }
        prefs.edit().putInt(PREF_ATTEMPTS, attempts + 1).commit()

        val tmp = File(dbFile.path + ENCRYPTING_SUFFIX)
        val bak = File(dbFile.path + PLAINTEXT_BACKUP_SUFFIX)
        return try {
            val parent = dbFile.absoluteFile.parentFile
            check(parent == null || parent.usableSpace > dbFile.length() * 2 + MIN_FREE_SPACE_MARGIN) {
                "not enough free storage to encrypt the database"
            }
            deleteDatabaseFiles(tmp)

            val facts = inspectPlaintext(dbFile)
            exportInto(context, tmp, key, dbFile, facts.version)
            verifyEncryptedCopy(context, tmp, key, facts)
            swapInEncryptedCopy(dbFile, tmp, bak)
            secureDelete(bak)

            prefs.edit().remove(PREF_ATTEMPTS).apply()
            Log.i(TAG, "Database encrypted with SQLCipher (${facts.snapshot.rowCounts.size} tables verified)")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Encrypting the legacy database failed; continuing with the plaintext database", e)
            cleanupAfterFailure(dbFile, tmp, bak)
            false
        } catch (e: LinkageError) {
            Log.e(TAG, "SQLCipher native code failed; continuing with the plaintext database", e)
            cleanupAfterFailure(dbFile, tmp, bak)
            false
        }
    }

    private fun cleanupAfterFailure(dbFile: File, tmp: File, bak: File) {
        deleteDatabaseFiles(tmp)
        // If we failed between the two renames the plaintext database is in the backup slot: put it back.
        if (!dbFile.exists() && bak.exists()) bak.renameTo(dbFile)
    }

    /** Merges the WAL, then records everything the encrypted copy must reproduce. Uses the platform SQLite. */
    private fun inspectPlaintext(dbFile: File): PlaintextFacts {
        val db = android.database.sqlite.SQLiteDatabase.openDatabase(
            dbFile.absolutePath,
            null,
            android.database.sqlite.SQLiteDatabase.OPEN_READWRITE
        )
        try {
            db.rawQuery("PRAGMA wal_checkpoint(TRUNCATE)", null).use { it.moveToFirst() }
            return PlaintextFacts(db.version, takeSnapshot { sql -> db.rawQuery(sql, null) })
        } finally {
            db.close()
        }
    }

    private fun exportInto(context: Context, target: File, key: ByteArray, plainFile: File, version: Int) {
        val helper = newHelper(context, target, key, version = 1)
        try {
            val db = helper.writableDatabase
            val plainPath = plainFile.absolutePath.replace("'", "''")
            db.execSQL("DROP TABLE IF EXISTS android_metadata")
            db.execSQL("ATTACH DATABASE '$plainPath' AS plaintext KEY ''")
            // sqlcipher_export is a SELECT: it only runs when the cursor is stepped.
            db.query("SELECT sqlcipher_export('main', 'plaintext')").use { it.moveToFirst() }
            db.execSQL("DETACH DATABASE plaintext")
            // user_version is not copied by sqlcipher_export, and Room relies on it.
            db.execSQL("PRAGMA user_version = $version")
        } finally {
            helper.close()
        }
    }

    private fun verifyEncryptedCopy(context: Context, tmp: File, key: ByteArray, facts: PlaintextFacts) {
        check(!isPlaintextSqlite(tmp)) { "exported database is not encrypted" }
        val helper = newHelper(context, tmp, key, version = facts.version)
        try {
            val db = helper.readableDatabase
            val userVersion = db.query("PRAGMA user_version").use { if (it.moveToFirst()) it.getInt(0) else -1 }
            check(userVersion == facts.version) { "schema version mismatch: $userVersion != ${facts.version}" }

            db.query("PRAGMA quick_check(1)").use {
                check(it.moveToFirst() && it.getString(0) == "ok") { "integrity check failed" }
            }

            val copy = takeSnapshot { sql -> db.query(sql) }
            check(copy.objects == facts.snapshot.objects) {
                "schema objects differ: missing=${facts.snapshot.objects - copy.objects} extra=${copy.objects - facts.snapshot.objects}"
            }
            check(copy.rowCounts == facts.snapshot.rowCounts) {
                "row counts differ in: ${
                    facts.snapshot.rowCounts.filter { (table, count) -> copy.rowCounts[table] != count }.keys
                }"
            }
        } finally {
            helper.close()
        }
    }

    /**
     * Crash-safe swap. Every step is a rename, and [recoverInterruptedMigration] undoes a partial one:
     * original -> .plain.bak, then .enc.tmp -> original.
     */
    private fun swapInEncryptedCopy(dbFile: File, tmp: File, bak: File) {
        check(!bak.exists() || bak.delete()) { "could not clear an old plaintext backup" }
        check(dbFile.renameTo(bak)) { "could not move the plaintext database aside" }
        deleteSidecars(dbFile) // -wal/-shm/-journal belonged to the plaintext file
        if (!tmp.renameTo(dbFile)) {
            bak.renameTo(dbFile)
            error("could not activate the encrypted database")
        }
    }

    /** Finishes or rolls back a migration that was interrupted by a crash or a kill. */
    private fun recoverInterruptedMigration(dbFile: File) {
        val tmp = File(dbFile.path + ENCRYPTING_SUFFIX)
        val bak = File(dbFile.path + PLAINTEXT_BACKUP_SUFFIX)
        deleteDatabaseFiles(tmp)
        if (!bak.exists()) return

        when {
            !dbFile.exists() -> {
                Log.w(TAG, "Recovering the plaintext database from an interrupted migration")
                bak.renameTo(dbFile)
            }

            !isPlaintextSqlite(dbFile) -> {
                Log.i(TAG, "Removing the plaintext backup left by a completed migration")
                secureDelete(bak)
            }
            // A plaintext main file next to a backup: keep both, the migration will simply retry.
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------------------------

    private fun newHelper(context: Context, file: File, key: ByteArray, version: Int): SupportSQLiteOpenHelper {
        val callback = object : SupportSQLiteOpenHelper.Callback(version) {
            override fun onCreate(db: SupportSQLiteDatabase) = Unit
            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }
        val configuration = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(file.absolutePath)
            .callback(callback)
            .build()
        return SupportOpenHelperFactory(passphraseFor(key)).create(configuration)
    }

    /**
     * SQLCipher "raw key" syntax: a 64-hex-digit key is used directly, without the (slow) passphrase KDF.
     * Safe because the key is 256 random bits. A new array per call: SQLCipher may clear what it is given.
     */
    private fun passphraseFor(key: ByteArray): ByteArray {
        val hex = key.joinToString(separator = "") { "%02x".format(it) }
        return "x'$hex'".toByteArray(Charsets.US_ASCII)
    }

    private fun takeSnapshot(query: (String) -> Cursor): Snapshot {
        val objects = sortedSetOf<String>()
        val tables = mutableListOf<String>()
        query("SELECT type, name FROM sqlite_master WHERE name NOT LIKE 'sqlite_%' AND name != 'android_metadata'").use { c ->
            while (c.moveToNext()) {
                val type = c.getString(0)
                val name = c.getString(1)
                objects += "$type:$name"
                if (type == "table") tables += name
            }
        }
        val counts = tables.associateWith { table ->
            query("SELECT COUNT(*) FROM \"${table.replace("\"", "\"\"")}\"").use { c ->
                if (c.moveToFirst()) c.getLong(0) else -1L
            }
        }
        return Snapshot(objects, counts)
    }

    private fun isPlaintextSqlite(file: File): Boolean {
        if (!file.isFile || file.length() < SQLITE_MAGIC.size) return false
        return try {
            file.inputStream().use { input ->
                val header = ByteArray(SQLITE_MAGIC.size)
                var read = 0
                while (read < header.size) {
                    val n = input.read(header, read, header.size - read)
                    if (n < 0) break
                    read += n
                }
                read == header.size && header.contentEquals(SQLITE_MAGIC)
            }
        } catch (e: java.io.IOException) {
            false
        }
    }

    private fun deleteDatabaseFiles(db: File) {
        db.delete()
        deleteSidecars(db)
    }

    private fun deleteSidecars(db: File) {
        File(db.path + "-wal").delete()
        File(db.path + "-shm").delete()
        File(db.path + "-journal").delete()
    }

    /** Best effort: flash storage may keep old blocks, but this removes the trivial recovery path. */
    private fun secureDelete(file: File) {
        if (!file.exists()) return
        try {
            RandomAccessFile(file, "rw").use { raf ->
                val zeros = ByteArray(64 * 1024)
                var remaining = raf.length()
                raf.seek(0)
                while (remaining > 0) {
                    val n = minOf(remaining, zeros.size.toLong()).toInt()
                    raf.write(zeros, 0, n)
                    remaining -= n
                }
                raf.fd.sync()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not overwrite the plaintext backup before deleting it", e)
        }
        file.delete()
        deleteSidecars(file)
    }

    private fun loadNativeLibrary(): Boolean =
        try {
            System.loadLibrary("sqlcipher")
            true
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "Could not load the SQLCipher native library", e)
            false
        }

    private fun isAndroidRuntime(): Boolean {
        val vm = System.getProperty("java.vm.name").orEmpty()
        val runtime = System.getProperty("java.runtime.name").orEmpty()
        return vm.contains("Dalvik", ignoreCase = true) || runtime.contains("Android", ignoreCase = true)
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
