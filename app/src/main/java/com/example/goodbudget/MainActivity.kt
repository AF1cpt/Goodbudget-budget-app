package com.example.goodbudget

import android.os.Bundle
import android.widget.EditText
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    private lateinit var categoryNameInput: EditText
    private lateinit var categoryLimitInput: EditText
    private lateinit var addCategoryButton: Button
    private lateinit var totalBalanceAmount: TextView

    private val db by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupBottomNavigation(MainActivity::class.java)

        // Category input views
        categoryNameInput = findViewById(R.id.categoryNameInput)
        categoryLimitInput = findViewById(R.id.categoryLimitInput)
        addCategoryButton = findViewById(R.id.addCategoryButton)
        totalBalanceAmount = findViewById(R.id.totalBalanceAmount)

        addCategoryButton.setOnClickListener {
            val categoryName = categoryNameInput.text.toString()
            val categoryLimit = categoryLimitInput.text.toString().toDoubleOrNull()

            if (categoryName.isNotEmpty() && categoryLimit != null && categoryLimit > 0) {
                addCategoryToDatabase(categoryName, categoryLimit)
            } else {
                Toast.makeText(this, "Please enter valid category details", Toast.LENGTH_SHORT).show()
            }
        }

        // Income input
        val incomeNameInput = findViewById<EditText>(R.id.incomeNameInput)
        val incomeAmountInput = findViewById<EditText>(R.id.incomeAmountInput)
        val addIncomeButton = findViewById<Button>(R.id.addIncomeButton)

        addIncomeButton.setOnClickListener {
            val incomeName = incomeNameInput.text.toString()
            val amountText = incomeAmountInput.text.toString()
            val incomeAmount = amountText.toDoubleOrNull()

            if (incomeAmount != null && incomeAmount > 0) {
                val income = Income(amount = incomeAmount)
                lifecycleScope.launch {
                    db.incomeDao().insertIncome(income)
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Income added successfully!", Toast.LENGTH_SHORT).show()
                        incomeNameInput.text.clear()
                        incomeAmountInput.text.clear()
                        fetchAndDisplayBalance()
                    }
                }
            } else {
                Toast.makeText(this, "Please enter a valid income amount", Toast.LENGTH_SHORT).show()
            }
        }

        // Purchase input
        val purchaseNameInput = findViewById<EditText>(R.id.purchaseNameInput)
        val purchaseAmountInput = findViewById<EditText>(R.id.purchaseAmountInput)
        val purchaseCategoryInput = findViewById<EditText>(R.id.purchaseCategoryNameInput)
        val spendIncomeButton = findViewById<Button>(R.id.spendIncomeButton)

        spendIncomeButton.setOnClickListener {
            val purchaseName = purchaseNameInput.text.toString()
            val amountText = purchaseAmountInput.text.toString()
            val categoryText = purchaseCategoryInput.text.toString()
            val purchaseAmount = amountText.toDoubleOrNull()

            if (purchaseAmount != null && purchaseAmount > 0 && categoryText.isNotEmpty()) {
                lifecycleScope.launch {
                    val totalIncome = db.incomeDao().getTotalIncome() ?: 0.0
                    val totalDebt = db.purchaseDao().getTotalDebt() ?: 0.0
                    val availableBalance = totalIncome - totalDebt

                    if (purchaseAmount <= availableBalance) {
                        val purchase = Purchase(amount = purchaseAmount, category = categoryText)
                        db.purchaseDao().insertPurchase(purchase)
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Purchase deducted successfully!", Toast.LENGTH_SHORT).show()
                            purchaseNameInput.text.clear()
                            purchaseAmountInput.text.clear()
                            purchaseCategoryInput.text.clear()
                            fetchAndDisplayBalance()
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

        // Load current balance
        fetchAndDisplayBalance()
    }

    private fun addCategoryToDatabase(name: String, limit: Double) {
        val category = Category(name = name, limit = limit)

        lifecycleScope.launch {
            db.categoryDao().insertCategory(category)
            runOnUiThread {
                Toast.makeText(this@MainActivity, "Category Added!", Toast.LENGTH_SHORT).show()
                clearInputs()
            }
        }
    }

    private fun clearInputs() {
        categoryNameInput.text.clear()
        categoryLimitInput.text.clear()
    }

    private fun fetchAndDisplayBalance() {
        lifecycleScope.launch {
            val totalIncome = db.incomeDao().getTotalIncome() ?: 0.0
            val totalDebt = db.purchaseDao().getTotalDebt() ?: 0.0
            val balance = totalIncome - totalDebt

            runOnUiThread {
                totalBalanceAmount.text = "R${"%.2f".format(balance)}"
            }
        }
    }
}