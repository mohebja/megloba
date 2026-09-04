package com.global.sms.core.contact.crm

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

class ContactCRMRepository {

    private val _contactProfiles = MutableStateFlow<List<ContactProfile>>(emptyList())
    val contactProfiles: Flow<List<ContactProfile>> = _contactProfiles.asStateFlow()

    init {
        // Seed default enterprise contacts if empty
        if (_contactProfiles.value.isEmpty()) {
            _contactProfiles.value = listOf(
                ContactProfile(
                    id = "crm_1",
                    fullName = "علی محمدی (مدیر فروش)",
                    phoneNumbers = listOf("09121111111", "09351111111"),
                    primaryPhoneNumber = "09121111111",
                    company = "شرکت آریا دیجيتال",
                    category = "همکاران",
                    tags = listOf(
                        ContactTag("t1", "VIP", ContactTagType.VIP, "#A855F7"),
                        ContactTag("t2", "همکار", ContactTagType.WORK, "#FF9800")
                    ),
                    isVip = true,
                    isFavorite = true
                ),
                ContactProfile(
                    id = "crm_2",
                    fullName = "بانک ملی ایران",
                    phoneNumbers = listOf("6000", "02188888888"),
                    primaryPhoneNumber = "6000",
                    company = "بانک ملی",
                    category = "بانک",
                    tags = listOf(
                        ContactTag("t3", "بانک", ContactTagType.BANK, "#00C853")
                    )
                ),
                ContactProfile(
                    id = "crm_3",
                    fullName = "رضا احمدی",
                    phoneNumbers = listOf("09122222222"),
                    primaryPhoneNumber = "09122222222",
                    company = "مشتری فروشگاه",
                    category = "مشتریان",
                    tags = listOf(
                        ContactTag("t4", "مشتری", ContactTagType.CUSTOMER, "#1A73E8")
                    )
                )
            )
        }
    }

    suspend fun saveContactProfile(profile: ContactProfile) {
        val current = _contactProfiles.value.toMutableList()
        val index = current.indexOfFirst { it.id == profile.id }
        if (index >= 0) {
            current[index] = profile
        } else {
            current.add(0, profile)
        }
        _contactProfiles.value = current
    }

    fun getContactById(id: String): ContactProfile? {
        return _contactProfiles.value.find { it.id == id }
    }

    fun findContactByPhone(phone: String): ContactProfile? {
        val normalized = phone.replace(" ", "").replace("-", "")
        return _contactProfiles.value.find { p ->
            p.phoneNumbers.any { it.replace(" ", "").replace("-", "") == normalized } ||
                    p.primaryPhoneNumber.replace(" ", "").replace("-", "") == normalized
        }
    }

    fun searchContacts(
        query: String = "",
        tagFilter: String? = null,
        categoryFilter: String? = null
    ): Flow<List<ContactProfile>> {
        return _contactProfiles.map { list ->
            list.filter { contact ->
                val matchesQuery = query.isBlank() ||
                        contact.fullName.contains(query, ignoreCase = true) ||
                        contact.primaryPhoneNumber.contains(query) ||
                        contact.company?.contains(query, ignoreCase = true) == true

                val matchesTag = tagFilter.isNullOrBlank() ||
                        contact.tags.any { it.name.equals(tagFilter, ignoreCase = true) }

                val matchesCategory = categoryFilter.isNullOrBlank() ||
                        contact.category.equals(categoryFilter, ignoreCase = true)

                matchesQuery && matchesTag && matchesCategory
            }
        }
    }

    fun getTimelineForContact(contactId: String): ContactTimeline {
        val profile = getContactById(contactId)
        val now = System.currentTimeMillis()
        val items = listOf(
            ContactHistoryItem(
                id = UUID.randomUUID().toString(),
                contactId = contactId,
                interactionType = InteractionType.SMS_RECEIVED,
                title = "پیامک دریافتی",
                snippet = "سلام، فاکتور ارسال شد؟",
                timestamp = now - 3600000,
                category = "BUSINESS"
            ),
            ContactHistoryItem(
                id = UUID.randomUUID().toString(),
                contactId = contactId,
                interactionType = InteractionType.SMS_SENT,
                title = "پیامک ارسالی",
                snippet = "بله، فاکتور خدمت شما ارسال شد.",
                timestamp = now - 1800000,
                category = "BUSINESS"
            )
        )

        return ContactTimeline(
            contactId = contactId,
            totalSent = 12,
            totalReceived = 18,
            totalScheduled = 1,
            historyItems = items
        )
    }

    suspend fun addNoteToContact(contactId: String, noteText: String) {
        val profile = getContactById(contactId) ?: return
        val newNote = ContactNote(
            id = UUID.randomUUID().toString(),
            contactId = contactId,
            noteText = noteText
        )
        val updatedProfile = profile.copy(
            notes = profile.notes + newNote
        )
        saveContactProfile(updatedProfile)
    }
}
