package com.example.goodbudget

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.goodbudget.gamification.BadgeManager
import kotlinx.coroutines.launch
import com.example.goodbudget.gamification.GamificationEngine
import com.example.goodbudget.gamification.GamificationStorage

class MainActivity : BaseActivity() {

    private lateinit var categoryNameInput: EditText
    private lateinit var categoryLimitInput: EditText
    private lateinit var addCategoryButton: Button
    private lateinit var receiptImagePreview: ImageView
    private lateinit var selectReceiptButton: Button
    private lateinit var purchaseCategorySpinner: Spinner
    private var selectedImageUri: Uri? = null

    private val db by lazy { AppDatabase.getDatabase(this) }

    val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri

            // 🔐 Persist permission so you can load the image later
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            receiptImagePreview.setImageURI(uri)
            receiptImagePreview.visibility = View.VISIBLE
        }
    }

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
        val incomeNameInput = findViewById<EditText>(R.id.incomeNameInput)
        val incomeAmountInput = findViewById<EditText>(R.id.incomeAmountInput)
        val addIncomeButton = findViewById<Button>(R.id.addIncomeButton)

        addIncomeButton.setOnClickListener {
            val incomeName = incomeNameInput.text.toString()
            val amountText = incomeAmountInput.text.toString()
            val incomeAmount = amountText.toDoubleOrNull()

            if (incomeAmount != null && incomeAmount > 0) {
                val userEmail = getSharedPreferences("USER_PREF", MODE_PRIVATE).getString("email", null)
                val income = Income(amount = incomeAmount, userEmail = userEmail ?: "")
                lifecycleScope.launch {
                    db.incomeDao().insertIncome(income)

                    // 🟢 Award XP for income
                    val result = GamificationEngine.awardXp(20) // 20 XP
                    val userStats = GamificationEngine.getUserStats()
                    val newBadges = BadgeManager.unlockBadges(userStats, db, userEmail ?: "")

                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Income added successfully!", Toast.LENGTH_SHORT).show()
                        incomeNameInput.text.clear()
                        incomeAmountInput.text.clear()
                        fetchBalance()

                        for (badge in newBadges) {
                            Toast.makeText(this@MainActivity, "\uD83C\uDFC5 Badge Unlocked: ${badge.title}!", Toast.LENGTH_LONG).show()
                        }

                        if (result.leveledUp) {
                            showLevelUpDialog(result.newLevel)
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Please enter a valid income amount", Toast.LENGTH_SHORT).show()
            }
        }

        // Purchase Views
        val purchaseNameInput = findViewById<EditText>(R.id.purchaseNameInput)
        val purchaseAmountInput = findViewById<EditText>(R.id.purchaseAmountInput)
        purchaseCategorySpinner = findViewById(R.id.purchaseCategoryNameSpinner)
        val purchaseDateInput = findViewById<EditText>(R.id.purchaseDateInput)
        receiptImagePreview = findViewById(R.id.receiptImagePreview)
        selectReceiptButton = findViewById(R.id.selectReceiptButton)
        val spendIncomeButton = findViewById<Button>(R.id.spendIncomeButton)

        selectReceiptButton.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        spendIncomeButton.setOnClickListener {
            val purchaseName = purchaseNameInput.text.toString().trim()
            val amountText = purchaseAmountInput.text.toString().trim()
            val selectedCategoryIndex = purchaseCategorySpinner.selectedItemPosition
            val categoryText = purchaseCategorySpinner.selectedItem?.toString()?.trim() ?: ""
            val purchaseDate = purchaseDateInput.text.toString().trim()
            val purchaseAmount = amountText.toDoubleOrNull()

            if (selectedCategoryIndex == 0 || categoryText == "Select a Category") {
                Toast.makeText(this, "Please select a valid category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (purchaseAmount != null && purchaseAmount > 0 &&
                categoryText.isNotEmpty() &&
                purchaseName.isNotEmpty() &&
                purchaseDate.matches(Regex("""\d{4}-\d{2}-\d{2}"""))
            ) {
                val userEmail = getSharedPreferences("USER_PREF", MODE_PRIVATE).getString("email", null) ?: ""

                lifecycleScope.launch {
                    val totalIncome = db.incomeDao().getTotalIncomeByUser(userEmail) ?: 0.0
                    val totalDebt = db.purchaseDao().getTotalDebtByUser(userEmail) ?: 0.0
                    val availableBalance = totalIncome - totalDebt

                    if (purchaseAmount <= availableBalance) {
                        val purchase = Purchase(
                            name = purchaseName,
                            amount = purchaseAmount,
                            category = categoryText,
                            date = purchaseDate,
                            userEmail = userEmail,
                            receiptImageUri = selectedImageUri?.toString()
                        )
                        db.purchaseDao().insertPurchase(purchase)

                        // 🟢 Award XP for purchase
                        val xpResult = GamificationEngine.awardXp(10) // 10 XP
                        if (selectedImageUri != null) {
                            GamificationEngine.incrementReceiptCount()
                        }

                        val updatedStats = GamificationEngine.getUserStats()
                        val newBadges = BadgeManager.unlockBadges(updatedStats, db, userEmail)

                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Purchase deducted successfully!", Toast.LENGTH_SHORT).show()
                            purchaseNameInput.text.clear()
                            purchaseAmountInput.text.clear()
                            purchaseDateInput.text.clear()
                            receiptImagePreview.setImageDrawable(null)
                            receiptImagePreview.visibility = View.GONE
                            selectedImageUri = null
                            fetchBalance()

                            for (badge in newBadges) {
                                Toast.makeText(this@MainActivity, "\uD83C\uDFC5 Badge Unlocked: ${badge.title}!", Toast.LENGTH_LONG).show()
                            }

                            if (xpResult.leveledUp) {
                                showLevelUpDialog(xpResult.newLevel)
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Not enough available income", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Please enter valid purchase details (Date format: yyyy-mm-dd)", Toast.LENGTH_SHORT).show()
            }
        }

        fetchBalance()
        populateCategorySpinner()
    }

    private fun fetchBalance() {
        val totalBalanceAmount = findViewById<TextView>(R.id.totalBalanceAmount)
        val userEmail = getSharedPreferences("USER_PREF", MODE_PRIVATE).getString("email", null)

        if (userEmail != null) {
            lifecycleScope.launch {
                val totalIncome = db.incomeDao().getTotalIncomeByUser(userEmail) ?: 0.0
                val totalDebt = db.purchaseDao().getTotalDebtByUser(userEmail) ?: 0.0
                val totalBalance = totalIncome - totalDebt

                runOnUiThread {
                    totalBalanceAmount.text = "R${"%.2f".format(totalBalance)}"
                }
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
                populateCategorySpinner()

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

        messageView?.text = "\uD83C\uDF89 Level Up!"
        levelView?.text = "You reached Level $newLevel"
        imageView?.setImageResource(R.drawable.fluent_emoji_flat_trophy)

        dialog.show()
    }

    private fun populateCategorySpinner() {
        val userEmail = getSharedPreferences("USER_PREF", MODE_PRIVATE).getString("email", null)

        if (userEmail != null) {
            lifecycleScope.launch {
                val categories = db.categoryDao().getCategoriesByUser(userEmail)

                runOnUiThread {
                    if (categories.isEmpty()) {
                        Toast.makeText(this@MainActivity, "No categories yet. Add one first.", Toast.LENGTH_SHORT).show()
                        val defaultList = listOf("Select a Category")
                        val defaultAdapter = ArrayAdapter(
                            this@MainActivity,
                            android.R.layout.simple_spinner_item,
                            defaultList
                        ).apply {
                            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        }
                        purchaseCategorySpinner.adapter = defaultAdapter
                    } else {
                        val categoryNames = mutableListOf("Select a Category")
                        categoryNames.addAll(categories.map { it.name })

                        val adapter = ArrayAdapter(
                            this@MainActivity,
                            R.layout.spinner_item,
                            categoryNames
                        ).apply {
                            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        }
                        purchaseCategorySpinner.adapter = adapter
                    }
                }
            }
        }
    }
}
