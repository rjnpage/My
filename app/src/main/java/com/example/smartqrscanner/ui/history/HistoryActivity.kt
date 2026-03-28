package com.example.smartqrscanner.ui.history

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartqrscanner.R
import com.example.smartqrscanner.data.AppDatabase
import com.example.smartqrscanner.data.ScanRepository
import com.example.smartqrscanner.databinding.ActivityHistoryBinding
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var repository: ScanRepository
    private val adapter = HistoryAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = ScanRepository(AppDatabase.getInstance(this).scanRecordDao())

        binding.toolbarHistory.setNavigationOnClickListener { finish() }
        binding.toolbarHistory.title = "Scan History"
        binding.toolbarHistory.setNavigationIcon(R.drawable.ic_arrow_back)

        binding.recyclerHistory.layoutManager = LinearLayoutManager(this)
        binding.recyclerHistory.adapter = adapter

        lifecycleScope.launch {
            repository.observeHistory().collect { list ->
                adapter.submitList(list)
            }
        }
    }
}
