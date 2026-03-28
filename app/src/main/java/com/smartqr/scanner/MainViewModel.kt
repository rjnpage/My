package com.smartqr.scanner

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.smartqr.scanner.data.AppDatabase
import com.smartqr.scanner.data.ScanRepository
import com.smartqr.scanner.model.ScanRecord
import com.smartqr.scanner.model.ScanType
import com.smartqr.scanner.util.DataTypeParser
import com.smartqr.scanner.util.QrGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(private val repository: ScanRepository) : ViewModel() {
    private val searchQuery = MutableStateFlow("")
    private val latestRecord = MutableStateFlow<ScanRecord?>(null)
    private val generatedQr = MutableStateFlow<android.graphics.Bitmap?>(null)

    val state: StateFlow<MainUiState> = combine(
        searchQuery,
        searchQuery.flatMapLatest { if (it.isBlank()) repository.observeAll() else repository.search(it) },
        latestRecord,
        generatedQr
    ) { query, history, latest, qr ->
        MainUiState(
            search = query,
            history = history,
            latestRecord = latest ?: history.firstOrNull(),
            generatedQr = qr
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState())

    fun onScanResult(raw: String) {
        val record = ScanRecord(rawData = raw, type = DataTypeParser.detect(raw))
        viewModelScope.launch {
            repository.save(record)
            latestRecord.value = record
        }
    }

    fun onSearch(value: String) = searchQuery.update { value }

    fun delete(record: ScanRecord) {
        viewModelScope.launch { repository.delete(record) }
    }

    fun generateQr(text: String) {
        generatedQr.value = if (text.isBlank()) null else QrGenerator.generate(text)
    }

    fun createActionIntent(record: ScanRecord): Intent? {
        return when (record.type) {
            ScanType.URL -> Intent(Intent.ACTION_VIEW, Uri.parse(if (record.rawData.startsWith("http")) record.rawData else "https://${record.rawData}"))
            ScanType.PHONE -> Intent(Intent.ACTION_DIAL, Uri.parse("tel:${record.rawData}"))
            ScanType.EMAIL -> Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${record.rawData}"))
            ScanType.UPI -> Intent(Intent.ACTION_VIEW, Uri.parse(record.rawData))
            ScanType.TEXT -> Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, record.rawData)
            }
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "smart_qr.db").build()
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(ScanRepository(db.scanRecordDao())) as T
            }
        }
    }
}

data class MainUiState(
    val search: String = "",
    val history: List<ScanRecord> = emptyList(),
    val latestRecord: ScanRecord? = null,
    val generatedQr: android.graphics.Bitmap? = null
)
