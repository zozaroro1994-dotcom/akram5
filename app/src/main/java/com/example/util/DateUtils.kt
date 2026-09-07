package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val arabicDateFormat = SimpleDateFormat("EEEE، d MMMM yyyy", Locale("ar"))
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale("ar"))

    val arabicMonths = listOf(
        "كانون الثاني (1)",
        "شباط (2)",
        "آذار (3)",
        "نيسان (4)",
        "أيار (5)",
        "حزيران (6)",
        "تموز (7)",
        "آب (8)",
        "أيلول (9)",
        "تشرين الأول (10)",
        "تشرين الثاني (11)",
        "كانون الأول (12)"
    )

    fun getCurrentDateIso(): String = isoFormat.format(Date())

    fun getCurrentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)

    fun getCurrentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1 // 1-indexed

    fun getMonthName(month: Int): String {
        return if (month in 1..12) arabicMonths[month - 1] else "شهر $month"
    }

    fun getFormattedCurrentDateArabic(): String = arabicDateFormat.format(Date())

    fun parseYearFromDate(dateStr: String): Int {
        return try {
            val parts = dateStr.split("-")
            if (parts.isNotEmpty()) parts[0].toInt() else getCurrentYear()
        } catch (e: Exception) {
            getCurrentYear()
        }
    }

    fun parseMonthFromDate(dateStr: String): Int {
        return try {
            val parts = dateStr.split("-")
            if (parts.size >= 2) parts[1].toInt() else getCurrentMonth()
        } catch (e: Exception) {
            getCurrentMonth()
        }
    }

    fun formatCurrencyIqd(amount: Double): String {
        return String.format(Locale.US, "%,.0f د.ع", amount)
    }
}
