package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incomes")
data class Income(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val title: String,
    val amount: Double,
    val date: String, // format: yyyy-MM-dd
    val year: Int,
    val month: Int, // 1 to 12
    val notes: String = "",
    val colorHex: String = "#10B981", // default Emerald Green
    val createdAt: Long = System.currentTimeMillis()
)
