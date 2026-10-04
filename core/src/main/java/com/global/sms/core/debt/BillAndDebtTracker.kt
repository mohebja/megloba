package com.global.sms.core.debt

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import com.global.sms.core.util.PersianUtils
import java.text.DecimalFormat
import java.util.UUID
import java.util.regex.Pattern
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class DebtType(val titleFa: String) {
    UTILITY_BILL("قبض خدماتی"),
    LOAN_INSTALLMENT("قسط وام و تسهیلات"),
    SAYAD_CHECK("چک صیادی"),
    OTHER("سایر بدهی‌ها")
}

data class BillAndDebtItem(
    val id: String = UUID.randomUUID().toString(),
    val messageId: Long = 0L,
    val sender: String = "",
    val title: String = "",
    val type: DebtType = DebtType.OTHER,
    val billId: String? = null,
    val paymentId: String? = null,
    val checkId: String? = null,
    val loanNumber: String? = null,
    val amountTomans: Long? = null,
    val dueTimestamp: Long? = null,
    val formattedDueDate: String? = null,
    val rawBody: String = "",
    val isPaid: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

object BillAndDebtTracker {

    private val numberFormat = DecimalFormat("#,###")

    // Patterns for Utility Bills
    private val BILL_ID_PATTERN = Pattern.compile("(?:شناسه قبض|ش قبض|قبض)[:\\s]*([0-9]{6,15})")
    private val PAY_ID_PATTERN = Pattern.compile("(?:شناسه پرداخت|ش پرداخت)[:\\s]*([0-9]{5,15})")

    // Patterns for Sayad Checks
    private val SAYAD_CHECK_PATTERN = Pattern.compile("(?:شناسه صیادی|چک صیادی|صیاد|چک به شماره|شناسه چک)[:\\s]*([0-9]{16})")

    // Patterns for Bank Loans / Installments
    private val LOAN_NUMBER_PATTERN = Pattern.compile("(?:تسهیلات|شماره وام|وام|قرارداد)[:\\s]*([0-9]{6,20})")

    // Amount Pattern
    private val AMOUNT_PATTERN = Pattern.compile("(?:مبلغ|مبلغ قابل پرداخت|مبلغ قسط|مبلغ چک)[:\\s]*([0-9,٬]+)\\s*(ریال|تومان|Rials|Toman)?")

    // Due Date Pattern (e.g. 1403/07/15 or 1403-07-15)
    private val DUE_DATE_PATTERN = Pattern.compile("(?:مهلت پرداخت|سررسید|تاریخ سررسید|مهلت)[:\\s]*([0-9]{4}[/\\-][0-9]{1,2}[/\\-][0-9]{1,2})")

    private val _trackedItems = MutableStateFlow<List<BillAndDebtItem>>(emptyList())
    val trackedItems: StateFlow<List<BillAndDebtItem>> = _trackedItems.asStateFlow()

    fun parseMessageForDebt(sender: String, body: String, messageId: Long = 0L, timestamp: Long = System.currentTimeMillis()): BillAndDebtItem? {
        val normalizedBody = PersianUtils.toEnglishDigits(body)

        // 1. Check for Sayad Check
        val sayadMatcher = SAYAD_CHECK_PATTERN.matcher(normalizedBody)
        if (sayadMatcher.find()) {
            val checkId = sayadMatcher.group(1)
            val amountTomans = extractAmountTomans(normalizedBody)
            val dueDateStr = extractDueDateString(normalizedBody)
            return BillAndDebtItem(
                messageId = messageId,
                sender = sender,
                title = "چک صیادی ($checkId)",
                type = DebtType.SAYAD_CHECK,
                checkId = checkId,
                amountTomans = amountTomans,
                formattedDueDate = dueDateStr,
                rawBody = body,
                timestamp = timestamp
            )
        }

        // 2. Check for Utility Bill
        val billIdMatcher = BILL_ID_PATTERN.matcher(normalizedBody)
        val payIdMatcher = PAY_ID_PATTERN.matcher(normalizedBody)
        if (billIdMatcher.find() && payIdMatcher.find()) {
            val billId = billIdMatcher.group(1)
            val payId = payIdMatcher.group(1)
            val amountTomans = extractAmountTomans(normalizedBody)
            val dueDateStr = extractDueDateString(normalizedBody)
            val title = detectBillTitle(sender, body)

            return BillAndDebtItem(
                messageId = messageId,
                sender = sender,
                title = title,
                type = DebtType.UTILITY_BILL,
                billId = billId,
                paymentId = payId,
                amountTomans = amountTomans,
                formattedDueDate = dueDateStr,
                rawBody = body,
                timestamp = timestamp
            )
        }

        // 3. Check for Loan Installment
        val isLoanMessage = body.contains("قسط") || body.contains("تسهیلات") || body.contains("سررسید وام")
        if (isLoanMessage) {
            val loanMatcher = LOAN_NUMBER_PATTERN.matcher(normalizedBody)
            val loanNumber = if (loanMatcher.find()) loanMatcher.group(1) else null
            val amountTomans = extractAmountTomans(normalizedBody)
            val dueDateStr = extractDueDateString(normalizedBody)
            val bankTitle = detectBankTitle(sender, body)

            return BillAndDebtItem(
                messageId = messageId,
                sender = sender,
                title = "قسط تسهیلات $bankTitle",
                type = DebtType.LOAN_INSTALLMENT,
                loanNumber = loanNumber,
                amountTomans = amountTomans,
                formattedDueDate = dueDateStr,
                rawBody = body,
                timestamp = timestamp
            )
        }

        return null
    }

    private fun detectBillTitle(sender: String, body: String): String {
        return when {
            body.contains("برق") || sender.contains("BARGH", ignoreCase = true) -> "قبض برق"
            body.contains("گاز") || sender.contains("GAS", ignoreCase = true) -> "قبض گاز"
            body.contains("آب") || sender.contains("AB", ignoreCase = true) -> "قبض آب و فاضلاب"
            body.contains("تلفن ثابت") || body.contains("مخابرات") -> "قبض تلفن ثابت"
            body.contains("همراه اول") || sender.contains("MCI", ignoreCase = true) -> "قبض همراه اول"
            body.contains("ایرانسل") || sender.contains("Irancell", ignoreCase = true) -> "قبض ایرانسل"
            body.contains("رایتل") || sender.contains("Rightel", ignoreCase = true) -> "قبض رایتل"
            body.contains("شهرداری") || body.contains("عوارض") -> "عوارض شهرداری / خودرو"
            else -> "قبض خدماتی"
        }
    }

    private fun detectBankTitle(sender: String, body: String): String {
        return when {
            body.contains("مسکن") || sender.contains("MASKAN", ignoreCase = true) -> "بانک مسکن"
            body.contains("ملی") || sender.contains("MELLI", ignoreCase = true) -> "بانک ملی"
            body.contains("ملت") || sender.contains("MELLAT", ignoreCase = true) -> "بانک ملت"
            body.contains("صادرات") || sender.contains("SADERAT", ignoreCase = true) -> "بانک صادرات"
            body.contains("تجارت") || sender.contains("TEJARAT", ignoreCase = true) -> "بانک تجارت"
            body.contains("رسالت") -> "بانک قرض‌الحسنه رسالت"
            body.contains("مهر ایران") -> "بانک قرض‌الحسنه مهر ایران"
            body.contains("سپه") -> "بانک سپه"
            body.contains("سامان") -> "بانک سامان"
            body.contains("پاسارگاد") -> "بانک پاسارگاد"
            else -> "بانک"
        }
    }

    private fun extractAmountTomans(text: String): Long? {
        val matcher = AMOUNT_PATTERN.matcher(text)
        if (matcher.find()) {
            val amountRaw = matcher.group(1)?.replace(",", "")?.replace("٬", "")?.trim()
            val unit = matcher.group(2)
            val amountLong = amountRaw?.toLongOrNull() ?: return null
            return if (unit?.contains("ریال", ignoreCase = true) == true || unit?.contains("Rials", ignoreCase = true) == true) {
                amountLong / 10
            } else {
                amountLong
            }
        }
        return null
    }

    private fun extractDueDateString(text: String): String? {
        val matcher = DUE_DATE_PATTERN.matcher(text)
        return if (matcher.find()) matcher.group(1) else null
    }

    fun addItem(item: BillAndDebtItem) {
        val current = _trackedItems.value.toMutableList()
        // avoid duplicates
        val exists = current.any {
            (it.billId != null && it.billId == item.billId && it.paymentId == item.paymentId) ||
            (it.checkId != null && it.checkId == item.checkId) ||
            (it.loanNumber != null && it.loanNumber == item.loanNumber && it.amountTomans == item.amountTomans)
        }
        if (!exists) {
            current.add(0, item)
            _trackedItems.value = current
        }
    }

    fun togglePaidStatus(itemId: String) {
        val current = _trackedItems.value.map {
            if (it.id == itemId) it.copy(isPaid = !it.isPaid) else it
        }
        _trackedItems.value = current
    }

    fun removeItem(itemId: String) {
        _trackedItems.value = _trackedItems.value.filter { it.id != itemId }
    }

    fun formatMoneyTomans(amount: Long?, usePersianDigits: Boolean): String {
        if (amount == null) return "نامشخص"
        val formatted = "${numberFormat.format(amount)} تومان"
        return if (usePersianDigits) PersianUtils.toPersianDigits(formatted) else formatted
    }

    fun createCalendarReminderIntent(item: BillAndDebtItem): Intent {
        return Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, item.title)
            putExtra(CalendarContract.Events.DESCRIPTION, "یادآوری پرداخت سررسید:\n${item.rawBody}")
            putExtra(CalendarContract.Events.EVENT_LOCATION, "Global SMS Financial")
            putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
            putExtra(CalendarContract.Events.HAS_ALARM, 1)
        }
    }

    fun createPaymentUssdIntent(item: BillAndDebtItem): Intent? {
        if (item.billId != null && item.paymentId != null) {
            // USSD code for bill payment
            val encodedUssd = Uri.encode("*733*${item.billId}*${item.paymentId}#")
            return Intent(Intent.ACTION_DIAL, Uri.parse("tel:$encodedUssd"))
        }
        return null
    }
}
