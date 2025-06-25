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

    fun initialize(context: Context) {
        AndroidThreeTen.init(context)
        GamificationStorage.init(context)
        userStats = GamificationStorage.loadStats()
    }

    fun getUserStats(): UserStats = userStats

    fun awardXp(xpToAdd: Int): AwardResult {
        val previousLevel = userStats.level
        val newTotalXp = userStats.xp + xpToAdd
        val newLevel = (newTotalXp / 100).coerceAtLeast(1)

        userStats.xp = newTotalXp
        userStats.level = newLevel

        val newlyUnlockedBadges = mutableListOf<String>()

        // 🥇 "Budget Beginner"
        if (newTotalXp > 0 && !"Budget Beginner".inBadges()) {
            userStats.badgesUnlocked.add("Budget Beginner")
            newlyUnlockedBadges.add("Budget Beginner")
        }

        val leveledUp = newLevel > previousLevel
        GamificationStorage.saveStats(userStats)

        return AwardResult(
            leveledUp = leveledUp,
            newLevel = newLevel,
            newXp = newTotalXp,
            newBadges = newlyUnlockedBadges
        )
    }

    fun logExpense(): AwardResult {
        val xpEarned = 10
        val result = awardXp(xpEarned)

        // 🧾 "First Spend"
        if (!"First Spend".inBadges()) {
            userStats.badgesUnlocked.add("First Spend")
            result.newBadges.add("First Spend")
            GamificationStorage.saveStats(userStats)
        }

        return result
    }

    fun incrementReceiptCount(): AwardResult? {
        return trackReceiptUpload()
    }

    private fun trackReceiptUpload(): AwardResult? {
        userStats.receiptUploadCount += 1

        var result: AwardResult? = null

        if (userStats.receiptUploadCount >= 10 && !"Proof Keeper".inBadges()) {
            userStats.badgesUnlocked.add("Proof Keeper")
            result = AwardResult(
                leveledUp = false,
                newLevel = userStats.level,
                newXp = userStats.xp,
                newBadges = mutableListOf("Proof Keeper")
            )
        }

        GamificationStorage.saveStats(userStats)
        return result
    }

    private fun String.inBadges(): Boolean {
        return userStats.badgesUnlocked.contains(this)
    }

    data class AwardResult(
        val leveledUp: Boolean,
        val newLevel: Int,
        val newXp: Int,
        val newBadges: MutableList<String> = mutableListOf()
    )
}
