package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "debts")
data class Debt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val personName: String,
    val amount: Double,
    val isOwedToMe: Boolean, // true = لنا (له في ذمتهم), false = علينا (في ذمتنا)
    val date: String,
    val notes: String = "",
    val isPaid: Boolean = false,
    val colorHex: String = "#8B5CF6", // Purple
    val createdAt: Long = System.currentTimeMillis()
)
