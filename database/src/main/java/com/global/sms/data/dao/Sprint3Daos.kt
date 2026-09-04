package com.global.sms.data.dao

import androidx.room.*
import com.global.sms.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactProfileDao {
    @Query("SELECT * FROM contact_profiles ORDER BY fullName ASC")
    fun getAllContactProfiles(): Flow<List<ContactProfileEntity>>

    @Query("SELECT * FROM contact_profiles WHERE id = :id")
    suspend fun getProfileById(id: String): ContactProfileEntity?

    @Query("SELECT * FROM contact_profiles WHERE primaryPhoneNumber = :phone LIMIT 1")
    suspend fun getProfileByPhone(phone: String): ContactProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: ContactProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: ContactProfileEntity)
}

@Dao
interface CampaignDao {
    @Query("SELECT * FROM campaigns ORDER BY createdTimestamp DESC")
    fun getAllCampaigns(): Flow<List<CampaignEntity>>

    @Query("SELECT * FROM campaigns WHERE id = :id")
    suspend fun getCampaignById(id: String): CampaignEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCampaign(campaign: CampaignEntity)

    @Query("UPDATE campaigns SET status = :status, deliveredCount = :delivered, failedCount = :failed, pendingCount = :pending WHERE id = :id")
    suspend fun updateCampaignStatus(id: String, status: String, delivered: Int, failed: Int, pending: Int)
}

@Dao
interface CampaignRecipientDao {
    @Query("SELECT * FROM campaign_recipients WHERE campaignId = :campaignId")
    fun getRecipientsForCampaign(campaignId: String): Flow<List<CampaignRecipientEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipients(recipients: List<CampaignRecipientEntity>)
}

@Dao
interface AiInsightDao {
    @Query("SELECT * FROM ai_insights WHERE conversationId = :conversationId LIMIT 1")
    suspend fun getInsightForConversation(conversationId: String): AiInsightEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInsight(insight: AiInsightEntity)
}

@Dao
interface BackupDao {
    @Query("SELECT * FROM backups ORDER BY timestamp DESC")
    fun getAllBackups(): Flow<List<BackupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackupLog(backup: BackupEntity)
}
