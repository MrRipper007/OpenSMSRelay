package io.github.opensmsrelay.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.github.opensmsrelay.data.local.entity.RuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {
    @Query("SELECT * FROM rules ORDER BY created_at DESC")
    fun getAllFlow(): Flow<List<RuleEntity>>

    @Query("SELECT * FROM rules ORDER BY created_at DESC")
    suspend fun getAll(): List<RuleEntity>

    @Query("SELECT * FROM rules WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): RuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: RuleEntity): Long

    @Update
    suspend fun update(rule: RuleEntity)

    @Delete
    suspend fun delete(rule: RuleEntity)

    @Query("DELETE FROM rules WHERE id = :id")
    suspend fun deleteById(id: Long)
}
