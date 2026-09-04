package com.global.sms.data.db

import android.content.Context
import android.util.Log
import com.global.sms.data.entity.MessageCategory
import com.global.sms.data.entity.MessageEntity
import com.global.sms.data.entity.MessageType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

object DatabaseMaintenanceManager {

    private const val TAG = "DbMaintenanceManager"

    /**
     * Automatic Database Maintenance for High-Scale Production (500,000+ messages).
     * Maintains indexing health, reclaims unused SQLite pages, and flushes WAL log files.
     */
    suspend fun runMaintenance(db: GlobalSmsDatabase) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting automatic database maintenance...")

            // 1. Optimize SQLite query planner based on index usage
            db.openHelper.writableDatabase.query("PRAGMA optimize;").close()

            // 2. Flush WAL log into main DB file
            db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL);").close()

            // 3. Clean up spam older than 30 days
            val thirtyDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
            val deletedSpamCount = db.messageDao().deleteOldSpamMessages(thirtyDaysAgo)
            Log.d(TAG, "Deleted $deletedSpamCount expired spam messages.")

            // 4. Run VACUUM if needed to rebuild database file & reduce fragmentation
            db.openHelper.writableDatabase.execSQL("VACUUM;")

            Log.d(TAG, "Database maintenance completed successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Database maintenance error", e)
        }
    }

    /**
     * Benchmark and test migration / performance insertion for high volume (e.g. 500,000 messages)
     */
    suspend fun testHighVolumeBatchInsert(db: GlobalSmsDatabase, batchSize: Int = 500): Long = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val sampleMessages = (1..batchSize).map { index ->
            val threadId = (index % 50).toLong() + 1000L
            MessageEntity(
                threadId = threadId,
                address = "+1555000$threadId",
                body = "Performance Test Message #$index for high-scale database evaluation.",
                timestamp = System.currentTimeMillis() - (index * 1000L),
                type = MessageType.INBOX.code,
                category = MessageCategory.PERSONAL
            )
        }
        db.messageDao().insertMessagesInTransaction(sampleMessages)
        val elapsedTime = System.currentTimeMillis() - startTime
        Log.d(TAG, "Inserted $batchSize messages in $elapsedTime ms.")
        elapsedTime
    }
}
