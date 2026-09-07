package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.VariableExpense
import kotlinx.coroutines.flow.Flow

@Dao
interface VariableExpenseDao {
    @Query("SELECT * FROM variable_expenses WHERE userId = :userId AND year = :year AND month = :month ORDER BY date DESC, id DESC")
    fun getVariableExpensesByMonth(userId: Long, year: Int, month: Int): Flow<List<VariableExpense>>

    @Query("SELECT * FROM variable_expenses WHERE userId = :userId AND year = :year ORDER BY date DESC, id DESC")
    fun getVariableExpensesByYear(userId: Long, year: Int): Flow<List<VariableExpense>>

    @Query("SELECT * FROM variable_expenses WHERE userId = :userId AND date >= :startDate AND date <= :endDate ORDER BY date ASC, id ASC")
    fun getVariableExpensesByDateRange(userId: Long, startDate: String, endDate: String): Flow<List<VariableExpense>>

    @Query("SELECT * FROM variable_expenses WHERE userId = :userId ORDER BY date DESC, id DESC")
    fun getAllVariableExpenses(userId: Long): Flow<List<VariableExpense>>

    @Query("SELECT SUM(amount) FROM variable_expenses WHERE userId = :userId AND year = :year AND month = :month")
    fun getTotalVariableExpensesByMonth(userId: Long, year: Int, month: Int): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariableExpense(expense: VariableExpense): Long

    @Update
    suspend fun updateVariableExpense(expense: VariableExpense)

    @Delete
    suspend fun deleteVariableExpense(expense: VariableExpense)

    @Query("DELETE FROM variable_expenses WHERE id = :id")
    suspend fun deleteVariableExpenseById(id: Long)

    @Query("DELETE FROM variable_expenses WHERE userId = :userId")
    suspend fun deleteAllByUserId(userId: Long)
}
