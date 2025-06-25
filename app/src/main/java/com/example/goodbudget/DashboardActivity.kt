package com.example.goodbudget

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.goodbudget.gamification.GamificationEngine
import com.example.goodbudget.gamification.models.UserStats

class DashboardActivity : BaseActivity() {

    private lateinit var greetingTextView: TextView
    private lateinit var spendingCategoriesLayout: LinearLayout
    private lateinit var netWorthTextView: TextView
    private lateinit var debtTextView: TextView
    private val db by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        setupBottomNavigation(DashboardActivity::class.java)

        val xpProgressBar = findViewById<ProgressBar>(R.id.xpProgressBar)
        val levelTextView = findViewById<TextView>(R.id.levelTextView)
        val streakTextView = findViewById<TextView>(R.id.streakTextView)

        val stats: UserStats = GamificationEngine.getUserStats()

// Display level
        levelTextView.text = "Level: ${stats.level}"

// Display XP
        xpProgressBar.max = stats.requiredXp
        xpProgressBar.progress = stats.currentXp

// Display streak
        streakTextView.text = "Daily Streak: ${stats.dailyStreak} days"


        // Initialize views
        greetingTextView = findViewById(R.id.greetingTextView)
        spendingCategoriesLayout = findViewById(R.id.spendingCategoriesLayout)
        netWorthTextView = findViewById(R.id.netWorthTextView)
        debtTextView = findViewById(R.id.debtTextView)

        // Fetch user data to display their name
        fetchUserData()
        // Fetch and display categories
        fetchCategoriesAndDisplay()
        // Fetch and update total balance and debt
        fetchIncomeAndPurchases()
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
                setMargins(0, 0, 0, 16)
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

        val progressBar =
            ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
                max = limit.toInt()
                progress = spent.coerceAtMost(limit).toInt()
                layoutParams =
                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
            }

        val spentTextView = TextView(context).apply {
            text = "R${"%.2f".format(spent)} / R${"%.2f".format(limit)}"
            setTextColor(android.graphics.Color.GRAY)
            textSize = 12f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 0, 0, 0)
            }
        }

        layout.addView(nameTextView)
        layout.addView(progressBar)
        layout.addView(spentTextView)

        return layout
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
}
