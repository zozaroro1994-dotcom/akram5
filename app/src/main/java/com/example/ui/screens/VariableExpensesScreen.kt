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
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.example.data.model.VariableExpense
import com.example.ui.components.ColorPickerRow
import com.example.ui.components.DialogUtils
import com.example.ui.components.ViewNotesDialog
import com.example.ui.components.parseColor
import com.example.ui.theme.AmberOrange
import com.example.ui.theme.CoralRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalBlue
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
fun VariableExpensesScreen(
    viewModel: AccountantViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val incomes by viewModel.incomes.collectAsState()
    val fixedExpenses by viewModel.fixedExpenses.collectAsState()
    val variableExpenses by viewModel.variableExpenses.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<VariableExpense?>(null) }
    var notesToView by remember { mutableStateOf<Pair<String, String>?>(null) }
    var expenseToDelete by remember { mutableStateOf<VariableExpense?>(null) }

    val totalIncome = incomes.sumOf { it.amount }
    val totalFixed = fixedExpenses.sumOf { it.amount }
    val totalVariable = variableExpenses.sumOf { it.amount }

    // 1. الواردات المتبقية بعد طرح المصروفات الثابتة
    val remainingAfterFixed = totalIncome - totalFixed
    // 3. صافي الواردات بعد طرح جميع الصرفيات
    val finalNetIncome = remainingAfterFixed - totalVariable

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "المصروفات المتغيرة",
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
                containerColor = AmberOrange,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_variable_expense_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة مصروف متغير")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate50)
                .padding(paddingValues)
        ) {
            // Three Summary Windows at the top as specifically requested:
            // 1. مجموع قيمة الواردات المتبقية بعد طرح المصروفات الثابتة منها
            // 2. مجموع مبالغ الصرفيات المتغيرة
            // 3. صافي الواردات بعد طرح جميع الصرفيات منها
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Window 1: المتبقي بعد طرح المصروفات الثابتة
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (remainingAfterFixed >= 0) Color(0xFFEFF6FF) else Color(0xFFFFF1F2))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الواردات المتبقية بعد الثابتة:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (remainingAfterFixed >= 0) Color(0xFF1E40AF) else Color(0xFF9F1239)
                        )
                        Text(
                            text = DateUtils.formatCurrencyIqd(remainingAfterFixed),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (remainingAfterFixed >= 0) RoyalBlue else CoralRed
                        )
                    }

                    // Window 2: مجموع مبالغ الصرفيات المتغيرة
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFFBEB))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "مجموع الصرفيات المتغيرة:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                        Text(
                            text = DateUtils.formatCurrencyIqd(totalVariable),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFB45309)
                        )
                    }

                    // Window 3: صافي الواردات النهائي بعد طرح جميع الصرفيات
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (finalNetIncome >= 0) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "صافي الواردات النهائي (المتبقي):",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (finalNetIncome >= 0) Color(0xFF065F46) else Color(0xFF991B1B)
                        )
                        Text(
                            text = DateUtils.formatCurrencyIqd(finalNetIncome),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (finalNetIncome >= 0) EmeraldGreen else CoralRed
                        )
                    }
                }
            }

            // List of Variable Expenses
            if (variableExpenses.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = Slate200,
                        modifier = Modifier.size(70.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "لا توجد مصروفات متغيرة مسجلة لهذا الشهر",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate700
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "سجل المصروفات اليومية مثل المسواك المنزلي، الوقود والمواصلات، العلاج، الترفيه، التسوق...",
                        fontSize = 13.sp,
                        color = Slate500,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(variableExpenses, key = { it.id }) { item ->
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

                                    IconButton(
                                        onClick = { editingExpense = item },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "تعديل",
                                            tint = Slate700,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { expenseToDelete = item },
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
                    item {
                        Spacer(modifier = Modifier.height(70.dp))
                    }
                }
            }
        }
    }

    // Add or Edit Dialog
    if (showAddDialog || editingExpense != null) {
        AddEditVariableExpenseDialog(
            expense = editingExpense,
            onDismiss = {
                showAddDialog = false
                editingExpense = null
            },
            onSave = { title, amount, date, notes, colorHex ->
                if (editingExpense != null) {
                    viewModel.updateVariableExpense(
                        editingExpense!!.copy(
                            title = title,
                            amount = amount,
                            date = date,
                            notes = notes,
                            colorHex = colorHex
                        )
                    )
                    Toast.makeText(context, "تم تعديل المصروف المتغير", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.addVariableExpense(title, amount, date, notes, colorHex)
                    Toast.makeText(context, "تمت إضافة المصروف المتغير", Toast.LENGTH_SHORT).show()
                }
                showAddDialog = false
                editingExpense = null
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
    expenseToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = { Text("تأكيد الحذف", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من حذف المصروف المتغير '${item.title}' بقيمة ${DateUtils.formatCurrencyIqd(item.amount)}؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteVariableExpense(item)
                        expenseToDelete = null
                        Toast.makeText(context, "تم الحذف", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed)
                ) {
                    Text("حذف", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun AddEditVariableExpenseDialog(
    expense: VariableExpense?,
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, date: String, notes: String, colorHex: String) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(expense?.title ?: "") }
    var amountStr by remember { mutableStateOf(expense?.amount?.let { String.format("%.0f", it) } ?: "") }
    var date by remember { mutableStateOf(expense?.date ?: DateUtils.getCurrentDateIso()) }
    var notes by remember { mutableStateOf(expense?.notes ?: "") }
    var selectedColorHex by remember { mutableStateOf(expense?.colorHex ?: "#F59E0B") }

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
                    text = if (expense != null) "تعديل المصروف المتغير" else "إنشاء مصروف متغير جديد",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("اسم المصروف المتغير *") },
                    placeholder = { Text("مثال: تسوق مواد غذائية، بنزين، علاج...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("قيمة المبلغ (بالدينار العراقي) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("التاريخ (سنة-شهر-يوم)") },
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

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("الملاحظات (اختياري)") },
                    placeholder = { Text("أي تفاصيل إضافية...") },
                    maxLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

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
                                Toast.makeText(context, "يرجى كتابة اسم المصروف", Toast.LENGTH_SHORT).show()
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
                        Text(if (expense != null) "حفظ التعديلات" else "إضافة المصروف المتغير")
                    }
                }
            }
        }
    }
}
