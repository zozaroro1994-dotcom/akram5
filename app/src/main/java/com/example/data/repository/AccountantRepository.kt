package com.example.data.repository

import com.example.data.local.DebtDao
import com.example.data.local.FixedExpenseDao
import com.example.data.local.IncomeDao
import com.example.data.local.SavingDao
import com.example.data.local.UserDao
import com.example.data.local.VariableExpenseDao
import com.example.data.model.Debt
import com.example.data.model.FixedExpense
import com.example.data.model.Income
import com.example.data.model.Saving
import com.example.data.model.User
import com.example.data.model.VariableExpense
import kotlinx.coroutines.flow.Flow

class AccountantRepository(
    private val userDao: UserDao,
    private val incomeDao: IncomeDao,
    private val fixedExpenseDao: FixedExpenseDao,
    private val variableExpenseDao: VariableExpenseDao,
    private val debtDao: DebtDao,
    private val savingDao: SavingDao
) {
    // --- Users ---
    val allUsers: Flow<List<User>> = userDao.getAllUsers()

    suspend fun getUserByUsername(username: String): User? = userDao.getUserByUsername(username)
    suspend fun getUserById(id: Long): User? = userDao.getUserById(id)
    suspend fun getUserCount(): Int = userDao.getUserCount()
    suspend fun insertUser(user: User): Long = userDao.insertUser(user)
    suspend fun updateUser(user: User) = userDao.updateUser(user)
    suspend fun deleteUserById(id: Long) {
        userDao.deleteUserById(id)
        incomeDao.deleteAllByUserId(id)
        fixedExpenseDao.deleteAllByUserId(id)
        variableExpenseDao.deleteAllByUserId(id)
        debtDao.deleteAllByUserId(id)
        savingDao.deleteAllByUserId(id)
    }

    // --- Incomes ---
    fun getIncomesByMonth(userId: Long, year: Int, month: Int): Flow<List<Income>> =
        incomeDao.getIncomesByMonth(userId, year, month)

    fun getIncomesByYear(userId: Long, year: Int): Flow<List<Income>> =
        incomeDao.getIncomesByYear(userId, year)

    fun getIncomesByDateRange(userId: Long, start: String, end: String): Flow<List<Income>> =
        incomeDao.getIncomesByDateRange(userId, start, end)

    fun getTotalIncomeByMonth(userId: Long, year: Int, month: Int): Flow<Double?> =
        incomeDao.getTotalIncomeByMonth(userId, year, month)

    suspend fun insertIncome(income: Income): Long = incomeDao.insertIncome(income)
    suspend fun updateIncome(income: Income) = incomeDao.updateIncome(income)
    suspend fun deleteIncome(income: Income) = incomeDao.deleteIncome(income)
    suspend fun deleteIncomeById(id: Long) = incomeDao.deleteIncomeById(id)

    // --- Fixed Expenses ---
    fun getFixedExpensesByMonth(userId: Long, year: Int, month: Int): Flow<List<FixedExpense>> =
        fixedExpenseDao.getFixedExpensesByMonth(userId, year, month)

    fun getFixedExpensesByYear(userId: Long, year: Int): Flow<List<FixedExpense>> =
        fixedExpenseDao.getFixedExpensesByYear(userId, year)

    fun getFixedExpensesByDateRange(userId: Long, start: String, end: String): Flow<List<FixedExpense>> =
        fixedExpenseDao.getFixedExpensesByDateRange(userId, start, end)

    fun getTotalFixedExpensesByMonth(userId: Long, year: Int, month: Int): Flow<Double?> =
        fixedExpenseDao.getTotalFixedExpensesByMonth(userId, year, month)

    suspend fun insertFixedExpense(expense: FixedExpense): Long = fixedExpenseDao.insertFixedExpense(expense)
    suspend fun updateFixedExpense(expense: FixedExpense) = fixedExpenseDao.updateFixedExpense(expense)
    suspend fun deleteFixedExpense(expense: FixedExpense) = fixedExpenseDao.deleteFixedExpense(expense)
    suspend fun deleteFixedExpenseById(id: Long) = fixedExpenseDao.deleteFixedExpenseById(id)

    // --- Variable Expenses ---
    fun getVariableExpensesByMonth(userId: Long, year: Int, month: Int): Flow<List<VariableExpense>> =
        variableExpenseDao.getVariableExpensesByMonth(userId, year, month)

    fun getVariableExpensesByYear(userId: Long, year: Int): Flow<List<VariableExpense>> =
        variableExpenseDao.getVariableExpensesByYear(userId, year)

    fun getVariableExpensesByDateRange(userId: Long, start: String, end: String): Flow<List<VariableExpense>> =
        variableExpenseDao.getVariableExpensesByDateRange(userId, start, end)

    fun getTotalVariableExpensesByMonth(userId: Long, year: Int, month: Int): Flow<Double?> =
        variableExpenseDao.getTotalVariableExpensesByMonth(userId, year, month)

    suspend fun insertVariableExpense(expense: VariableExpense): Long = variableExpenseDao.insertVariableExpense(expense)
    suspend fun updateVariableExpense(expense: VariableExpense) = variableExpenseDao.updateVariableExpense(expense)
    suspend fun deleteVariableExpense(expense: VariableExpense) = variableExpenseDao.deleteVariableExpense(expense)
    suspend fun deleteVariableExpenseById(id: Long) = variableExpenseDao.deleteVariableExpenseById(id)

    // --- Debts ---
    fun getAllDebts(userId: Long): Flow<List<Debt>> = debtDao.getAllDebts(userId)
    suspend fun insertDebt(debt: Debt): Long = debtDao.insertDebt(debt)
    suspend fun updateDebt(debt: Debt) = debtDao.updateDebt(debt)
    suspend fun deleteDebt(debt: Debt) = debtDao.deleteDebt(debt)
    suspend fun deleteDebtById(id: Long) = debtDao.deleteDebtById(id)

    // --- Savings ---
    fun getAllSavings(userId: Long): Flow<List<Saving>> = savingDao.getAllSavings(userId)
    fun getTotalSavings(userId: Long): Flow<Double?> = savingDao.getTotalSavings(userId)
    suspend fun insertSaving(saving: Saving): Long = savingDao.insertSaving(saving)
    suspend fun updateSaving(saving: Saving) = savingDao.updateSaving(saving)
    suspend fun deleteSaving(saving: Saving) = savingDao.deleteSaving(saving)
    suspend fun deleteSavingById(id: Long) = savingDao.deleteSavingById(id)
}
