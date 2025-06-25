package com.example.goodbudget

import android.os.Bundle
import android.widget.EditText
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.goodbudget.gamification.GamificationEngine
import com.example.goodbudget.gamification.GamificationStorage

class MainActivity : BaseActivity() {

    private lateinit var categoryNameInput: EditText
    private lateinit var categoryLimitInput: EditText
    private lateinit var addCategoryButton: Button

    private val db by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GamificationEngine.initialize(applicationContext)
        setContentView(R.layout.activity_main)

        // Initialize gamification
        GamificationStorage.init(applicationContext)

        setupBottomNavigation(MainActivity::class.java)

        // Category Views
        categoryNameInput = findViewById(R.id.categoryNameInput)
        categoryLimitInput = findViewById(R.id.categoryLimitInput)
        addCategoryButton = findViewById(R.id.addCategoryButton)

        addCategoryButton.setOnClickListener {
            val categoryName = categoryNameInput.text.toString()
            val categoryLimit = categoryLimitInput.text.toString().toDoubleOrNull()

            if (categoryName.isNotEmpty() && categoryLimit != null && categoryLimit > 0) {
                addCategoryToDatabase(categoryName, categoryLimit)
            } else {
                Toast.makeText(this, "Please enter valid category details", Toast.LENGTH_SHORT).show()
            }
        }

        // Income Views
        val incomeAmountInput = findViewById<EditText>(R.id.incomeAmountInput)
        val addIncomeButton = findViewById<Button>(R.id.addIncomeButton)

        addIncomeButton.setOnClickListener {
            val amountText = incomeAmountInput.text.toString()
            val incomeAmount = amountText.toDoubleOrNull()

            if (incomeAmount != null && incomeAmount > 0) {
                val userEmail = getSharedPreferences("USER_PREF", MODE_PRIVATE).getString("email", null)
                val income = Income(amount = incomeAmount, userEmail = userEmail ?: "")
                lifecycleScope.launch {
                    db.incomeDao().insertIncome(income)
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Income added successfully!", Toast.LENGTH_SHORT).show()
                        incomeAmountInput.text.clear()
                    }
                }
            } else {
                Toast.makeText(this, "Please enter a valid income amount", Toast.LENGTH_SHORT).show()
            }
        }

        // Purchase Views
        val purchaseAmountInput = findViewById<EditText>(R.id.purchaseAmountInput)
        val purchaseCategoryInput = findViewById<EditText>(R.id.purchaseCategoryNameInput)
        val spendIncomeButton = findViewById<Button>(R.id.spendIncomeButton)

        spendIncomeButton.setOnClickListener {
            val amountText = purchaseAmountInput.text.toString()
            val categoryText = purchaseCategoryInput.text.toString()
            val purchaseAmount = amountText.toDoubleOrNull()

            if (purchaseAmount != null && purchaseAmount > 0 && categoryText.isNotEmpty()) {
                val userEmail = getSharedPreferences("USER_PREF", MODE_PRIVATE).getString("email", null)
                lifecycleScope.launch {
                    val totalIncome = db.incomeDao().getTotalIncomeByUser(userEmail ?: "") ?: 0.0
                    val totalDebt = db.purchaseDao().getTotalDebtByUser(userEmail ?: "") ?: 0.0
                    val availableBalance = totalIncome - totalDebt

                    if (purchaseAmount <= availableBalance) {
                        val purchase = Purchase(amount = purchaseAmount, category = categoryText, userEmail = userEmail ?: "")
                        db.purchaseDao().insertPurchase(purchase)
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Purchase deducted successfully!", Toast.LENGTH_SHORT).show()
                            purchaseAmountInput.text.clear()
                            purchaseCategoryInput.text.clear()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Not enough available income", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Please enter valid purchase details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addCategoryToDatabase(name: String, limit: Double) {
        val userEmail = getSharedPreferences("USER_PREF", MODE_PRIVATE).getString("email", null)
        val category = Category(name = name, limit = limit, userEmail = userEmail ?: "")

        lifecycleScope.launch {
            db.categoryDao().insertCategory(category)

            // Award XP for adding a category
            val result = GamificationEngine.awardXp(30) // 30 XP

            runOnUiThread {
                Toast.makeText(this@MainActivity, "Category Added!", Toast.LENGTH_SHORT).show()
                clearInputs()

                if (result.leveledUp) {
                    showLevelUpDialog(result.newLevel)
                }
            }
        }
    }

    private fun clearInputs() {
        categoryNameInput.text.clear()
        categoryLimitInput.text.clear()
    }

    private fun showLevelUpDialog(newLevel: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_celebration, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val messageView = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val levelView = dialogView.findViewById<TextView>(R.id.dialogLevel)
        val imageView = dialogView.findViewById<ImageView>(R.id.dialogImage)

        messageView?.text = "🎉 Level Up!"
        levelView?.text = "You reached Level $newLevel"
        imageView?.setImageResource(R.drawable.fluent_emoji_flat_trophy)

        dialog.show()
    }
}
