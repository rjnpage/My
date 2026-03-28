package com.example.smartqrscanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.smartqrscanner.data.AppDatabase
import com.example.smartqrscanner.data.ScanRepository
import com.example.smartqrscanner.databinding.ActivityMainBinding
import com.example.smartqrscanner.ui.history.HistoryActivity
import com.example.smartqrscanner.ui.scanner.BarcodeAnalyzer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var repository: ScanRepository

    private var camera: Camera? = null
    private var flashEnabled = false
    private var lastScannedValue: String? = null
    private var duplicateGuardJob: Job? = null

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera() else {
            Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()
        repository = ScanRepository(AppDatabase.getInstance(this).scanRecordDao())

        setupUi()
        checkCameraPermissionAndStart()
    }

    private fun setupUi() {
        binding.btnToggleFlash.setOnClickListener { toggleFlash() }
        binding.btnOpenHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        binding.topAppBar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.menuClearHistory) {
                lifecycleScope.launch {
                    repository.clearHistory()
                    Toast.makeText(this@MainActivity, "History cleared", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }
    }

    private fun checkCameraPermissionAndStart() {
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            startCamera()
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .apply {
                    setAnalyzer(cameraExecutor, BarcodeAnalyzer(::onBarcodeDetected))
                }

            val selector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(this, selector, preview, analysis)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun onBarcodeDetected(rawValue: String) {
        if (rawValue == lastScannedValue) return
        lastScannedValue = rawValue

        duplicateGuardJob?.cancel()
        duplicateGuardJob = lifecycleScope.launch {
            delay(1200)
            lastScannedValue = null
        }

        val type = detectType(rawValue)
        runOnUiThread {
            binding.tvResultType.text = "Type: $type"
            binding.tvResultData.text = rawValue
        }

        lifecycleScope.launch {
            repository.addRecord(rawValue, type)
        }

        when (type) {
            "URL" -> openUrl(rawValue)
            "PHONE" -> dialPhone(rawValue)
        }
    }

    private fun detectType(value: String): String {
        return when {
            android.util.Patterns.WEB_URL.matcher(value).matches() -> "URL"
            android.util.Patterns.PHONE.matcher(value).matches() -> "PHONE"
            else -> "TEXT"
        }
    }

    private fun openUrl(url: String) {
        val normalized = if (url.startsWith("http://") || url.startsWith("https://")) {
            url
        } else {
            "https://$url"
        }
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(normalized)))
    }

    private fun dialPhone(phone: String) {
        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
    }

    private fun toggleFlash() {
        flashEnabled = !flashEnabled
        camera?.cameraControl?.enableTorch(flashEnabled)
        binding.btnToggleFlash.text = if (flashEnabled) "Flash On" else "Flash Off"
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
