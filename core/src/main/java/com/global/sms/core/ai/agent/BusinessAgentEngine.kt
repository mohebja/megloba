package com.global.sms.core.ai.agent

import com.global.sms.data.entity.MessageEntity

data class CustomerInquiryAnalysis(
    val isCustomerInquiry: Boolean,
    val isPendingReply: Boolean,
    val isSalesOpportunity: Boolean,
    val customerAttentionScore: Int, // 0 to 100
    val suggestedFollowUp: String
)

data class EnterpriseBusinessMetrics(
    val totalCustomerAttentionScore: Int,
    val pendingRepliesCount: Int,
    val salesOpportunitiesCount: Int,
    val highPriorityCustomersCount: Int
)

object BusinessAgentEngine {

    fun analyzeMessageForBusiness(message: MessageEntity): CustomerInquiryAnalysis {
        val text = message.body
        val lower = text.lowercase()

        val isSales = text.contains("قیمت") || text.contains("کاتالوگ") || text.contains("خرید") || text.contains("سفارش") || text.contains("تخفیف") || lower.contains("price") || lower.contains("order")
        val isInquiry = isSales || text.contains("راهنمایی") || text.contains("سؤال") || text.contains("مشاوره") || text.contains("؟")
        val isPending = isInquiry && message.type == 1 // Incoming unreplied

        var attentionScore = 40
        if (isInquiry) attentionScore += 25
        if (isSales) attentionScore += 25
        if (isPending) attentionScore += 10
        attentionScore = attentionScore.coerceIn(0, 100)

        val followUp = when {
            isSales -> "ارسال پیشنهاد قیمت و پیگیری تا ۲۴ ساعت آینده"
            isInquiry -> "پاسخ‌دهی فنی به سوالات مشتری"
            else -> "ثبت در سوابق تعامل با مشتری"
        }

        return CustomerInquiryAnalysis(
            isCustomerInquiry = isInquiry,
            isPendingReply = isPending,
            isSalesOpportunity = isSales,
            customerAttentionScore = attentionScore,
            suggestedFollowUp = followUp
        )
    }

    fun computeEnterpriseMetrics(messages: List<MessageEntity>): EnterpriseBusinessMetrics {
        var pendingCount = 0
        var salesCount = 0
        var sumAttentionScore = 0
        var highPriorityCount = 0

        for (msg in messages) {
            val analysis = analyzeMessageForBusiness(msg)
            if (analysis.isPendingReply) pendingCount++
            if (analysis.isSalesOpportunity) salesCount++
            sumAttentionScore += analysis.customerAttentionScore
            if (analysis.customerAttentionScore >= 80) highPriorityCount++
        }

        val avgAttentionScore = if (messages.isNotEmpty()) sumAttentionScore / messages.size else 85

        return EnterpriseBusinessMetrics(
            totalCustomerAttentionScore = avgAttentionScore,
            pendingRepliesCount = pendingCount,
            salesOpportunitiesCount = salesCount,
            highPriorityCustomersCount = highPriorityCount
        )
    }
}
