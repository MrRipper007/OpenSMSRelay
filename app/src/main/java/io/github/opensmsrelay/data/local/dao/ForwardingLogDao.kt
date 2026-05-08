package io.github.opensmsrelay.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.github.opensmsrelay.data.local.entity.ForwardingLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ForwardingLogDao {
    @Query("SELECT * FROM forwarding_logs ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<ForwardingLogEntity>>

    @Query("SELECT * FROM forwarding_logs WHERE is_matched = 1 ORDER BY timestamp DESC")
    fun getMatchedFlow(): Flow<List<ForwardingLogEntity>>

    @Query("SELECT * FROM forwarding_logs WHERE is_matched = 0 ORDER BY timestamp DESC")
    fun getIgnoredFlow(): Flow<List<ForwardingLogEntity>>

    @Query("SELECT * FROM forwarding_logs WHERE email_status = 'FAILED' OR sms_status = 'FAILED' ORDER BY timestamp DESC")
    fun getFailedFlow(): Flow<List<ForwardingLogEntity>>

    @Query("SELECT * FROM forwarding_logs ORDER BY timestamp DESC")
    suspend fun getAll(): List<ForwardingLogEntity>

    @Query("SELECT * FROM forwarding_logs WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ForwardingLogEntity?

    @Query("SELECT * FROM forwarding_logs WHERE email_status = 'FAILED' OR email_status = 'PENDING'")
    suspend fun getFailedEmailLogs(): List<ForwardingLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: ForwardingLogEntity): Long

    @Update
    suspend fun update(log: ForwardingLogEntity)

    @Query("DELETE FROM forwarding_logs")
    suspend fun deleteAll()
}
