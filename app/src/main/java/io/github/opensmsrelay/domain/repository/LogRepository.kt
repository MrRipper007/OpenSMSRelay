package io.github.opensmsrelay.domain.repository

import io.github.opensmsrelay.domain.model.ForwardingLog
import io.github.opensmsrelay.domain.model.ForwardingStatus
import kotlinx.coroutines.flow.Flow

interface LogRepository {
    fun getAllFlow(): Flow<List<ForwardingLog>>
    fun getFilteredFlow(emailStatus: ForwardingStatus? = null, smsStatus: ForwardingStatus? = null): Flow<List<ForwardingLog>>
    suspend fun getAll(): List<ForwardingLog>
    suspend fun getById(id: Long): ForwardingLog?
    suspend fun getFailedEmailLogs(): List<ForwardingLog>
    suspend fun insert(log: ForwardingLog): Long
    suspend fun update(log: ForwardingLog)
    suspend fun deleteAll()
}
