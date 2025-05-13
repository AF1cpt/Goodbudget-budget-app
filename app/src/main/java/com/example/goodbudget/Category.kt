package com.example.goodbudget

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,  // Auto-generate unique IDs
    val name: String,
    val limit: Double
)
