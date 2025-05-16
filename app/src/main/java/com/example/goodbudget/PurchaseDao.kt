package com.example.goodbudget

import androidx.room.*

@Dao
interface PurchaseDao {
    @Insert
    suspend fun insertPurchase(purchase: Purchase)

    @Query("SELECT SUM(amount) FROM purchases")
    suspend fun getTotalDebt(): Double?

    @Query("SELECT * FROM purchases")
    suspend fun getAllPurchases(): List<Purchase>
}