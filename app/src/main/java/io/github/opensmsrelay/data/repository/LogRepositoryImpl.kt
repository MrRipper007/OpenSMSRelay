package io.github.opensmsrelay.data.repository

import io.github.opensmsrelay.data.local.dao.ForwardingLogDao
import io.github.opensmsrelay.data.mapper.toDomain
import io.github.opensmsrelay.data.mapper.toEntity
import io.github.opensmsrelay.domain.model.ForwardingLog
import io.github.opensmsrelay.domain.model.ForwardingStatus
import io.github.opensmsrelay.domain.repository.LogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LogRepositoryImpl @Inject constructor(
    private val logDao: ForwardingLogDao
) : LogRepository {

    override fun getAllFlow(): Flow<List<ForwardingLog>> =
        logDao.getAllFlow().map { it.map { e -> e.toDomain() } }

    override fun getFilteredFlow(
        emailStatus: ForwardingStatus?,
        smsStatus: ForwardingStatus?
    ): Flow<List<ForwardingLog>> = when {
        emailStatus == ForwardingStatus.FAILED || smsStatus == ForwardingStatus.FAILED ->
            logDao.getFailedFlow().map { it.map { e -> e.toDomain() } }
        else -> getAllFlow()
    }

    override suspend fun getAll(): List<ForwardingLog> =
        logDao.getAll().map { it.toDomain() }

    override suspend fun getById(id: Long): ForwardingLog? =
        logDao.getById(id)?.toDomain()

    override suspend fun getFailedEmailLogs(): List<ForwardingLog> =
        logDao.getFailedEmailLogs().map { it.toDomain() }

    override suspend fun insert(log: ForwardingLog): Long =
        logDao.insert(log.toEntity())

    override suspend fun update(log: ForwardingLog) =
        logDao.update(log.toEntity())

    override suspend fun deleteAll() =
        logDao.deleteAll()
}
