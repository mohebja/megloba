package com.global.sms

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.global.sms.core.financial.FinancialExpenseAnalyticsEngine
import com.global.sms.core.parser.BankSmsAnalysis
import com.global.sms.core.parser.TransactionType
import com.global.sms.core.postal.PostalTrackingEngine
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Sprint19_FinancialAndPostalSuiteTest {

    // -------------------------------------------------------------
    // 1. Monthly Expense & Budget Analytics Tests
    // -------------------------------------------------------------
    @Test
    fun testFinancialExpenseAnalytics_MonthlySummaryAndBudget() {
        val sampleAnalyses = listOf(
            BankSmsAnalysis(
                messageId = 1L,
                sender = "MELLI",
                rawBody = "واریز ۵,۰۰۰,۰۰۰ تومان",
                isBankMessage = true,
                bankName = "بانک ملی",
                transactionType = TransactionType.CREDIT,
                amountTomans = 5_000_000L,
                amountRials = 50_000_000L
            ),
            BankSmsAnalysis(
                messageId = 2L,
                sender = "MELLAT",
                rawBody = "برداشت ۱,۵۰۰,۰۰۰ تومان",
                isBankMessage = true,
                bankName = "بانک ملت",
                transactionType = TransactionType.DEBIT,
                amountTomans = 1_500_000L,
                amountRials = 15_000_000L
            ),
            BankSmsAnalysis(
                messageId = 3L,
                sender = "MELLAT",
                rawBody = "برداشت ۵۰۰,۰۰۰ تومان",
                isBankMessage = true,
                bankName = "بانک ملت",
                transactionType = TransactionType.DEBIT,
                amountTomans = 500_000L,
                amountRials = 5_000_000L
            )
        )

        val summary = FinancialExpenseAnalyticsEngine.calculateMonthlySummary(
            analyses = sampleAnalyses,
            budgetTargetTomans = 10_000_000L
        )

        assertEquals(5_000_000L, summary.totalIncomeTomans)
        assertEquals(2_000_000L, summary.totalExpenseTomans)
        assertEquals(3_000_000L, summary.netBalanceTomans)
        assertEquals(20f, summary.budgetConsumptionPercentage, 0.1f)
        assertFalse(summary.isOverBudget)
        assertEquals("بانک ملت", summary.topSpendingBank)

        // Test over budget scenario
        val overBudgetSummary = FinancialExpenseAnalyticsEngine.calculateMonthlySummary(
            analyses = sampleAnalyses,
            budgetTargetTomans = 1_000_000L
        )
        assertTrue(overBudgetSummary.isOverBudget)
        assertEquals(100f, overBudgetSummary.budgetConsumptionPercentage, 0.1f)
    }

    // -------------------------------------------------------------
    // 2. CSV Export with UTF-8 BOM
    // -------------------------------------------------------------
    @Test
    fun testFinancialExpenseAnalytics_CsvExportWithUtf8Bom() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sampleAnalyses = listOf(
            BankSmsAnalysis(
                messageId = 101L,
                sender = "BLU",
                rawBody = "خرید از فروشگاه به مبلغ ۳۵۰,۰۰۰ تومان",
                isBankMessage = true,
                bankName = "بلوبانک",
                transactionType = TransactionType.DEBIT,
                amountTomans = 350_000L,
                amountRials = 3_500_000L,
                trackingNumber = "99887766",
                cardNumber = "۶۲۱۹-****-****-۴۴۵۵"
            )
        )

        val csvFile = FinancialExpenseAnalyticsEngine.exportTransactionsToCsv(context, sampleAnalyses)
        assertTrue(csvFile.exists())
        assertTrue(csvFile.length() > 0)

        val rawBytes = csvFile.readBytes()
        // Verify UTF-8 BOM header: 0xEF, 0xBB, 0xBF
        assertEquals(0xEF.toByte(), rawBytes[0])
        assertEquals(0xBB.toByte(), rawBytes[1])
        assertEquals(0xBF.toByte(), rawBytes[2])

        val content = csvFile.readText(Charsets.UTF_8)
        assertTrue(content.contains("ردیف,بانک عامل,نوع تراکنش"))
        assertTrue(content.contains("بلوبانک"))
        assertTrue(content.contains("برداشت"))
        assertTrue(content.contains("350000"))
        assertTrue(content.contains("99887766"))

        val shareIntent = FinancialExpenseAnalyticsEngine.createShareCsvIntent(context, csvFile)
        assertEquals(Intent.ACTION_SEND, shareIntent.action)
        assertEquals("text/csv", shareIntent.type)
        assertNotNull(shareIntent.getParcelableExtra(Intent.EXTRA_STREAM, android.net.Uri::class.java))
    }

    // -------------------------------------------------------------
    // 3. Postal Tracking Code Extraction & Intent
    // -------------------------------------------------------------
    @Test
    fun testPostalTrackingEngine_CodeExtractionAndIntent() {
        val sampleSms = "مشتری گرامی، بسته پستی شما با شماره مرسوله ۱۹۸۷۶۵۴۳۲۱۰۹۸۷۶۵۴۳۲۱۰۹۸۷ ارسال شد."
        val postalInfo = PostalTrackingEngine.extractPostalTracking(sampleSms)

        assertNotNull(postalInfo)
        postalInfo?.let {
            assertEquals("198765432109876543210987", it.trackingCode)
            assertEquals("https://tracking.post.ir/?id=198765432109876543210987", it.trackingUrl)
            assertTrue(it.formattedCode.contains("۱۹۸۷۶۵۴۳۲۱۰۹۸۷۶۵۴۳۲۱۰۹۸۷"))

            val intent = PostalTrackingEngine.createTrackingIntent(it.trackingCode)
            assertEquals(Intent.ACTION_VIEW, intent.action)
            assertEquals("https://tracking.post.ir/?id=198765432109876543210987", intent.dataString)
        }

        // Negative check: regular OTP message without shipping context must not produce postal info
        val otpSms = "کد تایید ورود: ۵۴۳۲۱"
        val noPostal = PostalTrackingEngine.extractPostalTracking(otpSms)
        assertNull(noPostal)
    }
}
