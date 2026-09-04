package com.global.sms.core.contact

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

import kotlinx.coroutines.flow.StateFlow

interface ContactRepository {
    fun hasPermission(): Boolean
    fun getContactsFlow(query: String = "", groupFilter: String? = null, forceRefresh: Boolean = false): Flow<List<ContactInfo>>
    suspend fun searchContacts(query: String = "", groupFilter: String? = null): List<ContactInfo>
    suspend fun resolveContact(phoneNumber: String): ContactInfo?
    suspend fun getSystemContactGroups(): List<ContactGroup>
    suspend fun getDuplicateGroups(): List<ContactDuplicateGroup>
    fun invalidateCache()

    // Contact Sync capabilities
    val syncState: StateFlow<ContactSyncState>
    val syncStats: StateFlow<ContactSyncStats>
    fun registerContentObserver()
    fun unregisterContentObserver()
    fun schedulePeriodicSync(intervalHours: Long = 24)
    fun cancelPeriodicSync()
    suspend fun syncContactsNow(): ContactSyncStats
}

class ContactRepositoryImpl(
    private val context: Context,
    private val cacheManager: ContactCacheManager = ContactCacheManager.getInstance()
) : ContactRepository {

    private val syncManager: ContactSyncManager by lazy {
        ContactSyncManager.getInstance(context)
    }

    override val syncState: StateFlow<ContactSyncState>
        get() = syncManager.syncState

    override val syncStats: StateFlow<ContactSyncStats>
        get() = syncManager.syncStats

    override fun registerContentObserver() {
        syncManager.registerContentObserver()
    }

    override fun unregisterContentObserver() {
        syncManager.unregisterContentObserver()
    }

    override fun schedulePeriodicSync(intervalHours: Long) {
        syncManager.schedulePeriodicSync(intervalHours)
    }

    override fun cancelPeriodicSync() {
        syncManager.cancelPeriodicSync()
    }

    override suspend fun syncContactsNow(): ContactSyncStats {
        return syncManager.performSyncInternal(force = true)
    }

    override fun hasPermission(): Boolean {
        return ContactManager.hasContactsPermission(context)
    }

    override fun getContactsFlow(
        query: String,
        groupFilter: String?,
        forceRefresh: Boolean
    ): Flow<List<ContactInfo>> = flow {
        val results = searchContactsInternal(query, groupFilter, forceRefresh)
        emit(results)
    }.flowOn(Dispatchers.IO)

    override suspend fun searchContacts(query: String, groupFilter: String?): List<ContactInfo> {
        return withContext(Dispatchers.IO) {
            searchContactsInternal(query, groupFilter, forceRefresh = false)
        }
    }

    private fun searchContactsInternal(
        query: String,
        groupFilter: String?,
        forceRefresh: Boolean
    ): List<ContactInfo> {
        if (!hasPermission()) return emptyList()

        val cleanQuery = query.trim()
        val cleanGroupFilter = groupFilter?.trim()

        // 1. Check LRU query cache if not forcing refresh
        if (!forceRefresh) {
            cacheManager.getCachedSearch(cleanQuery, cleanGroupFilter)?.let {
                return it
            }
        }

        // 2. Load or refresh all contacts
        var allContacts = cacheManager.getAllContacts()
        if (allContacts == null || forceRefresh) {
            allContacts = ContactManager.getAllContacts(context)
            cacheManager.updateContacts(allContacts)
        }

        // 3. Filter by group if requested
        var filtered = allContacts
        if (!cleanGroupFilter.isNullOrEmpty()) {
            filtered = filtered.filter { contact ->
                contact.groupNames.any { g -> g.equals(cleanGroupFilter, ignoreCase = true) }
            }
        }

        // 4. Filter by search query if requested
        if (cleanQuery.isNotEmpty()) {
            filtered = filtered.filter { contact ->
                PersianContactUtils.matchesQuery(contact.name, cleanQuery) ||
                PersianContactUtils.matchesQuery(contact.rawName, cleanQuery) ||
                PhoneNumberNormalizer.normalize(contact.phoneNumber).contains(PhoneNumberNormalizer.normalize(cleanQuery)) ||
                PhoneNumberNormalizer.extractMatchableDigits(contact.phoneNumber).contains(PhoneNumberNormalizer.extractMatchableDigits(cleanQuery))
            }
        }

        // Cache results in LRU Cache
        cacheManager.putCachedSearch(cleanQuery, cleanGroupFilter, filtered)

        return filtered
    }

    override suspend fun resolveContact(phoneNumber: String): ContactInfo? {
        if (phoneNumber.isBlank()) return null
        
        return withContext(Dispatchers.IO) {
            // 1. Check fast O(1) cache
            val cached = cacheManager.lookupContact(phoneNumber)
            if (cached != null) return@withContext cached

            // 2. If missing and permission available, query provider or fallback resolve
            if (hasPermission()) {
                val (name, photo) = ContactManager.resolveContactNameAndPhoto(context, phoneNumber)
                if (name != null) {
                    val norm = PhoneNumberNormalizer.normalize(phoneNumber)
                    val resolved = ContactInfo(
                        id = "resolved_$norm",
                        name = name,
                        rawName = name,
                        phoneNumber = norm,
                        normalizedNumber = norm,
                        photoUri = photo
                    )
                    return@withContext resolved
                }
            }

            null
        }
    }

    override suspend fun getSystemContactGroups(): List<ContactGroup> {
        return withContext(Dispatchers.IO) {
            val cachedGroups = cacheManager.getSystemGroups()
            if (cachedGroups != null) return@withContext cachedGroups

            if (!hasPermission()) return@withContext emptyList()

            val groups = ContactManager.getSystemContactGroups(context)
            cacheManager.updateSystemGroups(groups)
            groups
        }
    }

    override suspend fun getDuplicateGroups(): List<ContactDuplicateGroup> {
        return withContext(Dispatchers.IO) {
            val contacts = searchContacts("", null)
            ContactManager.findDuplicateContactGroups(contacts)
        }
    }

    override fun invalidateCache() {
        cacheManager.invalidateAll()
    }
}
