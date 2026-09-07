package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Income
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    @Query("SELECT * FROM incomes WHERE userId = :userId AND year = :year AND month = :month ORDER BY date DESC, id DESC")
    fun getIncomesByMonth(userId: Long, year: Int, month: Int): Flow<List<Income>>

    @Query("SELECT * FROM incomes WHERE userId = :userId AND year = :year ORDER BY date DESC, id DESC")
    fun getIncomesByYear(userId: Long, year: Int): Flow<List<Income>>

    @Query("SELECT * FROM incomes WHERE userId = :userId AND date >= :startDate AND date <= :endDate ORDER BY date ASC, id ASC")
    fun getIncomesByDateRange(userId: Long, startDate: String, endDate: String): Flow<List<Income>>

    @Query("SELECT * FROM incomes WHERE userId = :userId ORDER BY date DESC, id DESC")
    fun getAllIncomes(userId: Long): Flow<List<Income>>

    @Query("SELECT SUM(amount) FROM incomes WHERE userId = :userId AND year = :year AND month = :month")
    fun getTotalIncomeByMonth(userId: Long, year: Int, month: Int): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: Income): Long

    @Update
    suspend fun updateIncome(income: Income)

    @Delete
    suspend fun deleteIncome(income: Income)

    @Query("DELETE FROM incomes WHERE id = :id")
    suspend fun deleteIncomeById(id: Long)

    @Query("DELETE FROM incomes WHERE userId = :userId")
    suspend fun deleteAllByUserId(userId: Long)
}
