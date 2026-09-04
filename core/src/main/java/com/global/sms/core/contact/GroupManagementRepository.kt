package com.global.sms.core.contact

import android.content.Context
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.data.entity.ContactGroupEntity
import com.global.sms.data.entity.ContactGroupMemberEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Enterprise Repository for managing custom contact groups and group memberships.
 * Seamlessly integrates with Room Database and Android System Contact Groups.
 */
class GroupManagementRepository(
    private val context: Context,
    private val database: GlobalSmsDatabase = GlobalSmsDatabase.getInstance(context)
) {
    private val groupDao = database.contactGroupDao()
    private val memberDao = database.contactGroupMemberDao()

    fun getAllGroupsFlow(): Flow<List<ContactGroupEntity>> {
        return groupDao.getAllGroupsFlow()
    }

    suspend fun getAllGroupsSync(): List<ContactGroupEntity> {
        return withContext(Dispatchers.IO) {
            groupDao.getAllGroupsSync()
        }
    }

    suspend fun getGroupById(id: Long): ContactGroupEntity? {
        return withContext(Dispatchers.IO) {
            groupDao.getGroupById(id)
        }
    }

    suspend fun createGroup(name: String, description: String? = null, color: Long? = 0xFF1A73E8): Long {
        return withContext(Dispatchers.IO) {
            val entity = ContactGroupEntity(
                name = name,
                description = description,
                color = color,
                createdDate = System.currentTimeMillis()
            )
            groupDao.insertGroup(entity)
        }
    }

    suspend fun updateGroup(group: ContactGroupEntity) {
        withContext(Dispatchers.IO) {
            groupDao.updateGroup(group)
        }
    }

    suspend fun deleteGroup(groupId: Long) {
        withContext(Dispatchers.IO) {
            memberDao.removeAllMembersByGroupId(groupId)
            groupDao.deleteGroupById(groupId)
        }
    }

    suspend fun addMemberToGroup(groupId: Long, contactId: Long) {
        withContext(Dispatchers.IO) {
            memberDao.insertMember(ContactGroupMemberEntity(groupId, contactId))
        }
    }

    suspend fun removeMemberFromGroup(groupId: Long, contactId: Long) {
        withContext(Dispatchers.IO) {
            memberDao.removeMember(groupId, contactId)
        }
    }

    suspend fun getGroupMembers(groupId: Long): List<ContactGroupMemberEntity> {
        return withContext(Dispatchers.IO) {
            memberDao.getMembersByGroupId(groupId)
        }
    }

    /**
     * Imports native system contact groups into the local database.
     */
    suspend fun syncSystemContactGroups(): Int {
        return withContext(Dispatchers.IO) {
            if (!ContactManager.hasContactsPermission(context)) return@withContext 0
            val systemGroups = ContactManager.getSystemContactGroups(context)
            var count = 0
            for (sysGroup in systemGroups) {
                val existing = groupDao.getAllGroupsSync().find { it.name.equals(sysGroup.title, ignoreCase = true) }
                if (existing == null) {
                    groupDao.insertGroup(
                        ContactGroupEntity(
                            name = sysGroup.title,
                            description = "System Contact Group (${sysGroup.count} contacts)",
                            createdDate = System.currentTimeMillis()
                        )
                    )
                    count++
                }
            }
            count
        }
    }
}
