package com.smartqr.scanner.data

import com.smartqr.scanner.model.ScanRecord
import kotlinx.coroutines.flow.Flow

class ScanRepository(
    private val dao: ScanRecordDao
) {
    fun observeAll(): Flow<List<ScanRecord>> = dao.observeAll()

    fun search(query: String): Flow<List<ScanRecord>> = dao.search(query)

    suspend fun save(record: ScanRecord) = dao.insert(record)

    suspend fun delete(record: ScanRecord) = dao.delete(record)
}
