package com.global.sms.core.enterprise

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

data class CampaignRecipient(
    val phone: String,
    val name: String = "",
    val company: String = "",
    val customValue: String = ""
)

data class EnterpriseTemplate(
    val id: Long,
    val title: String,
    val category: String,
    val content: String,
    val variableTags: List<String>
)

data class CampaignAnalytics(
    val campaignTitle: String,
    val totalRecipients: Int,
    val sentCount: Int,
    val deliveredCount: Int,
    val failedCount: Int,
    val responseRatePercentage: Float
)

/**
 * Enterprise Campaign Engine for Global SMS.
 * Manages bulk SMS merge tags, CSV recipient parsing, personalized broadcast generation,
 * and delivery rate analytics.
 */
object EnterpriseCampaignEngine {

    /**
     * Replace merge tags in template body (e.g. {Name}, {Company}, {Value}) with recipient values.
     */
    fun personalizeMessage(
        templateBody: String,
        recipient: CampaignRecipient
    ): String {
        return templateBody
            .replace("{Name}", recipient.name.ifBlank { "مشتری گرامی" })
            .replace("{Company}", recipient.company.ifBlank { "مجموعه" })
            .replace("{Value}", recipient.customValue)
            .replace("{Phone}", recipient.phone)
    }

    /**
     * Parse CSV stream into Campaign Recipients.
     */
    suspend fun parseCsvRecipients(inputStream: InputStream): List<CampaignRecipient> = withContext(Dispatchers.IO) {
        val recipients = mutableListOf<CampaignRecipient>()
        inputStream.bufferedReader().useLines { lines ->
            lines.drop(1).forEach { line -> // Skip header line
                val tokens = line.split(",").map { it.trim().removeSurrounding("\"") }
                if (tokens.isNotEmpty() && tokens[0].isNotBlank()) {
                    val phone = tokens[0]
                    val name = tokens.getOrElse(1) { "" }
                    val company = tokens.getOrElse(2) { "" }
                    val custom = tokens.getOrElse(3) { "" }
                    recipients.add(CampaignRecipient(phone, name, company, custom))
                }
            }
        }
        recipients
    }

    /**
     * Default Enterprise Templates
     */
    fun getDefaultTemplates(): List<EnterpriseTemplate> {
        return listOf(
            EnterpriseTemplate(
                id = 1,
                title = "یادآوری سررسید فاکتور",
                category = "مالی و حسابداری",
                content = "جناب آقای/خانم {Name}، با سلام. سررسید فاکتور شما در شرکت {Company} فرا رسیده است. جهت تسویه حساب اقدام فرمایید.",
                variableTags = listOf("{Name}", "{Company}")
            ),
            EnterpriseTemplate(
                id = 2,
                title = "تایید رزرو نوبت",
                category = "خدماتی و پزشکی",
                content = "مشتری عزیز {Name}، نوبت شما برای تاریخ {Value} در مجموعه {Company} تایید شد.",
                variableTags = listOf("{Name}", "{Company}", "{Value}")
            ),
            EnterpriseTemplate(
                id = 3,
                title = "تبریک مناسبت ویژه",
                category = "بازاریابی و مشتریان",
                content = "جناب {Name} عزیز، فرارسیدن مناسبت ویژه را از طرف {Company} به شما تبریک می‌گوییم.",
                variableTags = listOf("{Name}", "{Company}")
            )
        )
    }
}
