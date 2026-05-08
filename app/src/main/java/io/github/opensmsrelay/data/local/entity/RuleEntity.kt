package io.github.opensmsrelay.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.opensmsrelay.domain.model.SenderMatchType

@Entity(tableName = "rules")
data class RuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean,
    @ColumnInfo(name = "match_type")
    val matchType: SenderMatchType,
    @ColumnInfo(name = "sender_value")
    val senderValue: String,
    @ColumnInfo(name = "body_filter")
    val bodyFilter: String?,
    @ColumnInfo(name = "forward_email")
    val forwardEmail: Boolean,
    @ColumnInfo(name = "forward_sms")
    val forwardSms: Boolean,
    @ColumnInfo(name = "email_destinations")
    val emailDestinations: List<String>,
    @ColumnInfo(name = "sms_destinations")
    val smsDestinations: List<String>,
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
