package com.smartqr.scanner.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.smartqr.scanner.model.ScanRecord
import com.smartqr.scanner.model.ScanType

@Database(entities = [ScanRecord::class], version = 1)
@TypeConverters(ScanTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scanRecordDao(): ScanRecordDao
}

class ScanTypeConverter {
    @TypeConverter
    fun fromType(value: ScanType): String = value.name

    @TypeConverter
    fun toType(value: String): ScanType = ScanType.valueOf(value)
}
