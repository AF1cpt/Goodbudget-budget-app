package com.example.goodbudget

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class BaseActivity : AppCompatActivity() {


    protected fun setupBottomNavigation(currentActivity: Class<*>) {
        val navView = findViewById<BottomNavigationView>(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    if (currentActivity != MainActivity::class.java) {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                    true
                }
                R.id.navigation_dashboard -> {
                    if (currentActivity != DashboardActivity::class.java) {
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    }
                    true
                }
                R.id.navigation_analytics -> {
                    if (currentActivity != AnalyticsActivity::class.java) {
                        startActivity(Intent(this, AnalyticsActivity::class.java))
                        finish()
                    }
                    true
                }
                else -> false
            }
        }
    }
}
