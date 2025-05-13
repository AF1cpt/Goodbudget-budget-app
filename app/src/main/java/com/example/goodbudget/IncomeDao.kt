package com.example.goodbudget

import androidx.room.*

@Dao
interface IncomeDao {
    @Insert
    suspend fun insertIncome(income: Income)

    @Query("SELECT SUM(amount) FROM income")
    suspend fun getTotalIncome(): Double
}