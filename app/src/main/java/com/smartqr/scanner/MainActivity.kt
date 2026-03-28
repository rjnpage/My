package com.smartqr.scanner

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartqr.scanner.model.ScanRecord
import com.smartqr.scanner.model.ScanType
import com.smartqr.scanner.ui.SmartQrTheme
import com.smartqr.scanner.util.ExportUtils
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels { MainViewModel.factory(this) }

    private val scannerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data?.getStringExtra(ScannerActivity.EXTRA_SCAN_RESULT)
        if (!data.isNullOrBlank()) {
            vm.onScanResult(data)
            copyToClipboard(data)
        }
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) openScanner() else Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartQrTheme {
                SmartQrApp(
                    vm = vm,
                    onScan = { ensureCameraAndScan() },
                    onShareFile = { file -> shareFile(file) },
                    onOpenIntent = { intent -> startActivity(intent) },
                    onSaveQr = { content, bitmap -> saveQr(content, bitmap) }
                )
            }
        }
    }

    private fun ensureCameraAndScan() {
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (granted) openScanner() else permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun openScanner() {
        scannerLauncher.launch(Intent(this, ScannerActivity::class.java))
    }

    private fun copyToClipboard(value: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("scan_result", value))
        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun shareFile(file: File) {
        val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = if (file.name.endsWith(".pdf")) "application/pdf" else "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Share"))
    }

    private fun saveQr(content: String, bitmap: android.graphics.Bitmap) {
        val filename = "qr_${content.take(8).replace("[^a-zA-Z0-9]".toRegex(), "")}_${System.currentTimeMillis()}.png"
        val file = File(getExternalFilesDir(null), filename)
        FileOutputStream(file).use { out -> bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out) }
        Toast.makeText(this, "QR saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SmartQrApp(
    vm: MainViewModel,
    onScan: () -> Unit,
    onShareFile: (File) -> Unit,
    onOpenIntent: (Intent) -> Unit,
    onSaveQr: (String, android.graphics.Bitmap) -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var route by remember { mutableStateOf("home") }
    val context = LocalContext.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("Smart QR & Data Scanner") }) },
        snackbarHost = { SnackbarHost(hostState = remember { SnackbarHostState() }) }
    ) { padding ->
        AnimatedContent(
            targetState = route,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            label = "nav"
        ) { target ->
            when (target) {
                "home" -> HomeScreen(
                    onScan = onScan,
                    onGenerate = { route = "generator" },
                    onHistory = { route = "history" },
                    latest = state.latestRecord,
                    onAction = { record, action ->
                        when (action) {
                            ResultAction.OPEN -> vm.createActionIntent(record)?.let(onOpenIntent)
                            ResultAction.SHARE -> onShareFile(ExportUtils.exportTxt(context, record))
                            ResultAction.EXPORT_PDF -> onShareFile(ExportUtils.exportPdf(context, record))
                            ResultAction.EXPORT_TXT -> onShareFile(ExportUtils.exportTxt(context, record))
                        }
                    }
                )

                "history" -> HistoryScreen(
                    records = state.history,
                    query = state.search,
                    onQuery = vm::onSearch,
                    onDelete = vm::delete,
                    onBack = { route = "home" }
                )

                else -> GeneratorScreen(
                    onBack = { route = "home" },
                    onGenerate = vm::generateQr,
                    qrBitmap = state.generatedQr,
                    onSave = onSaveQr
                )
            }
        }
    }
}

enum class ResultAction { OPEN, SHARE, EXPORT_PDF, EXPORT_TXT }

@Composable
private fun HomeScreen(
    onScan: () -> Unit,
    onGenerate: () -> Unit,
    onHistory: () -> Unit,
    latest: ScanRecord?,
    onAction: (ScanRecord, ResultAction) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onScan, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.scan_qr)) }
            Button(onClick = onGenerate, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.generate_qr)) }
            Button(onClick = onHistory, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.history)) }
        }
        Spacer(Modifier.height(12.dp))
        Text("Latest Scan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        latest?.let { record -> ResultCard(record = record, onAction = onAction) } ?: Text("No scans yet")
    }
}

@Composable
private fun ResultCard(record: ScanRecord, onAction: (ScanRecord, ResultAction) -> Unit) {
    val formatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(record.type.name, style = MaterialTheme.typography.labelLarge)
            Text(record.rawData)
            Text(formatter.format(Date(record.timestamp)), style = MaterialTheme.typography.labelSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onAction(record, ResultAction.OPEN) }) { Text("Open") }
                Button(onClick = { onAction(record, ResultAction.EXPORT_PDF) }) { Text(stringResource(R.string.export_pdf)) }
                Button(onClick = { onAction(record, ResultAction.EXPORT_TXT) }) { Text(stringResource(R.string.export_txt)) }
            }
        }
    }
}

@Composable
private fun HistoryScreen(
    records: List<ScanRecord>,
    query: String,
    onQuery: (String) -> Unit,
    onDelete: (ScanRecord) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = onBack) { Text("Back") }
        OutlinedTextField(value = query, onValueChange = onQuery, modifier = Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.search)) })
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 60.dp)) {
            items(records, key = { it.id }) { record ->
                Card {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(record.type.name, fontWeight = FontWeight.Bold)
                        Text(record.rawData)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = stringResource(R.string.delete),
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.clickable { onDelete(record) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GeneratorScreen(
    onBack: () -> Unit,
    onGenerate: (String) -> Unit,
    qrBitmap: android.graphics.Bitmap?,
    onSave: (String, android.graphics.Bitmap) -> Unit
) {
    var text by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = onBack, modifier = Modifier.align(Alignment.Start)) { Text("Back") }
        OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Text / URL / Phone / UPI") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { onGenerate(text) }, enabled = text.isNotBlank()) { Text("Generate") }
        qrBitmap?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxWidth().height(280.dp))
            Button(onClick = { onSave(text, it) }) { Text("Download PNG") }
        }
    }
}
