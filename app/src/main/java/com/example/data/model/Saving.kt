package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings")
data class Saving(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val title: String,
    val amount: Double,
    val date: String,
    val notes: String = "",
    val colorHex: String = "#0284C7", // Sky Blue
    val createdAt: Long = System.currentTimeMillis()
)
