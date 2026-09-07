package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MonthYearPickerDialog
import com.example.ui.theme.AmberOrange
import com.example.ui.theme.AmberOrangeBg
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CoralRedBg
import com.example.ui.theme.CyanTeal
import com.example.ui.theme.CyanTealBg
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenBg
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueBg
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.VioletPurple
import com.example.ui.theme.VioletPurpleBg
import com.example.ui.viewmodel.AccountantViewModel
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AccountantViewModel,
    onNavigateToIncomes: () -> Unit,
    onNavigateToFixedExpenses: () -> Unit,
    onNavigateToVariableExpenses: () -> Unit,
    onNavigateToDebts: () -> Unit,
    onNavigateToSavings: () -> Unit,
    onNavigateToReports: () -> Unit,
    onLogout: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()

    val incomes by viewModel.incomes.collectAsState()
    val fixedExpenses by viewModel.fixedExpenses.collectAsState()
    val variableExpenses by viewModel.variableExpenses.collectAsState()
    val savings by viewModel.savings.collectAsState()
    val debts by viewModel.debts.collectAsState()

    var showMonthPicker by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showUserSettings by remember { mutableStateOf(false) }

    val totalIncome = incomes.sumOf { it.amount }
    val totalFixed = fixedExpenses.sumOf { it.amount }
    val totalVariable = variableExpenses.sumOf { it.amount }
    val totalExpenses = totalFixed + totalVariable
    val netBalance = totalIncome - totalExpenses

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "المحاسب المنزلي",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "المستخدم: ${currentUser?.username ?: ""}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showUserSettings = true },
                        modifier = Modifier.testTag("dashboard_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "الإعدادات وتغيير كلمة المرور",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = {
                            viewModel.logout()
                            onLogout()
                        },
                        modifier = Modifier.testTag("dashboard_logout_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "تسجيل الخروج",
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .background(Slate50)
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Month & Year Selector Banner (Spans full width)
            item(span = { GridItemSpan(2) }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { showMonthPicker = true },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.selectPreviousMonth() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "الشهر السابق",
                                tint = Color.White
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showMonthPicker = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Color(0xFF60A5FA),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${DateUtils.getMonthName(selectedMonth)} $selectedYear",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "اضغط لتغيير الشهر أو السنة",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.selectNextMonth() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "الشهر التالي",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Monthly Summary Card (Spans full width)
            item(span = { GridItemSpan(2) }) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "ملخص الحسابات لشهر ${DateUtils.getMonthName(selectedMonth)}:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate700
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Incomes
                            Column {
                                Text("إجمالي الواردات", fontSize = 11.sp, color = Slate500)
                                Text(
                                    text = DateUtils.formatCurrencyIqd(totalIncome),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen
                                )
                            }

                            // Expenses
                            Column(horizontalAlignment = Alignment.End) {
                                Text("إجمالي المصروفات", fontSize = 11.sp, color = Slate500)
                                Text(
                                    text = DateUtils.formatCurrencyIqd(totalExpenses),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CoralRed
                                )
                            }
                        }

                        // Net Balance Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (netBalance >= 0) Color(0xFFECFDF5) else Color(0xFFFEF2F2))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (netBalance >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = if (netBalance >= 0) EmeraldGreen else CoralRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "صافي الواردات المتبقي:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (netBalance >= 0) Color(0xFF047857) else Color(0xFFB91C1C)
                                )
                            }
                            Text(
                                text = DateUtils.formatCurrencyIqd(netBalance),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (netBalance >= 0) EmeraldGreen else CoralRed
                            )
                        }
                    }
                }
            }

            // Section 1: الواردات
            item {
                ActionDashboardCard(
                    title = "الواردات المالية",
                    subtitle = "${incomes.size} واردة مسجلة",
                    amount = DateUtils.formatCurrencyIqd(totalIncome),
                    icon = Icons.Default.MonetizationOn,
                    iconTint = EmeraldGreen,
                    containerColor = EmeraldGreenBg,
                    onClick = onNavigateToIncomes,
                    testTag = "nav_incomes_card"
                )
            }

            // Section 2: المصروفات الثابتة
            item {
                ActionDashboardCard(
                    title = "المصروفات الثابتة",
                    subtitle = "${fixedExpenses.size} مصروف ثابت",
                    amount = DateUtils.formatCurrencyIqd(totalFixed),
                    icon = Icons.Default.Home,
                    iconTint = CoralRed,
                    containerColor = CoralRedBg,
                    onClick = onNavigateToFixedExpenses,
                    testTag = "nav_fixed_expenses_card"
                )
            }

            // Section 3: المصروفات المتغيرة
            item {
                ActionDashboardCard(
                    title = "المصروفات المتغيرة",
                    subtitle = "${variableExpenses.size} مصروف متغير",
                    amount = DateUtils.formatCurrencyIqd(totalVariable),
                    icon = Icons.Default.ShoppingCart,
                    iconTint = AmberOrange,
                    containerColor = AmberOrangeBg,
                    onClick = onNavigateToVariableExpenses,
                    testTag = "nav_variable_expenses_card"
                )
            }

            // Section 4: الديون والالتزامات
            item {
                val unpaidDebtsCount = debts.count { !it.isPaid }
                ActionDashboardCard(
                    title = "سجل الديون",
                    subtitle = "$unpaidDebtsCount دين غير مسدد",
                    amount = "${debts.size} سجلات",
                    icon = Icons.Default.AccountBalanceWallet,
                    iconTint = VioletPurple,
                    containerColor = VioletPurpleBg,
                    onClick = onNavigateToDebts,
                    testTag = "nav_debts_card"
                )
            }

            // Section 5: المدخرات
            item {
                val totalSavings = savings.sumOf { it.amount }
                ActionDashboardCard(
                    title = "صندوق المدخرات",
                    subtitle = "${savings.size} مدخرات",
                    amount = DateUtils.formatCurrencyIqd(totalSavings),
                    icon = Icons.Default.Savings,
                    iconTint = CyanTeal,
                    containerColor = CyanTealBg,
                    onClick = onNavigateToSavings,
                    testTag = "nav_savings_card"
                )
            }

            // Section 6: التقارير والمخططات
            item {
                ActionDashboardCard(
                    title = "التقارير والإحصائيات",
                    subtitle = "مخططات بيانية",
                    amount = "رسوم بيانية",
                    icon = Icons.Default.Assessment,
                    iconTint = RoyalBlue,
                    containerColor = RoyalBlueBg,
                    onClick = onNavigateToReports,
                    testTag = "nav_reports_card"
                )
            }

            // Section 7: تصدير إلى ملف إكسل (Spans full width)
            item(span = { GridItemSpan(2) }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { showExportDialog = true }
                        .testTag("nav_export_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFECFDF5)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "تصدير البيانات إلى ملف Excel",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Slate900
                                )
                                Text(
                                    text = "حفظ كجدول CSV في مجلد التنزيلات باللغة العربية",
                                    fontSize = 12.sp,
                                    color = Slate500
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Slate500
                        )
                    }
                }
            }

            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Month & Year Picker Dialog
    if (showMonthPicker) {
        MonthYearPickerDialog(
            currentMonth = selectedMonth,
            currentYear = selectedYear,
            onDismiss = { showMonthPicker = false },
            onConfirm = { m, y ->
                viewModel.setSelectedMonthYear(m, y)
                showMonthPicker = false
            }
        )
    }

    // Export Dialog
    if (showExportDialog) {
        ExportDataDialog(
            viewModel = viewModel,
            onDismiss = { showExportDialog = false }
        )
    }

    // User Settings Dialog
    if (showUserSettings) {
        UserSettingsDialog(
            viewModel = viewModel,
            onDismiss = { showUserSettings = false }
        )
    }
}

@Composable
private fun ActionDashboardCard(
    title: String,
    subtitle: String,
    amount: String,
    icon: ImageVector,
    iconTint: Color,
    containerColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Slate900
            )

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Slate500
            )

            Text(
                text = amount,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = iconTint
            )
        }
    }
}
