package com.example.goodbudget.gamification.models


data class Badge(
    val id: String,
    val title: String,
    val description: String,
    var isUnlocked: Boolean = false,
    val dateUnlocked: String? = null // Optional timestamp
)