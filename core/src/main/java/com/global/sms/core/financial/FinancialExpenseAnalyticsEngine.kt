package com.global.sms.core.financial

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.global.sms.core.parser.BankSmsAnalysis
import com.global.sms.core.parser.TransactionType
import com.global.sms.core.util.PersianUtils
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MonthlyBudgetSummary(
    val totalIncomeTomans: Long,
    val totalExpenseTomans: Long,
    val netBalanceTomans: Long,
    val transactionCount: Int,
    val monthlyBudgetTargetTomans: Long,
    val budgetConsumptionPercentage: Float,
    val isOverBudget: Boolean,
    val topSpendingBank: String?,
    val formattedIncome: String,
    val formattedExpense: String,
    val formattedNet: String
)

object FinancialExpenseAnalyticsEngine {

    private val numberFormat = DecimalFormat("#,###")

    fun calculateMonthlySummary(
        analyses: List<BankSmsAnalysis>,
        budgetTargetTomans: Long = 15_000_000L
    ): MonthlyBudgetSummary {
        var totalIncome = 0L
        var totalExpense = 0L
        val bankSpending = mutableMapOf<String, Long>()

        for (item in analyses) {
            when (item.transactionType) {
                TransactionType.CREDIT -> {
                    totalIncome += (item.amountTomans ?: 0L)
                }
                TransactionType.DEBIT -> {
                    val exp = (item.amountTomans ?: 0L)
                    totalExpense += exp
                    val bName = item.bankName
                    bankSpending[bName] = (bankSpending[bName] ?: 0L) + exp
                }
                else -> {}
            }
        }

        val netBalance = totalIncome - totalExpense
        val topBank = bankSpending.maxByOrNull { it.value }?.key

        val consumptionPct = if (budgetTargetTomans > 0) {
            ((totalExpense.toDouble() / budgetTargetTomans.toDouble()) * 100).toFloat().coerceIn(0f, 100f)
        } else 0f

        val isOver = totalExpense > budgetTargetTomans

        return MonthlyBudgetSummary(
            totalIncomeTomans = totalIncome,
            totalExpenseTomans = totalExpense,
            netBalanceTomans = netBalance,
            transactionCount = analyses.size,
            monthlyBudgetTargetTomans = budgetTargetTomans,
            budgetConsumptionPercentage = consumptionPct,
            isOverBudget = isOver,
            topSpendingBank = topBank,
            formattedIncome = PersianUtils.toPersianDigits(numberFormat.format(totalIncome)) + " تومان",
            formattedExpense = PersianUtils.toPersianDigits(numberFormat.format(totalExpense)) + " تومان",
            formattedNet = PersianUtils.toPersianDigits(numberFormat.format(netBalance)) + " تومان"
        )
    }

    /**
     * Exports transactions to CSV with UTF-8 BOM (\uFEFF) so Excel on Windows, Mac, and Android
     * correctly displays Persian/Arabic characters without encoding issues.
     */
    fun exportTransactionsToCsv(context: Context, analyses: List<BankSmsAnalysis>): File {
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val csvFile = File(exportDir, "transactions_$timestamp.csv")

        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())

        FileOutputStream(csvFile).use { fos ->
            // Write UTF-8 BOM
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            OutputStreamWriter(fos, Charsets.UTF_8).use { writer ->
                // CSV Headers
                writer.write("ردیف,بانک عامل,نوع تراکنش,مبلغ (تومان),مبلغ (ریال),شماره پیگیری,شماره کارت/حساب,مانده (تومان),تاریخ و زمان,متن پیامک\n")

                analyses.forEachIndexed { index, item ->
                    val typeStr = when (item.transactionType) {
                        TransactionType.CREDIT -> "واریز"
                        TransactionType.DEBIT -> "برداشت"
                        TransactionType.BALANCE_INQUIRY -> "مانده‌گیری"
                        TransactionType.OTP -> "رمز پویا"
                        else -> "سایر"
                    }

                    val dateStr = sdf.format(Date(item.timestamp))
                    val amountTomans = item.amountTomans?.toString() ?: "0"
                    val amountRials = item.amountRials?.toString() ?: "0"
                    val tracking = item.trackingNumber ?: "-"
                    val card = item.cardNumber ?: "-"
                    val balance = item.balanceTomans?.toString() ?: "-"
                    val cleanBody = item.rawBody.replace("\n", " ").replace(",", "،").replace("\"", "'")

                    writer.write("${index + 1},\"${item.bankName}\",\"$typeStr\",$amountTomans,$amountRials,\"$tracking\",\"$card\",$balance,\"$dateStr\",\"$cleanBody\"\n")
                }
                writer.flush()
            }
        }

        return csvFile
    }

    fun createShareCsvIntent(context: Context, csvFile: File): Intent {
        val fileUri = try {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", csvFile)
        } catch (e: Exception) {
            android.net.Uri.fromFile(csvFile)
        }

        return Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            putExtra(Intent.EXTRA_SUBJECT, "گزارش اکسل تراکنش‌های بانکی - Global SMS")
            putExtra(Intent.EXTRA_TEXT, "گزارش رسمی و جامع تراکنش‌های بانکی به پیوست ارسال می‌گردد.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
