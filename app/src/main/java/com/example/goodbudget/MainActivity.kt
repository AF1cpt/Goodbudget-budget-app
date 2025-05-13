package com.example.goodbudget

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var categoryNameInput: EditText
    private lateinit var categoryLimitInput: EditText
    private lateinit var addCategoryButton: Button
    private val db by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        categoryNameInput = findViewById(R.id.categoryNameInput)
        categoryLimitInput = findViewById(R.id.categoryLimitInput)
        addCategoryButton = findViewById(R.id.addCategoryButton)

        addCategoryButton.setOnClickListener {
            val categoryName = categoryNameInput.text.toString()
            val categoryLimit = categoryLimitInput.text.toString().toDoubleOrNull()

            // Validate inputs
            if (categoryName.isNotEmpty() && categoryLimit != null && categoryLimit > 0) {
                // Add the new category to the database
                addCategoryToDatabase(categoryName, categoryLimit)
            } else {
                // Show an error message
                Toast.makeText(this, "Please enter valid category details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addCategoryToDatabase(name: String, limit: Double) {
        val category = Category(name = name, limit = limit)

        lifecycleScope.launch {
            // Insert the category into the database
            db.categoryDao().insertCategory(category)
            // Show a success message
            runOnUiThread {
                Toast.makeText(this@MainActivity, "Category Added!", Toast.LENGTH_SHORT).show()
                clearInputs()
            }
        }
    }

    private fun clearInputs() {
        // Clear the input fields after adding the category
        categoryNameInput.text.clear()
        categoryLimitInput.text.clear()
    }
}
