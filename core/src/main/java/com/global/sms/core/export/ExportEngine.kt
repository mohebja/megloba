package com.global.sms.core.export

import android.content.Context
import com.global.sms.data.entity.MessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ExportFormat {
    PDF_HTML,
    CSV,
    EXCEL_CSV,
    HTML_DOCUMENT
}

data class ExportFilterCriteria(
    val address: String? = null,
    val category: String? = null,
    val startTimestamp: Long? = null,
    val endTimestamp: Long? = null
)

/**
 * Enterprise Professional Export Engine for Global SMS.
 * Converts SMS message threads into PDF/HTML, CSV, Excel-compatible UTF-8 BOM CSV,
 * or standalone interactive HTML documents with custom date and category filters.
 */
object ExportEngine {

    suspend fun exportMessages(
        context: Context,
        messages: List<MessageEntity>,
        format: ExportFormat,
        criteria: ExportFilterCriteria = ExportFilterCriteria()
    ): File = withContext(Dispatchers.IO) {

        // 1. Filter messages
        val filtered = messages.filter { msg ->
            val targetAddr = criteria.address
            val matchAddress = targetAddr.isNullOrBlank() || msg.address.contains(targetAddr, true)
            val msgCat = msg.category ?: ""
            val critCat = criteria.category ?: ""
            val matchCategory = criteria.category.isNullOrBlank() || (msgCat as java.lang.String).equalsIgnoreCase(critCat)
            val matchStart = criteria.startTimestamp == null || msg.timestamp >= criteria.startTimestamp
            val matchEnd = criteria.endTimestamp == null || msg.timestamp <= criteria.endTimestamp
            matchAddress && matchCategory && matchStart && matchEnd
        }.sortedBy { it.timestamp }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

        when (format) {
            ExportFormat.CSV -> generateCsvFile(context, filtered, "export_sms_$timestamp.csv", isExcel = false)
            ExportFormat.EXCEL_CSV -> generateCsvFile(context, filtered, "export_excel_$timestamp.csv", isExcel = true)
            ExportFormat.HTML_DOCUMENT, ExportFormat.PDF_HTML -> generateHtmlFile(context, filtered, "export_doc_$timestamp.html")
        }
    }

    private fun generateCsvFile(
        context: Context,
        messages: List<MessageEntity>,
        filename: String,
        isExcel: Boolean
    ): File {
        val file = File(context.cacheDir, filename)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

        val sb = StringBuilder()
        if (isExcel) {
            // UTF-8 Byte Order Mark (BOM) for Excel Persian text compatibility
            sb.append("\uFEFF")
        }

        // CSV Header
        sb.append("ID,Sender/Address,Timestamp,Type,Category,Body\n")

        messages.forEach { msg ->
            val dateStr = dateFormat.format(Date(msg.timestamp))
            val typeStr = if (msg.type == 1) "Received" else "Sent"
            val sanitizedBody = msg.body.replace("\"", "\"\"").replace("\n", " ")

            sb.append("${msg.id},\"${msg.address}\",\"$dateStr\",\"$typeStr\",\"${msg.category}\",\"$sanitizedBody\"\n")
        }

        file.writeText(sb.toString(), Charsets.UTF_8)
        return file
    }

    private fun generateHtmlFile(
        context: Context,
        messages: List<MessageEntity>,
        filename: String
    ): File {
        val file = File(context.cacheDir, filename)
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US)

        val html = buildString {
            append("""
                <!DOCTYPE html>
                <html lang="fa" dir="rtl">
                <head>
                    <meta charset="UTF-8">
                    <title>گزارش پیامک‌های سیستم سامانه Global SMS</title>
                    <style>
                        body { font-family: Tahoma, Arial, sans-serif; background-color: #f8f9fa; color: #212529; padding: 20px; }
                        h1 { color: #1a73e8; border-bottom: 2px solid #1a73e8; padding-bottom: 10px; }
                        .summary { background-color: #e8f0fe; border-radius: 8px; padding: 12px 20px; margin-bottom: 20px; font-weight: bold; }
                        table { width: 100%; border-collapse: collapse; background: #fff; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
                        th, td { border: 1px solid #dee2e6; padding: 10px 12px; text-align: right; }
                        th { background-color: #f1f3f4; color: #3c4043; }
                        tr:nth-child(even) { background-color: #f8f9fa; }
                        .badge { display: inline-block; padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: bold; background: #e8eaed; color: #3c4043; }
                    </style>
                </head>
                <body>
                    <h1>گزارش اختصاصی استخراج پیامک‌ها — Global SMS</h1>
                    <div class="summary">
                        تعداد کل پیام‌ها: ${messages.size} | تاریخ خروجی: ${dateFormat.format(Date())}
                    </div>
                    <table>
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>فرستنده / گیرنده</th>
                                <th>تاریخ و زمان</th>
                                <th>دسته‌بندی</th>
                                <th>متن پیام</th>
                            </tr>
                        </thead>
                        <tbody>
            """.trimIndent())

            messages.forEachIndexed { idx, msg ->
                val dateStr = dateFormat.format(Date(msg.timestamp))
                append("""
                    <tr>
                        <td>${idx + 1}</td>
                        <td><strong>${msg.address}</strong></td>
                        <td>$dateStr</td>
                        <td><span class="badge">${msg.category}</span></td>
                        <td>${msg.body}</td>
                    </tr>
                """.trimIndent())
            }

            append("""
                        </tbody>
                    </table>
                </body>
                </html>
            """.trimIndent())
        }

        file.writeText(html, Charsets.UTF_8)
        return file
    }
}
