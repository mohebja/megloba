package com.global.sms.core.engine

import android.content.Context
import com.global.sms.data.entity.MessageEntity
import com.global.sms.security.crypto.CryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ExportFormat {
    TXT,
    PDF,
    ENCRYPTED_JSON
}

class MessageExportEngine(
    private val context: Context
) {

    suspend fun exportMessages(
        messages: List<MessageEntity>,
        format: ExportFormat,
        passphrase: String? = null
    ): File = withContext(Dispatchers.IO) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val fileName = "sms_export_${System.currentTimeMillis()}"

        val outputDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }

        when (format) {
            ExportFormat.TXT -> {
                val file = File(outputDir, "$fileName.txt")
                val sb = StringBuilder()
                sb.appendLine("==========================================")
                sb.appendLine("GLOBAL SMS - EXPORTED CONVERSATION REPORT")
                sb.appendLine("Generated: ${dateFormat.format(Date())}")
                sb.appendLine("Total Messages: ${messages.size}")
                sb.appendLine("==========================================\n")

                messages.forEachIndexed { index, msg ->
                    val senderType = if (msg.type == 1) "INCOMING" else "OUTGOING"
                    val dateStr = dateFormat.format(Date(msg.timestamp))
                    sb.appendLine("[$index] Date: $dateStr | Type: $senderType | Address: ${msg.address}")
                    sb.appendLine("Body: ${msg.body}")
                    if (!msg.attachmentUri.isNullOrEmpty()) {
                        sb.appendLine("Attachment: ${msg.attachmentUri} (${msg.mimeType})")
                    }
                    sb.appendLine("------------------------------------------")
                }

                file.writeText(sb.toString())
                file
            }

            ExportFormat.PDF -> {
                val file = File(outputDir, "$fileName.pdf")
                val sb = StringBuilder()
                sb.appendLine("PDF Document Header: Global SMS Report")
                sb.appendLine("Exported: ${dateFormat.format(Date())}")
                messages.forEach { msg ->
                    sb.appendLine("${dateFormat.format(Date(msg.timestamp))} | ${msg.address}: ${msg.body}")
                }
                file.writeText(sb.toString())
                file
            }

            ExportFormat.ENCRYPTED_JSON -> {
                val file = File(outputDir, "$fileName.enc")
                val jsonArray = JSONArray()
                messages.forEach { msg ->
                    val obj = JSONObject().apply {
                        put("id", msg.id)
                        put("address", msg.address)
                        put("body", msg.body)
                        put("timestamp", msg.timestamp)
                        put("type", msg.type)
                        put("category", msg.category.name)
                        put("attachmentUri", msg.attachmentUri ?: "")
                    }
                    jsonArray.put(obj)
                }

                val jsonStr = jsonArray.toString(2)
                val encryptedData = if (!passphrase.isNullOrEmpty()) {
                    CryptoManager.encryptWithPassword(jsonStr, passphrase)
                } else {
                    CryptoManager.encryptHardware(jsonStr)
                }
                file.writeText(encryptedData)
                file
            }
        }
    }
}
