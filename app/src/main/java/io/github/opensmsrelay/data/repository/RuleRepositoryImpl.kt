package io.github.opensmsrelay.data.repository

import io.github.opensmsrelay.data.local.dao.RuleDao
import io.github.opensmsrelay.data.mapper.toDomain
import io.github.opensmsrelay.data.mapper.toEntity
import io.github.opensmsrelay.domain.model.Rule
import io.github.opensmsrelay.domain.repository.RuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RuleRepositoryImpl @Inject constructor(
    private val ruleDao: RuleDao
) : RuleRepository {

    override fun getAllFlow(): Flow<List<Rule>> =
        ruleDao.getAllFlow().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getAll(): List<Rule> =
        ruleDao.getAll().map { it.toDomain() }

    override suspend fun getById(id: Long): Rule? =
        ruleDao.getById(id)?.toDomain()

    override suspend fun insert(rule: Rule): Long =
        ruleDao.insert(rule.toEntity())

    override suspend fun update(rule: Rule) =
        ruleDao.update(rule.toEntity())

    override suspend fun delete(rule: Rule) =
        ruleDao.delete(rule.toEntity())

    override suspend fun deleteById(id: Long) =
        ruleDao.deleteById(id)
}
