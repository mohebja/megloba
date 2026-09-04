package com.global.sms.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.Index
import androidx.room.PrimaryKey

enum class MessageCategory {
    OTP,
    BANK,
    TRANSACTIONS,
    SPAM,
    ADVERTISEMENT,
    BUSINESS,
    SHOPPING,
    DELIVERY,
    GOVERNMENT,
    PERSONAL,
    UNKNOWN,
    WORK,
    TRAVEL,
    MEDICAL,
    IMPORTANT,
    PRIVATE
}

enum class MessageType(val code: Int) {
    INBOX(1), SENT(2), DRAFT(3), OUTBOX(4), FAILED(5), QUEUED(6)
}

enum class MessageStatus(val code: Int) {
    NONE(0),
    PENDING(1),
    SENT(2),
    DELIVERED(3),
    FAILED(4),
    RETRYING(5),
    SCHEDULED(6)
}

@Entity(
    tableName = "messages",
    indices = [
        Index("threadId"),
        Index("timestamp"),
        Index("address"),
        Index("category"),
        Index("deliveryStatus"),
        Index("isRead"),
        Index("isHidden"),
        Index("isPinned"),
        Index("simSlot"),
        Index(value = ["threadId", "isHidden", "timestamp"]),
        Index(value = ["deliveryStatus", "timestamp"]),
        Index(value = ["category", "timestamp"]),
        Index(value = ["isHidden", "category", "timestamp"])
    ]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val threadId: Long = 0L,
    val address: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: Int = MessageType.INBOX.code,
    val simSlot: Int = 0,
    val isRead: Boolean = false,
    val isHidden: Boolean = false,
    val category: MessageCategory = MessageCategory.PERSONAL,
    val isPinned: Boolean = false,
    val isEncrypted: Boolean = false,
    val otpCode: String? = null,
    val deliveryStatus: Int = MessageStatus.NONE.code,
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val attachmentUri: String? = null,
    val mimeType: String? = null,
    val subId: Int = -1,
    val isMms: Boolean = false
)

@Entity(
    tableName = "conversations",
    indices = [
        Index("address"),
        Index("category"),
        Index(value = ["isHidden", "isPinned", "lastTimestamp"])
    ]
)
data class ConversationEntity(
    @PrimaryKey val threadId: Long,
    val address: String,
    val contactName: String? = null,
    val lastMessage: String,
    val lastTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val category: MessageCategory = MessageCategory.PERSONAL,
    val isPinned: Boolean = false,
    val isHidden: Boolean = false,
    val avatarUri: String? = null,
    val isArchived: Boolean = false,
    val wallpaperUri: String? = null,
    val isMuted: Boolean = false,
    val pinnedTimestamp: Long = 0L,
    val isFavorite: Boolean = false,
    val muteUntil: Long = 0L
)

@Entity(tableName = "messages_fts")
@Fts4(contentEntity = MessageEntity::class)
data class MessageFtsEntity(
    @PrimaryKey @ColumnInfo(name = "rowid") val rowid: Long = 0,
    val body: String,
    val address: String
)

@Entity(
    tableName = "scheduled_messages",
    indices = [
        Index("status"),
        Index("scheduledTimestamp"),
        Index(value = ["status", "scheduledTimestamp"])
    ]
)
data class ScheduledMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val address: String,
    val body: String,
    val scheduledTimestamp: Long,
    val simSlot: Int = 0,
    val status: String = "PENDING" // PENDING, SENT, CANCELLED, FAILED
)

@Entity(tableName = "spam_rules")
data class SpamRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pattern: String,
    val ruleType: String = "KEYWORD" // KEYWORD, SENDER, REGEX
)

@Entity(tableName = "quick_replies")
data class QuickReplyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val icon: String = "Folder",
    val color: Long = 0xFF1A73E8,
    val priority: Int = 0,
    val autoRule: String = "",
    val createdDate: Long = System.currentTimeMillis(),
    val updatedDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    
    // Font Settings
    val fontFamily: String = "Default", // "Default", "SansSerif", "Serif", "Monospace"
    val fontSizeScale: Float = 1.0f,
    val messageTextSizeSp: Int = 15,
    val senderNameSizeSp: Int = 16,
    val dateTextSizeSp: Int = 12,

    // Color Settings (ARGB Long)
    val incomingBubbleBgColor: Long = 0xFFE9EEF6,
    val incomingBubbleTextColor: Long = 0xFF1B1F2A,
    val outgoingBubbleBgColor: Long = 0xFF1A73E8,
    val outgoingBubbleTextColor: Long = 0xFFFFFFFF,
    val headerColor: Long = 0xFF1A73E8,
    val timestampColor: Long = 0xFF707784,

    // SMS Center Settings
    val sim1SmscAddress: String = "+9891100500",
    val sim2SmscAddress: String = "+989350000000",
    val autoDetectSmsc: Boolean = true,

    // Appearance Theme Settings
    val isDarkTheme: Boolean = false,
    val isAmoledMode: Boolean = false,
    val isDynamicColors: Boolean = false,
    val usePersianDigits: Boolean = true,
    val usePersianCalendar: Boolean = true,
    val isRtlPersian: Boolean = true,

    // Chat Layout & Customization Settings
    val chatDensity: String = "Normal", // "Compact", "Normal", "Spacious"
    val bubbleRadiusDp: Int = 16,
    val wallpaperPreset: String = "None",
    val enableSwipeActions: Boolean = true,
    val conversationStyle: String = "MODERN", // "CLASSIC", "MODERN", "ENTERPRISE"
    val messageFontScale: Float = 1.0f
)

@Entity(tableName = "contact_groups")
data class ContactGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String? = null,
    val avatar: String? = null,
    val color: Long? = 0xFF1A73E8,
    val members: String = "", // Comma-separated or JSON list of phone numbers
    val createdDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "search_history", indices = [Index(value = ["query"], unique = true)])
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val categoryTag: String? = null
)

@Entity(
    tableName = "classification_rules",
    indices = [
        Index("priority"),
        Index("isEnabled"),
        Index("targetCategory")
    ]
)
data class ClassificationRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetCategory: String, // OTP, BANK, TRANSACTIONS, SPAM, ADVERTISEMENT, BUSINESS, SHOPPING, DELIVERY, GOVERNMENT, PERSONAL, UNKNOWN
    val keywords: String = "", // Comma-separated keywords or expressions
    val senderPattern: String = "", // Sender match pattern (e.g. 983000*, Digikala, Tipax)
    val ruleType: String = "KEYWORD", // KEYWORD, SENDER, REGEX, COMBINED
    val priority: Int = 50, // 1 to 100+
    val isEnabled: Boolean = true,
    val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "contacts",
    indices = [
        Index("phoneNumber"),
        Index("normalizedNumber"),
        Index("favorite")
    ]
)
data class ContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val normalizedNumber: String,
    val photoUri: String? = null,
    val favorite: Boolean = false,
    val lastInteraction: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "contact_group_members",
    primaryKeys = ["groupId", "contactId"],
    indices = [
        Index("groupId"),
        Index("contactId")
    ]
)
data class ContactGroupMemberEntity(
    val groupId: Long,
    val contactId: Long
)

@Entity(
    tableName = "reminders",
    indices = [
        Index("threadId"),
        Index("scheduledTime"),
        Index("isCompleted")
    ]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val threadId: Long,
    val title: String,
    val scheduledTime: Long,
    val isCompleted: Boolean = false,
    val createdTime: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "conversation_tags",
    indices = [
        Index("threadId"),
        Index("tag")
    ]
)
data class ConversationTagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val threadId: Long,
    val tag: String
)

@Entity(
    tableName = "bookmarks",
    indices = [
        Index("messageId"),
        Index("threadId"),
        Index("timestamp")
    ]
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val messageId: Long,
    val threadId: Long,
    val note: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "sms_import_logs",
    indices = [Index("timestamp")]
)
data class SmsImportLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val totalSystemSms: Int = 0,
    val newlyImportedCount: Int = 0,
    val skippedDuplicatesCount: Int = 0,
    val failedCount: Int = 0,
    val status: String = "SUCCESS",
    val durationMs: Long = 0L
)




