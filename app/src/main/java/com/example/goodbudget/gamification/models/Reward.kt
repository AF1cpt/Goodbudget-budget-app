package com.example.goodbudget.gamification.models

sealed class Reward {
    data class XP(val amount: Int) : Reward()
    data class Coin(val amount: Int) : Reward()
    object BadgeReward : Reward() // Trigger badge grant
}