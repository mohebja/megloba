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
    private val conversationDao = db.conversationDao()
    private val messageDao = db.messageDao()

    val enterpriseProfile: StateFlow<EnterpriseProfileEntity> = profileDao.getProfileFlow()
        .map { it ?: EnterpriseProfileEntity() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EnterpriseProfileEntity()
        )

    val organization: StateFlow<OrganizationEntity> = organizationDao.getOrganizationFlow()
        .map { it ?: OrganizationEntity(id = "default_org", companyName = "گروه ارتباطات و خدمات سازمانی", organizationType = "شرکت دانش‌بنیان / سازمانی") }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = OrganizationEntity(id = "default_org", companyName = "گروه ارتباطات و خدمات سازمانی", organizationType = "شرکت دانش‌بنیان / سازمانی")
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

    val conversations: StateFlow<List<ConversationEntity>> = conversationDao.getAllConversations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val sentMessagesCount: StateFlow<Int> = messageDao.getSentMessageCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val totalMessagesCount: StateFlow<Int> = messageDao.getTotalMessageCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    init {
        // Populate default enterprise state and check database connections
        viewModelScope.launch {
            if (profileDao.getProfile() == null) {
                profileDao.saveProfile(EnterpriseProfileEntity())
            }

            if (organizationDao.getOrganization() == null) {
                organizationDao.insertOrganization(
                    OrganizationEntity(
                        id = "default_org",
                        companyName = "گروه ارتباطات و خدمات سازمانی",
                        organizationType = "شرکت دانش‌بنیان / سازمانی"
                    )
                )
            }

            if (departmentDao.getAllDepartments().isEmpty()) {
                departmentDao.insertDepartments(
                    listOf(
                        DepartmentEntity("dept_support", "default_org", "پشتیبانی و ارتباط با مشتریان", "مهندس اکبری"),
                        DepartmentEntity("dept_sales", "default_org", "فروش و بازاریابی", "دکتر شمس"),
                        DepartmentEntity("dept_it", "default_org", "فناوری اطلاعات و زیرساخت", "مهندس رضایی")
                    )
                )
            }

            if (employeeDao.getAllEmployees().isEmpty()) {
                employeeDao.insertEmployees(
                    listOf(
                        EmployeeEntity("emp_1", "dept_support", "علی محمدی", "MANAGER", "SEND_SMS,VIEW_CRM,RESOLVE_TICKET"),
                        EmployeeEntity("emp_2", "dept_sales", "سارا احمدی", "EMPLOYEE", "SEND_SMS,VIEW_CRM"),
                        EmployeeEntity("emp_3", "dept_it", "امیر حسینی", "ADMIN", "ALL_PERMISSIONS")
                    )
                )
            }

            val existingTemplates = templateDao.getAllTemplatesFlow().firstOrNull() ?: emptyList()
            if (existingTemplates.isEmpty()) {
                templateDao.insertOrUpdateTemplate(
                    BusinessTemplateEntity(
                        title = "تایید ثبت سفارش و کد پیگیری",
                        body = "سلام {name} گرامی، سفارش شما ثبت و در حال آماده‌سازی است. کد پیگیری شما: {order_number}",
                        category = "فروش"
                    )
                )
                templateDao.insertOrUpdateTemplate(
                    BusinessTemplateEntity(
                        title = "صدور فاکتور و پرداخت",
                        body = "مشتری ارجمند {name}، فاکتور به مبلغ {amount} ریال صادر شد. جهت پرداخت آنلاین اقدام فرمایید.",
                        category = "مالی"
                    )
                )
                templateDao.insertOrUpdateTemplate(
                    BusinessTemplateEntity(
                        title = "پیگیری قرارداد و خدمات",
                        body = "جناب آقای/خانم {name}، جهت پیگیری مفاد قرارداد لطفا با واحد پشتیبانی در تماس باشید.",
                        category = "پشتیبانی"
                    )
                )
                templateDao.insertOrUpdateTemplate(
                    BusinessTemplateEntity(
                        title = "تبریک مناسبتی و هدیه ویژه",
                        body = "همراه گرامی {name}، سالروز میلادتان فرخنده باد! هدیه ویژه سازمانی برای خرید بعدی شما منظور گردید.",
                        category = "عمومی"
                    )
                )
            }

            val existingRules = ruleDao.getAllRulesFlow().firstOrNull() ?: emptyList()
            if (existingRules.isEmpty()) {
                ruleDao.insertOrUpdateRule(
                    AutomationRuleEntity(
                        name = "پیشنهاد خودکار قالب استعلام قیمت",
                        triggerKeyword = "قیمت",
                        actionType = "SUGGEST_TEMPLATE",
                        actionValue = "ارسال تعرفه و لیست قیمت رسمی",
                        isEnabled = true
                    )
                )
                ruleDao.insertOrUpdateRule(
                    AutomationRuleEntity(
                        name = "ارجاع خودکار پیام‌های فوری",
                        triggerKeyword = "فوری",
                        actionType = "MARK_STATUS",
                        actionValue = "VIP",
                        isEnabled = true
                    )
                )
            }

            val existingCustomers = crmDao.getAllCustomersFlow().firstOrNull() ?: emptyList()
            if (existingCustomers.isEmpty()) {
                crmDao.insertOrUpdateCustomer(
                    CrmCustomerEntity(
                        name = "شرکت مهندسی داده‌ورزان پارس",
                        phoneNumber = "09121112233",
                        company = "داده‌ورزان پارس",
                        email = "info@dadevarzan.ir",
                        notes = "مشتری کلیدی سازمانی - قرارداد سالانه فعال",
                        tags = "VIP, مشتری ویژه",
                        customerStatus = "VIP"
                    )
                )
                crmDao.insertOrUpdateCustomer(
                    CrmCustomerEntity(
                        name = "بازرگانی پیشگامان صنعت نوین",
                        phoneNumber = "09359876543",
                        company = "پیشگامان صنعت نوین",
                        email = "contact@pishgaman.com",
                        notes = "استعلام تعرفه ارسال پیامک انبوه و قالب‌ها",
                        tags = "مشتری, سرنخ فروش",
                        customerStatus = "LEAD"
                    )
                )
                crmDao.insertOrUpdateCustomer(
                    CrmCustomerEntity(
                        name = "فروشگاه زنجیره‌ای تارا",
                        phoneNumber = "09198765432",
                        company = "هایپرمارکت‌های تارا",
                        email = "crm@taramarket.ir",
                        notes = "مشتری فعال کمپین‌های هفتگی تخفیف",
                        tags = "مشتری, فعال",
                        customerStatus = "ACTIVE"
                    )
                )
            }

            auditDao.insertLog(
                SecurityAuditLogEntity(
                    eventType = "ENTERPRISE_INIT",
                    description = "سامانه سازمانی و پایگاه داده با کلیه جداول و قالب‌های اولیه آماده بکار شد."
                )
            )
        }
    }

    fun getMessagesForThread(threadId: Long): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForThread(threadId)
    }

    fun sendMessage(threadId: Long, recipient: String, body: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val effectiveThreadId = if (threadId > 0) threadId else now
            val msg = MessageEntity(
                threadId = effectiveThreadId,
                address = recipient,
                body = body,
                timestamp = now,
                isRead = true,
                type = 2 // SENT
            )
            messageDao.insertMessage(msg)

            val conv = conversationDao.getConversationByThreadId(effectiveThreadId)
            val customer = crmDao.getCustomerByPhone(recipient)
            val updatedConv = conv?.copy(
                lastMessage = body,
                lastTimestamp = now,
                unreadCount = 0
            ) ?: ConversationEntity(
                threadId = effectiveThreadId,
                address = recipient,
                contactName = customer?.name ?: recipient,
                lastMessage = body,
                lastTimestamp = now,
                unreadCount = 0
            )
            conversationDao.insertOrUpdateConversation(updatedConv)

            // Update customer last contact date
            if (customer != null) {
                crmDao.insertOrUpdateCustomer(customer.copy(lastContactDate = now))
            }

            auditDao.insertLog(
                SecurityAuditLogEntity(
                    eventType = "SMS_SEND",
                    description = "پیامک سازمانی به $recipient با متن: ${body.take(30)}... ارسال شد."
                )
            )
        }
    }

    fun deleteConversation(threadId: Long) {
        viewModelScope.launch {
            conversationDao.setConversationHidden(threadId, true)
            auditDao.insertLog(
                SecurityAuditLogEntity(
                    eventType = "CONVERSATION_ARCHIVE",
                    description = "گفتگو با شناسه $threadId بایگانی گردید."
                )
            )
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
