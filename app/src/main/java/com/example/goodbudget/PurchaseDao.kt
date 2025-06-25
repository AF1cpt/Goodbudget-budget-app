package com.example.goodbudget

import androidx.room.*

@Dao
interface PurchaseDao {
    @Insert
    suspend fun insertPurchase(purchase: Purchase)


    @Query("SELECT SUM(amount) FROM purchases WHERE userEmail = :email")
    suspend fun getTotalDebtByUser(email: String): Double?

    @Query("SELECT * FROM purchases")
    suspend fun getAllPurchases(): List<Purchase>

    @Query("SELECT * FROM purchases WHERE userEmail = :email")
    suspend fun getPurchasesByUser(email: String): List<Purchase>

    @Query("SELECT * FROM purchases WHERE userEmail = :email AND date LIKE :month || '%'")
    suspend fun getPurchasesByUserAndMonth(email: String, month: String): List<Purchase>
}