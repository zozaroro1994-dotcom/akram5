package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Income
import com.example.ui.components.ColorPickerRow
import com.example.ui.components.DialogUtils
import com.example.ui.components.ViewNotesDialog
import com.example.ui.components.parseColor
import com.example.ui.theme.CoralRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenBg
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.AccountantViewModel
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomesScreen(
    viewModel: AccountantViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val incomes by viewModel.incomes.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingIncome by remember { mutableStateOf<Income?>(null) }
    var notesToView by remember { mutableStateOf<Pair<String, String>?>(null) }
    var incomeToDelete by remember { mutableStateOf<Income?>(null) }

    val totalIncome = incomes.sumOf { it.amount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "سجل الواردات المالية",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "${DateUtils.getMonthName(selectedMonth)} $selectedYear",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate900,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = EmeraldGreen,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_income_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة واردة")
            }
        },
        bottomBar = {
            // Window at the bottom displaying total income as requested
            Surface(
                color = Slate900,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مجموع الواردات:",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = DateUtils.formatCurrencyIqd(totalIncome),
                        color = Color(0xFF6EE7B7),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate50)
                .padding(paddingValues)
        ) {
            if (incomes.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = Slate200,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "لا توجد واردات مسجلة لهذا الشهر",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate700
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "اضغط على زر (+) في الأسفل لإضافة واردة مالية جديدة (راتب، أرباح، إيرادات...)",
                        fontSize = 13.sp,
                        color = Slate500,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(incomes, key = { it.id }) { item ->
                        val itemColor = parseColor(item.colorHex)
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, itemColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Custom color bar indicator
                                    Box(
                                        modifier = Modifier
                                            .width(5.dp)
                                            .height(44.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(itemColor)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = item.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Slate900
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CalendarToday,
                                                contentDescription = null,
                                                tint = Slate500,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = item.date,
                                                fontSize = 12.sp,
                                                color = Slate500
                                            )
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    // Colored Amount Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(itemColor.copy(alpha = 0.12f))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = DateUtils.formatCurrencyIqd(item.amount),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = itemColor
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Notes button
                                    IconButton(
                                        onClick = { notesToView = Pair(item.title, item.notes) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Notes,
                                            contentDescription = "ملاحظات",
                                            tint = if (item.notes.isNotBlank()) Slate900 else Slate200,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    // Edit button
                                    IconButton(
                                        onClick = { editingIncome = item },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "تعديل",
                                            tint = Slate700,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Delete button
                                    IconButton(
                                        onClick = { incomeToDelete = item },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "حذف",
                                            tint = CoralRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add or Edit Income Dialog
    if (showAddDialog || editingIncome != null) {
        AddEditIncomeDialog(
            income = editingIncome,
            defaultYear = selectedYear,
            defaultMonth = selectedMonth,
            onDismiss = {
                showAddDialog = false
                editingIncome = null
            },
            onSave = { title, amount, date, notes, colorHex ->
                if (editingIncome != null) {
                    viewModel.updateIncome(
                        editingIncome!!.copy(
                            title = title,
                            amount = amount,
                            date = date,
                            notes = notes,
                            colorHex = colorHex
                        )
                    )
                    Toast.makeText(context, "تم تعديل الواردة بنجاح", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.addIncome(title, amount, date, notes, colorHex)
                    Toast.makeText(context, "تمت إضافة الواردة بنجاح", Toast.LENGTH_SHORT).show()
                }
                showAddDialog = false
                editingIncome = null
            }
        )
    }

    // View Notes Dialog
    notesToView?.let { (title, notes) ->
        ViewNotesDialog(
            title = title,
            notes = notes,
            onDismiss = { notesToView = null }
        )
    }

    // Delete Confirmation Dialog
    incomeToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { incomeToDelete = null },
            title = { Text("تأكيد الحذف", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من حذف الواردة '${item.title}' بقيمة ${DateUtils.formatCurrencyIqd(item.amount)}؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteIncome(item)
                        incomeToDelete = null
                        Toast.makeText(context, "تم الحذف", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed)
                ) {
                    Text("حذف", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { incomeToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun AddEditIncomeDialog(
    income: Income?,
    defaultYear: Int,
    defaultMonth: Int,
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, date: String, notes: String, colorHex: String) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(income?.title ?: "") }
    var amountStr by remember { mutableStateOf(income?.amount?.let { String.format("%.0f", it) } ?: "") }
    var date by remember {
        mutableStateOf(
            income?.date ?: DateUtils.getCurrentDateIso()
        )
    }
    var notes by remember { mutableStateOf(income?.notes ?: "") }
    var selectedColorHex by remember { mutableStateOf(income?.colorHex ?: "#10B981") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (income != null) "تعديل الواردة" else "إنشاء واردة جديدة",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("اسم / مصدر الواردة *") },
                    placeholder = { Text("مثال: الراتب الشهري، أرباح تجارة...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Amount Input
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("قيمة المبلغ (بالدينار العراقي) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Date Selection (manual or automatic today)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("تاريخ الواردة (سنة-شهر-يوم)") },
                        trailingIcon = {
                            IconButton(onClick = {
                                DialogUtils.showSystemDatePicker(context, date) { newDate ->
                                    date = newDate
                                }
                            }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "اختيار التاريخ")
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(onClick = { date = DateUtils.getCurrentDateIso() }) {
                        Text("اليوم", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Notes Input
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("الملاحظات (اختياري)") },
                    placeholder = { Text("أي تفاصيل إضافية...") },
                    maxLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Color Picker Row
                ColorPickerRow(
                    selectedColorHex = selectedColorHex,
                    onColorSelected = { selectedColorHex = it }
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إلغاء", color = Slate500)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountStr.toDoubleOrNull()
                            if (title.isBlank()) {
                                Toast.makeText(context, "يرجى كتابة اسم الواردة", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (amt == null || amt <= 0) {
                                Toast.makeText(context, "يرجى إدخال مبلغ صحيح بالدينار", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            onSave(title, amt, date, notes, selectedColorHex)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (income != null) "حفظ التعديلات" else "إضافة الواردة")
                    }
                }
            }
        }
    }
}
