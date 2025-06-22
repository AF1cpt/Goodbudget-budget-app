package com.example.goodbudget.gamification

import com.example.goodbudget.gamification.models.Badge
import com.example.goodbudget.gamification.models.UserStats
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object BadgeManager {

    val availableBadges = listOf(
        Badge("first_budget", "Budget Beginner", "Created your first budget!"),
        Badge("checkin_5", "Habit Starter", "Checked in 5 days in a row!"),
        Badge("saver_100", "Super Saver", "Saved R100+!"),
        Badge("streak_7", "Expense Champion", "Logged expenses for 7 days straight!")
    )

    fun unlockBadges(userStats: UserStats): List<Badge> {
        val newlyUnlocked = mutableListOf<Badge>()
        for (badge in availableBadges) {
            if (!userStats.badgesUnlocked.contains(badge.id) && isEligible(badge, userStats)) {
                userStats.badgesUnlocked.add(badge.id)
                newlyUnlocked.add(badge.copy(isUnlocked = true, dateUnlocked = LocalDate.now().format(DateTimeFormatter.ISO_DATE)))
            }
        }
        return newlyUnlocked
    }

    private fun isEligible(badge: Badge, stats: UserStats): Boolean {
        return when (badge.id) {
            "first_budget" -> stats.xp > 0
            "checkin_5" -> stats.dailyCheckInStreak >= 5
            "saver_100" -> stats.totalSavings >= 100
            "streak_7" -> stats.expenseLogStreak >= 7
            else -> false
        }
    }
}
