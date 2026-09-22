package com.global.sms.core.security

import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.sqlite.db.SupportSQLiteDatabase
import com.global.sms.data.db.GlobalSmsDatabase

/**
 * One-time conversion of rows written by the old *field-level* encryption.
 *
 * Earlier versions stored message bodies, contact names and conversation snippets as `enc:v1:<base64>`
 * inside a plaintext SQLite file, and no screen ever decrypted them. The database file is now encrypted as
 * a whole (SQLCipher), so those values are turned back into plain text, which also lets full-text search see
 * them. Rows are processed newest first in small transactions, so the conversation list and recent chats are
 * fixed within moments even on a very large history, and an interrupted run simply continues later.
 */
object LegacyFieldDecryptionMigration {

    private const val TAG = "LegacyFieldDecrypt"
    private const val PREFS = "global_sms_db_state"
    private const val PREF_DONE = "legacy_field_decrypt_done"
    private const val BATCH_SIZE = 400
    private const val PREFIX = "enc:v1:"

    data class Outcome(val complete: Boolean, val converted: Int, val failed: Int)

    /** A table that may still contain legacy ciphertext. */
    private class Target(
        val table: String,
        val idColumn: String,
        val columns: List<String>,
        /** Extra assignment applied to a row once its fields were decrypted. */
        val extraSet: String? = null
    )

    // Order matters: the conversation list is small and the first thing the user sees.
    private val targets = listOf(
        Target("conversations", "threadId", listOf("contactName", "lastMessage")),
        Target("messages", "id", listOf("body"), extraSet = "isEncrypted = 0"),
        Target("scheduled_messages", "id", listOf("body")),
        Target("quick_replies", "id", listOf("content"))
    )

    fun isDone(context: Context): Boolean =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(PREF_DONE, false)

    /** Runs (or continues) the conversion. Must be called off the main thread. */
    fun run(context: Context, timeBudgetMs: Long = 8 * 60_000L): Outcome {
        val appContext = context.applicationContext
        if (isDone(appContext)) return Outcome(complete = true, converted = 0, failed = 0)

        val db = GlobalSmsDatabase.getInstance(appContext).openHelper.writableDatabase
        val outcome = migrate(db, timeBudgetMs)
        if (outcome.complete) {
            appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(PREF_DONE, true).apply()
        }
        Log.i(TAG, "converted=${outcome.converted} failed=${outcome.failed} complete=${outcome.complete}")
        return outcome
    }

    internal fun migrate(db: SupportSQLiteDatabase, timeBudgetMs: Long): Outcome {
        val deadline = SystemClock.elapsedRealtime() + timeBudgetMs
        var converted = 0
        var failed = 0

        for (target in targets) {
            var lastId = Long.MAX_VALUE
            while (true) {
                if (SystemClock.elapsedRealtime() > deadline) return Outcome(false, converted, failed)

                val batch = readBatch(db, target, lastId)
                if (batch.isEmpty()) break
                lastId = batch.last().id // ids are read in descending order

                // Decrypt outside the transaction: every call is a Keystore operation.
                val updates = ArrayList<RowUpdate>(batch.size)
                for (row in batch) {
                    val decrypted = HashMap<String, String>()
                    row.values.forEach { (column, value) ->
                        try {
                            decrypted[column] = FieldEncryptionManager.decrypt(value)
                        } catch (e: SecurityException) {
                            failed++ // key lost or ciphertext damaged: leave the row as it is, never loop on it
                        }
                    }
                    if (decrypted.isNotEmpty()) updates += RowUpdate(row.id, decrypted)
                }
                if (updates.isNotEmpty()) {
                    applyUpdates(db, target, updates)
                    converted += updates.size
                }
            }
        }
        return Outcome(true, converted, failed)
    }

    private class Row(val id: Long, val values: Map<String, String>)

    private class RowUpdate(val id: Long, val decrypted: Map<String, String>)

    private fun readBatch(db: SupportSQLiteDatabase, target: Target, beforeId: Long): List<Row> {
        val select = (listOf(target.idColumn) + target.columns).joinToString(", ")
        val legacy = target.columns.joinToString(" OR ") { "substr($it, 1, ${PREFIX.length}) = '$PREFIX'" }
        val sql = "SELECT $select FROM ${target.table} WHERE ${target.idColumn} < ? AND ($legacy) " +
            "ORDER BY ${target.idColumn} DESC LIMIT $BATCH_SIZE"

        val rows = ArrayList<Row>(BATCH_SIZE)
        db.query(sql, arrayOf<Any>(beforeId)).use { cursor ->
            while (cursor.moveToNext()) {
                val values = HashMap<String, String>()
                target.columns.forEachIndexed { index, column ->
                    val value = cursor.getString(index + 1)
                    if (value != null && value.startsWith(PREFIX)) values[column] = value
                }
                rows += Row(cursor.getLong(0), values)
            }
        }
        return rows
    }

    private fun applyUpdates(db: SupportSQLiteDatabase, target: Target, updates: List<RowUpdate>) {
        db.beginTransaction()
        try {
            for (update in updates) {
                val columns = update.decrypted.keys.toList()
                val assignments = columns.map { "$it = ?" } + listOfNotNull(target.extraSet)
                val args = ArrayList<Any?>(columns.size + 1)
                columns.forEach { args += update.decrypted.getValue(it) }
                args += update.id
                db.execSQL(
                    "UPDATE ${target.table} SET ${assignments.joinToString(", ")} WHERE ${target.idColumn} = ?",
                    args.toTypedArray()
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}
