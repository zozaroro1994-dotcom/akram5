package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FixedExpense
import kotlinx.coroutines.flow.Flow

@Dao
interface FixedExpenseDao {
    @Query("SELECT * FROM fixed_expenses WHERE userId = :userId AND year = :year AND month = :month ORDER BY date DESC, id DESC")
    fun getFixedExpensesByMonth(userId: Long, year: Int, month: Int): Flow<List<FixedExpense>>

    @Query("SELECT * FROM fixed_expenses WHERE userId = :userId AND year = :year ORDER BY date DESC, id DESC")
    fun getFixedExpensesByYear(userId: Long, year: Int): Flow<List<FixedExpense>>

    @Query("SELECT * FROM fixed_expenses WHERE userId = :userId AND date >= :startDate AND date <= :endDate ORDER BY date ASC, id ASC")
    fun getFixedExpensesByDateRange(userId: Long, startDate: String, endDate: String): Flow<List<FixedExpense>>

    @Query("SELECT * FROM fixed_expenses WHERE userId = :userId ORDER BY date DESC, id DESC")
    fun getAllFixedExpenses(userId: Long): Flow<List<FixedExpense>>

    @Query("SELECT SUM(amount) FROM fixed_expenses WHERE userId = :userId AND year = :year AND month = :month")
    fun getTotalFixedExpensesByMonth(userId: Long, year: Int, month: Int): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFixedExpense(expense: FixedExpense): Long

    @Update
    suspend fun updateFixedExpense(expense: FixedExpense)

    @Delete
    suspend fun deleteFixedExpense(expense: FixedExpense)

    @Query("DELETE FROM fixed_expenses WHERE id = :id")
    suspend fun deleteFixedExpenseById(id: Long)

    @Query("DELETE FROM fixed_expenses WHERE userId = :userId")
    suspend fun deleteAllByUserId(userId: Long)
}
