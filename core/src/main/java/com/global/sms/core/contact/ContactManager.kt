package com.global.sms.core.contact

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.core.content.ContextCompat

object ContactManager {
    private const val TAG = "ContactManager"

    fun hasContactsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Reads all contacts from Android Contacts Provider along with their photos, group memberships,
     * and performs normalization and duplicate merging.
     */
    fun getAllContacts(context: Context): List<ContactInfo> {
        if (!hasContactsPermission(context)) return emptyList()

        val rawContactsMap = mutableMapOf<String, MutableContactHolder>()
        val groupMembershipMap = getGroupMemberships(context)

        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
            ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY
        )

        val sortOrder = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"

        try {
            context.contentResolver.query(
                uri, projection, null, null, sortOrder
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)
                val lookupIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY)

                while (cursor.moveToNext()) {
                    val id = if (idIdx >= 0) cursor.getString(idIdx) ?: "" else ""
                    val rawName = if (nameIdx >= 0) cursor.getString(nameIdx) ?: "" else ""
                    val rawNumber = if (numberIdx >= 0) cursor.getString(numberIdx) ?: "" else ""
                    val photo = if (photoIdx >= 0) cursor.getString(photoIdx) else null
                    val lookup = if (lookupIdx >= 0) cursor.getString(lookupIdx) else null

                    val normalizedNum = PhoneNumberNormalizer.normalize(rawNumber)
                    if (normalizedNum.isBlank()) continue

                    val formattedName = PersianContactUtils.toPersianDigits(rawName)
                    val displayableName = if (formattedName.isNotBlank()) formattedName else normalizedNum
                    val contactGroups = groupMembershipMap[id] ?: emptyList()

                    val matchableKey = PhoneNumberNormalizer.extractMatchableDigits(normalizedNum)

                    val existing = rawContactsMap[matchableKey]
                    if (existing != null) {
                        if (!existing.phoneNumbers.contains(normalizedNum)) {
                            existing.phoneNumbers.add(normalizedNum)
                        }
                        if (existing.photoUri == null && photo != null) {
                            existing.photoUri = photo
                        }
                    } else {
                        rawContactsMap[matchableKey] = MutableContactHolder(
                            id = id,
                            name = displayableName,
                            rawName = rawName,
                            primaryNumber = normalizedNum,
                            phoneNumbers = mutableListOf(normalizedNum),
                            photoUri = photo,
                            lookupKey = lookup,
                            groups = contactGroups.toMutableList()
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed reading raw contacts from ContactsContract", e)
        }

        // Convert mutable holders to immutable ContactInfo list
        val resultList = rawContactsMap.values.map { holder ->
            val duplicateNumbersList = holder.phoneNumbers.filter { it != holder.primaryNumber }
            ContactInfo(
                id = holder.id,
                name = holder.name,
                rawName = holder.rawName,
                phoneNumber = holder.primaryNumber,
                normalizedNumber = holder.primaryNumber,
                photoUri = holder.photoUri,
                lookupKey = holder.lookupKey,
                groupNames = holder.groups,
                isDuplicate = duplicateNumbersList.isNotEmpty(),
                duplicateNumbers = duplicateNumbersList
            )
        }

        // Sort by Persian alphabetical order
        return resultList.sortedWith { c1, c2 ->
            PersianContactUtils.persianNameComparator.compare(c1.name, c2.name)
        }
    }

    /**
     * Reads system contact groups from Android Contacts Provider.
     */
    fun getSystemContactGroups(context: Context): List<ContactGroup> {
        if (!hasContactsPermission(context)) return emptyList()

        val groups = mutableListOf<ContactGroup>()
        val uri = ContactsContract.Groups.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.Groups._ID,
            ContactsContract.Groups.TITLE,
            ContactsContract.Groups.ACCOUNT_NAME,
            ContactsContract.Groups.SUMMARY_COUNT
        )

        val selection = "${ContactsContract.Groups.DELETED} = 0"

        try {
            context.contentResolver.query(
                uri, projection, selection, null, "${ContactsContract.Groups.TITLE} ASC"
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndex(ContactsContract.Groups._ID)
                val titleIdx = cursor.getColumnIndex(ContactsContract.Groups.TITLE)
                val accountIdx = cursor.getColumnIndex(ContactsContract.Groups.ACCOUNT_NAME)
                val countIdx = cursor.getColumnIndex(ContactsContract.Groups.SUMMARY_COUNT)

                while (cursor.moveToNext()) {
                    val id = if (idIdx >= 0) cursor.getString(idIdx) ?: "" else ""
                    val title = if (titleIdx >= 0) cursor.getString(titleIdx) ?: "" else ""
                    val account = if (accountIdx >= 0) cursor.getString(accountIdx) else null
                    val count = if (countIdx >= 0) cursor.getInt(countIdx) else 0

                    if (title.isNotBlank()) {
                        val formattedTitle = PersianContactUtils.toPersianDigits(title)
                        groups.add(
                            ContactGroup(
                                id = id,
                                title = formattedTitle,
                                accountName = account,
                                count = count
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed reading contact groups from ContactsContract", e)
        }

        return groups
    }

    /**
     * Maps Contact IDs to Group Title Names from ContactsContract.Data.
     */
    private fun getGroupMemberships(context: Context): Map<String, List<String>> {
        val map = mutableMapOf<String, MutableList<String>>()
        val groupTitlesMap = getGroupTitlesMap(context)

        val uri = ContactsContract.Data.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID
        )
        val selection = "${ContactsContract.Data.MIMETYPE} = ?"
        val selectionArgs = arrayOf(ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE)

        try {
            context.contentResolver.query(
                uri, projection, selection, selectionArgs, null
            )?.use { cursor ->
                val contactIdIdx = cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID)
                val groupIdIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID)

                while (cursor.moveToNext()) {
                    val contactId = if (contactIdIdx >= 0) cursor.getString(contactIdIdx) ?: "" else ""
                    val groupId = if (groupIdIdx >= 0) cursor.getString(groupIdIdx) ?: "" else ""

                    val groupTitle = groupTitlesMap[groupId]
                    if (contactId.isNotBlank() && !groupTitle.isNullOrBlank()) {
                        map.getOrPut(contactId) { mutableListOf() }.add(groupTitle)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed resolving contact group memberships", e)
        }

        return map
    }

    private fun getGroupTitlesMap(context: Context): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val uri = ContactsContract.Groups.CONTENT_URI
        val projection = arrayOf(ContactsContract.Groups._ID, ContactsContract.Groups.TITLE)

        try {
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                val idIdx = cursor.getColumnIndex(ContactsContract.Groups._ID)
                val titleIdx = cursor.getColumnIndex(ContactsContract.Groups.TITLE)
                while (cursor.moveToNext()) {
                    val id = if (idIdx >= 0) cursor.getString(idIdx) ?: "" else ""
                    val title = if (titleIdx >= 0) cursor.getString(titleIdx) ?: "" else ""
                    if (id.isNotBlank() && title.isNotBlank()) {
                        map[id] = title
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed retrieving group titles map", e)
        }
        return map
    }

    /**
     * Resolves contact name and photo by phone number from ContactsContract.PhoneLookup.
     */
    fun resolveContactNameAndPhoto(context: Context, phoneNumber: String): Pair<String?, String?> {
        if (!hasContactsPermission(context) || phoneNumber.isBlank()) return Pair(null, null)

        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val projection = arrayOf(
            ContactsContract.PhoneLookup.DISPLAY_NAME,
            ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI
        )

        try {
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    val photoIdx = cursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI)

                    val name = if (nameIdx >= 0) cursor.getString(nameIdx) else null
                    val photo = if (photoIdx >= 0) cursor.getString(photoIdx) else null

                    val formattedName = name?.let { PersianContactUtils.toPersianDigits(it) }
                    return Pair(formattedName, photo)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed looking up contact info for phone number", e)
        }

        return Pair(null, null)
    }

    /**
     * Group duplicate contacts by identical phone number or identical name.
     */
    fun findDuplicateContactGroups(contacts: List<ContactInfo>): List<ContactDuplicateGroup> {
        val duplicateGroups = mutableListOf<ContactDuplicateGroup>()

        // 1. Group by matchable phone digits
        val phoneGroups = contacts.groupBy { PhoneNumberNormalizer.extractMatchableDigits(it.normalizedNumber) }
        for ((_, group) in phoneGroups) {
            if (group.size > 1) {
                duplicateGroups.add(
                    ContactDuplicateGroup(
                        primaryContact = group.first(),
                        duplicates = group.drop(1),
                        reason = "شماره تماس یکسان"
                    )
                )
            }
        }

        // 2. Group by normalized Persian name
        val nameGroups = contacts.groupBy { PersianContactUtils.normalizePersianText(it.name) }
        for ((normName, group) in nameGroups) {
            if (normName.isNotBlank() && group.size > 1) {
                // Avoid adding duplicate if already added by phone
                if (!duplicateGroups.any { dg -> dg.primaryContact.id == group.first().id }) {
                    duplicateGroups.add(
                        ContactDuplicateGroup(
                            primaryContact = group.first(),
                            duplicates = group.drop(1),
                            reason = "نام یکسان"
                        )
                    )
                }
            }
        }

        return duplicateGroups
    }

    private data class MutableContactHolder(
        val id: String,
        val name: String,
        val rawName: String,
        val primaryNumber: String,
        val phoneNumbers: MutableList<String>,
        var photoUri: String?,
        val lookupKey: String?,
        val groups: MutableList<String>
    )

    private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()
}
