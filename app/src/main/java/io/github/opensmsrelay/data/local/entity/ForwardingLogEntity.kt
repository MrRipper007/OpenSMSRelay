package io.github.opensmsrelay.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.opensmsrelay.domain.model.ForwardingStatus

@Entity(tableName = "forwarding_logs")
data class ForwardingLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val sender: String,
    val body: String,
    @ColumnInfo(name = "matched_rule_id")
    val matchedRuleId: Long?,
    @ColumnInfo(name = "matched_rule_name")
    val matchedRuleName: String?,
    @ColumnInfo(name = "is_matched")
    val isMatched: Boolean,
    @ColumnInfo(name = "email_status")
    val emailStatus: ForwardingStatus,
    @ColumnInfo(name = "sms_status")
    val smsStatus: ForwardingStatus,
    @ColumnInfo(name = "retry_count")
    val retryCount: Int,
    @ColumnInfo(name = "error_message")
    val errorMessage: String?
)
