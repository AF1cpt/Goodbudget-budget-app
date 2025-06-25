package com.example.goodbudget.gamification.models

data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val targetCount: Int,
    var currentCount: Int = 0,
    var isCompleted: Boolean = false
)