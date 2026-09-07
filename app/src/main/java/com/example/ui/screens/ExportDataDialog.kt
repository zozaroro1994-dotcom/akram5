package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.DialogUtils
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.AccountantViewModel
import com.example.util.DateUtils
import java.io.File

@Composable
fun ExportDataDialog(
    viewModel: AccountantViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentMonth by viewModel.selectedMonth.collectAsState()
    val currentYear by viewModel.selectedYear.collectAsState()

    var exportMode by remember { mutableIntStateOf(0) } // 0: Month, 1: Year, 2: Range
    var startDate by remember {
        val y = DateUtils.getCurrentYear()
        val m = DateUtils.getCurrentMonth()
        mutableStateOf(String.format("%04d-%02d-01", y, m))
    }
    var endDate by remember { mutableStateOf(DateUtils.getCurrentDateIso()) }

    var isExporting by remember { mutableStateOf(false) }
    var exportedFile by remember { mutableStateOf<File?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFECFDF5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "تصدير إلى ملف Excel",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Text(
                    text = "سيتم تصدير الواردات والمصروفات والديون والمدخرات إلى جدول Excel متكامل بصيغة CSV في مجلد التنزيلات (Downloads) مع دعم اللغة العربية:",
                    fontSize = 13.sp,
                    color = Slate700,
                    lineHeight = 18.sp
                )

                // Select Export Scope
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = exportMode == 0,
                        onClick = { exportMode = 0 },
                        label = { Text("شهر $currentMonth", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = exportMode == 1,
                        onClick = { exportMode = 1 },
                        label = { Text("سنة $currentYear", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = exportMode == 2,
                        onClick = { exportMode = 2 },
                        label = { Text("نطاق زمني", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (exportMode == 2) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = startDate,
                                onValueChange = { startDate = it },
                                label = { Text("من تاريخ") },
                                trailingIcon = {
                                    IconButton(onClick = {
                                        DialogUtils.showSystemDatePicker(context, startDate) { startDate = it }
                                    }) {
                                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = endDate,
                                onValueChange = { endDate = it },
                                label = { Text("إلى تاريخ") },
                                trailingIcon = {
                                    IconButton(onClick = {
                                        DialogUtils.showSystemDatePicker(context, endDate) { endDate = it }
                                    }) {
                                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (exportedFile != null) {
                    // Success View
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "تم إنشاء وحفظ الملف بنجاح في مجلد التنزيلات!",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                            Text(
                                text = exportedFile!!.name,
                                fontSize = 12.sp,
                                color = Color(0xFF047857)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = {
                                    viewModel.shareExportedFile(context, exportedFile!!)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("مشاركة أو فتح الملف الآن")
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            isExporting = true
                            when (exportMode) {
                                0 -> {
                                    viewModel.exportMonthToCsv(context, currentYear, currentMonth) { file ->
                                        isExporting = false
                                        exportedFile = file
                                        if (file != null) {
                                            Toast.makeText(context, "تم حفظ الملف في التنزيلات بنجاح", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "حدث خطأ أثناء حفظ الملف", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                                1 -> {
                                    viewModel.exportYearToCsv(context, currentYear) { file ->
                                        isExporting = false
                                        exportedFile = file
                                        if (file != null) {
                                            Toast.makeText(context, "تم حفظ الملف في التنزيلات بنجاح", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "حدث خطأ أثناء حفظ الملف", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                                2 -> {
                                    viewModel.exportRangeToCsv(context, startDate, endDate) { file ->
                                        isExporting = false
                                        exportedFile = file
                                        if (file != null) {
                                            Toast.makeText(context, "تم حفظ الملف في التنزيلات بنجاح", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "حدث خطأ أثناء حفظ الملف", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !isExporting,
                        colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("جارٍ إنشاء الملف...")
                        } else {
                            Icon(Icons.Default.FileDownload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تصدير وحفظ الملف في التنزيلات", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
