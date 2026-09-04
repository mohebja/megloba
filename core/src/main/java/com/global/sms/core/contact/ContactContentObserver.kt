package com.global.sms.core.contact

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * ContentObserver for Android Contacts Provider.
 * Automatically detects contact added, removed, name changed, and phone number changed
 * with a debounced Coroutine execution pipeline.
 */
class ContactContentObserver(
    private val context: Context,
    private val debounceDelayMs: Long = 1500L
) : ContentObserver(Handler(Looper.getMainLooper())) {

    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var debounceJob: Job? = null

    private val _changeEventFlow = MutableSharedFlow<Long>(extraBufferCapacity = 1)
    val changeEventFlow: SharedFlow<Long> = _changeEventFlow.asSharedFlow()

    private var isRegistered = false

    fun register() {
        if (isRegistered) return
        try {
            context.contentResolver.registerContentObserver(
                ContactsContract.Contacts.CONTENT_URI,
                true,
                this
            )
            isRegistered = true
        } catch (e: Exception) {
            Log.e("ContactContentObserver", "Failed to register Contacts content observer", e)
        }
    }

    fun unregister() {
        if (!isRegistered) return
        try {
            context.contentResolver.unregisterContentObserver(this)
            isRegistered = false
        } catch (e: Exception) {
            Log.e("ContactContentObserver", "Failed to unregister Contacts content observer", e)
        }
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        scheduleDebouncedSync()
    }

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        scheduleDebouncedSync()
    }

    private fun scheduleDebouncedSync() {
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(debounceDelayMs)
            _changeEventFlow.emit(System.currentTimeMillis())
        }
    }
}
