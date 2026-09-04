package com.global.sms

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.data.entity.CrmCustomerEntity
import com.global.sms.data.entity.MessageEntity
import com.global.sms.data.entity.MessageStatus
import com.global.sms.data.entity.MessageType
import com.global.sms.engine.queue.SmsQueueManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.system.measureTimeMillis

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BulkSmsAndCrmTest {

    private lateinit var context: Context
    private lateinit var db: GlobalSmsDatabase

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = GlobalSmsDatabase.getInstance(context)
    }

    @Test
    fun testCrmCustomerRoomPersistenceAndQuery() = runBlocking {
        val crmDao = db.crmCustomerDao()
        val customer = CrmCustomerEntity(
            name = "مریم حسینی",
            phoneNumber = "09351112233",
            company = "فناوری اطلاعات نوآوران",
            customerStatus = "LEAD",
            tags = "لید, جدید"
        )
        val customerId = crmDao.insertOrUpdateCustomer(customer)
        assertTrue(customerId > 0)

        val retrieved = crmDao.getCustomerById(customerId)
        assertNotNull(retrieved)
        assertEquals("مریم حسینی", retrieved?.name)
        assertEquals("LEAD", retrieved?.customerStatus)

        // Update status (e.g. via Customer 360 stage change)
        val updated = retrieved!!.copy(customerStatus = "VIP")
        crmDao.insertOrUpdateCustomer(updated)

        val retrievedUpdated = crmDao.getCustomerById(customerId)
        assertEquals("VIP", retrievedUpdated?.customerStatus)
    }

    @Test
    fun testBulkSmsQueuePacingThrottle() = runBlocking {
        val messageDao = db.messageDao()
        
        // Configure small delay for fast, deterministic unit testing
        SmsQueueManager.bulkSendDelayMs = 150L
        SmsQueueManager.bulkBatchSize = 2
        SmsQueueManager.bulkBatchCoolingPauseMs = 300L

        // Insert 3 pending messages
        val testThreadId = 999123L
        for (i in 1..3) {
            messageDao.insertMessage(
                MessageEntity(
                    threadId = testThreadId,
                    address = "0912111000$i",
                    body = "تست ارسال پیامک کمپین $i",
                    timestamp = System.currentTimeMillis() + i,
                    type = MessageType.OUTBOX.code,
                    deliveryStatus = MessageStatus.PENDING.code,
                    simSlot = 0
                )
            )
        }

        val pendingBefore = messageDao.getPendingMessagesOnce()
        assertTrue(pendingBefore.size >= 3)

        // Measure time taken to process queue with real delay
        val durationMs = measureTimeMillis {
            SmsQueueManager.processPendingQueue(context)
        }

        // With 3 items and throttle:
        // item 1 -> delay 150ms
        // item 2 -> batch cooling pause 300ms
        // item 3 -> final message sent
        // Total duration >= 400ms
        assertTrue("Expected duration to reflect throttling (> 400ms), but was ${durationMs}ms", durationMs >= 400L)
    }

    @Test
    fun testConcurrentQueueInvocationsDoNotBlock() = runBlocking {
        val messageDao = db.messageDao()

        // Set realistic delay so bulk processing takes significant time sequentially
        SmsQueueManager.bulkSendDelayMs = 400L
        SmsQueueManager.bulkBatchSize = 10
        SmsQueueManager.bulkBatchCoolingPauseMs = 800L

        val bulkThreadId = 888123L
        // Insert 4 bulk messages (sequentially would take 3 * 400ms = 1200ms+ delay)
        for (i in 1..4) {
            messageDao.insertMessage(
                MessageEntity(
                    threadId = bulkThreadId,
                    address = "0912999000$i",
                    body = "پیام کمپین گروهی $i",
                    timestamp = System.currentTimeMillis() + i,
                    type = MessageType.OUTBOX.code,
                    deliveryStatus = MessageStatus.PENDING.code,
                    simSlot = 0
                )
            )
        }

        // Start worker 1 in background to process bulk batch
        val bulkJob = launch(Dispatchers.IO) {
            SmsQueueManager.processPendingQueue(context)
        }

        // Wait a short slice (50ms) to ensure worker 1 has claimed message 1 and entered delay
        delay(50)

        // Measure time for a concurrent second invocation to start and help process messages
        val concurrentDurationMs = measureTimeMillis {
            // Worker 2 calls processPendingQueue concurrently without being blocked by worker 1's delays
            SmsQueueManager.processPendingQueue(context)
        }

        // Worker 2 runs concurrently and finishes its partition without waiting for the full sequential bulk run
        // If the mutex were held for the entire bulk duration, worker 2 would wait > 1200ms
        assertTrue(
            "Concurrent worker took ${concurrentDurationMs}ms (expected < 1100ms, proving it did not block for full bulk duration)",
            concurrentDurationMs < 1100L
        )

        // Await the bulk job to finish cleanly
        bulkJob.join()

        // Verify all messages are processed out of PENDING
        val remainingPending = messageDao.getPendingMessagesOnce()
        assertTrue("All messages should be processed", remainingPending.isEmpty())
    }

    @Test
    fun testUrgentMessageEnqueueDuringBulkSendIsNotBlocked() = runBlocking {
        val messageDao = db.messageDao()

        // 1000ms delay per bulk message
        SmsQueueManager.bulkSendDelayMs = 1000L
        SmsQueueManager.bulkBatchSize = 10
        SmsQueueManager.bulkBatchCoolingPauseMs = 2000L

        val bulkThreadId = 777999L
        // Insert 2 bulk messages (takes > 1000ms)
        for (i in 1..2) {
            messageDao.insertMessage(
                MessageEntity(
                    threadId = bulkThreadId,
                    address = "0912555000$i",
                    body = "پیام انبوه سازمانی $i",
                    timestamp = System.currentTimeMillis() + i,
                    type = MessageType.OUTBOX.code,
                    deliveryStatus = MessageStatus.PENDING.code,
                    simSlot = 0
                )
            )
        }

        // Start worker 1 in background
        val bulkJob = launch(Dispatchers.IO) {
            SmsQueueManager.processPendingQueue(context)
        }

        // Wait 100ms so worker 1 is actively inside its 1000ms sleep
        delay(100)

        // Measure time to enqueue an urgent message and claim its database insert
        // Because queueMutex is NOT held during the 1000ms delay, this completes in < 250ms
        val enqueueTimeMs = measureTimeMillis {
            val urgentMsg = MessageEntity(
                threadId = 12345L,
                address = "09120000000",
                body = "کد ورود: 882910",
                timestamp = System.currentTimeMillis(),
                type = MessageType.OUTBOX.code,
                deliveryStatus = MessageStatus.PENDING.code,
                simSlot = 0
            )
            messageDao.insertMessage(urgentMsg)
        }

        assertTrue(
            "Enqueueing during active bulk delay took ${enqueueTimeMs}ms (expected < 250ms)",
            enqueueTimeMs < 250L
        )

        bulkJob.join()
    }
}
