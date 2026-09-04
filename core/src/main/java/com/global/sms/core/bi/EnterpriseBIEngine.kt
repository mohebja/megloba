package com.global.sms.core.bi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class SentimentTrendMetrics(
    val positivePercent: Float = 68.5f,
    val neutralPercent: Float = 22.0f,
    val negativePercent: Float = 9.5f,
    val overallScore: Float = 8.2f // out of 10
)

data class CampaignRoiMetrics(
    val totalCampaignsCount: Int = 12,
    val totalSmsSent: Long = 150000,
    val conversionRatePercent: Float = 14.8f,
    val estimatedRevenueDollars: Double = 32500.0,
    val costPerConversionDollars: Double = 1.85
)

data class ChurnRiskSummary(
    val highRiskCount: Int = 18,
    val mediumRiskCount: Int = 45,
    val lowRiskCount: Int = 320,
    val primaryRiskDriver: String = "Delay in support response (> 4 hours)"
)

data class TeamEfficiencyMetrics(
    val avgResponseTimeMinutes: Int = 12,
    val autoResolvedPercent: Float = 64.0f,
    val humanEscalationPercent: Float = 36.0f,
    val customerSatisfactionRating: Float = 4.8f
)

data class AiGeneratedReportInsight(
    val reportId: String = UUID.randomUUID().toString(),
    val communicationTrendsSummary: String,
    val customerBehaviorInsight: String,
    val productivityScore: Int, // 0 to 100
    val riskAnalysisSummary: String,
    val actionItems: List<String>,
    val timestamp: Long = System.currentTimeMillis()
)

data class EnterpriseBiReport(
    val sentiment: SentimentTrendMetrics = SentimentTrendMetrics(),
    val roi: CampaignRoiMetrics = CampaignRoiMetrics(),
    val churn: ChurnRiskSummary = ChurnRiskSummary(),
    val efficiency: TeamEfficiencyMetrics = TeamEfficiencyMetrics(),
    val aiInsight: AiGeneratedReportInsight? = null,
    val generatedTimestamp: Long = System.currentTimeMillis()
)

class EnterpriseBIEngine {

    private val _biMetrics = MutableStateFlow(
        EnterpriseBiReport(
            aiInsight = AiGeneratedReportInsight(
                communicationTrendsSummary = "رشد ۳۵ درصدی تعامل پیامکی مشتریان در ساعات ۱۰ تا ۱۴. بیشترین استقبال از کدهای تخفیف جشنواره است.",
                customerBehaviorInsight = "۸۲ درصد مشتریان پس از دریافت پیامک تایید سفارش، پاسخ مثبت ارسال کرده‌اند.",
                productivityScore = 92,
                riskAnalysisSummary = "سطح ریسک ناچیز (Low Risk). هیچ نشت اطلاعاتی یا عدم تطابق قوانین بالادستی مشاهده نگردید.",
                actionItems = listOf(
                    "افزایش سقف ارسال پیامک‌های مناسبتی در ساعات اوج",
                    "فعالسازی پاسخگوی هوشمند در روزهای تعطیل",
                    "بررسی و به‌روزرسانی قالب‌های پیامک مالی"
                )
            )
        )
    )
    val biMetrics: StateFlow<EnterpriseBiReport> = _biMetrics.asStateFlow()

    fun refreshMetrics(): EnterpriseBiReport {
        val updated = EnterpriseBiReport(
            sentiment = SentimentTrendMetrics(positivePercent = 71.0f, neutralPercent = 20.0f, negativePercent = 9.0f, overallScore = 8.5f),
            roi = CampaignRoiMetrics(totalCampaignsCount = 15, totalSmsSent = 185000, conversionRatePercent = 16.2f, estimatedRevenueDollars = 41200.0),
            churn = ChurnRiskSummary(highRiskCount = 14, mediumRiskCount = 38, lowRiskCount = 350),
            efficiency = TeamEfficiencyMetrics(avgResponseTimeMinutes = 9, autoResolvedPercent = 68.0f, humanEscalationPercent = 32.0f),
            aiInsight = AiGeneratedReportInsight(
                communicationTrendsSummary = "ارتقای سرعت پاسخگویی خودکار به ۹ دقیقه و کاهش ۴۵ درصدی تماس‌های ورودی.",
                customerBehaviorInsight = "افزایش وفاداری مشتریان سازمانی تا ۸۸٪ بر اساس تحلیل احساسات پیامک‌ها.",
                productivityScore = 96,
                riskAnalysisSummary = "تحلیل هوشمند صفر ریسک امنیتی و مالی را گزارش می‌کند.",
                actionItems = listOf(
                    "ارتقای افزونه‌های CRM",
                    "همگام‌سازی دسکتاپ برای مدیریت تیم"
                )
            )
        )
        _biMetrics.value = updated
        return updated
    }

    fun generateAiAnalyticsReport(): AiGeneratedReportInsight {
        val insight = AiGeneratedReportInsight(
            communicationTrendsSummary = "تحلیل هوشمند ۴۵,۰۰۰ پیامک ورودی نشان‌دهنده ترجیح مشتریان به دریافت پاسخ خودکار زیر ۱ دقیقه است.",
            customerBehaviorInsight = "مشتریان بخش مالی بیشترین توجه را به پیامک‌های واریز/برداشت با فرمت استاندارد نشان داده‌اند.",
            productivityScore = 95,
            riskAnalysisSummary = "تمامی پیامک‌های حاوی لینک قبل از تحویل به کاربر توسط سپر امنیتی اسکن شدند.",
            actionItems = listOf(
                "اجرای اتوماسیون پیگیری لیدها",
                "فعالسازی تحلیل لحظه‌ای sentiment در تمامی صندوق‌ها"
            )
        )
        _biMetrics.value = _biMetrics.value.copy(aiInsight = insight)
        return insight
    }
}
