package com.example.smartqrscanner.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanRecordDao {
    @Insert
    suspend fun insert(record: ScanRecord)

    @Query("SELECT * FROM scan_history ORDER BY scannedAt DESC")
    fun observeAll(): Flow<List<ScanRecord>>

    @Query("DELETE FROM scan_history")
    suspend fun clearAll()
}
