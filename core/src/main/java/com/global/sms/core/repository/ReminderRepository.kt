package com.global.sms.core.repository

import com.global.sms.data.dao.ReminderDao
import com.global.sms.data.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

class ReminderRepository(
    private val reminderDao: ReminderDao
) {
    fun getRemindersForThread(threadId: Long): Flow<List<ReminderEntity>> =
        reminderDao.getRemindersForThread(threadId)

    fun getAllActiveReminders(): Flow<List<ReminderEntity>> =
        reminderDao.getAllActiveReminders()

    suspend fun createReminder(threadId: Long, title: String, scheduledTime: Long): Long {
        return reminderDao.insertReminder(
            ReminderEntity(
                threadId = threadId,
                title = title,
                scheduledTime = scheduledTime
            )
        )
    }

    suspend fun markCompleted(reminder: ReminderEntity) {
        reminderDao.updateReminder(reminder.copy(isCompleted = true))
    }

    suspend fun deleteReminder(id: Long) {
        reminderDao.deleteReminder(id)
    }
}
