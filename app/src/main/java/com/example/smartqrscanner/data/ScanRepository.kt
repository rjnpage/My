package com.example.smartqrscanner.data

import kotlinx.coroutines.flow.Flow

class ScanRepository(private val dao: ScanRecordDao) {
    fun observeHistory(): Flow<List<ScanRecord>> = dao.observeAll()

    suspend fun addRecord(content: String, type: String) {
        dao.insert(ScanRecord(content = content, type = type))
    }

    suspend fun clearHistory() {
        dao.clearAll()
    }
}
