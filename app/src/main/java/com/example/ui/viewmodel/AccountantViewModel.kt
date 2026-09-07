package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Debt
import com.example.data.model.FixedExpense
import com.example.data.model.Income
import com.example.data.model.Saving
import com.example.data.model.User
import com.example.data.model.VariableExpense
import com.example.data.repository.AccountantRepository
import com.example.util.DateUtils
import com.example.util.ExportManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class AccountantViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AccountantRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = AccountantRepository(
            userDao = db.userDao(),
            incomeDao = db.incomeDao(),
            fixedExpenseDao = db.fixedExpenseDao(),
            variableExpenseDao = db.variableExpenseDao(),
            debtDao = db.debtDao(),
            savingDao = db.savingDao()
        )
        // Seed default user if empty
        viewModelScope.launch {
            if (repository.getUserCount() == 0) {
                repository.insertUser(
                    User(
                        username = "admin",
                        passwordHash = "1234",
                        fullName = "المستخدم الافتراضي"
                    )
                )
            }
        }
    }

    val allUsers: StateFlow<List<User>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _selectedYear = MutableStateFlow(DateUtils.getCurrentYear())
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow(DateUtils.getCurrentMonth())
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    // Key to trigger refresh of filtered flows when user, year, or month changes
    private data class FilterKey(val userId: Long, val year: Int, val month: Int)

    private val filterKey = combine(_currentUser, _selectedYear, _selectedMonth) { user, year, month ->
        if (user != null) FilterKey(user.id, year, month) else null
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val incomes: StateFlow<List<Income>> = filterKey.flatMapLatest { key ->
        if (key != null) {
            repository.getIncomesByMonth(key.userId, key.year, key.month)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val fixedExpenses: StateFlow<List<FixedExpense>> = filterKey.flatMapLatest { key ->
        if (key != null) {
            repository.getFixedExpensesByMonth(key.userId, key.year, key.month)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val variableExpenses: StateFlow<List<VariableExpense>> = filterKey.flatMapLatest { key ->
        if (key != null) {
            repository.getVariableExpensesByMonth(key.userId, key.year, key.month)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val debts: StateFlow<List<Debt>> = _currentUser.flatMapLatest { user ->
        if (user != null) {
            repository.getAllDebts(user.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val savings: StateFlow<List<Saving>> = _currentUser.flatMapLatest { user ->
        if (user != null) {
            repository.getAllSavings(user.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Authentication ---
    fun login(username: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val trimmedUsername = username.trim()
            val user = repository.getUserByUsername(trimmedUsername)
            if (user == null) {
                onResult(false, "اسم المستخدم غير موجود")
            } else if (user.passwordHash != password) {
                onResult(false, "كلمة المرور غير صحيحة")
            } else {
                _currentUser.value = user
                onResult(true, "تم تسجيل الدخول بنجاح")
            }
        }
    }

    fun register(username: String, password: String, fullName: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val trimmedUsername = username.trim()
            if (trimmedUsername.isBlank()) {
                onResult(false, "يرجى كتابة اسم المستخدم")
                return@launch
            }
            if (password.length < 3) {
                onResult(false, "كلمة المرور يجب أن تكون 3 أحرف على الأقل")
                return@launch
            }
            val existing = repository.getUserByUsername(trimmedUsername)
            if (existing != null) {
                onResult(false, "اسم المستخدم مسجل مسبقاً، اختر اسماً آخر")
                return@launch
            }
            val newId = repository.insertUser(
                User(
                    username = trimmedUsername,
                    passwordHash = password,
                    fullName = fullName.trim().ifBlank { trimmedUsername }
                )
            )
            val createdUser = repository.getUserById(newId)
            _currentUser.value = createdUser
            onResult(true, "تم إنشاء الحساب وتسجيل الدخول")
        }
    }

    fun changePassword(oldPassword: String, newPassword: String, onResult: (Boolean, String) -> Unit) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            if (user.passwordHash != oldPassword) {
                onResult(false, "كلمة المرور القديمة غير صحيحة")
                return@launch
            }
            if (newPassword.length < 3) {
                onResult(false, "كلمة المرور الجديدة يجب أن تكون 3 أحرف على الأقل")
                return@launch
            }
            val updated = user.copy(passwordHash = newPassword)
            repository.updateUser(updated)
            _currentUser.value = updated
            onResult(true, "تم تغيير كلمة المرور بنجاح")
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    fun deleteUser(userId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteUserById(userId)
            if (_currentUser.value?.id == userId) {
                _currentUser.value = null
            }
            onComplete()
        }
    }

    // --- Navigation & Date Selection ---
    fun setMonthAndYear(month: Int, year: Int) {
        _selectedMonth.value = month
        _selectedYear.value = year
    }

    fun setSelectedMonthYear(month: Int, year: Int) {
        setMonthAndYear(month, year)
    }

    fun selectPreviousMonth() {
        var currentM = _selectedMonth.value
        var currentY = _selectedYear.value
        if (currentM == 1) {
            currentM = 12
            currentY -= 1
        } else {
            currentM -= 1
        }
        _selectedMonth.value = currentM
        _selectedYear.value = currentY
    }

    fun selectNextMonth() {
        var currentM = _selectedMonth.value
        var currentY = _selectedYear.value
        if (currentM == 12) {
            currentM = 1
            currentY += 1
        } else {
            currentM += 1
        }
        _selectedMonth.value = currentM
        _selectedYear.value = currentY
    }

    // --- Income Actions ---
    fun addIncome(title: String, amount: Double, date: String, notes: String, colorHex: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val year = DateUtils.parseYearFromDate(date)
            val month = DateUtils.parseMonthFromDate(date)
            repository.insertIncome(
                Income(
                    userId = user.id,
                    title = title.trim(),
                    amount = amount,
                    date = date,
                    year = year,
                    month = month,
                    notes = notes.trim(),
                    colorHex = colorHex
                )
            )
        }
    }

    fun updateIncome(income: Income) {
        viewModelScope.launch {
            val year = DateUtils.parseYearFromDate(income.date)
            val month = DateUtils.parseMonthFromDate(income.date)
            repository.updateIncome(income.copy(year = year, month = month))
        }
    }

    fun deleteIncome(income: Income) {
        viewModelScope.launch {
            repository.deleteIncome(income)
        }
    }

    // --- Fixed Expense Actions ---
    fun addFixedExpense(title: String, amount: Double, date: String, notes: String, colorHex: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val year = DateUtils.parseYearFromDate(date)
            val month = DateUtils.parseMonthFromDate(date)
            repository.insertFixedExpense(
                FixedExpense(
                    userId = user.id,
                    title = title.trim(),
                    amount = amount,
                    date = date,
                    year = year,
                    month = month,
                    notes = notes.trim(),
                    colorHex = colorHex
                )
            )
        }
    }

    fun updateFixedExpense(expense: FixedExpense) {
        viewModelScope.launch {
            val year = DateUtils.parseYearFromDate(expense.date)
            val month = DateUtils.parseMonthFromDate(expense.date)
            repository.updateFixedExpense(expense.copy(year = year, month = month))
        }
    }

    fun deleteFixedExpense(expense: FixedExpense) {
        viewModelScope.launch {
            repository.deleteFixedExpense(expense)
        }
    }

    // --- Variable Expense Actions ---
    fun addVariableExpense(title: String, amount: Double, date: String, notes: String, colorHex: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val year = DateUtils.parseYearFromDate(date)
            val month = DateUtils.parseMonthFromDate(date)
            repository.insertVariableExpense(
                VariableExpense(
                    userId = user.id,
                    title = title.trim(),
                    amount = amount,
                    date = date,
                    year = year,
                    month = month,
                    notes = notes.trim(),
                    colorHex = colorHex
                )
            )
        }
    }

    fun updateVariableExpense(expense: VariableExpense) {
        viewModelScope.launch {
            val year = DateUtils.parseYearFromDate(expense.date)
            val month = DateUtils.parseMonthFromDate(expense.date)
            repository.updateVariableExpense(expense.copy(year = year, month = month))
        }
    }

    fun deleteVariableExpense(expense: VariableExpense) {
        viewModelScope.launch {
            repository.deleteVariableExpense(expense)
        }
    }

    // --- Debt Actions ---
    fun addDebt(personName: String, amount: Double, isOwedToMe: Boolean, date: String, notes: String, colorHex: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.insertDebt(
                Debt(
                    userId = user.id,
                    personName = personName.trim(),
                    amount = amount,
                    isOwedToMe = isOwedToMe,
                    date = date,
                    notes = notes.trim(),
                    isPaid = false,
                    colorHex = colorHex
                )
            )
        }
    }

    fun updateDebt(debt: Debt) {
        viewModelScope.launch {
            repository.updateDebt(debt)
        }
    }

    fun toggleDebtPaidStatus(debt: Debt) {
        viewModelScope.launch {
            repository.updateDebt(debt.copy(isPaid = !debt.isPaid))
        }
    }

    fun deleteDebt(debt: Debt) {
        viewModelScope.launch {
            repository.deleteDebt(debt)
        }
    }

    // --- Savings Actions ---
    fun addSaving(title: String, amount: Double, date: String, notes: String, colorHex: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.insertSaving(
                Saving(
                    userId = user.id,
                    title = title.trim(),
                    amount = amount,
                    date = date,
                    notes = notes.trim(),
                    colorHex = colorHex
                )
            )
        }
    }

    fun updateSaving(saving: Saving) {
        viewModelScope.launch {
            repository.updateSaving(saving)
        }
    }

    fun deleteSaving(saving: Saving) {
        viewModelScope.launch {
            repository.deleteSaving(saving)
        }
    }

    // --- Export Functions ---
    fun exportMonthToCsv(context: Context, year: Int, month: Int, onResult: (File?) -> Unit) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val inList = repository.getIncomesByMonth(user.id, year, month).first()
            val fixList = repository.getFixedExpensesByMonth(user.id, year, month).first()
            val varList = repository.getVariableExpensesByMonth(user.id, year, month).first()
            val debtList = repository.getAllDebts(user.id).first()
            val saveList = repository.getAllSavings(user.id).first()

            val file = ExportManager.exportToExcelCsv(
                context = context,
                username = user.fullName.ifBlank { user.username },
                periodTitle = "شهر_${month}_سنة_$year",
                incomes = inList,
                fixedExpenses = fixList,
                variableExpenses = varList,
                debts = debtList,
                savings = saveList
            )
            onResult(file)
        }
    }

    fun exportYearToCsv(context: Context, year: Int, onResult: (File?) -> Unit) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val inList = repository.getIncomesByYear(user.id, year).first()
            val fixList = repository.getFixedExpensesByYear(user.id, year).first()
            val varList = repository.getVariableExpensesByYear(user.id, year).first()
            val debtList = repository.getAllDebts(user.id).first()
            val saveList = repository.getAllSavings(user.id).first()

            val file = ExportManager.exportToExcelCsv(
                context = context,
                username = user.fullName.ifBlank { user.username },
                periodTitle = "سنة_$year",
                incomes = inList,
                fixedExpenses = fixList,
                variableExpenses = varList,
                debts = debtList,
                savings = saveList
            )
            onResult(file)
        }
    }

    fun exportRangeToCsv(context: Context, startDate: String, endDate: String, onResult: (File?) -> Unit) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val inList = repository.getIncomesByDateRange(user.id, startDate, endDate).first()
            val fixList = repository.getFixedExpensesByDateRange(user.id, startDate, endDate).first()
            val varList = repository.getVariableExpensesByDateRange(user.id, startDate, endDate).first()
            val debtList = repository.getAllDebts(user.id).first()
            val saveList = repository.getAllSavings(user.id).first()

            val file = ExportManager.exportToExcelCsv(
                context = context,
                username = user.fullName.ifBlank { user.username },
                periodTitle = "من_${startDate}_إلى_$endDate",
                incomes = inList,
                fixedExpenses = fixList,
                variableExpenses = varList,
                debts = debtList,
                savings = saveList
            )
            onResult(file)
        }
    }

    fun shareExportedFile(context: Context, file: File) {
        ExportManager.shareExportedFile(context, file)
    }

    fun exportCurrentMonth(context: Context, onResult: (File?) -> Unit) {
        val user = _currentUser.value ?: return
        val year = _selectedYear.value
        val month = _selectedMonth.value
        val monthName = DateUtils.getMonthName(month)
        viewModelScope.launch {
            val inList = repository.getIncomesByMonth(user.id, year, month).first()
            val fixList = repository.getFixedExpensesByMonth(user.id, year, month).first()
            val varList = repository.getVariableExpensesByMonth(user.id, year, month).first()
            val debtList = repository.getAllDebts(user.id).first()
            val saveList = repository.getAllSavings(user.id).first()

            val file = ExportManager.exportToExcelCsv(
                context = context,
                username = user.fullName.ifBlank { user.username },
                periodTitle = "شهر_${month}_سنة_$year",
                incomes = inList,
                fixedExpenses = fixList,
                variableExpenses = varList,
                debts = debtList,
                savings = saveList
            )
            onResult(file)
        }
    }

    fun exportSpecificYear(context: Context, year: Int, onResult: (File?) -> Unit) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val inList = repository.getIncomesByYear(user.id, year).first()
            val fixList = repository.getFixedExpensesByYear(user.id, year).first()
            val varList = repository.getVariableExpensesByYear(user.id, year).first()
            val debtList = repository.getAllDebts(user.id).first()
            val saveList = repository.getAllSavings(user.id).first()

            val file = ExportManager.exportToExcelCsv(
                context = context,
                username = user.fullName.ifBlank { user.username },
                periodTitle = "سنة_$year",
                incomes = inList,
                fixedExpenses = fixList,
                variableExpenses = varList,
                debts = debtList,
                savings = saveList
            )
            onResult(file)
        }
    }

    fun exportDateRange(context: Context, startDate: String, endDate: String, onResult: (File?) -> Unit) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val inList = repository.getIncomesByDateRange(user.id, startDate, endDate).first()
            val fixList = repository.getFixedExpensesByDateRange(user.id, startDate, endDate).first()
            val varList = repository.getVariableExpensesByDateRange(user.id, startDate, endDate).first()
            val debtList = repository.getAllDebts(user.id).first()
            val saveList = repository.getAllSavings(user.id).first()

            val file = ExportManager.exportToExcelCsv(
                context = context,
                username = user.fullName.ifBlank { user.username },
                periodTitle = "من_${startDate}_إلى_$endDate",
                incomes = inList,
                fixedExpenses = fixList,
                variableExpenses = varList,
                debts = debtList,
                savings = saveList
            )
            onResult(file)
        }
    }

    // --- Report Query Functions ---
    suspend fun getReportDataForMonth(year: Int, month: Int): Triple<List<Income>, List<FixedExpense>, List<VariableExpense>> {
        val user = _currentUser.value ?: return Triple(emptyList(), emptyList(), emptyList())
        val inList = repository.getIncomesByMonth(user.id, year, month).first()
        val fixList = repository.getFixedExpensesByMonth(user.id, year, month).first()
        val varList = repository.getVariableExpensesByMonth(user.id, year, month).first()
        return Triple(inList, fixList, varList)
    }

    suspend fun getReportDataForYear(year: Int): Triple<List<Income>, List<FixedExpense>, List<VariableExpense>> {
        val user = _currentUser.value ?: return Triple(emptyList(), emptyList(), emptyList())
        val inList = repository.getIncomesByYear(user.id, year).first()
        val fixList = repository.getFixedExpensesByYear(user.id, year).first()
        val varList = repository.getVariableExpensesByYear(user.id, year).first()
        return Triple(inList, fixList, varList)
    }

    suspend fun getReportDataForRange(start: String, end: String): Triple<List<Income>, List<FixedExpense>, List<VariableExpense>> {
        val user = _currentUser.value ?: return Triple(emptyList(), emptyList(), emptyList())
        val inList = repository.getIncomesByDateRange(user.id, start, end).first()
        val fixList = repository.getFixedExpensesByDateRange(user.id, start, end).first()
        val varList = repository.getVariableExpensesByDateRange(user.id, start, end).first()
        return Triple(inList, fixList, varList)
    }
}
