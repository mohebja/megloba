package com.global.sms.core.license

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class LicenseTier {
    FREE_EDITION,
    PROFESSIONAL_EDITION,
    ENTERPRISE_EDITION
}

data class LicenseInfo(
    val tier: LicenseTier = LicenseTier.FREE_EDITION,
    val licenseKey: String = "",
    val organizationName: String = "",
    val maxSeats: Int = 1,
    val isActivated: Boolean = false,
    val issuedTimestampMs: Long = System.currentTimeMillis()
)

data class TierFeature(
    val id: String,
    val name: String,
    val isAvailable: Boolean
)

class LicenseManager {
    private val _currentLicense = MutableStateFlow(LicenseInfo())
    val currentLicense: StateFlow<LicenseInfo> = _currentLicense.asStateFlow()

    fun isFeatureAccessible(requiredTier: LicenseTier): Boolean {
        val current = _currentLicense.value.tier
        return when (requiredTier) {
            LicenseTier.FREE_EDITION -> true
            LicenseTier.PROFESSIONAL_EDITION -> current == LicenseTier.PROFESSIONAL_EDITION || current == LicenseTier.ENTERPRISE_EDITION
            LicenseTier.ENTERPRISE_EDITION -> current == LicenseTier.ENTERPRISE_EDITION
        }
    }

    fun activateOfflineLicense(key: String, organization: String, tier: LicenseTier) {
        _currentLicense.value = LicenseInfo(
            tier = tier,
            licenseKey = key,
            organizationName = organization,
            maxSeats = if (tier == LicenseTier.ENTERPRISE_EDITION) 500 else 10,
            isActivated = true,
            issuedTimestampMs = System.currentTimeMillis()
        )
    }

    fun getTierFeaturesSummary(): List<TierFeature> {
        val isEnt = isFeatureAccessible(LicenseTier.ENTERPRISE_EDITION)
        return listOf(
            TierFeature("ai_copilot", "Smart AI Copilot", true),
            TierFeature("private_vault", "Zero-Trust Private Vault", true),
            TierFeature("crm_workforce", "CRM & Workforce Management", isEnt),
            TierFeature("broadcast_campaigns", "Bulk Broadcast Campaigns", isEnt),
            TierFeature("cloud_connectors", "Enterprise Private Cloud Sync", isEnt)
        )
    }
}
