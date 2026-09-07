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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Debt
import com.example.ui.components.ColorPickerRow
import com.example.ui.components.DialogUtils
import com.example.ui.components.ViewNotesDialog
import com.example.ui.components.parseColor
import com.example.ui.theme.CoralRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.VioletPurple
import com.example.ui.viewmodel.AccountantViewModel
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtsScreen(
    viewModel: AccountantViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val debts by viewModel.debts.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingDebt by remember { mutableStateOf<Debt?>(null) }
    var notesToView by remember { mutableStateOf<Pair<String, String>?>(null) }
    var debtToDelete by remember { mutableStateOf<Debt?>(null) }
    var filterTab by remember { mutableIntStateOf(0) } // 0: All, 1: Unpaid, 2: Paid

    val filteredDebts = when (filterTab) {
        1 -> debts.filter { !it.isPaid }
        2 -> debts.filter { it.isPaid }
        else -> debts
    }

    val totalOwedToMeUnpaid = debts.filter { it.isOwedToMe && !it.isPaid }.sumOf { it.amount }
    val totalIOweUnpaid = debts.filter { !it.isOwedToMe && !it.isPaid }.sumOf { it.amount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "سجل الديون والالتزامات",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "قائمة مستقلة منفصلة عن الحسابات الشهرية",
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
                containerColor = VioletPurple,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_debt_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة دين")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate50)
                .padding(paddingValues)
        ) {
            // Top Summary Cards (ديون لنا / ديون علينا)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ديون لنا (لنا في ذمة الآخرين)
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("ديون لنا (مطلوب):", fontSize = 12.sp, color = Color(0xFF047857), fontWeight = FontWeight.Bold)
                        Text(
                            text = DateUtils.formatCurrencyIqd(totalOwedToMeUnpaid),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF065F46)
                        )
                    }
                }

                // ديون علينا (علينا للآخرين)
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("ديون علينا (مستحق):", fontSize = 12.sp, color = Color(0xFFB91C1C), fontWeight = FontWeight.Bold)
                        Text(
                            text = DateUtils.formatCurrencyIqd(totalIOweUnpaid),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF991B1B)
                        )
                    }
                }
            }

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterTab == 0,
                    onClick = { filterTab = 0 },
                    label = { Text("الكل (${debts.size})") }
                )
                FilterChip(
                    selected = filterTab == 1,
                    onClick = { filterTab = 1 },
                    label = { Text("غير مسدد (${debts.count { !it.isPaid }})") }
                )
                FilterChip(
                    selected = filterTab == 2,
                    onClick = { filterTab = 2 },
                    label = { Text("تم السداد (${debts.count { it.isPaid }})") }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredDebts.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = Slate200,
                        modifier = Modifier.size(70.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "لا توجد ديون مسجلة في هذا القسم",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate700
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "اضغط على زر (+) لإضافة دين جديد وتحديد ما إذا كان (لنا) أو (علينا) وتتبع حالة السداد.",
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
                    items(filteredDebts, key = { it.id }) { item ->
                        val itemColor = parseColor(item.colorHex)
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (item.isPaid) 1.dp else 1.5.dp,
                                    color = if (item.isPaid) Slate200 else itemColor.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (item.isOwedToMe) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (item.isOwedToMe) "دين لنا" else "دين علينا",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (item.isOwedToMe) Color(0xFF047857) else Color(0xFFB91C1C)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = item.personName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Slate900
                                        )
                                    }

                                    // Amount
                                    Text(
                                        text = DateUtils.formatCurrencyIqd(item.amount),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = if (item.isOwedToMe) Color(0xFF047857) else Color(0xFFB91C1C)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = Slate500,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = item.date, fontSize = 12.sp, color = Slate500)
                                    }

                                    // Status Badge Button
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(if (item.isPaid) Color(0xFFD1FAE5) else Color(0xFFFEF3C7))
                                            .clickable { viewModel.toggleDebtPaidStatus(item) }
                                            .padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (item.isPaid) Icons.Default.CheckCircle else Icons.Default.Pending,
                                            contentDescription = null,
                                            tint = if (item.isPaid) Color(0xFF059669) else Color(0xFFD97706),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (item.isPaid) "تم السداد" else "غير مسدد (اضغط لتغيير)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.isPaid) Color(0xFF047857) else Color(0xFF92400E)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    if (item.notes.isNotBlank()) {
                                        TextButton(onClick = { notesToView = Pair(item.personName, item.notes) }) {
                                            Icon(Icons.Default.Notes, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("الملاحظات", fontSize = 12.sp)
                                        }
                                    }

                                    IconButton(
                                        onClick = { editingDebt = item },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = Slate700, modifier = Modifier.size(18.dp))
                                    }

                                    IconButton(
                                        onClick = { debtToDelete = item },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = CoralRed, modifier = Modifier.size(18.dp))
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
    if (showAddDialog || editingDebt != null) {
        AddEditDebtDialog(
            debt = editingDebt,
            onDismiss = {
                showAddDialog = false
                editingDebt = null
            },
            onSave = { name, amount, isOwedToMe, date, notes, colorHex ->
                if (editingDebt != null) {
                    viewModel.updateDebt(
                        editingDebt!!.copy(
                            personName = name,
                            amount = amount,
                            isOwedToMe = isOwedToMe,
                            date = date,
                            notes = notes,
                            colorHex = colorHex
                        )
                    )
                    Toast.makeText(context, "تم تعديل بيانات الدين", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.addDebt(name, amount, isOwedToMe, date, notes, colorHex)
                    Toast.makeText(context, "تمت إضافة الدين بنجاح", Toast.LENGTH_SHORT).show()
                }
                showAddDialog = false
                editingDebt = null
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
    debtToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { debtToDelete = null },
            title = { Text("تأكيد حذف الدين", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من حذف هذا السجل الخاص بـ '${item.personName}' بقيمة ${DateUtils.formatCurrencyIqd(item.amount)}؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDebt(item)
                        debtToDelete = null
                        Toast.makeText(context, "تم الحذف", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed)
                ) {
                    Text("حذف", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { debtToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun AddEditDebtDialog(
    debt: Debt?,
    onDismiss: () -> Unit,
    onSave: (name: String, amount: Double, isOwedToMe: Boolean, date: String, notes: String, colorHex: String) -> Unit
) {
    val context = LocalContext.current
    var personName by remember { mutableStateOf(debt?.personName ?: "") }
    var amountStr by remember { mutableStateOf(debt?.amount?.let { String.format("%.0f", it) } ?: "") }
    var isOwedToMe by remember { mutableStateOf(debt?.isOwedToMe ?: true) }
    var date by remember { mutableStateOf(debt?.date ?: DateUtils.getCurrentDateIso()) }
    var notes by remember { mutableStateOf(debt?.notes ?: "") }
    var selectedColorHex by remember { mutableStateOf(debt?.colorHex ?: "#8B5CF6") }

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
                    text = if (debt != null) "تعديل الدين" else "تسجيل دين جديد",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )

                // Type selector (لنا / علينا)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { isOwedToMe = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isOwedToMe) Color(0xFFECFDF5) else Color.Transparent
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (isOwedToMe) EmeraldGreen else Slate200
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "دين لنا (في ذمتهم)",
                            fontWeight = if (isOwedToMe) FontWeight.Bold else FontWeight.Normal,
                            color = if (isOwedToMe) Color(0xFF047857) else Slate700
                        )
                    }

                    OutlinedButton(
                        onClick = { isOwedToMe = false },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (!isOwedToMe) Color(0xFFFEF2F2) else Color.Transparent
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (!isOwedToMe) CoralRed else Slate200
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "دين علينا (في ذمتنا)",
                            fontWeight = if (!isOwedToMe) FontWeight.Bold else FontWeight.Normal,
                            color = if (!isOwedToMe) Color(0xFFB91C1C) else Slate700
                        )
                    }
                }

                OutlinedTextField(
                    value = personName,
                    onValueChange = { personName = it },
                    label = { Text("اسم الشخص أو الجهة *") },
                    placeholder = { Text("مثال: أحمد، محل البقالة...") },
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
                    placeholder = { Text("موعد السداد، شروط...") },
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
                            if (personName.isBlank()) {
                                Toast.makeText(context, "يرجى كتابة اسم الشخص أو الجهة", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (amt == null || amt <= 0) {
                                Toast.makeText(context, "يرجى إدخال مبلغ صحيح بالدينار", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            onSave(personName, amt, isOwedToMe, date, notes, selectedColorHex)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (debt != null) "حفظ التعديلات" else "إضافة الدين")
                    }
                }
            }
        }
    }
}
