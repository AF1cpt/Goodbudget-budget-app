package com.example.goodbudget.gamification.models

data class UserStats(
    var level: Int = 1,
    var xp: Int = 0, // Total accumulated XP
    var dailyCheckInStreak: Int = 0,
    var receiptUploadCount: Int = 0,
    var expenseLogStreak: Int = 0,
    val badgesUnlocked: MutableList<String> = mutableListOf()
) {
    // XP needed to level up increases per level (e.g., 100 * level)
    val requiredXp: Int
        get() = level * 100

    // XP progress toward the next level
    val currentXp: Int
        get() = xp % requiredXp

    // For use elsewhere if needed
    val totalXp: Int
        get() = xp
}
