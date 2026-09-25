package com.global.sms.core.export

import com.global.sms.data.entity.FinancialTransactionEntity
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FinancialExportEngine {

    private val numberFormat = DecimalFormat("#,###")
    private val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())

    fun generateCsvExport(
        transactions: List<FinancialTransactionEntity>,
        targetFile: File
    ): File {
        val totalExpense = transactions.filter { it.transactionType == "EXPENSE" }.sumOf { it.amount }
        val totalIncome = transactions.filter { it.transactionType == "INCOME" }.sumOf { it.amount }
        val netBalance = totalIncome - totalExpense

        val sb = StringBuilder()
        // UTF-8 BOM for Persian characters support in Microsoft Excel
        sb.append('\uFEFF')

        sb.append("گزارش تراز مالی و مخارج - Global SMS\n")
        sb.append("تاریخ استخراج: ${dateFormat.format(Date())}\n")
        sb.append("مجموع واریزی‌ها: ${numberFormat.format(totalIncome.toLong())} تومان\n")
        sb.append("مجموع مخارج و هزینه‌ها: ${numberFormat.format(totalExpense.toLong())} تومان\n")
        sb.append("تراز خالص: ${numberFormat.format(netBalance.toLong())} تومان\n")
        sb.append("\n")

        // CSV Header
        sb.append("شناسه,بانک,نوع تراکنش,مبلغ (تومان),مانده حساب (تومان),شماره کارت/حساب,دسته‌بندی,تاریخ و زمان\n")

        transactions.forEach { tx ->
            val typeFa = if (tx.transactionType == "EXPENSE") "برداشت / هزینه" else "واریز / درآمد"
            val balanceStr = tx.balanceAfter?.let { numberFormat.format(it.toLong()) } ?: "-"
            val cardStr = tx.cardOrAccount ?: "-"
            val catStr = tx.category
            val dateStr = dateFormat.format(Date(tx.timestamp))

            sb.append("${tx.id},\"${tx.bankName}\",\"$typeFa\",${tx.amount.toLong()},\"$balanceStr\",\"$cardStr\",\"$catStr\",\"$dateStr\"\n")
        }

        targetFile.parentFile?.mkdirs()
        targetFile.writeText(sb.toString(), Charsets.UTF_8)
        return targetFile
    }

    fun generateTextSummary(transactions: List<FinancialTransactionEntity>): String {
        val totalExpense = transactions.filter { it.transactionType == "EXPENSE" }.sumOf { it.amount }
        val totalIncome = transactions.filter { it.transactionType == "INCOME" }.sumOf { it.amount }
        val netBalance = totalIncome - totalExpense

        return """
            📊 خلاصه هوش مالی Global SMS
            ------------------------------------
            🔹 کل واریزی‌ها: ${numberFormat.format(totalIncome.toLong())} تومان
            🔸 کل مخارج: ${numberFormat.format(totalExpense.toLong())} تومان
            💰 تراز خالص: ${if (netBalance >= 0) "+" else ""}${numberFormat.format(netBalance.toLong())} تومان
            📝 تعداد کل تراکنش‌ها: ${transactions.size}
            📅 تاریخ گزارش: ${dateFormat.format(Date())}
        """.trimIndent()
    }
}
