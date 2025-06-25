package com.example.goodbudget

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class AnalyticsActivity : BaseActivity() {

    private lateinit var db: AppDatabase
    private lateinit var spendingPurchasesLayout: LinearLayout
    private lateinit var dateRangeInput: EditText
    private lateinit var totalMonthExpenseView: TextView
    private lateinit var categoryBarChart: BarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        setupBottomNavigation(AnalyticsActivity::class.java)

        db = AppDatabase.getDatabase(this)
        spendingPurchasesLayout = findViewById(R.id.spendingPurchasesLayout)
        dateRangeInput = findViewById(R.id.dateRangeInput)
        totalMonthExpenseView = findViewById(R.id.totalMonthExpense)
        categoryBarChart = findViewById(R.id.categoryBarChart)

        val dateFilterButton = findViewById<Button>(R.id.datefilterButton)
        dateFilterButton.setOnClickListener {
            val monthInput = dateRangeInput.text.toString().trim()
            if (monthInput.matches(Regex("""\d{4}-\d{2}"""))) {
                fetchAndDisplayPurchases(monthInput)
                fetchMonthlyTotal(monthInput)
                drawCategoryBarChart(monthInput)
            } else {
                dateRangeInput.error = "Please use YYYY-MM format"
            }
        }
    }

    private fun fetchMonthlyTotal(month: String) {
        val userEmail = getSharedPreferences("USER_PREF", MODE_PRIVATE).getString("email", null)

        if (userEmail != null) {
            lifecycleScope.launch {
                val purchases = db.purchaseDao().getPurchasesByUserAndMonth(userEmail, month)
                val totalMonthExpense = purchases.sumOf { it.amount }

                runOnUiThread {
                    totalMonthExpenseView.text = "R${"%.2f".format(totalMonthExpense)}"
                }
            }
        }
    }

    private fun fetchAndDisplayPurchases(month: String) {
        val userEmail = getSharedPreferences("USER_PREF", MODE_PRIVATE).getString("email", null)

        if (userEmail != null) {
            lifecycleScope.launch {
                val purchases = db.purchaseDao().getPurchasesByUserAndMonth(userEmail, month)

                runOnUiThread {
                    spendingPurchasesLayout.removeAllViews()

                    if (purchases.isEmpty()) {
                        val noDataText = TextView(this@AnalyticsActivity).apply {
                            text = "No purchases found for $month"
                            textSize = 16f
                        }
                        spendingPurchasesLayout.addView(noDataText)
                    } else {
                        for (purchase in purchases) {
                            Log.d(
                                "AnalyticsActivity",
                                "Purchase: ${purchase.name} - ${purchase.amount} on ${purchase.date}"
                            )
                            val itemView = createPurchaseView(
                                purchaseName = purchase.name,
                                amount = purchase.amount,
                                date = purchase.date
                            )
                            spendingPurchasesLayout.addView(itemView)
                        }
                    }
                }
            }
        }
    }

    private fun createPurchaseView(purchaseName: String, amount: Double, date: String): LinearLayout {
        val context = this

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
            setPadding(16, 16, 16, 16)
            setBackgroundResource(android.R.drawable.dialog_holo_light_frame)
        }

        val nameTextView = TextView(context).apply {
            text = "Name: $purchaseName"
            textSize = 16f
            setTextColor(android.graphics.Color.BLACK)
        }

        val amountTextView = TextView(context).apply {
            text = "Amount: R${"%.2f".format(amount)}"
            textSize = 14f
            setTextColor(android.graphics.Color.BLACK)
        }

        val dateTextView = TextView(context).apply {
            text = "Date: $date"
            textSize = 14f
            setTextColor(android.graphics.Color.BLACK)
        }

        layout.addView(nameTextView)
        layout.addView(amountTextView)
        layout.addView(dateTextView)

        return layout
    }

    private fun drawCategoryBarChart(month: String) {
        val userEmail = getSharedPreferences("USER_PREF", MODE_PRIVATE).getString("email", null)

        if (userEmail != null) {
            lifecycleScope.launch {
                val categories = db.categoryDao().getCategoriesByUser(userEmail)
                val purchases = db.purchaseDao().getPurchasesByUserAndMonth(userEmail, month)

                // Group purchases by category
                val spendingByCategory = purchases.groupBy { it.category }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }

                val barEntriesSpent = ArrayList<BarEntry>()
                val barEntriesLimit = ArrayList<BarEntry>()
                val labels = ArrayList<String>()

                categories.forEachIndexed { index, category ->
                    val spent = spendingByCategory[category.name] ?: 0.0
                    val limit = category.limit

                    barEntriesSpent.add(BarEntry(index.toFloat(), spent.toFloat()))
                    barEntriesLimit.add(BarEntry(index.toFloat(), limit.toFloat()))
                    labels.add(category.name)
                }

                val spentDataSet = BarDataSet(barEntriesSpent, "Spent")
                spentDataSet.color = getColor(R.color.purple_500)

                val limitDataSet = BarDataSet(barEntriesLimit, "Limit")
                limitDataSet.color = getColor(R.color.teal_200)

                val data = BarData(spentDataSet, limitDataSet)
                data.barWidth = 0.4f

                categoryBarChart.data = data
                categoryBarChart.description.isEnabled = false

                // Set up grouped bars
                val groupSpace = 0.2f
                val barSpace = 0.05f

                categoryBarChart.xAxis.apply {
                    valueFormatter = IndexAxisValueFormatter(labels)
                    granularity = 1f
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                }

                categoryBarChart.axisLeft.axisMinimum = 0f
                categoryBarChart.axisRight.isEnabled = false
                categoryBarChart.setVisibleXRangeMaximum(10f)
                categoryBarChart.setFitBars(true)
                categoryBarChart.groupBars(0f, groupSpace, barSpace)
                categoryBarChart.invalidate()
            }
        }
    }
}
