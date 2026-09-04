package com.global.sms.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.global.sms.data.entity.CalendarSuggestionEntity
import com.global.sms.data.entity.ContactInsightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarSuggestionDao {
    @Query("SELECT * FROM calendar_suggestions WHERE isAccepted = 0 ORDER BY eventDateMillis ASC")
    fun getPendingSuggestions(): Flow<List<CalendarSuggestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuggestion(suggestion: CalendarSuggestionEntity): Long

    @Query("UPDATE calendar_suggestions SET isAccepted = 1 WHERE id = :id")
    suspend fun markAccepted(id: Long)

    @Query("DELETE FROM calendar_suggestions WHERE id = :id")
    suspend fun deleteSuggestion(id: Long)
}

@Dao
interface ContactInsightDao {
    @Query("SELECT * FROM contact_insights ORDER BY priorityScore DESC")
    fun getAllContactInsights(): Flow<List<ContactInsightEntity>>

    @Query("SELECT * FROM contact_insights WHERE address = :address LIMIT 1")
    suspend fun getContactInsightByAddress(address: String): ContactInsightEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateContactInsight(insight: ContactInsightEntity)
}
