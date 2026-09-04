package com.global.sms.data.dao

import androidx.room.*
import com.global.sms.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EnterpriseProfileDao {
    @Query("SELECT * FROM enterprise_profiles WHERE id = 1")
    fun getProfileFlow(): Flow<EnterpriseProfileEntity?>

    @Query("SELECT * FROM enterprise_profiles WHERE id = 1")
    suspend fun getProfile(): EnterpriseProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: EnterpriseProfileEntity)
}

@Dao
interface CrmCustomerDao {
    @Query("SELECT * FROM crm_customers ORDER BY lastContactDate DESC")
    fun getAllCustomersFlow(): Flow<List<CrmCustomerEntity>>

    @Query("SELECT * FROM crm_customers WHERE id = :id LIMIT 1")
    fun getCustomerByIdFlow(id: Long): Flow<CrmCustomerEntity?>

    @Query("SELECT * FROM crm_customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: Long): CrmCustomerEntity?

    @Query("SELECT * FROM crm_customers WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getCustomerByPhone(phoneNumber: String): CrmCustomerEntity?

    @Query("SELECT * FROM crm_customers WHERE name LIKE '%' || :query || '%' OR phoneNumber LIKE '%' || :query || '%' OR company LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' ORDER BY lastContactDate DESC")
    fun searchCustomers(query: String): Flow<List<CrmCustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCustomer(customer: CrmCustomerEntity): Long

    @Delete
    suspend fun deleteCustomer(customer: CrmCustomerEntity)
}

@Dao
interface BusinessTemplateDao {
    @Query("SELECT * FROM business_templates ORDER BY usageCount DESC, id DESC")
    fun getAllTemplatesFlow(): Flow<List<BusinessTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTemplate(template: BusinessTemplateEntity): Long

    @Query("UPDATE business_templates SET usageCount = usageCount + 1 WHERE id = :id")
    suspend fun incrementUsage(id: Long)

    @Delete
    suspend fun deleteTemplate(template: BusinessTemplateEntity)
}

@Dao
interface AutomationRuleDao {
    @Query("SELECT * FROM automation_rules WHERE isEnabled = 1")
    fun getEnabledRulesFlow(): Flow<List<AutomationRuleEntity>>

    @Query("SELECT * FROM automation_rules ORDER BY id DESC")
    fun getAllRulesFlow(): Flow<List<AutomationRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRule(rule: AutomationRuleEntity): Long

    @Delete
    suspend fun deleteRule(rule: AutomationRuleEntity)
}

@Dao
interface SecurityAuditLogDao {
    @Query("SELECT * FROM security_audit_logs ORDER BY timestamp DESC LIMIT 200")
    fun getRecentLogsFlow(): Flow<List<SecurityAuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SecurityAuditLogEntity): Long
}

@Dao
interface BulkSmsJobDao {
    @Query("SELECT * FROM bulk_sms_jobs ORDER BY timestamp DESC")
    fun getAllJobsFlow(): Flow<List<BulkSmsJobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: BulkSmsJobEntity): Long

    @Query("UPDATE bulk_sms_jobs SET sentCount = :sent, failedCount = :failed, status = :status WHERE id = :id")
    suspend fun updateJobProgress(id: Long, sent: Int, failed: Int, status: String)
}
