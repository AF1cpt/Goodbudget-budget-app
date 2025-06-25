package com.example.goodbudget.gamification

import com.example.goodbudget.AppDatabase
import com.example.goodbudget.gamification.models.Badge
import com.example.goodbudget.gamification.models.UserStats
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object BadgeManager {

    private val availableBadges = listOf(
        Badge("first_budget", "Budget Beginner", "Created your first budget!"),
        Badge("checkin_5", "Habit Starter", "Checked in 5 days in a row!"),
        Badge("first_spend", "First Spend", "Logged your first expense."),
        Badge("proof_keeper", "Proof Keeper", "Uploaded 10 receipts.")
    )

    fun unlockBadges(userStats: UserStats, db: AppDatabase, userEmail: String): List<Badge> {
        val newlyUnlocked = mutableListOf<Badge>()

        for (badge in availableBadges) {
            if (!userStats.badgesUnlocked.contains(badge.id) && isEligible(badge, userStats, db, userEmail)) {
                userStats.badgesUnlocked.add(badge.id)
                newlyUnlocked.add(
                    badge.copy(
                        isUnlocked = true,
                        dateUnlocked = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                    )
                )
            }
        }

        return newlyUnlocked
    }

    private fun isEligible(badge: Badge, stats: UserStats, db: AppDatabase, userEmail: String): Boolean = runBlocking {
        when (badge.id) {
            "first_budget" -> stats.xp > 0
            "checkin_5" -> stats.dailyCheckInStreak >= 5
            "first_spend" -> stats.expenseLogStreak >= 1
            "proof_keeper" -> {
                val receipts = db.purchaseDao().getPurchasesByUser(userEmail)
                receipts.count { !it.receiptImageUri.isNullOrEmpty() } >= 10
            }
            else -> false
        }
    }
}
