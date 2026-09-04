package com.global.sms.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TaskPriority {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL
}

enum class TaskStatus {
    NEW,
    IN_PROGRESS,
    DONE,
    CANCELLED
}

enum class TaskSource {
    USER_CREATED,
    AI_SUGGESTED,
    MESSAGE_GENERATED
}

@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["messageId"]),
        Index(value = ["isCompleted"]),
        Index(value = ["dueDateMillis"]),
        Index(value = ["status"])
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val messageId: Long = 0,
    val title: String,
    val description: String = "",
    val dueDateMillis: Long = 0,
    val isCompleted: Boolean = false,
    val priority: String = "NORMAL",
    val status: String = "NEW",
    val source: String = "USER_CREATED",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "task_reminders",
    indices = [
        Index(value = ["taskId"]),
        Index(value = ["isTriggered"])
    ]
)
data class TaskReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long,
    val reminderTimeMillis: Long,
    val isTriggered: Boolean = false
)
