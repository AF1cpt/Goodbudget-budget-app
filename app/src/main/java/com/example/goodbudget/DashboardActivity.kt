package com.example.goodbudget

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {

    private lateinit var greetingTextView: TextView
    private lateinit var spendingCategoriesLayout: LinearLayout
    private lateinit var netWorthTextView: TextView
    private lateinit var debtTextView: TextView
    private val db by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

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
        val userEmail = "user@example.com"  // Replace with dynamic email or get from shared preferences
        lifecycleScope.launch {
            val user = db.userDao().getUserByEmail(userEmail)
            runOnUiThread {
                greetingTextView.text = "Welcome, ${user?.name}"
            }
        }
    }

    private fun fetchCategoriesAndDisplay() {
        lifecycleScope.launch {
            val categories = db.categoryDao().getAllCategories()
            runOnUiThread {
                for (category in categories) {
                    val categoryView = createCategoryView(category)
                    spendingCategoriesLayout.addView(categoryView)
                }
            }
        }
    }

    private fun createCategoryView(category: Category): LinearLayout {
        val categoryLayout = LinearLayout(this)
        categoryLayout.orientation = LinearLayout.HORIZONTAL
        categoryLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val categoryName = TextView(this)
        categoryName.text = category.name
        categoryLayout.addView(categoryName)

        val progressBar = ProgressBar(this)
        progressBar.max = category.limit.toInt()
        progressBar.progress = (category.limit * 0.5).toInt() // Example: 50% of the limit as a default spent amount
        categoryLayout.addView(progressBar)

        val categoryLimit = TextView(this)
        categoryLimit.text = "R${"%.2f".format(category.limit)}"
        categoryLayout.addView(categoryLimit)

        return categoryLayout
    }

    private fun fetchIncomeAndPurchases() {
        lifecycleScope.launch {
            val totalIncome = db.incomeDao().getTotalIncome()  // Fetch total income
            val totalDebt = db.purchaseDao().getTotalDebt()  // Fetch total debt

            val totalBalance = totalIncome - totalDebt
            runOnUiThread {
                netWorthTextView.text = "R${"%.2f".format(totalBalance)}"
                debtTextView.text = "R${"%.2f".format(totalDebt)}"
            }
        }
    }
}
