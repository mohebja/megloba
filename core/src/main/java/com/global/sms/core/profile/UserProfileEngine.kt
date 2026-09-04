package com.global.sms.core.profile

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SystemUserProfile {
    PERSONAL_MODE,
    BUSINESS_MODE,
    PRIVATE_MODE,
    DRIVING_MODE,
    MEETING_MODE
}

data class ProfileSettings(
    val activeProfile: SystemUserProfile = SystemUserProfile.PERSONAL_MODE,
    val autoReplyEnabled: Boolean = false,
    val autoReplyText: String = "در حال حاضر پاسخگو نیستم. پیام شما ثبت شد.",
    val muteNonVipNotifications: Boolean = false,
    val aiCopilotActive: Boolean = true
)

class UserProfileEngine {

    private val _state = MutableStateFlow(ProfileSettings())
    val state: StateFlow<ProfileSettings> = _state.asStateFlow()

    fun switchProfile(profile: SystemUserProfile) {
        val updated = when (profile) {
            SystemUserProfile.PERSONAL_MODE -> ProfileSettings(
                activeProfile = SystemUserProfile.PERSONAL_MODE,
                autoReplyEnabled = false,
                muteNonVipNotifications = false,
                aiCopilotActive = true
            )
            SystemUserProfile.BUSINESS_MODE -> ProfileSettings(
                activeProfile = SystemUserProfile.BUSINESS_MODE,
                autoReplyEnabled = true,
                autoReplyText = "ساعات کاری پایان یافته است. پیام شما در اولین فرصت بررسی خواهد شد.",
                muteNonVipNotifications = false,
                aiCopilotActive = true
            )
            SystemUserProfile.PRIVATE_MODE -> ProfileSettings(
                activeProfile = SystemUserProfile.PRIVATE_MODE,
                autoReplyEnabled = false,
                muteNonVipNotifications = true,
                aiCopilotActive = false
            )
            SystemUserProfile.DRIVING_MODE -> ProfileSettings(
                activeProfile = SystemUserProfile.DRIVING_MODE,
                autoReplyEnabled = true,
                autoReplyText = "در حال رانندگی هستم. پس از رسیدن به مقصد پاسخ می‌دهم.",
                muteNonVipNotifications = true,
                aiCopilotActive = true
            )
            SystemUserProfile.MEETING_MODE -> ProfileSettings(
                activeProfile = SystemUserProfile.MEETING_MODE,
                autoReplyEnabled = true,
                autoReplyText = "در جلسه هستم. لطفا در صورت لزوم پیامک بزنید.",
                muteNonVipNotifications = true,
                aiCopilotActive = true
            )
        }
        _state.value = updated
    }
}
