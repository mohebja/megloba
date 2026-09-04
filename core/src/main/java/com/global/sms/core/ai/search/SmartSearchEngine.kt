package com.global.sms.core.ai.search

import com.global.sms.core.ai.brain.LocalAIBrain
import com.global.sms.core.ai.nlp.LocalNlpEngine
import com.global.sms.data.entity.MessageCategory
import com.global.sms.data.entity.MessageEntity
import java.util.Calendar
import java.util.Locale

data class SearchCriteria(
    val queryText: String? = null,
    val senderAddress: String? = null,
    val contactName: String? = null,
    val category: MessageCategory? = null,
    val isOtpOnly: Boolean = false,
    val isBankTransactionOnly: Boolean = false,
    val startTimestamp: Long? = null,
    val endTimestamp: Long? = null
)

data class SemanticSearchResult(
    val messages: List<MessageEntity>,
    val parsedIntent: String,
    val matchedKeywords: List<String>,
    val querySummary: String
)

/**
 * High-performance smart search engine supporting Persian character/digit normalization,
 * category filters, date ranges, semantic intent resolution, and AI Brain integration.
 */
object SmartSearchEngine {

    fun executeSemanticSearch(
        naturalQuery: String,
        messages: List<MessageEntity>,
        contactMap: Map<String, String> = emptyMap()
    ): SemanticSearchResult {
        val cleanQuery = LocalNlpEngine.normalizeDigits(naturalQuery.lowercase(Locale.ROOT)).trim()
        val lang = LocalAIBrain.detectLanguage(cleanQuery)

        val calendar = Calendar.getInstance()
        val now = System.currentTimeMillis()

        var isBankQuery = cleanQuery.contains("بانک") || cleanQuery.contains("وام") || cleanQuery.contains("تراکنش")
        var isRecentWeek = cleanQuery.contains("این هفته") || cleanQuery.contains("هفته جاری") || cleanQuery.contains("this week")
        var isImportantOnly = cleanQuery.contains("مهم") || cleanQuery.contains("فوری") || cleanQuery.contains("important")
        
        var targetContact: String? = null
        contactMap.forEach { (address, name) ->
            if (cleanQuery.contains(name.lowercase(Locale.ROOT))) {
                targetContact = name
            }
        }

        val matchedKeywords = mutableListOf<String>()
        if (isBankQuery) matchedKeywords.add("بانک/امور مالی")
        if (isRecentWeek) matchedKeywords.add("محدوده زمانی: این هفته")
        if (isImportantOnly) matchedKeywords.add("پیام‌های مهم/فوری")
        targetContact?.let { matchedKeywords.add("مخاطب: $it") }

        val oneWeekAgo = now - (7 * 24 * 60 * 60 * 1000L)

        val filtered = messages.filter { msg ->
            if (isBankQuery && msg.category != MessageCategory.BANK && !msg.body.contains("وام") && !msg.body.contains("بانک")) {
                return@filter false
            }
            if (isRecentWeek && msg.timestamp < oneWeekAgo) {
                return@filter false
            }
            if (isImportantOnly && !msg.body.contains("مهم") && !msg.body.contains("فوری") && !msg.body.contains("کد")) {
                return@filter false
            }
            if (targetContact != null) {
                val cName = contactMap[msg.address] ?: ""
                if (!cName.lowercase(Locale.ROOT).contains(targetContact.lowercase(Locale.ROOT))) {
                    return@filter false
                }
            }
            if (!isBankQuery && targetContact == null && !isRecentWeek && !isImportantOnly) {
                if (!LocalNlpEngine.normalizeDigits(msg.body.lowercase(Locale.ROOT)).contains(cleanQuery)) {
                    return@filter false
                }
            }
            true
        }.sortedByDescending { it.timestamp }

        val summary = "جستجوی هوشمند برای \"$naturalQuery\": تعداد ${filtered.size} پیام با اولویت بالا یافت شد."

        return SemanticSearchResult(
            messages = filtered,
            parsedIntent = if (isBankQuery) "BANK_LOAN_SEARCH" else if (targetContact != null) "CONTACT_HISTORY_SEARCH" else "SEMANTIC_QUERY",
            matchedKeywords = matchedKeywords,
            querySummary = summary
        )
    }

    fun searchMessages(
        messages: List<MessageEntity>,
        criteria: SearchCriteria,
        contactMap: Map<String, String> = emptyMap()
    ): List<MessageEntity> {
        val cleanQuery = criteria.queryText?.let { LocalNlpEngine.normalizeDigits(it).lowercase(Locale.ROOT).trim() }
        val cleanSender = criteria.senderAddress?.let { LocalNlpEngine.normalizeDigits(it).lowercase(Locale.ROOT).trim() }
        val cleanContact = criteria.contactName?.lowercase(Locale.ROOT)?.trim()

        return messages.filter { msg ->
            if (criteria.category != null && msg.category != criteria.category) {
                return@filter false
            }

            if (criteria.isOtpOnly && (msg.category != MessageCategory.OTP && msg.otpCode == null)) {
                return@filter false
            }

            if (criteria.isBankTransactionOnly && msg.category != MessageCategory.BANK && msg.category != MessageCategory.TRANSACTIONS) {
                return@filter false
            }

            if (criteria.startTimestamp != null && msg.timestamp < criteria.startTimestamp) {
                return@filter false
            }
            if (criteria.endTimestamp != null && msg.timestamp > criteria.endTimestamp) {
                return@filter false
            }

            if (cleanSender != null) {
                val msgSender = LocalNlpEngine.normalizeDigits(msg.address).lowercase(Locale.ROOT)
                if (!msgSender.contains(cleanSender)) {
                    return@filter false
                }
            }

            if (cleanContact != null) {
                val name = contactMap[msg.address]?.lowercase(Locale.ROOT) ?: ""
                if (!name.contains(cleanContact)) {
                    return@filter false
                }
            }

            if (!cleanQuery.isNullOrBlank()) {
                val normalizedBody = LocalNlpEngine.normalizeDigits(msg.body).lowercase(Locale.ROOT)
                val normalizedAddress = LocalNlpEngine.normalizeDigits(msg.address).lowercase(Locale.ROOT)
                val contactName = contactMap[msg.address]?.lowercase(Locale.ROOT) ?: ""

                val matchesBody = normalizedBody.contains(cleanQuery)
                val matchesAddress = normalizedAddress.contains(cleanQuery)
                val matchesContact = contactName.contains(cleanQuery)

                if (!matchesBody && !matchesAddress && !matchesContact) {
                    return@filter false
                }
            }

            true
        }
    }
}
