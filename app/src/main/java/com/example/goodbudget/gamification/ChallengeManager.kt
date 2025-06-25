package com.example.goodbudget.gamification

import com.example.goodbudget.gamification.models.Challenge
import com.example.goodbudget.gamification.models.Reward

object ChallengeManager {

    private val activeChallenges = mutableListOf<Challenge>()

    init {
        // Weekly example
        activeChallenges.add(
            Challenge(
                id = "no_takeout",
                title = "No Takeout!",
                description = "Don't spend money on takeout for 5 days",
                targetCount = 5
            )
        )
    }

    fun progressChallenge(challengeId: String): Reward? {
        val challenge = activeChallenges.find { it.id == challengeId } ?: return null
        if (!challenge.isCompleted) {
            challenge.currentCount++
            if (challenge.currentCount >= challenge.targetCount) {
                challenge.isCompleted = true
                return Reward.XP(50)
            }
        }
        return null
    }

    fun getActiveChallenges(): List<Challenge> = activeChallenges
}