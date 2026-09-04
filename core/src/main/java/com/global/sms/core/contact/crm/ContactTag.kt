package com.global.sms.core.contact.crm

enum class ContactTagType(val displayNameFa: String, val colorHex: String) {
    CUSTOMER("مشتری", "#1A73E8"),
    FAMILY("خانواده", "#E53935"),
    BANK("بانک", "#00C853"),
    WORK("همکار / کاری", "#FF9800"),
    VIP("مشتری ویژه (VIP)", "#A855F7"),
    OTHER("سایر", "#78909C")
}

data class ContactTag(
    val id: String,
    val name: String,
    val type: ContactTagType = ContactTagType.OTHER,
    val colorHex: String = "#1A73E8"
)
