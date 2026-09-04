package com.global.sms.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "calendar_suggestions",
    indices = [
        Index(value = ["messageId"]),
        Index(value = ["eventDateMillis"]),
        Index(value = ["isAccepted"])
    ]
)
data class CalendarSuggestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val messageId: Long = 0,
    val title: String,
    val eventDateMillis: Long,
    val timeString: String = "",
    val location: String = "",
    val isAccepted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "contact_insights",
    indices = [
        Index(value = ["address"], unique = true),
        Index(value = ["smartCategory"])
    ]
)
data class ContactInsightEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val address: String,
    val smartCategory: String = "PERSONAL", // "VIP", "IMPORTANT", "BUSINESS", "PERSONAL"
    val interactionCount: Int = 1,
    val lastContactMillis: Long = System.currentTimeMillis(),
    val priorityScore: Int = 50
)
