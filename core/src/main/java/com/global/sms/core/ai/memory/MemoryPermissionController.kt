package com.global.sms.core.ai.memory

enum class MemoryScope {
    ORGANIZATION_WIDE,
    DEPARTMENT_LEVEL,
    CUSTOMER_PREFERENCES,
    PRIVATE_VAULT
}

enum class UserRole {
    SUPER_ADMIN,
    ENTERPRISE_ADMIN,
    DEPARTMENT_MANAGER,
    REGULAR_AGENT,
    GUEST
}

data class EnterpriseMemoryRecord(
    val memoryId: String,
    val scope: MemoryScope,
    val departmentId: String?,
    val key: String,
    val value: String,
    val sensitivityLevel: Int = 1, // 1: Low, 2: Medium, 3: Restricted
    val updatedAt: Long = System.currentTimeMillis()
)

object MemoryPermissionController {

    fun canReadMemory(userRole: UserRole, userDepartmentId: String?, record: EnterpriseMemoryRecord): Boolean {
        if (userRole == UserRole.SUPER_ADMIN || userRole == UserRole.ENTERPRISE_ADMIN) return true

        return when (record.scope) {
            MemoryScope.ORGANIZATION_WIDE -> true
            MemoryScope.DEPARTMENT_LEVEL -> record.departmentId == userDepartmentId || userRole == UserRole.DEPARTMENT_MANAGER
            MemoryScope.CUSTOMER_PREFERENCES -> record.sensitivityLevel <= 2
            MemoryScope.PRIVATE_VAULT -> false
        }
    }

    fun canWriteMemory(userRole: UserRole, userDepartmentId: String?, targetScope: MemoryScope): Boolean {
        if (userRole == UserRole.SUPER_ADMIN || userRole == UserRole.ENTERPRISE_ADMIN) return true

        return when (targetScope) {
            MemoryScope.ORGANIZATION_WIDE -> false
            MemoryScope.DEPARTMENT_LEVEL -> userRole == UserRole.DEPARTMENT_MANAGER
            MemoryScope.CUSTOMER_PREFERENCES -> userRole == UserRole.REGULAR_AGENT || userRole == UserRole.DEPARTMENT_MANAGER
            MemoryScope.PRIVATE_VAULT -> false
        }
    }

    fun filterAccessibleMemories(
        userRole: UserRole,
        userDepartmentId: String?,
        memories: List<EnterpriseMemoryRecord>
    ): List<EnterpriseMemoryRecord> {
        return memories.filter { canReadMemory(userRole, userDepartmentId, it) }
    }
}
