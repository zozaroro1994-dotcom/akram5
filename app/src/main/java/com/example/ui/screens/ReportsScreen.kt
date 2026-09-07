package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FixedExpense
import com.example.data.model.Income
import com.example.data.model.VariableExpense
import com.example.ui.components.DialogUtils
import com.example.ui.components.MonthYearPickerDialog
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
fun ReportsScreen(
    viewModel: AccountantViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentSelectedMonth by viewModel.selectedMonth.collectAsState()
    val currentSelectedYear by viewModel.selectedYear.collectAsState()

    // Filter Mode: 0 = شهر محدد, 1 = سنة كاملة, 2 = نطاق تاريخ مخصص
    var reportMode by remember { mutableIntStateOf(0) }

    var reportMonth by remember { mutableIntStateOf(currentSelectedMonth) }
    var reportYear by remember { mutableIntStateOf(currentSelectedYear) }
    var showMonthPicker by remember { mutableStateOf(false) }

    var startDate by remember {
        val y = DateUtils.getCurrentYear()
        val m = DateUtils.getCurrentMonth()
        mutableStateOf(String.format("%04d-%02d-01", y, m))
    }
    var endDate by remember { mutableStateOf(DateUtils.getCurrentDateIso()) }

    var reportIncomes by remember { mutableStateOf<List<Income>>(emptyList()) }
    var reportFixed by remember { mutableStateOf<List<FixedExpense>>(emptyList()) }
    var reportVariable by remember { mutableStateOf<List<VariableExpense>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // Query data whenever filter changes
    LaunchedEffect(reportMode, reportMonth, reportYear, startDate, endDate) {
        isLoading = true
        when (reportMode) {
            0 -> {
                val data = viewModel.getReportDataForMonth(reportYear, reportMonth)
                reportIncomes = data.first
                reportFixed = data.second
                reportVariable = data.third
            }
            1 -> {
                val data = viewModel.getReportDataForYear(reportYear)
                reportIncomes = data.first
                reportFixed = data.second
                reportVariable = data.third
            }
            2 -> {
                val data = viewModel.getReportDataForRange(startDate, endDate)
                reportIncomes = data.first
                reportFixed = data.second
                reportVariable = data.third
            }
        }
        isLoading = false
    }

    val totalIncome = reportIncomes.sumOf { it.amount }
    val totalFixed = reportFixed.sumOf { it.amount }
    val totalVariable = reportVariable.sumOf { it.amount }
    val totalExpenses = totalFixed + totalVariable
    val netBalance = totalIncome - totalExpenses

    val savingsRate = if (totalIncome > 0) ((netBalance / totalIncome) * 100).coerceIn(-100.0, 100.0) else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "التقارير والمخططات البيانية",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = when (reportMode) {
                                0 -> "${DateUtils.getMonthName(reportMonth)} $reportYear"
                                1 -> "سنة كاملة: $reportYear"
                                else -> "من $startDate إلى $endDate"
                            },
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate50)
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Filter Mode Selector (3 options as requested)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "طريقة عرض التقرير والمخطط البياني:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate700
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = reportMode == 0,
                            onClick = { reportMode = 0 },
                            label = { Text("شهر واحد", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = reportMode == 1,
                            onClick = { reportMode = 1 },
                            label = { Text("سنة واحدة", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = reportMode == 2,
                            onClick = { reportMode = 2 },
                            label = { Text("نطاق تاريخ", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Options detail row
                    when (reportMode) {
                        0 -> {
                            OutlinedButton(
                                onClick = { showMonthPicker = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "الشهر المعروض: ${DateUtils.getMonthName(reportMonth)} $reportYear (اضغط لتغيير)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            }
                        }
                        1 -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Slate100)
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { reportYear-- }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "السنة السابقة")
                                }
                                Text("سنة: $reportYear", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
                                IconButton(onClick = { reportYear++ }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "السنة التالية")
                                }
                            }
                        }
                        2 -> {
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
                    }
                }
            }

            // Financial Summary Metrics
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "الخلاصة المالية للفترة المحددة",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("إجمالي الواردات:", fontSize = 12.sp, color = Color(0xFFA7F3D0))
                            Text(
                                text = DateUtils.formatCurrencyIqd(totalIncome),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6EE7B7)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("إجمالي المصروفات:", fontSize = 12.sp, color = Color(0xFFFECDD3))
                            Text(
                                text = DateUtils.formatCurrencyIqd(totalExpenses),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFDA4AF)
                            )
                        }
                    }

                    // Net Income Banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (netBalance >= 0) Color(0xFF064E3B) else Color(0xFF881337))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (netBalance >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = if (netBalance >= 0) Color(0xFF34D399) else Color(0xFFFB7185)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (netBalance >= 0) "صافي الواردات (فائض مدخر):" else "صافي الواردات (عجز مالي):",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = DateUtils.formatCurrencyIqd(netBalance),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (netBalance >= 0) Color(0xFF34D399) else Color(0xFFFB7185)
                        )
                    }
                }
            }

            // Interactive Bar Chart Comparison
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "مخطط مقارنة الواردات والصرفيات:",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val maxValue = maxOf(totalIncome, totalFixed, totalVariable, 1.0)

                    // Financial Comparison Bars
                    FinancialBarItem(
                        label = "الواردات",
                        amount = totalIncome,
                        ratio = (totalIncome / maxValue).toFloat(),
                        barColor = EmeraldGreen
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    FinancialBarItem(
                        label = "المصروفات الثابتة",
                        amount = totalFixed,
                        ratio = (totalFixed / maxValue).toFloat(),
                        barColor = CoralRed
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    FinancialBarItem(
                        label = "المصروفات المتغيرة",
                        amount = totalVariable,
                        ratio = (totalVariable / maxValue).toFloat(),
                        barColor = AmberOrange
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    FinancialBarItem(
                        label = "صافي المتبقي",
                        amount = netBalance,
                        ratio = (netBalance.coerceAtLeast(0.0) / maxValue).toFloat(),
                        barColor = RoyalBlue
                    )
                }
            }

            // Expense Distribution Breakdown
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "نسبة توزيع المصروفات:",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )

                    val fixedPercent = if (totalExpenses > 0) ((totalFixed / totalExpenses) * 100).toInt() else 0
                    val variablePercent = if (totalExpenses > 0) ((totalVariable / totalExpenses) * 100).toInt() else 0

                    // Distribution progress bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .clip(CircleShape)
                            .background(Slate200)
                    ) {
                        if (fixedPercent > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(fixedPercent.toFloat().coerceAtLeast(0.01f))
                                    .fillMaxSize()
                                    .background(CoralRed)
                            )
                        }
                        if (variablePercent > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(variablePercent.toFloat().coerceAtLeast(0.01f))
                                    .fillMaxSize()
                                    .background(AmberOrange)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(CoralRed))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("مصروفات ثابتة: $fixedPercent%", fontSize = 13.sp, color = Slate700)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(AmberOrange))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("صرفيات متغيرة: $variablePercent%", fontSize = 13.sp, color = Slate700)
                        }
                    }
                }
            }
        }
    }

    if (showMonthPicker) {
        MonthYearPickerDialog(
            currentMonth = reportMonth,
            currentYear = reportYear,
            onDismiss = { showMonthPicker = false },
            onConfirm = { m, y ->
                reportMonth = m
                reportYear = y
                showMonthPicker = false
            }
        )
    }
}

@Composable
private fun FinancialBarItem(
    label: String,
    amount: Double,
    ratio: Float,
    barColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
            Text(
                text = DateUtils.formatCurrencyIqd(amount),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = barColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Slate100)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio.coerceIn(0.02f, 1f))
                    .fillMaxSize()
                    .clip(RoundedCornerShape(6.dp))
                    .background(barColor)
            )
        }
    }
}
