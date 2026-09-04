package com.global.sms

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.data.entity.MessageEntity
import com.global.sms.data.entity.MessageStatus
import com.global.sms.data.entity.MessageType
import com.global.sms.engine.dispatcher.MessageDispatcher
import com.global.sms.engine.receiver.DeliveryReportReceiver
import com.global.sms.engine.sender.SmsSender
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SmsEngineReliabilityTest {

    private lateinit var context: Context
    private lateinit var db: GlobalSmsDatabase

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = GlobalSmsDatabase.getInstance(context)
    }

    @Test
    fun testIncomingSmsDeliverAction_PersianAndEmoji() = runBlocking {
        val testSender = "+989123456789"
        val testBody = "سلام! کد تایید: ۹۸۷۶۵۴ 😃🎉"
        
        MessageDispatcher.onIncomingSms(
            context = context,
            address = testSender,
            body = testBody,
            timestamp = System.currentTimeMillis(),
            simSlot = 0,
            subId = 1
        )

        val messages = db.messageDao().getAllMessagesSync().filter { it.address == testSender }
        assertFalse("Messages list should not be empty", messages.isEmpty())
        val lastMsg = com.global.sms.core.security.FieldEncryptionManager.decryptMessage(messages.last())
        assertEquals("Sender address should match", testSender, lastMsg.address)
        assertEquals("Persian and Emoji body must match exactly", testBody, lastMsg.body)
        assertEquals("OTP code should be extracted correctly", "987654", lastMsg.otpCode)
    }

    @Test
    fun testIncomingLargeMultipartMessage() = runBlocking {
        val testSender = "CRM_SERVER"
        val largeBody = "این یک پیامک بسیار طولانی جهت تست پایداری موتور پیامک است. ".repeat(15) + " 🚀🔥"

        MessageDispatcher.onIncomingSms(
            context = context,
            address = testSender,
            body = largeBody,
            timestamp = System.currentTimeMillis(),
            simSlot = 1,
            subId = 2
        )

        val messages = db.messageDao().getAllMessagesSync().filter { it.address == testSender }
        val msg = com.global.sms.core.security.FieldEncryptionManager.decryptMessage(messages.first())
        assertEquals("Large message body length check", largeBody.length, msg.body.length)
        assertEquals("Full text integrity verified", largeBody, msg.body)
    }

    @Test
    fun testOutgoingSingleAndMultipartSms() {
        val messageId = 101L
        val shortBody = "تست ارسال پیامک تکی"
        val resultSingle = SmsSender.sendSms(
            context = context,
            messageId = messageId,
            address = "+989121111111",
            body = shortBody,
            subId = 1,
            simSlot = 0,
            requestDeliveryReport = true
        )

        assertNotNull(resultSingle)

        val longBody = "پیامک انبوه تست با متن فارسی طولانی ".repeat(10)
        val resultMultipart = SmsSender.sendSms(
            context = context,
            messageId = 102L,
            address = "+989121111111",
            body = longBody,
            subId = 2,
            simSlot = 1,
            requestDeliveryReport = true
        )

        assertNotNull(resultMultipart)
    }

    @Test
    fun testDeliveryReportReceiverSentAndDelivered() = runBlocking {
        val testMsgId = 555L
        val testAddress = "+989129999999"

        val messageEntity = MessageEntity(
            id = testMsgId,
            threadId = testAddress.hashCode().toLong(),
            address = testAddress,
            body = "تست دلیوری ریپورت",
            timestamp = System.currentTimeMillis(),
            type = MessageType.OUTBOX.code,
            deliveryStatus = MessageStatus.PENDING.code
        )
        db.messageDao().insertMessage(messageEntity)

        // Simulate SENT broadcast
        val receiver = DeliveryReportReceiver()
        val sentIntent = Intent(DeliveryReportReceiver.ACTION_SMS_SENT).apply {
            putExtra(DeliveryReportReceiver.EXTRA_MESSAGE_ID, testMsgId)
            putExtra(DeliveryReportReceiver.EXTRA_PART_INDEX, 0)
            putExtra(DeliveryReportReceiver.EXTRA_TOTAL_PARTS, 1)
        }
        receiver.onReceive(context, sentIntent)

        // Simulate DELIVERED broadcast
        val deliveredIntent = Intent(DeliveryReportReceiver.ACTION_SMS_DELIVERED).apply {
            putExtra(DeliveryReportReceiver.EXTRA_MESSAGE_ID, testMsgId)
            putExtra(DeliveryReportReceiver.EXTRA_PART_INDEX, 0)
            putExtra(DeliveryReportReceiver.EXTRA_TOTAL_PARTS, 1)
        }
        receiver.onReceive(context, deliveredIntent)

        val updatedMsg = db.messageDao().getMessageById(testMsgId)
        assertNotNull("Updated message should exist", updatedMsg)
    }

    @Test
    fun testMmsSendWithAttachmentUri() = runBlocking {
        val testMsgId = 777L
        val testUri = Uri.parse("content://media/external/images/media/1234")
        
        val result = SmsSender.sendMms(
            context = context,
            messageId = testMsgId,
            address = "+989128888888",
            contentUri = testUri,
            subId = 1
        )

        assertNotNull(result)
        assertEquals(testMsgId, result.messageId)
    }

    @Test
    fun testPenetrationMaliciousInputResilience() = runBlocking {
        // Blank address or empty body should return immediate error without crash
        val invalidResult = SmsSender.sendSms(
            context = context,
            messageId = 999L,
            address = "",
            body = "",
            subId = -1
        )
        assertFalse("Invalid input should return isSuccess = false", invalidResult.isSuccess)
        assertEquals(-1, invalidResult.errorCode)

        // Test SQL injection attempt in address & body
        val sqlInjectionAddress = "'+OR+1=1;--"
        val sqlInjectionBody = "'; DROP TABLE messages; --"

        MessageDispatcher.onIncomingSms(
            context = context,
            address = sqlInjectionAddress,
            body = sqlInjectionBody
        )

        val msgs = db.messageDao().getAllMessagesSync().filter { it.address == sqlInjectionAddress }
        assertFalse("Database should sanitize SQL injection attempts safely", msgs.isEmpty())
    }
}
