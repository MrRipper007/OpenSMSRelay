package io.github.opensmsrelay.domain.repository

import io.github.opensmsrelay.domain.model.Rule
import kotlinx.coroutines.flow.Flow

interface RuleRepository {
    fun getAllFlow(): Flow<List<Rule>>
    suspend fun getAll(): List<Rule>
    suspend fun getById(id: Long): Rule?
    suspend fun insert(rule: Rule): Long
    suspend fun update(rule: Rule)
    suspend fun delete(rule: Rule)
    suspend fun deleteById(id: Long)
}
