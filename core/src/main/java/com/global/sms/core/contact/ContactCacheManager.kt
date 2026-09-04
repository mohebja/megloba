package com.global.sms.core.contact

import java.util.concurrent.ConcurrentHashMap

class ContactCacheManager private constructor() {

    // Fast lookup maps for O(1) contact resolution
    private val phoneToContactMap = ConcurrentHashMap<String, ContactInfo>()
    private val matchableDigitToContactMap = ConcurrentHashMap<String, ContactInfo>()
    private val idToContactMap = ConcurrentHashMap<String, ContactInfo>()

    // Thread-safe pure Java LRU Cache for search query results (Max 50 queries)
    private val searchCache = SimpleLruCache<String, List<ContactInfo>>(50)

    private class SimpleLruCache<K, V>(private val maxSize: Int) {
        private val map = object : java.util.LinkedHashMap<K, V>(maxSize, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
                return size > maxSize
            }
        }

        @Synchronized
        fun get(key: K): V? = map[key]

        @Synchronized
        fun put(key: K, value: V) {
            map[key] = value
        }

        @Synchronized
        fun evictAll() {
            map.clear()
        }
    }

    // Cached full list of contacts
    @Volatile
    private var allContactsCache: List<ContactInfo>? = null

    // Cached system groups
    @Volatile
    private var systemGroupsCache: List<ContactGroup>? = null

    @Volatile
    var lastUpdatedTimeMillis: Long = 0
        private set

    companion object {
        @Volatile
        private var INSTANCE: ContactCacheManager? = null

        fun getInstance(): ContactCacheManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ContactCacheManager().also { INSTANCE = it }
            }
        }
    }

    /**
     * Updates the cache with a full fresh contact list.
     */
    fun updateContacts(contacts: List<ContactInfo>) {
        phoneToContactMap.clear()
        matchableDigitToContactMap.clear()
        idToContactMap.clear()
        searchCache.evictAll()

        allContactsCache = contacts

        for (contact in contacts) {
            idToContactMap[contact.id] = contact
            
            if (contact.normalizedNumber.isNotBlank()) {
                phoneToContactMap[contact.normalizedNumber] = contact
                
                val matchable = PhoneNumberNormalizer.extractMatchableDigits(contact.normalizedNumber)
                if (matchable.isNotBlank()) {
                    matchableDigitToContactMap[matchable] = contact
                }
            }
            
            for (altNum in contact.duplicateNumbers) {
                val normAlt = PhoneNumberNormalizer.normalize(altNum)
                if (normAlt.isNotBlank()) {
                    phoneToContactMap[normAlt] = contact
                    val matchableAlt = PhoneNumberNormalizer.extractMatchableDigits(normAlt)
                    if (matchableAlt.isNotBlank()) {
                        matchableDigitToContactMap[matchableAlt] = contact
                    }
                }
            }
        }

        lastUpdatedTimeMillis = System.currentTimeMillis()
    }

    /**
     * Fast O(1) lookup of a contact by raw or normalized phone number.
     */
    fun lookupContact(phoneNumber: String): ContactInfo? {
        if (phoneNumber.isBlank()) return null
        
        val normalized = PhoneNumberNormalizer.normalize(phoneNumber)
        
        // 1. Direct match on normalized number
        phoneToContactMap[normalized]?.let { return it }

        // 2. Direct match on E.164
        val e164 = PhoneNumberNormalizer.toE164Format(phoneNumber)
        phoneToContactMap[e164]?.let { return it }

        // 3. Match on significant matchable digits (last 9 digits)
        val matchable = PhoneNumberNormalizer.extractMatchableDigits(phoneNumber)
        if (matchable.isNotBlank()) {
            matchableDigitToContactMap[matchable]?.let { return it }
        }

        return null
    }

    /**
     * Retrieves cached search results if available.
     */
    fun getCachedSearch(query: String, groupFilter: String?): List<ContactInfo>? {
        val cacheKey = "$query|${groupFilter ?: ""}"
        return searchCache.get(cacheKey)
    }

    /**
     * Stores search query results in LRU Cache.
     */
    fun putCachedSearch(query: String, groupFilter: String?, results: List<ContactInfo>) {
        val cacheKey = "$query|${groupFilter ?: ""}"
        searchCache.put(cacheKey, results)
    }

    /**
     * Returns full cached contacts if available and not empty.
     */
    fun getAllContacts(): List<ContactInfo>? = allContactsCache

    /**
     * System contact groups caching
     */
    fun getSystemGroups(): List<ContactGroup>? = systemGroupsCache

    fun updateSystemGroups(groups: List<ContactGroup>) {
        systemGroupsCache = groups
    }

    /**
     * Invalidates a specific phone number from cache.
     */
    fun invalidatePhoneNumber(phoneNumber: String) {
        val normalized = PhoneNumberNormalizer.normalize(phoneNumber)
        phoneToContactMap.remove(normalized)
        val matchable = PhoneNumberNormalizer.extractMatchableDigits(phoneNumber)
        if (matchable.isNotBlank()) {
            matchableDigitToContactMap.remove(matchable)
        }
        searchCache.evictAll()
    }

    /**
     * Clears all cached contact data (Cache Invalidation).
     */
    fun invalidateAll() {
        phoneToContactMap.clear()
        matchableDigitToContactMap.clear()
        idToContactMap.clear()
        searchCache.evictAll()
        allContactsCache = null
        systemGroupsCache = null
        lastUpdatedTimeMillis = 0
    }

    /**
     * Memory optimization: trims caches when memory is constrained.
     */
    fun trimMemory() {
        searchCache.evictAll()
    }
}
