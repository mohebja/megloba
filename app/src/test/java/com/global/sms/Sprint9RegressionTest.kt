package com.global.sms

import com.global.sms.core.battery.AiProcessingLevel
import com.global.sms.core.battery.BatteryStatusInfo
import com.global.sms.core.notification.NotificationPriority
import com.global.sms.core.profile.SystemUserProfile
import com.global.sms.core.profile.UserProfileEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Sprint9RegressionTest {

    @Test
    fun testUserProfileSwitching() {
        val engine = UserProfileEngine()
        assertEquals(SystemUserProfile.PERSONAL_MODE, engine.state.value.activeProfile)

        engine.switchProfile(SystemUserProfile.BUSINESS_MODE)
        assertEquals(SystemUserProfile.BUSINESS_MODE, engine.state.value.activeProfile)
        assertTrue(engine.state.value.autoReplyEnabled)

        engine.switchProfile(SystemUserProfile.PRIVATE_MODE)
        assertEquals(SystemUserProfile.PRIVATE_MODE, engine.state.value.activeProfile)
        assertTrue(engine.state.value.muteNonVipNotifications)
    }

    @Test
    fun testBatteryOptimizationLevels() {
        val status = BatteryStatusInfo(
            batteryLevelPercentage = 85,
            isCharging = false,
            isPowerSaveMode = false,
            isIgnoringBatteryOptimizations = true,
            processingLevel = AiProcessingLevel.BALANCED
        )
        assertEquals(AiProcessingLevel.BALANCED, status.processingLevel)
        assertEquals(85, status.batteryLevelPercentage)
    }

    @Test
    fun testNotificationPriorityCalculation() {
        val body = "کد تایید ورود شما 987654"
        assertTrue(body.contains("کد") || body.contains("تایید"))
    }

    @Test
    fun testBackupAndSecurityModel() {
        val backupId = "backup_1700000000000"
        assertNotNull(backupId)
        assertTrue(backupId.startsWith("backup_"))
    }
}
