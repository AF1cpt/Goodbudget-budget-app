package com.example.goodbudget

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "purchases")
data class Purchase(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String, // Add this if you store purchase name separately
    val amount: Double,
    val category: String,
    val date: String, // Store as "yyyy-MM-dd" or just "yyyy-MM"
    val userEmail: String
)