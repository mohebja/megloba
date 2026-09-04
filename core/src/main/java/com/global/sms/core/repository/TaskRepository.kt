package com.global.sms.core.repository

import com.global.sms.data.dao.TaskDao
import com.global.sms.data.entity.TaskEntity
import com.global.sms.data.entity.TaskReminderEntity
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()

    fun getPendingTasks(): Flow<List<TaskEntity>> = taskDao.getPendingTasks()

    suspend fun createTask(task: TaskEntity): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    suspend fun deleteTask(taskId: Long) = taskDao.deleteTask(taskId)

    suspend fun setTaskCompleted(taskId: Long, isCompleted: Boolean) = taskDao.setTaskCompleted(taskId, isCompleted)

    suspend fun addReminder(reminder: TaskReminderEntity): Long = taskDao.insertReminder(reminder)

    suspend fun getRemindersForTask(taskId: Long): List<TaskReminderEntity> = taskDao.getRemindersForTask(taskId)

    suspend fun markReminderTriggered(reminderId: Long) = taskDao.markReminderTriggered(reminderId)
}
