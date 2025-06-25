package com.example.goodbudget

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.goodbudget.gamification.GamificationEngine
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.example.goodbudget.gamification.models.UserStats

class DashboardActivity : BaseActivity() {

    private lateinit var greetingTextView: TextView
    private lateinit var spendingCategoriesLayout: LinearLayout
    private lateinit var netWorthTextView: TextView
    private lateinit var debtTextView: TextView
    private lateinit var xpText: TextView
    private lateinit var xpProgressBar: ProgressBar
    private lateinit var levelTextView: TextView
    private val db by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        setupBottomNavigation(DashboardActivity::class.java)

        // Initialize UI elements
        xpText = findViewById(R.id.xpText)
        xpProgressBar = findViewById(R.id.xpProgressBar)
        levelTextView = findViewById(R.id.levelTextView)
        greetingTextView = findViewById(R.id.greetingTextView)
        spendingCategoriesLayout = findViewById(R.id.spendingCategoriesLayout)
        netWorthTextView = findViewById(R.id.netWorthTextView)
        debtTextView = findViewById(R.id.debtTextView)

        // Initial load
        refreshGamificationUI()
        fetchUserData()
        fetchCategoriesAndDisplay()
        fetchIncomeAndPurchases()
    }

    override fun onResume() {
        super.onResume()
        refreshGamificationUI() // 🔁 Refresh XP and level when returning to dashboard
    }

    private fun refreshGamificationUI() {
        val stats: UserStats = GamificationEngine.getUserStats()

        // Update level text
        levelTextView.text = "Level: ${stats.level}"

        // Update progress bar max and progress
        xpProgressBar.max = stats.requiredXp
        xpProgressBar.progress = stats.currentXp

        // Update the XP text (e.g., "45 / 100 XP")
        xpText.text = "${stats.currentXp} / ${stats.requiredXp} XP"

        // Show toast for each unlocked badge
        for (badge in stats.badgesUnlocked) {
            Toast.makeText(this, "🏅 Badge Unlocked: $badge", Toast.LENGTH_LONG).show()
        }
    }


    private fun fetchUserData() {
        val sharedPref = getSharedPreferences("USER_PREF", MODE_PRIVATE)
        val userEmail = sharedPref.getString("email", null)

        if (userEmail != null) {
            lifecycleScope.launch {
                val user = db.userDao().getUserByEmail(userEmail)
                runOnUiThread {
                    greetingTextView.text = "Welcome, ${user?.name ?: "User"}"
                }
            }
        } else {
            greetingTextView.text = "Welcome, User"
        }
    }

    private fun fetchIncomeAndPurchases() {
        val userEmail = getSharedPreferences("USER_PREF", MODE_PRIVATE).getString("email", null)

        if (userEmail != null) {
            lifecycleScope.launch {
                val totalIncome = db.incomeDao().getTotalIncomeByUser(userEmail) ?: 0.0
                val totalDebt = db.purchaseDao().getTotalDebtByUser(userEmail) ?: 0.0
                val totalBalance = totalIncome - totalDebt

                runOnUiThread {
                    netWorthTextView.text = "R${"%.2f".format(totalIncome)}"
                    debtTextView.text = "R${"%.2f".format(totalDebt)}"
                    val balanceTextView = findViewById<TextView>(R.id.totalBalanceAmount)
                    balanceTextView?.text = "R${"%.2f".format(totalBalance)}"
                }
            }
        }
    }

    private fun fetchCategoriesAndDisplay() {
        val userEmail = getSharedPreferences("USER_PREF", MODE_PRIVATE).getString("email", null)

        if (userEmail != null) {
            lifecycleScope.launch {
                val categories = db.categoryDao().getCategoriesByUser(userEmail)
                val purchases = db.purchaseDao().getPurchasesByUser(userEmail)

                runOnUiThread {
                    spendingCategoriesLayout.removeAllViews()
                    for (category in categories) {
                        val totalSpent = purchases
                            .filter { it.category.equals(category.name, ignoreCase = true) }
                            .sumOf { it.amount }

                        val categoryView =
                            createCategoryView(category.name, totalSpent, category.limit)
                        spendingCategoriesLayout.addView(categoryView)
                    }
                }
            }
        }
    }

    private fun createCategoryView(
        categoryName: String,
        spent: Double,
        limit: Double
    ): LinearLayout {
        val context = this

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 24)
            }
            setPadding(8, 8, 8, 8)
        }

        val nameTextView = TextView(context).apply {
            text = categoryName
            setTextColor(android.graphics.Color.DKGRAY)
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val percentage = ((spent / limit) * 100).toInt().coerceAtMost(100)

        val progressIndicator = CircularProgressIndicator(context).apply {
            isIndeterminate = false
            trackThickness = 12
            setProgressCompat(percentage, true)
            layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                setMargins(16, 0, 16, 0)
            }

            // Color selection logic
            val colors = listOf(
                android.graphics.Color.RED,
                android.graphics.Color.MAGENTA,
                android.graphics.Color.BLUE,
                android.graphics.Color.GREEN,
                android.graphics.Color.rgb(255, 165, 0) // Orange
            )
            val colorIndex = (categoryName.hashCode().absoluteValue) % colors.size
            setIndicatorColor(colors[colorIndex])
        }

        val spentTextView = TextView(context).apply {
            text = "R${"%.2f".format(spent)} / R${"%.2f".format(limit)}"
            setTextColor(android.graphics.Color.GRAY)
            textSize = 12f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        layout.addView(nameTextView)
        layout.addView(progressIndicator)
        layout.addView(spentTextView)

        return layout
    }
}
