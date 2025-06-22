package com.example.goodbudget.gamification

import android.content.Context
import android.content.SharedPreferences
import com.example.goodbudget.gamification.models.UserStats
import com.google.gson.Gson

object GamificationStorage {

    private const val PREF_NAME = "gamification_prefs"
    private const val STATS_KEY = "user_stats"

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveStats(stats: UserStats) {
        prefs.edit().putString(STATS_KEY, gson.toJson(stats)).apply()
    }

    fun loadStats(): UserStats {
        val json = prefs.getString(STATS_KEY, null)
        return if (json != null) gson.fromJson(json, UserStats::class.java) else UserStats()
    }
}