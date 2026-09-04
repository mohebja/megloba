package com.global.sms.core.enterprise

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class EnterpriseRole {
    OWNER,
    ADMIN,
    MANAGER,
    EMPLOYEE,
    VIEW_ONLY
}

enum class EnterprisePermission {
    SEND_SMS,
    DELETE_SMS,
    EXPORT_MESSAGES,
    VIEW_ANALYTICS,
    MANAGE_CAMPAIGNS,
    ACCESS_AI
}

data class RoleDefinition(
    val role: EnterpriseRole,
    val titlePersian: String,
    val defaultPermissions: Set<EnterprisePermission>
)

class RolePermissionEngine {

    private val roleMap = mapOf(
        EnterpriseRole.OWNER to RoleDefinition(
            role = EnterpriseRole.OWNER,
            titlePersian = "مالک سازمان",
            defaultPermissions = EnterprisePermission.values().toSet()
        ),
        EnterpriseRole.ADMIN to RoleDefinition(
            role = EnterpriseRole.ADMIN,
            titlePersian = "مدیر ارشد سیستم",
            defaultPermissions = setOf(
                EnterprisePermission.SEND_SMS,
                EnterprisePermission.DELETE_SMS,
                EnterprisePermission.EXPORT_MESSAGES,
                EnterprisePermission.VIEW_ANALYTICS,
                EnterprisePermission.MANAGE_CAMPAIGNS,
                EnterprisePermission.ACCESS_AI
            )
        ),
        EnterpriseRole.MANAGER to RoleDefinition(
            role = EnterpriseRole.MANAGER,
            titlePersian = "مدیر دپارتمان",
            defaultPermissions = setOf(
                EnterprisePermission.SEND_SMS,
                EnterprisePermission.VIEW_ANALYTICS,
                EnterprisePermission.MANAGE_CAMPAIGNS,
                EnterprisePermission.ACCESS_AI
            )
        ),
        EnterpriseRole.EMPLOYEE to RoleDefinition(
            role = EnterpriseRole.EMPLOYEE,
            titlePersian = "کارمند عادی",
            defaultPermissions = setOf(
                EnterprisePermission.SEND_SMS,
                EnterprisePermission.ACCESS_AI
            )
        ),
        EnterpriseRole.VIEW_ONLY to RoleDefinition(
            role = EnterpriseRole.VIEW_ONLY,
            titlePersian = "مشاهده‌گر",
            defaultPermissions = setOf(
                EnterprisePermission.VIEW_ANALYTICS
            )
        )
    )

    private val _activeUserRole = MutableStateFlow(EnterpriseRole.ADMIN)
    val activeUserRole: StateFlow<EnterpriseRole> = _activeUserRole.asStateFlow()

    fun switchRole(role: EnterpriseRole) {
        _activeUserRole.value = role
    }

    fun hasPermission(role: EnterpriseRole, permission: EnterprisePermission): Boolean {
        val def = roleMap[role] ?: return false
        return def.defaultPermissions.contains(permission)
    }

    fun isActionAllowed(permission: EnterprisePermission): Boolean {
        return hasPermission(_activeUserRole.value, permission)
    }

    fun getRoleDefinition(role: EnterpriseRole): RoleDefinition {
        return roleMap[role] ?: RoleDefinition(role, role.name, emptySet())
    }
}
