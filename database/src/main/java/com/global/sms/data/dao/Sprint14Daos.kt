package com.global.sms.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.global.sms.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LicenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLicense(license: LicenseEntity)

    @Query("SELECT * FROM licenses ORDER BY issuedTimestampMs DESC")
    fun getAllLicenses(): Flow<List<LicenseEntity>>

    @Query("SELECT * FROM licenses WHERE licenseId = :id")
    suspend fun getLicenseById(id: String): LicenseEntity?
}

@Dao
interface PluginMarketplaceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlugin(plugin: PluginMarketplaceEntity)

    @Query("SELECT * FROM plugin_marketplace")
    fun getAllPlugins(): Flow<List<PluginMarketplaceEntity>>

    @Query("SELECT * FROM plugin_marketplace WHERE pluginId = :id")
    suspend fun getPluginById(id: String): PluginMarketplaceEntity?
}

@Dao
interface EnterpriseUserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: EnterpriseUserEntity)

    @Query("SELECT * FROM enterprise_users")
    fun getAllUsers(): Flow<List<EnterpriseUserEntity>>
}

@Dao
interface CloudConnectorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnector(connector: CloudConnectorEntity)

    @Query("SELECT * FROM cloud_connectors")
    fun getAllConnectors(): Flow<List<CloudConnectorEntity>>
}

@Dao
interface MigrationHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMigration(history: MigrationHistoryEntity)

    @Query("SELECT * FROM migration_history ORDER BY timestampMs DESC")
    fun getAllMigrations(): Flow<List<MigrationHistoryEntity>>
}
