package com.global.sms.core.repository

import com.global.sms.core.ai.summarizer.ConversationSummarizerEngine
import com.global.sms.data.dao.MessageDao
import com.global.sms.data.entity.MessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Smart Summary Repository that aggregates recent database messages
 * and dynamically calculates offline AI conversation summaries.
 */
class SmartSummaryRepository(
    private val messageDao: MessageDao
) {
    /**
     * Returns a live Flow of the calculated summary based on the latest database messages.
     */
    fun getOverallSummaryFlow(): Flow<String> {
        return messageDao.searchMessagesAdvanced(
            query = null,
            category = null,
            isOtpOnly = false,
            hasAttachmentOnly = false,
            isUnreadOnly = false,
            isPinnedOnly = false,
            isBankOnly = false,
            senderFilter = null,
            startDate = null,
            endDate = null,
            includeHidden = false
        ).map { messages ->
            if (messages.isEmpty()) {
                "صندوق ورودی شما خالی است. پیامک جدیدی برای خلاصه یافت نشد."
            } else {
                generateSummaryFromMessages(messages)
            }
        }
    }

    /**
     * Generates a concise summary for a specific message thread.
     */
    fun getThreadSummaryFlow(threadId: Long): Flow<String> {
        return messageDao.getMessagesForThread(threadId).map { messages ->
            ConversationSummarizerEngine.summarizeThread(messages)
        }
    }

    private fun generateSummaryFromMessages(messages: List<MessageEntity>): String {
        val todayMs = System.currentTimeMillis() - 24 * 3600 * 1000L
        val todayMessages = messages.filter { it.timestamp >= todayMs }

        val bankCount = todayMessages.count { msg ->
            val body = msg.body.lowercase()
            body.contains("واریز") || body.contains("برداشت") || body.contains("تراکنش") || body.contains("مانده")
        }

        val otpCount = todayMessages.count { msg ->
            val body = msg.body.lowercase()
            body.contains("کد ورود") || body.contains("کد تایید") || body.contains("رمز پویا") || body.contains("رمز عبور")
        }

        val totalToday = todayMessages.size

        return when {
            totalToday == 0 -> {
                val latest = messages.maxByOrNull { it.timestamp }
                if (latest != null) {
                    val snippet = latest.body.take(60).replace("\n", " ")
                    "آخرین پیام دریافت شده از «${latest.address}»: «$snippet...»"
                } else {
                    "پیامک جدیدی ثبت نشده است."
                }
            }
            bankCount > 0 && otpCount > 0 -> {
                "امروز $totalToday پیامک دریافت کرده‌اید: $otpCount کد تایید و $bankCount تراکنش بانکی ثبت شده است."
            }
            bankCount > 0 -> {
                "امروز $totalToday پیامک دریافت شده که $bankCount مورد مربوط به تراکنش‌های بانکی است."
            }
            otpCount > 0 -> {
                "امروز $totalToday پیامک دریافت شده که $otpCount مورد کد ورود و رمز یکبارمصرف است."
            }
            else -> {
                val latest = todayMessages.maxByOrNull { it.timestamp } ?: messages.first()
                val snippet = latest.body.take(60).replace("\n", " ")
                "امروز $totalToday پیامک دریافت شد. آخرین پیام: «$snippet...»"
            }
        }
    }
}
