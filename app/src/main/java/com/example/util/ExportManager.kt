package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.Debt
import com.example.data.model.FixedExpense
import com.example.data.model.Income
import com.example.data.model.Saving
import com.example.data.model.VariableExpense
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportManager {

    fun exportToExcelCsv(
        context: Context,
        username: String,
        periodTitle: String,
        incomes: List<Income>,
        fixedExpenses: List<FixedExpense>,
        variableExpenses: List<VariableExpense>,
        debts: List<Debt>,
        savings: List<Saving>
    ): File? {
        return try {
            val totalIncome = incomes.sumOf { it.amount }
            val totalFixed = fixedExpenses.sumOf { it.amount }
            val totalVariable = variableExpenses.sumOf { it.amount }
            val totalExpenses = totalFixed + totalVariable
            val netIncome = totalIncome - totalExpenses
            val totalSavings = savings.sumOf { it.amount }
            val totalDebtsOwedToMe = debts.filter { it.isOwedToMe && !it.isPaid }.sumOf { it.amount }
            val totalDebtsIOwe = debts.filter { !it.isOwedToMe && !it.isPaid }.sumOf { it.amount }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "المحاسب_المنزلي_${periodTitle.replace(" ", "_")}_$timestamp.csv"

            // Save in Downloads folder if accessible, or in app's external files directory
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val exportDir = if (downloadsDir != null && downloadsDir.exists()) {
                downloadsDir
            } else {
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
            }

            val file = File(exportDir, fileName)
            val fileOutputStream = FileOutputStream(file)
            val writer = OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)

            // UTF-8 BOM for Microsoft Excel to recognize Arabic text properly
            writer.write("\uFEFF")

            val sb = StringBuilder()

            // Header
            sb.append("تقرير المحاسب المنزلي\n")
            sb.append("اسم المستخدم,$username\n")
            sb.append("الفترة الزمنية,$periodTitle\n")
            sb.append("تاريخ التصدير,${DateUtils.getFormattedCurrentDateArabic()}\n\n")

            // Summary Section
            sb.append("=== الملخص المالي العام ===\n")
            sb.append("البيان,المبلغ بالدينار العراقي\n")
            sb.append("إجمالي الواردات,${String.format(Locale.US, "%.0f", totalIncome)}\n")
            sb.append("إجمالي المصروفات الثابتة,${String.format(Locale.US, "%.0f", totalFixed)}\n")
            sb.append("إجمالي المصروفات المتغيرة,${String.format(Locale.US, "%.0f", totalVariable)}\n")
            sb.append("إجمالي المصروفات الكلية,${String.format(Locale.US, "%.0f", totalExpenses)}\n")
            sb.append("صافي الواردات (المتبقي),${String.format(Locale.US, "%.0f", netIncome)}\n")
            sb.append("إجمالي المدخرات,${String.format(Locale.US, "%.0f", totalSavings)}\n")
            sb.append("ديون لنا (في ذمتهم),${String.format(Locale.US, "%.0f", totalDebtsOwedToMe)}\n")
            sb.append("ديون علينا (في ذمتنا),${String.format(Locale.US, "%.0f", totalDebtsIOwe)}\n\n")

            // Incomes Section
            sb.append("=== جدول الواردات ===\n")
            sb.append("م,اسم الواردة,المبلغ (د.ع),التاريخ,الملاحظات\n")
            incomes.forEachIndexed { index, item ->
                sb.append("${index + 1},\"${escapeCsv(item.title)}\",${item.amount},${item.date},\"${escapeCsv(item.notes)}\"\n")
            }
            sb.append("المجموع,,\"${String.format(Locale.US, "%.0f", totalIncome)}\",,\n\n")

            // Fixed Expenses Section
            sb.append("=== جدول المصروفات الثابتة ===\n")
            sb.append("م,اسم المصروف الثابت,المبلغ (د.ع),التاريخ,الملاحظات\n")
            fixedExpenses.forEachIndexed { index, item ->
                sb.append("${index + 1},\"${escapeCsv(item.title)}\",${item.amount},${item.date},\"${escapeCsv(item.notes)}\"\n")
            }
            sb.append("المجموع,,\"${String.format(Locale.US, "%.0f", totalFixed)}\",,\n\n")

            // Variable Expenses Section
            sb.append("=== جدول المصروفات المتغيرة ===\n")
            sb.append("م,اسم المصروف المتغير,المبلغ (د.ع),التاريخ,الملاحظات\n")
            variableExpenses.forEachIndexed { index, item ->
                sb.append("${index + 1},\"${escapeCsv(item.title)}\",${item.amount},${item.date},\"${escapeCsv(item.notes)}\"\n")
            }
            sb.append("المجموع,,\"${String.format(Locale.US, "%.0f", totalVariable)}\",,\n\n")

            // Debts Section
            sb.append("=== جدول الديون ===\n")
            sb.append("م,الطرف / الشخص,النوع,المبلغ (د.ع),التاريخ,حالة السداد,الملاحظات\n")
            debts.forEachIndexed { index, item ->
                val type = if (item.isOwedToMe) "ديون لنا" else "ديون علينا"
                val status = if (item.isPaid) "تم السداد" else "غير مسدد"
                sb.append("${index + 1},\"${escapeCsv(item.personName)}\",$type,${item.amount},${item.date},$status,\"${escapeCsv(item.notes)}\"\n")
            }
            sb.append("\n")

            // Savings Section
            sb.append("=== جدول المدخرات ===\n")
            sb.append("م,اسم المدخرة / الهدف,المبلغ (د.ع),التاريخ,الملاحظات\n")
            savings.forEachIndexed { index, item ->
                sb.append("${index + 1},\"${escapeCsv(item.title)}\",${item.amount},${item.date},\"${escapeCsv(item.notes)}\"\n")
            }
            sb.append("المجموع,,\"${String.format(Locale.US, "%.0f", totalSavings)}\",,\n")

            writer.write(sb.toString())
            writer.flush()
            writer.close()
            fileOutputStream.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"").replace("\n", " ")
    }

    fun shareExportedFile(context: Context, file: File) {
        try {
            val uri: Uri = try {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } catch (e: Exception) {
                Uri.fromFile(file)
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "تصدير بيانات المحاسب المنزلي")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "مشاركة تقرير إكسل"))
        } catch (e: Exception) {
            Toast.makeText(context, "تم حفظ الملف في التحميلات: ${file.name}", Toast.LENGTH_LONG).show()
        }
    }
}
