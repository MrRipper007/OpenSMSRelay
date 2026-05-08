package io.github.opensmsrelay.data.local.converter

import androidx.room.TypeConverter
import io.github.opensmsrelay.domain.model.ForwardingStatus
import io.github.opensmsrelay.domain.model.SenderMatchType
import io.github.opensmsrelay.domain.model.SecurityType

class EnumConverters {
    @TypeConverter
    fun fromSenderMatchType(value: SenderMatchType): String = value.name

    @TypeConverter
    fun toSenderMatchType(value: String): SenderMatchType =
        SenderMatchType.valueOf(value)

    @TypeConverter
    fun fromSecurityType(value: SecurityType): String = value.name

    @TypeConverter
    fun toSecurityType(value: String): SecurityType =
        SecurityType.valueOf(value)

    @TypeConverter
    fun fromForwardingStatus(value: ForwardingStatus): String = value.name

    @TypeConverter
    fun toForwardingStatus(value: String): ForwardingStatus =
        ForwardingStatus.valueOf(value)
}
