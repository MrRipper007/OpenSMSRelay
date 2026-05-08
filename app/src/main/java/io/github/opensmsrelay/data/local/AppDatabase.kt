package io.github.opensmsrelay.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.opensmsrelay.data.local.converter.EnumConverters
import io.github.opensmsrelay.data.local.converter.ListConverter
import io.github.opensmsrelay.data.local.dao.ForwardingLogDao
import io.github.opensmsrelay.data.local.dao.RuleDao
import io.github.opensmsrelay.data.local.entity.ForwardingLogEntity
import io.github.opensmsrelay.data.local.entity.RuleEntity

@Database(
    entities = [RuleEntity::class, ForwardingLogEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(EnumConverters::class, ListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ruleDao(): RuleDao
    abstract fun forwardingLogDao(): ForwardingLogDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE rules ADD COLUMN email_destinations TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE rules ADD COLUMN sms_destinations TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
