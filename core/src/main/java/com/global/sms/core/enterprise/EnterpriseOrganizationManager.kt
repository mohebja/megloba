package com.global.sms.core.enterprise

import android.content.Context
import com.global.sms.data.dao.DepartmentDao
import com.global.sms.data.dao.EmployeeDao
import com.global.sms.data.dao.OrganizationDao
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.data.entity.DepartmentEntity
import com.global.sms.data.entity.EmployeeEntity
import com.global.sms.data.entity.OrganizationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import java.util.UUID

data class OrganizationModel(
    val id: String = UUID.randomUUID().toString(),
    val companyName: String = "سازمان من",
    val organizationType: String = "شرکت خصوصی",
    val createdDate: Long = System.currentTimeMillis(),
    val securityPolicy: String = "HIGH_STRICT",
    val subscriptionLevel: String = "PLATINUM_ENTERPRISE"
)

data class DepartmentModel(
    val id: String = UUID.randomUUID().toString(),
    val organizationId: String = "default_org",
    val name: String,
    val manager: String
)

data class EmployeeModel(
    val id: String = UUID.randomUUID().toString(),
    val departmentId: String,
    val name: String,
    val role: String,
    val permissions: List<String> = emptyList()
)

class EnterpriseOrganizationManager(
    private val orgDao: OrganizationDao? = null,
    private val departmentDao: DepartmentDao? = null,
    private val employeeDao: EmployeeDao? = null
) {

    constructor(context: Context) : this(
        orgDao = GlobalSmsDatabase.getInstance(context).organizationDao(),
        departmentDao = GlobalSmsDatabase.getInstance(context).departmentDao(),
        employeeDao = GlobalSmsDatabase.getInstance(context).employeeDao()
    )

    private val _memOrganization = MutableStateFlow<OrganizationModel?>(null)
    private val _memDepartments = MutableStateFlow<List<DepartmentModel>>(emptyList())
    private val _memEmployees = MutableStateFlow<List<EmployeeModel>>(emptyList())

    val organization: Flow<OrganizationModel> = if (orgDao != null) {
        orgDao.getOrganizationFlow().map { entity ->
            entity?.toModel() ?: OrganizationModel(id = "default_org", companyName = "سازمان من", organizationType = "شرکت خصوصی")
        }
    } else {
        _memOrganization.map { it ?: OrganizationModel(id = "default_org", companyName = "سازمان من", organizationType = "شرکت خصوصی") }
    }

    val departments: Flow<List<DepartmentModel>> = if (departmentDao != null) {
        departmentDao.getAllDepartmentsFlow().map { list ->
            list.map { it.toModel() }
        }
    } else {
        _memDepartments.asStateFlow()
    }

    val employees: Flow<List<EmployeeModel>> = if (employeeDao != null) {
        employeeDao.getAllEmployeesFlow().map { list ->
            list.map { it.toModel() }
        }
    } else {
        _memEmployees.asStateFlow()
    }

    fun createOrganization(
        name: String,
        type: String,
        securityPolicy: String = "HIGH_STRICT",
        subscriptionLevel: String = "PLATINUM_ENTERPRISE"
    ): OrganizationModel = runBlocking(Dispatchers.IO) {
        val model = OrganizationModel(
            companyName = name,
            organizationType = type,
            securityPolicy = securityPolicy,
            subscriptionLevel = subscriptionLevel
        )
        if (orgDao != null) {
            orgDao.insertOrganization(model.toEntity())
        } else {
            _memOrganization.value = model
        }
        model
    }

    fun addDepartment(name: String, manager: String, orgId: String = "default_org"): DepartmentModel = runBlocking(Dispatchers.IO) {
        val dep = DepartmentModel(
            id = UUID.randomUUID().toString(),
            organizationId = orgId,
            name = name,
            manager = manager
        )
        if (departmentDao != null) {
            departmentDao.insertDepartment(dep.toEntity())
        } else {
            _memDepartments.value = _memDepartments.value + dep
        }
        dep
    }

    fun deleteDepartment(departmentId: String) = runBlocking(Dispatchers.IO) {
        if (departmentDao != null) {
            departmentDao.deleteDepartmentById(departmentId)
        } else {
            _memDepartments.value = _memDepartments.value.filter { it.id != departmentId }
        }
    }

    fun addEmployee(departmentId: String, name: String, role: String, permissions: List<String>): EmployeeModel = runBlocking(Dispatchers.IO) {
        val emp = EmployeeModel(
            id = UUID.randomUUID().toString(),
            departmentId = departmentId,
            name = name,
            role = role,
            permissions = permissions
        )
        if (employeeDao != null) {
            employeeDao.insertEmployee(emp.toEntity())
        } else {
            _memEmployees.value = _memEmployees.value + emp
        }
        emp
    }

    fun deleteEmployee(employeeId: String) = runBlocking(Dispatchers.IO) {
        if (employeeDao != null) {
            employeeDao.deleteEmployeeById(employeeId)
        } else {
            _memEmployees.value = _memEmployees.value.filter { it.id != employeeId }
        }
    }

    fun updateEmployeePermissions(employeeId: String, permissions: List<String>): Boolean = runBlocking(Dispatchers.IO) {
        if (employeeDao != null) {
            val emp = employeeDao.getEmployeeById(employeeId) ?: return@runBlocking false
            employeeDao.updateEmployee(emp.copy(permissions = permissions.joinToString(",")))
            true
        } else {
            val currentList = _memEmployees.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == employeeId }
            if (index != -1) {
                currentList[index] = currentList[index].copy(permissions = permissions)
                _memEmployees.value = currentList
                true
            } else {
                false
            }
        }
    }
}

fun OrganizationEntity.toModel(): OrganizationModel = OrganizationModel(
    id = id,
    companyName = companyName,
    organizationType = organizationType,
    createdDate = createdDate,
    securityPolicy = securityPolicy,
    subscriptionLevel = subscriptionLevel
)

fun OrganizationModel.toEntity(): OrganizationEntity = OrganizationEntity(
    id = id,
    companyName = companyName,
    organizationType = organizationType,
    createdDate = createdDate,
    securityPolicy = securityPolicy,
    subscriptionLevel = subscriptionLevel
)

fun DepartmentEntity.toModel(): DepartmentModel = DepartmentModel(
    id = id,
    organizationId = organizationId,
    name = name,
    manager = manager
)

fun DepartmentModel.toEntity(): DepartmentEntity = DepartmentEntity(
    id = id,
    organizationId = organizationId,
    name = name,
    manager = manager
)

fun EmployeeEntity.toModel(): EmployeeModel = EmployeeModel(
    id = id,
    departmentId = departmentId,
    name = name,
    role = role,
    permissions = if (permissions.isBlank()) emptyList() else permissions.split(",").map { it.trim() }
)

fun EmployeeModel.toEntity(): EmployeeEntity = EmployeeEntity(
    id = id,
    departmentId = departmentId,
    name = name,
    role = role,
    permissions = permissions.joinToString(",")
)
