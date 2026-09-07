package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Debt
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts WHERE userId = :userId ORDER BY isPaid ASC, date DESC, id DESC")
    fun getAllDebts(userId: Long): Flow<List<Debt>>

    @Query("SELECT * FROM debts WHERE userId = :userId AND isPaid = 0 ORDER BY date DESC")
    fun getUnpaidDebts(userId: Long): Flow<List<Debt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: Debt): Long

    @Update
    suspend fun updateDebt(debt: Debt)

    @Delete
    suspend fun deleteDebt(debt: Debt)

    @Query("DELETE FROM debts WHERE id = :id")
    suspend fun deleteDebtById(id: Long)

    @Query("DELETE FROM debts WHERE userId = :userId")
    suspend fun deleteAllByUserId(userId: Long)
}
