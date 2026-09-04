package com.global.sms.core.contact

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

data class ContactSyncDiff(
    val addedCount: Int = 0,
    val removedCount: Int = 0,
    val updatedCount: Int = 0,
    val nameChangedCount: Int = 0,
    val phoneChangedCount: Int = 0
)

data class ContactSyncStats(
    val lastSyncTimeMillis: Long = 0,
    val totalContactsCount: Int = 0,
    val duplicateGroupCount: Int = 0,
    val lastDiff: ContactSyncDiff = ContactSyncDiff(),
    val syncDurationMs: Long = 0,
    val isObserverActive: Boolean = false,
    val isPeriodicScheduled: Boolean = false
)

sealed class ContactSyncState {
    object Idle : ContactSyncState()
    data class Syncing(val progressPercent: Int = 0) : ContactSyncState()
    data class Success(val stats: ContactSyncStats) : ContactSyncState()
    data class Error(val message: String) : ContactSyncState()
}

class ContactSyncManager private constructor(
    private val appContext: Context
) {

    private val cacheManager = ContactCacheManager.getInstance()
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private val _syncState = MutableStateFlow<ContactSyncState>(ContactSyncState.Idle)
    val syncState: StateFlow<ContactSyncState> = _syncState.asStateFlow()

    private val _syncStats = MutableStateFlow(ContactSyncStats())
    val syncStats: StateFlow<ContactSyncStats> = _syncStats.asStateFlow()

    private var contentObserver: ContactContentObserver? = null
    var isObserverActive: Boolean = false
        private set

    companion object {
        @Volatile
        private var INSTANCE: ContactSyncManager? = null

        fun getInstance(context: Context): ContactSyncManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ContactSyncManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    /**
     * Registers the Contacts ContentObserver to observe contact modifications in realtime.
     */
    fun registerContentObserver() {
        if (isObserverActive) return
        if (contentObserver == null) {
            contentObserver = ContactContentObserver(appContext)
        }
        contentObserver?.register()
        isObserverActive = true

        scope.launch {
            contentObserver?.changeEventFlow?.collect {
                performSyncInternal(force = true)
            }
        }

        _syncStats.value = _syncStats.value.copy(isObserverActive = true)
    }

    /**
     * Unregisters the Contacts ContentObserver.
     */
    fun unregisterContentObserver() {
        if (!isObserverActive) return
        contentObserver?.unregister()
        isObserverActive = false
        _syncStats.value = _syncStats.value.copy(isObserverActive = false)
    }

    /**
     * Schedules periodic background sync using WorkManager.
     */
    fun schedulePeriodicSync(intervalHours: Long = 24) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<ContactSyncWorker>(
            intervalHours, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
            ContactSyncWorker.WORK_NAME_PERIODIC,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicWorkRequest
        )

        _syncStats.value = _syncStats.value.copy(isPeriodicScheduled = true)
    }

    /**
     * Cancels scheduled periodic sync.
     */
    fun cancelPeriodicSync() {
        WorkManager.getInstance(appContext).cancelUniqueWork(ContactSyncWorker.WORK_NAME_PERIODIC)
        _syncStats.value = _syncStats.value.copy(isPeriodicScheduled = false)
    }

    /**
     * Triggers an immediate background sync using WorkManager.
     */
    fun triggerImmediateBackgroundSync() {
        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<ContactSyncWorker>()
            .build()

        WorkManager.getInstance(appContext).enqueueUniqueWork(
            ContactSyncWorker.WORK_NAME_ONE_TIME,
            ExistingWorkPolicy.REPLACE,
            oneTimeWorkRequest
        )
    }

    /**
     * Performs full contact sync and updates memory caches and statistics.
     */
    suspend fun performSyncInternal(force: Boolean = false): ContactSyncStats = withContext(Dispatchers.IO) {
        if (!ContactPermissionHelper.hasReadContactsPermission(appContext)) {
            _syncState.value = ContactSyncState.Error("مجوز دسترسی به مخاطبین داده نشده است")
            return@withContext _syncStats.value
        }

        _syncState.value = ContactSyncState.Syncing(progressPercent = 10)
        val startTime = System.currentTimeMillis()

        val oldContactsList = cacheManager.getAllContacts() ?: emptyList()
        val oldContactsMap = oldContactsList.associateBy { it.id }

        _syncState.value = ContactSyncState.Syncing(progressPercent = 40)
        val newContactsList = ContactManager.getAllContacts(appContext)

        _syncState.value = ContactSyncState.Syncing(progressPercent = 70)

        // Calculate diff metrics
        val newContactsMap = newContactsList.associateBy { it.id }
        var addedCount = 0
        var removedCount = 0
        var updatedCount = 0
        var nameChangedCount = 0
        var phoneChangedCount = 0

        for (newContact in newContactsList) {
            val oldContact = oldContactsMap[newContact.id]
            if (oldContact == null) {
                addedCount++
            } else {
                var isUpdated = false
                if (oldContact.name != newContact.name || oldContact.rawName != newContact.rawName) {
                    nameChangedCount++
                    isUpdated = true
                }
                if (oldContact.phoneNumber != newContact.phoneNumber ||
                    oldContact.duplicateNumbers != newContact.duplicateNumbers) {
                    phoneChangedCount++
                    isUpdated = true
                }
                if (isUpdated) {
                    updatedCount++
                }
            }
        }

        for (oldContact in oldContactsList) {
            if (!newContactsMap.containsKey(oldContact.id)) {
                removedCount++
            }
        }

        val diff = ContactSyncDiff(
            addedCount = addedCount,
            removedCount = removedCount,
            updatedCount = updatedCount,
            nameChangedCount = nameChangedCount,
            phoneChangedCount = phoneChangedCount
        )

        // Update Cache Manager
        cacheManager.updateContacts(newContactsList)

        // Find system groups & duplicate contacts count
        val duplicates = ContactManager.findDuplicateContactGroups(newContactsList)
        val duration = System.currentTimeMillis() - startTime

        val stats = ContactSyncStats(
            lastSyncTimeMillis = System.currentTimeMillis(),
            totalContactsCount = newContactsList.size,
            duplicateGroupCount = duplicates.size,
            lastDiff = diff,
            syncDurationMs = duration,
            isObserverActive = isObserverActive,
            isPeriodicScheduled = _syncStats.value.isPeriodicScheduled
        )

        _syncStats.value = stats
        _syncState.value = ContactSyncState.Success(stats)

        return@withContext stats
    }
}
