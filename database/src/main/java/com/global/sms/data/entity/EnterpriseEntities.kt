package com.global.sms.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "enterprise_profiles")
data class EnterpriseProfileEntity(
    @PrimaryKey val id: Long = 1,
    val profileMode: String = "PERSONAL", // PERSONAL, BUSINESS, ENTERPRISE
    val companyName: String = "گروه تجاری آریا",
    val operatorName: String = "مدیر سیستم",
    val signatureText: String = "Sent via Global SMS Enterprise",
    val isSignatureEnabled: Boolean = true,
    val workingHoursStart: String = "08:00",
    val workingHoursEnd: String = "18:00",
    val isWorkingHoursEnforced: Boolean = false,
    val syncToCloud: Boolean = false,
    val teamOperatorRole: String = "ADMIN"
)

@Entity(
    tableName = "crm_customers",
    indices = [
        Index("phoneNumber"),
        Index("company"),
        Index("customerStatus")
    ]
)
data class CrmCustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val company: String? = null,
    val email: String? = null,
    val notes: String? = null,
    val tags: String = "مشتری", // Comma-separated: VIP, مشتری, تامین کننده, همکار
    val lastContactDate: Long = System.currentTimeMillis(),
    val assignedOperator: String? = "اپراتور ۱",
    val customerStatus: String = "ACTIVE" // LEAD, ACTIVE, VIP, INACTIVE
)

@Entity(tableName = "business_templates")
data class BusinessTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String, // e.g. "سلام {name}، سفارش شماره {order_number} شما آماده ارسال است."
    val category: String = "عمومی", // فروش, پشتیبانی, عمومی, پیگیری
    val usageCount: Int = 0
)

@Entity(tableName = "automation_rules")
data class AutomationRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val triggerKeyword: String, // e.g. "قیمت" or "لغو"
    val conditionSenderType: String = "ALL", // ALL, CRM_ONLY, UNKNOWN
    val actionType: String = "SUGGEST_TEMPLATE", // SUGGEST_TEMPLATE, MARK_STATUS, AUTO_REPLY, TAG_CUSTOMER
    val actionValue: String, // Template ID or Tag Name or Status
    val isEnabled: Boolean = true
)

@Entity(
    tableName = "security_audit_logs",
    indices = [
        Index("timestamp"),
        Index("eventType")
    ]
)
data class SecurityAuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String, // PROFILE_SWITCH, BULK_SMS_SEND, EXPORT_DATA, LOGIN, SETTINGS_CHANGE
    val description: String,
    val operatorName: String = "مدیر سیستم",
    val ipOrDeviceId: String = "LOCAL_DEVICE"
)

@Entity(tableName = "bulk_sms_jobs")
data class BulkSmsJobEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val totalRecipients: Int,
    val sentCount: Int = 0,
    val failedCount: Int = 0,
    val status: String = "QUEUED", // QUEUED, RUNNING, COMPLETED, PAUSED
    val timestamp: Long = System.currentTimeMillis(),
    val templateBody: String
)
