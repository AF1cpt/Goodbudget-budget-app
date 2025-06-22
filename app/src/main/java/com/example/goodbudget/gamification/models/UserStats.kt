package com.example.goodbudget.gamification.models

data class UserStats(
    var xp: Int = 0,
    var level: Int = 1,
    var dailyCheckInStreak: Int = 0,
    var lastCheckInDate: String? = null,  // Format: "yyyy-MM-dd"
    var expenseLogStreak: Int = 0,
    var lastExpenseLogDate: String? = null,
    var totalSavings: Double = 0.0,
    var badgesUnlocked: MutableList<String> = mutableListOf()
) {
    fun calculateLevel(): Int {
        return 1 + (xp / 100)
    }

    fun addXP(amount: Int) {
        xp += amount
        level = calculateLevel()
    }
}