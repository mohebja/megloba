package com.global.sms.data.dao

import androidx.room.*
import com.global.sms.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OrganizationDao {
    @Query("SELECT * FROM organizations LIMIT 1")
    fun getOrganizationFlow(): Flow<OrganizationEntity?>

    @Query("SELECT * FROM organizations LIMIT 1")
    suspend fun getOrganization(): OrganizationEntity?

    @Query("SELECT * FROM organizations WHERE id = :id")
    suspend fun getOrganizationById(id: String): OrganizationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrganization(organization: OrganizationEntity)

    @Update
    suspend fun updateOrganization(organization: OrganizationEntity)

    @Delete
    suspend fun deleteOrganization(organization: OrganizationEntity)

    @Query("DELETE FROM organizations")
    suspend fun deleteAllOrganizations()
}

@Dao
interface DepartmentDao {
    @Query("SELECT * FROM departments ORDER BY name ASC")
    fun getAllDepartmentsFlow(): Flow<List<DepartmentEntity>>

    @Query("SELECT * FROM departments WHERE organizationId = :organizationId ORDER BY name ASC")
    fun getDepartmentsByOrgFlow(organizationId: String): Flow<List<DepartmentEntity>>

    @Query("SELECT * FROM departments WHERE id = :id")
    suspend fun getDepartmentById(id: String): DepartmentEntity?

    @Query("SELECT * FROM departments")
    suspend fun getAllDepartments(): List<DepartmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepartment(department: DepartmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepartments(departments: List<DepartmentEntity>)

    @Update
    suspend fun updateDepartment(department: DepartmentEntity)

    @Delete
    suspend fun deleteDepartment(department: DepartmentEntity)

    @Query("DELETE FROM departments WHERE id = :id")
    suspend fun deleteDepartmentById(id: String)
}

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getAllEmployeesFlow(): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees WHERE departmentId = :departmentId ORDER BY name ASC")
    fun getEmployeesByDeptFlow(departmentId: String): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees WHERE id = :id")
    suspend fun getEmployeeById(id: String): EmployeeEntity?

    @Query("SELECT * FROM employees")
    suspend fun getAllEmployees(): List<EmployeeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: EmployeeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployees(employees: List<EmployeeEntity>)

    @Update
    suspend fun updateEmployee(employee: EmployeeEntity)

    @Delete
    suspend fun deleteEmployee(employee: EmployeeEntity)

    @Query("DELETE FROM employees WHERE id = :id")
    suspend fun deleteEmployeeById(id: String)
}

@Dao
interface PermissionDao {
    @Query("SELECT * FROM permissions")
    fun getAllPermissionsFlow(): Flow<List<PermissionEntity>>

    @Query("SELECT * FROM permissions WHERE id = :id")
    suspend fun getPermissionById(id: String): PermissionEntity?

    @Query("SELECT * FROM permissions WHERE roleName = :roleName LIMIT 1")
    suspend fun getPermissionByRole(roleName: String): PermissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPermission(permission: PermissionEntity)

    @Delete
    suspend fun deletePermission(permission: PermissionEntity)
}

@Dao
interface SyncDao {
    @Query("SELECT * FROM sync_logs ORDER BY timestamp DESC")
    fun getAllSyncLogsFlow(): Flow<List<SyncEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncLog(sync: SyncEntity)
}

@Dao
interface AuditTrailDao {
    @Query("SELECT * FROM audit_trail ORDER BY timestamp DESC")
    fun getAllAuditLogsFlow(): Flow<List<AuditEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(audit: AuditEntity)
}
