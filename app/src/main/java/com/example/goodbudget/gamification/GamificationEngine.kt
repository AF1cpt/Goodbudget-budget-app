package com.example.goodbudget.gamification

import android.content.Context
import com.example.goodbudget.gamification.models.UserStats
import com.example.goodbudget.gamification.models.Reward
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import com.jakewharton.threetenabp.AndroidThreeTen

object GamificationEngine {

    private var userStats: UserStats = UserStats()
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // This should be called once from Application or MainActivity
    fun initialize(context: Context) {
        AndroidThreeTen.init(context)
        GamificationStorage.init(context)
        userStats = GamificationStorage.getUserStats()
    }

    fun awardXp(xp: Int): AwardResult {
        val currentStats = GamificationStorage.getUserStats()
        val newTotalXp = currentStats.xp + xp
        val newLevel = newTotalXp / 100

        val leveledUp = newLevel > currentStats.level

        val updatedStats = currentStats.copy(xp = newTotalXp, level = newLevel)
        GamificationStorage.saveUserStats(updatedStats)

        return AwardResult(
            leveledUp = leveledUp,
            newLevel = newLevel,
            newXp = newTotalXp
        )
    }

    fun checkInToday(): Reward? {
        val today = LocalDate.now().format(dateFormatter)
        if (userStats.lastCheckInDate != today) {
            userStats.dailyCheckInStreak = if (wasYesterday(userStats.lastCheckInDate)) {
                userStats.dailyCheckInStreak + 1
            } else 1
            userStats.lastCheckInDate = today
            addXP(10)
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
            addXP(15)
            return Reward.XP(15)
        }
        return null
    }

    fun addSavings(amount: Double): Reward? {
        userStats.totalSavings += amount
        val rewardXp = (amount * 0.5).toInt()
        addXP(rewardXp)
        return Reward.XP(rewardXp)
    }

    private fun addXP(xp: Int) {
        userStats.xp += xp
        userStats.level = userStats.xp / 100
        GamificationStorage.saveUserStats(userStats)
    }

    private fun wasYesterday(dateStr: String?): Boolean {
        if (dateStr == null) return false
        val yesterday = LocalDate.now().minusDays(1).format(dateFormatter)
        return dateStr == yesterday
    }

    data class AwardResult(
        val leveledUp: Boolean,
        val newLevel: Int,
        val newXp: Int
    )
}
