package com.smartqr.scanner.util

import com.smartqr.scanner.model.ScanType

object DataTypeParser {
    private val urlRegex = Regex("^(https?://|www\\.).+", RegexOption.IGNORE_CASE)
    private val phoneRegex = Regex("^\\+?[0-9]{7,15}$")
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

    fun detect(value: String): ScanType {
        val text = value.trim()
        return when {
            text.startsWith("upi://pay", ignoreCase = true) || text.contains("pa=", ignoreCase = true) -> ScanType.UPI
            urlRegex.matches(text) -> ScanType.URL
            phoneRegex.matches(text.replace(" ", "")) -> ScanType.PHONE
            emailRegex.matches(text) -> ScanType.EMAIL
            else -> ScanType.TEXT
        }
    }
}
