package com.example.goodbudget

import androidx.room.*

@Dao
interface UserDao {
    @Insert suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    suspend fun login(email: String, password: String): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Update suspend fun updateUser(user: User)


}