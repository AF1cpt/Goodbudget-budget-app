package com.example.goodbudget.gamification


import com.example.goodbudget.gamification.models.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object GamificationEngine {

    var userStats: UserStats = UserStats() // Ideally load from persistent storage

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun checkInToday(): Reward? {
        val today = LocalDate.now().format(dateFormatter)
        if (userStats.lastCheckInDate != today) {
            userStats.dailyCheckInStreak = if (wasYesterday(userStats.lastCheckInDate)) {
                userStats.dailyCheckInStreak + 1
            } else 1
            userStats.lastCheckInDate = today
            userStats.addXP(10)
            return Reward.XP(10)
        }
        return null
    }

    fun logExpense(): Reward? {
        val today = LocalDate.now().format(dateFormatter)
        if (userStats.lastExpenseLogDate != today) {
            userStats.expenseLogStreak = if (wasYesterday(userStats.lastExpenseLogDate)) {
                userStats.expenseLogStreak + 1
            } else 1
            userStats.lastExpenseLogDate = today
            userStats.addXP(15)
            return Reward.XP(15)
        }
        return null
    }

    fun addSavings(amount: Double): Reward? {
        userStats.totalSavings += amount
        userStats.addXP((amount * 0.5).toInt())
        return Reward.XP((amount * 0.5).toInt())
    }

    private fun wasYesterday(dateStr: String?): Boolean {
        if (dateStr == null) return false
        val yesterday = LocalDate.now().minusDays(1).format(dateFormatter)
        return dateStr == yesterday
    }
}
