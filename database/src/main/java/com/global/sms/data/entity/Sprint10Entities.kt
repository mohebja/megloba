package com.global.sms.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "organizations")
data class OrganizationEntity(
    @PrimaryKey val id: String,
    val companyName: String,
    val organizationType: String,
    val createdDate: Long = System.currentTimeMillis(),
    val securityPolicy: String = "STANDARD",
    val subscriptionLevel: String = "ENTERPRISE"
)

@Entity(tableName = "departments")
data class DepartmentEntity(
    @PrimaryKey val id: String,
    val organizationId: String,
    val name: String,
    val manager: String
)

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey val id: String,
    val departmentId: String,
    val name: String,
    val role: String,
    val permissions: String
)

@Entity(tableName = "permissions")
data class PermissionEntity(
    @PrimaryKey val id: String,
    val roleName: String,
    val allowedPermissions: String
)

@Entity(tableName = "sync_logs")
data class SyncEntity(
    @PrimaryKey val syncId: String,
    val deviceType: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String,
    val encryptedPayloadSize: Long
)

@Entity(tableName = "audit_trail")
data class AuditEntity(
    @PrimaryKey val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val action: String,
    val actor: String,
    val details: String,
    val isSecurityViolation: Boolean = false
)
