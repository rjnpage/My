package com.smartqr.scanner.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_records")
data class ScanRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rawData: String,
    val type: ScanType,
    val timestamp: Long = System.currentTimeMillis()
)
