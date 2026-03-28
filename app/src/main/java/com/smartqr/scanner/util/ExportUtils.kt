package com.smartqr.scanner.util

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.smartqr.scanner.model.ScanRecord
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtils {
    private val formatter = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())

    fun exportTxt(context: Context, record: ScanRecord): File {
        val file = File(context.cacheDir, "scan_${formatter.format(Date())}.txt")
        file.writeText("Type: ${record.type}\nData: ${record.rawData}\nTime: ${Date(record.timestamp)}")
        return file
    }

    fun exportPdf(context: Context, record: ScanRecord): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val paint = Paint().apply { textSize = 16f }

        val lines = listOf(
            "Smart QR & Data Scanner",
            "",
            "Type: ${record.type}",
            "Data: ${record.rawData}",
            "Time: ${Date(record.timestamp)}"
        )

        var y = 80f
        lines.forEach {
            page.canvas.drawText(it, 40f, y, paint)
            y += 28f
        }

        document.finishPage(page)
        val file = File(context.cacheDir, "scan_${formatter.format(Date())}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    fun fileUri(context: Context, file: File) =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
