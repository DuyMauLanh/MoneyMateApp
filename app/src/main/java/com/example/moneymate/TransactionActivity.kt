package com.example.moneymate

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymate.adapter.CategoryAdapter
import com.example.moneymate.adapter.IconAdapter
import com.example.moneymate.databinding.ActivityTransactionBinding
import com.example.moneymate.model.Category
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class TransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionBinding
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private lateinit var categoryAdapter: CategoryAdapter
    private var selectedIconResId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCategoryGrid()
        setupViews()
        setupListeners()
    }

    private fun setupCategoryGrid() {
        val categories = mutableListOf(
            Category("Salary", R.drawable.ic_salary),
            Category("Food", R.drawable.ic_food),
            Category("Transport", R.drawable.ic_transport),
            Category("Shopping", R.drawable.ic_shopping),
            Category("Bills", R.drawable.ic_bills),
            Category("Entertainment", R.drawable.ic_entertainment),
            Category("Health", R.drawable.ic_health),
            Category("Education", R.drawable.ic_education),
            Category("Other", R.drawable.ic_other)
        )

        categoryAdapter = CategoryAdapter(
            categories = categories,
            onCategoryClick = { category ->
                // Handle category selection
                Toast.makeText(this, "Selected: ${category.name}", Toast.LENGTH_SHORT).show()
            },
            onAddCategoryClick = {
                showAddCategoryDialog()
            }
        )

        binding.categoryRecyclerView.apply {
            layoutManager = GridLayoutManager(this@TransactionActivity, 3)
            adapter = categoryAdapter
        }
    }

    private fun setupViews() {
        // Set default date
        binding.etDate.setText(dateFormatter.format(calendar.time))

        // Set initial state
        binding.toggleGroup.check(binding.btnIncome.id)
        updateToggleButtonColors(true)
        updateSubmitButtonWithAnimation(true)
    }

    private fun setupListeners() {
        // Toggle group listener
        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val isIncome = checkedId == binding.btnIncome.id
                updateToggleButtonColors(isIncome)
                updateSubmitButtonWithAnimation(isIncome)
            }
        }

        // Date picker
        binding.etDate.setOnClickListener {
            showDatePicker()
        }

        // Submit button
        binding.btnSubmit.setOnClickListener {
            // TODO: Handle transaction submission
        }

        // Bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_add -> {
                    // Stay on current screen
                    true
                }
                R.id.navigation_report -> {
                    Toast.makeText(this, "Switching to Reports screen", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.navigation_calendar -> {
                    Toast.makeText(this, "Switching to Calendar screen", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.navigation_more -> {
                    Toast.makeText(this, "Switching to More screen", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun updateToggleButtonColors(isIncome: Boolean) {
        val incomeColor = ContextCompat.getColor(this, R.color.income_color)
        val expenseColor = ContextCompat.getColor(this, R.color.expense_color)
        val textColor = ContextCompat.getColor(this, R.color.text_primary)
        val dividerColor = ContextCompat.getColor(this, R.color.divider_color)

        binding.btnIncome.apply {
            setTextColor(if (isIncome) incomeColor else textColor)
            strokeColor = ColorStateList.valueOf(if (isIncome) incomeColor else dividerColor)
        }

        binding.btnExpense.apply {
            setTextColor(if (!isIncome) expenseColor else textColor)
            strokeColor = ColorStateList.valueOf(if (!isIncome) expenseColor else dividerColor)
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                binding.etDate.setText(dateFormatter.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateSubmitButtonWithAnimation(isIncome: Boolean) {
        val colorFrom = ContextCompat.getColor(
            this,
            if (isIncome) R.color.expense_color else R.color.income_color
        )
        val colorTo = ContextCompat.getColor(
            this,
            if (isIncome) R.color.income_color else R.color.expense_color
        )

        ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo).apply {
            duration = 300 // Animation duration in milliseconds
            addUpdateListener { animator ->
                binding.btnSubmit.setBackgroundColor(animator.animatedValue as Int)
            }
            start()
        }

        binding.btnSubmit.text = getString(
            if (isIncome) R.string.add_income else R.string.add_expense
        )
    }

    private fun showAddCategoryDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_add_category)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val nameInput: TextInputEditText = dialog.findViewById(R.id.categoryNameInput)
        val iconRecyclerView: RecyclerView = dialog.findViewById(R.id.iconRecyclerView)
        val addButton: MaterialButton = dialog.findViewById(R.id.addButton)

        // Additional icons for selection
        val icons = listOf(
            R.drawable.ic_salary,
            R.drawable.ic_food,
            R.drawable.ic_transport,
            R.drawable.ic_shopping,
            R.drawable.ic_bills,
            R.drawable.ic_entertainment,
            R.drawable.ic_health,
            R.drawable.ic_education,
            R.drawable.ic_investment,
            R.drawable.ic_gift,
            R.drawable.ic_travel,
            R.drawable.ic_pets,
            R.drawable.ic_coffee,
            R.drawable.ic_groceries,
            R.drawable.ic_fitness,
            R.drawable.ic_beauty,
            R.drawable.ic_electronics,
            R.drawable.ic_car,
            R.drawable.ic_home,
            R.drawable.ic_clothing,
            R.drawable.ic_other
        )

        iconRecyclerView.layoutManager = GridLayoutManager(this, 5)
        iconRecyclerView.adapter = IconAdapter(icons) { iconResId ->
            selectedIconResId = iconResId
        }

        addButton.setOnClickListener {
            val name = nameInput.text?.toString()
            if (name.isNullOrBlank()) {
                nameInput.error = "Please enter a category name"
                return@setOnClickListener
            }
            if (selectedIconResId == -1) {
                Toast.makeText(this, "Please select an icon", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            categoryAdapter.addCategory(Category(name, selectedIconResId))
            dialog.dismiss()
        }

        dialog.show()
    }
} 