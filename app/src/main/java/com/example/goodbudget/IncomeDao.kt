package com.example.goodbudget

import androidx.room.*

@Dao
interface IncomeDao {
    @Insert
    suspend fun insertIncome(income: Income)

    @Query("SELECT SUM(amount) FROM income WHERE userEmail = :email")
    suspend fun getTotalIncomeByUser(email: String): Double?
}