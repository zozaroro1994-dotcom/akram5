package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Saving
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingDao {
    @Query("SELECT * FROM savings WHERE userId = :userId ORDER BY date DESC, id DESC")
    fun getAllSavings(userId: Long): Flow<List<Saving>>

    @Query("SELECT SUM(amount) FROM savings WHERE userId = :userId")
    fun getTotalSavings(userId: Long): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaving(saving: Saving): Long

    @Update
    suspend fun updateSaving(saving: Saving)

    @Delete
    suspend fun deleteSaving(saving: Saving)

    @Query("DELETE FROM savings WHERE id = :id")
    suspend fun deleteSavingById(id: Long)

    @Query("DELETE FROM savings WHERE userId = :userId")
    suspend fun deleteAllByUserId(userId: Long)
}
