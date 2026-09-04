package com.global.sms.core.group

import com.global.sms.core.contact.crm.ContactProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class SmartGroupType(val titleFa: String) {
    ALL_BANK_CONTACTS("تمام مخاطبین بانکی"),
    CUSTOMERS("مشتریان فعال"),
    UNREAD_CONVERSATIONS("گفتگوهای خوانده نشده"),
    VIP_USERS("مخاطبین ویژه (VIP)"),
    CUSTOM_GROUP("گروه سفارشی")
}

data class EnterpriseGroup(
    val id: String,
    val name: String,
    val type: SmartGroupType,
    val memberPhoneNumbers: List<String>,
    val description: String = ""
)

class GroupCampaignManager {

    private val _smartGroups = MutableStateFlow<List<EnterpriseGroup>>(
        listOf(
            EnterpriseGroup(
                id = "grp_bank",
                name = "تمام مخاطبین بانکی",
                type = SmartGroupType.ALL_BANK_CONTACTS,
                memberPhoneNumbers = listOf("6000", "2000", "1000"),
                description = "گروه هوشمند شامل سرشماره‌های اطلاع‌رسانی بانکی"
            ),
            EnterpriseGroup(
                id = "grp_vip",
                name = "مخاطبین ویژه (VIP)",
                type = SmartGroupType.VIP_USERS,
                memberPhoneNumbers = listOf("09121111111"),
                description = "مشتریان و همکاران دارای نشان VIP"
            ),
            EnterpriseGroup(
                id = "grp_customers",
                name = "مشتریان فعال",
                type = SmartGroupType.CUSTOMERS,
                memberPhoneNumbers = listOf("09122222222", "09353333333"),
                description = "لیست مشتریان ثبت‌شده در سیستم CRM"
            )
        )
    )

    val smartGroups: Flow<List<EnterpriseGroup>> = _smartGroups.asStateFlow()

    fun getGroupById(groupId: String): EnterpriseGroup? {
        return _smartGroups.value.find { it.id == groupId }
    }

    suspend fun createGroup(name: String, members: List<String>, description: String = ""): EnterpriseGroup {
        val group = EnterpriseGroup(
            id = UUID.randomUUID().toString(),
            name = name,
            type = SmartGroupType.CUSTOM_GROUP,
            memberPhoneNumbers = members,
            description = description
        )
        _smartGroups.value = _smartGroups.value + group
        return group
    }

    suspend fun executeGroupCampaign(
        group: EnterpriseGroup,
        messageTemplate: String,
        simSlot: Int = 0
    ): CampaignReport {
        val start = System.currentTimeMillis()
        val statuses = group.memberPhoneNumbers.map { phone ->
            RecipientDeliveryStatus(
                recipientPhone = phone,
                recipientName = "مخاطب ($phone)",
                status = DeliveryState.DELIVERED,
                sentTimestamp = System.currentTimeMillis()
            )
        }

        return CampaignReport(
            campaignId = UUID.randomUUID().toString(),
            campaignName = "کمپین ارسال به ${group.name}",
            groupName = group.name,
            totalRecipients = group.memberPhoneNumbers.size,
            deliveredCount = group.memberPhoneNumbers.size,
            failedCount = 0,
            pendingCount = 0,
            successRatePercentage = 100.0f,
            startTimestamp = start,
            endTimestamp = System.currentTimeMillis(),
            recipientStatuses = statuses
        )
    }
}
