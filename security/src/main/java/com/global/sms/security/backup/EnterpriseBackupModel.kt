package com.global.sms.security.backup

import org.json.JSONArray
import org.json.JSONObject

data class BackupMessageItem(
    val id: Long = 0L,
    val threadId: Long = 0L,
    val address: String = "",
    val body: String = "",
    val date: Long = 0L,
    val type: Int = 1,
    val read: Int = 1,
    val status: Int = 0
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("threadId", threadId)
            put("address", address)
            put("body", body)
            put("date", date)
            put("type", type)
            put("read", read)
            put("status", status)
        }
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): BackupMessageItem {
            return BackupMessageItem(
                id = obj.optLong("id", 0L),
                threadId = obj.optLong("threadId", 0L),
                address = obj.optString("address", ""),
                body = obj.optString("body", ""),
                date = obj.optLong("date", 0L),
                type = obj.optInt("type", 1),
                read = obj.optInt("read", 1),
                status = obj.optInt("status", 0)
            )
        }
    }
}

data class EnterpriseBackupModel(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val appVersion: String = "2.0.0",
    val messages: List<BackupMessageItem> = emptyList(),
    val categories: List<String> = emptyList(),
    val settings: Map<String, String> = emptyMap()
) {
    fun toJson(): String {
        val root = JSONObject()
        root.put("version", version)
        root.put("timestamp", timestamp)
        root.put("appVersion", appVersion)

        val msgsArray = JSONArray()
        messages.forEach { msgsArray.put(it.toJsonObject()) }
        root.put("messages", msgsArray)

        val catsArray = JSONArray()
        categories.forEach { catsArray.put(it) }
        root.put("categories", catsArray)

        val setsObj = JSONObject()
        settings.forEach { (k, v) -> setsObj.put(k, v) }
        root.put("settings", setsObj)

        return root.toString()
    }

    companion object {
        fun fromJson(jsonStr: String): EnterpriseBackupModel {
            val root = JSONObject(jsonStr)
            val version = root.optInt("version", 1)
            val timestamp = root.optLong("timestamp", System.currentTimeMillis())
            val appVersion = root.optString("appVersion", "2.0.0")

            val messages = mutableListOf<BackupMessageItem>()
            val msgsArray = root.optJSONArray("messages")
            if (msgsArray != null) {
                for (i in 0 until msgsArray.length()) {
                    val obj = msgsArray.optJSONObject(i)
                    if (obj != null) {
                        messages.add(BackupMessageItem.fromJsonObject(obj))
                    }
                }
            }

            val categories = mutableListOf<String>()
            val catsArray = root.optJSONArray("categories")
            if (catsArray != null) {
                for (i in 0 until catsArray.length()) {
                    categories.add(catsArray.getString(i))
                }
            }

            val settings = mutableMapOf<String, String>()
            val setsObj = root.optJSONObject("settings")
            if (setsObj != null) {
                val keys = setsObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    settings[key] = setsObj.getString(key)
                }
            }

            return EnterpriseBackupModel(
                version = version,
                timestamp = timestamp,
                appVersion = appVersion,
                messages = messages,
                categories = categories,
                settings = settings
            )
        }
    }
}
