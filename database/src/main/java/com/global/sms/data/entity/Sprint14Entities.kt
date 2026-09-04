package com.global.sms.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "licenses")
data class LicenseEntity(
    @PrimaryKey val licenseId: String,
    val licenseKey: String,
    val tier: String,
    val organizationName: String,
    val maxSeats: Int,
    val expiresAtMs: Long?,
    val isActivatedOffline: Boolean,
    val issuedTimestampMs: Long
)

@Entity(tableName = "plugin_marketplace")
data class PluginMarketplaceEntity(
    @PrimaryKey val pluginId: String,
    val nameFa: String,
    val nameEn: String,
    val category: String,
    val version: String,
    val author: String,
    val description: String,
    val requiredPermissions: String,
    val isInstalled: Boolean,
    val isEnabled: Boolean,
    val isSandboxed: Boolean,
    val rating: Float
)

@Entity(tableName = "enterprise_users")
data class EnterpriseUserEntity(
    @PrimaryKey val userId: String,
    val name: String,
    val emailOrPhone: String,
    val role: String,
    val department: String,
    val isActive: Boolean,
    val createdTimestampMs: Long
)

@Entity(tableName = "cloud_connectors")
data class CloudConnectorEntity(
    @PrimaryKey val connectorId: String,
    val providerType: String,
    val serverEndpoint: String,
    val isEnabled: Boolean,
    val autoSyncEnabled: Boolean,
    val lastSyncTimestampMs: Long?,
    val requiresTls: Boolean
)

@Entity(tableName = "migration_history")
data class MigrationHistoryEntity(
    @PrimaryKey val migrationId: String,
    val sourceAppVersion: String,
    val schemaVersion: Int,
    val totalMessages: Int,
    val totalContacts: Int,
    val totalWorkflows: Int,
    val checksumSha256: String,
    val status: String,
    val timestampMs: Long
)
