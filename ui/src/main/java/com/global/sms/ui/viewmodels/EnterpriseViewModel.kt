package com.global.sms.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.global.sms.data.db.GlobalSmsDatabase
import com.global.sms.data.entity.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EnterpriseViewModel(application: Application) : AndroidViewModel(application) {

    private val db = GlobalSmsDatabase.getInstance(application)
    private val profileDao = db.enterpriseProfileDao()
    private val crmDao = db.crmCustomerDao()
    private val templateDao = db.businessTemplateDao()
    private val ruleDao = db.automationRuleDao()
    private val auditDao = db.securityAuditLogDao()
    private val bulkJobDao = db.bulkSmsJobDao()
    private val organizationDao = db.organizationDao()
    private val departmentDao = db.departmentDao()
    private val employeeDao = db.employeeDao()

    val enterpriseProfile: StateFlow<EnterpriseProfileEntity> = profileDao.getProfileFlow()
        .map { it ?: EnterpriseProfileEntity() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EnterpriseProfileEntity()
        )

    val organization: StateFlow<OrganizationEntity> = organizationDao.getOrganizationFlow()
        .map { it ?: OrganizationEntity(id = "default_org", companyName = "سازمان پیش‌فرض شرکت", organizationType = "شرکت خصوصی") }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = OrganizationEntity(id = "default_org", companyName = "سازمان پیش‌فرض شرکت", organizationType = "شرکت خصوصی")
        )

    val departments: StateFlow<List<DepartmentEntity>> = departmentDao.getAllDepartmentsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val employees: StateFlow<List<EmployeeEntity>> = employeeDao.getAllEmployeesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val customers: StateFlow<List<CrmCustomerEntity>> = crmDao.getAllCustomersFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val templates: StateFlow<List<BusinessTemplateEntity>> = templateDao.getAllTemplatesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val automationRules: StateFlow<List<AutomationRuleEntity>> = ruleDao.getAllRulesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val auditLogs: StateFlow<List<SecurityAuditLogEntity>> = auditDao.getRecentLogsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val bulkJobs: StateFlow<List<BulkSmsJobEntity>> = bulkJobDao.getAllJobsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Populate default enterprise state if empty
        viewModelScope.launch {
            if (profileDao.getProfile() == null) {
                profileDao.saveProfile(EnterpriseProfileEntity())
                auditDao.insertLog(
                    SecurityAuditLogEntity(
                        eventType = "SYSTEM_INIT",
                        description = "سیستم Enterprise با موفقیت مقداردهی اولیه شد."
                    )
                )
            }
        }
    }

    fun updateProfile(profile: EnterpriseProfileEntity) {
        viewModelScope.launch {
            profileDao.saveProfile(profile)
            auditDao.insertLog(
                SecurityAuditLogEntity(
                    eventType = "PROFILE_UPDATE",
                    description = "پروفایل سازمانی بروزرسانی شد: حالت ${profile.profileMode}"
                )
            )
        }
    }

    fun getCustomerByIdFlow(customerId: Long): Flow<CrmCustomerEntity?> {
        return crmDao.getCustomerByIdFlow(customerId)
    }

    fun saveCustomer(customer: CrmCustomerEntity) {
        viewModelScope.launch {
            crmDao.insertOrUpdateCustomer(customer)
            auditDao.insertLog(
                SecurityAuditLogEntity(
                    eventType = "CRM_CUSTOMER_SAVE",
                    description = "مخاطب CRM ذخیره شد: ${customer.name} (${customer.phoneNumber})"
                )
            )
        }
    }

    fun updateCustomerLifecycle(customerId: Long, newStatus: String) {
        viewModelScope.launch {
            val customer = crmDao.getCustomerById(customerId)
            if (customer != null) {
                val updated = customer.copy(customerStatus = newStatus)
                crmDao.insertOrUpdateCustomer(updated)
                auditDao.insertLog(
                    SecurityAuditLogEntity(
                        eventType = "CRM_STAGE_UPDATE",
                        description = "مرحله چرخه عمر ${customer.name} به $newStatus تغییر یافت."
                    )
                )
            }
        }
    }

    fun deleteCustomer(customer: CrmCustomerEntity) {
        viewModelScope.launch {
            crmDao.deleteCustomer(customer)
            auditDao.insertLog(
                SecurityAuditLogEntity(
                    eventType = "CRM_CUSTOMER_DELETE",
                    description = "مخاطب CRM حذف شد: ${customer.name}"
                )
            )
        }
    }

    fun saveTemplate(template: BusinessTemplateEntity) {
        viewModelScope.launch {
            templateDao.insertOrUpdateTemplate(template)
            auditDao.insertLog(
                SecurityAuditLogEntity(
                    eventType = "TEMPLATE_SAVE",
                    description = "قالب پیامکی ایجاد/ویرایش شد: ${template.title}"
                )
            )
        }
    }

    fun deleteTemplate(template: BusinessTemplateEntity) {
        viewModelScope.launch {
            templateDao.deleteTemplate(template)
        }
    }

    fun saveAutomationRule(rule: AutomationRuleEntity) {
        viewModelScope.launch {
            ruleDao.insertOrUpdateRule(rule)
            auditDao.insertLog(
                SecurityAuditLogEntity(
                    eventType = "AUTOMATION_RULE_SAVE",
                    description = "قانون اتوماسیون تنظیم شد: ${rule.name}"
                )
            )
        }
    }

    fun deleteAutomationRule(rule: AutomationRuleEntity) {
        viewModelScope.launch {
            ruleDao.deleteRule(rule)
        }
    }

    fun createBulkJob(title: String, recipientsCount: Int, templateBody: String) {
        viewModelScope.launch {
            val job = BulkSmsJobEntity(
                title = title,
                totalRecipients = recipientsCount,
                templateBody = templateBody,
                status = "COMPLETED",
                sentCount = recipientsCount
            )
            bulkJobDao.insertJob(job)
            auditDao.insertLog(
                SecurityAuditLogEntity(
                    eventType = "BULK_SMS_SEND",
                    description = "کمپین ارسال انبوه انجام شد: $title ($recipientsCount دریافت‌کننده)"
                )
            )
        }
    }

    fun createOrUpdateOrganization(name: String, type: String) {
        viewModelScope.launch {
            val org = OrganizationEntity(
                id = organization.value.id,
                companyName = name,
                organizationType = type
            )
            organizationDao.insertOrganization(org)
            auditDao.insertLog(
                SecurityAuditLogEntity(
                    eventType = "ORG_UPDATE",
                    description = "اطلاعات سازمان بروزرسانی شد: $name"
                )
            )
        }
    }

    fun addDepartment(name: String, manager: String) {
        viewModelScope.launch {
            val dept = DepartmentEntity(
                id = java.util.UUID.randomUUID().toString(),
                organizationId = organization.value.id,
                name = name,
                manager = manager
            )
            departmentDao.insertDepartment(dept)
            auditDao.insertLog(
                SecurityAuditLogEntity(
                    eventType = "DEPARTMENT_ADD",
                    description = "دپارتمان جدید ایجاد شد: $name (مدیر: $manager)"
                )
            )
        }
    }

    fun deleteDepartment(departmentId: String) {
        viewModelScope.launch {
            departmentDao.deleteDepartmentById(departmentId)
            auditDao.insertLog(
                SecurityAuditLogEntity(
                    eventType = "DEPARTMENT_DELETE",
                    description = "دپارتمان با شناسه $departmentId حذف شد."
                )
            )
        }
    }

    fun addEmployee(departmentId: String, name: String, role: String, permissions: List<String>) {
        viewModelScope.launch {
            val emp = EmployeeEntity(
                id = java.util.UUID.randomUUID().toString(),
                departmentId = departmentId,
                name = name,
                role = role,
                permissions = permissions.joinToString(",")
            )
            employeeDao.insertEmployee(emp)
            auditDao.insertLog(
                SecurityAuditLogEntity(
                    eventType = "EMPLOYEE_ADD",
                    description = "پرسنل جدید اضافه شد: $name (نقش: $role)"
                )
            )
        }
    }

    fun deleteEmployee(employeeId: String) {
        viewModelScope.launch {
            employeeDao.deleteEmployeeById(employeeId)
            auditDao.insertLog(
                SecurityAuditLogEntity(
                    eventType = "EMPLOYEE_DELETE",
                    description = "پرسنل با شناسه $employeeId حذف شد."
                )
            )
        }
    }

    fun updateEmployeePermissions(employeeId: String, permissions: List<String>) {
        viewModelScope.launch {
            val emp = employeeDao.getEmployeeById(employeeId)
            if (emp != null) {
                employeeDao.updateEmployee(emp.copy(permissions = permissions.joinToString(",")))
                auditDao.insertLog(
                    SecurityAuditLogEntity(
                        eventType = "EMPLOYEE_PERMISSIONS_UPDATE",
                        description = "مجوزهای پرسنل ${emp.name} بروزرسانی شد."
                    )
                )
            }
        }
    }
}
