package com.global.sms.core.search

import com.global.sms.data.entity.MessageCategory
import com.global.sms.data.entity.MessageEntity

data class SearchFilterCriteria(
    val query: String = "",
    val categories: Set<MessageCategory> = emptySet(),
    val isUnreadOnly: Boolean = false,
    val isFavoritesOnly: Boolean = false,
    val isPinnedOnly: Boolean = false,
    val hasAttachmentOnly: Boolean = false,
    val isOtpOnly: Boolean = false,
    val isBankOnly: Boolean = false,
    val isHiddenOnly: Boolean = false,
    val isArchivedOnly: Boolean = false,
    val simSlot: Int? = null,
    val deliveryStatus: Int? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val senderFilter: String? = null
) {
    val isEmpty: Boolean
        get() = query.isBlank() && categories.isEmpty() && !isUnreadOnly &&
                !isFavoritesOnly && !isPinnedOnly && !hasAttachmentOnly &&
                !isOtpOnly && !isBankOnly && !isHiddenOnly && !isArchivedOnly &&
                simSlot == null && deliveryStatus == null && startDate == null &&
                endDate == null && senderFilter.isNullInformed()

    private fun String?.isNullInformed() = this.isNullOrBlank()
}

class SearchFilterEngine {

    fun filterMessages(messages: List<MessageEntity>, criteria: SearchFilterCriteria): List<MessageEntity> {
        if (criteria.isEmpty) return messages

        val normalizedQuery = SearchQueryParser.normalizeText(criteria.query).lowercase()

        return messages.filter { msg ->
            // Text query match
            if (normalizedQuery.isNotBlank()) {
                val normalizedBody = SearchQueryParser.normalizeText(msg.body).lowercase()
                val normalizedAddress = SearchQueryParser.normalizeText(msg.address).lowercase()
                if (!normalizedBody.contains(normalizedQuery) && !normalizedAddress.contains(normalizedQuery)) {
                    return@filter false
                }
            }

            // Categories
            if (criteria.categories.isNotEmpty() && !criteria.categories.contains(msg.category)) {
                return@filter false
            }

            // Unread
            if (criteria.isUnreadOnly && msg.isRead) {
                return@filter false
            }

            // Pinned
            if (criteria.isPinnedOnly && !msg.isPinned) {
                return@filter false
            }

            // OTP Only
            if (criteria.isOtpOnly && msg.category != MessageCategory.OTP && msg.otpCode.isNullOrEmpty()) {
                return@filter false
            }

            // Bank Only
            if (criteria.isBankOnly && msg.category != MessageCategory.BANK) {
                return@filter false
            }

            // Attachment Only
            if (criteria.hasAttachmentOnly && msg.attachmentUri.isNullOrEmpty() && !msg.isMms) {
                return@filter false
            }

            // SIM Slot
            if (criteria.simSlot != null && msg.simSlot != criteria.simSlot) {
                return@filter false
            }

            // Delivery Status
            if (criteria.deliveryStatus != null && msg.deliveryStatus != criteria.deliveryStatus) {
                return@filter false
            }

            // Sender Filter
            if (!criteria.senderFilter.isNullOrBlank()) {
                val normSender = SearchQueryParser.normalizeText(criteria.senderFilter)
                if (!msg.address.contains(normSender, ignoreCase = true)) {
                    return@filter false
                }
            }

            // Date range
            if (criteria.startDate != null && msg.timestamp < criteria.startDate) {
                return@filter false
            }
            if (criteria.endDate != null && msg.timestamp > criteria.endDate) {
                return@filter false
            }

            // Private Vault Safeguard
            if (msg.isHidden && !criteria.isHiddenOnly) {
                return@filter false
            }
            if (criteria.isHiddenOnly && !msg.isHidden) {
                return@filter false
            }

            true
        }
    }
}
