package com.example.moneymate.fragment

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.moneymate.R
import com.example.moneymate.adapter.CategoryAdapter
import com.example.moneymate.adapter.IconAdapter
import com.example.moneymate.database.AppDatabase
import com.example.moneymate.databinding.FragmentTransactionBinding
import com.example.moneymate.model.Category
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TransactionFragment : Fragment() {
    private var _binding: FragmentTransactionBinding? = null
    private val binding get() = _binding!!
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private lateinit var categoryAdapter: CategoryAdapter
    private var selectedIconResId: Int = -1


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategoryGrid()
        setupViews()
        setupListeners()
    }

    @SuppressLint("SuspiciousIndentation")
    private fun setupCategoryGrid() {
        val db = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java,
            "moneyapp.db"
        ).build()
        val sharedPref = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        val category = mutableListOf(
            Category(0, userId, "Salary", "income", R.drawable.ic_salary, true),
            Category(0, userId, "Food", "income", R.drawable.ic_food, true),
            Category(0, userId, "Transport", "income", R.drawable.ic_transport, true),
            Category(0, userId, "Shopping", "income", R.drawable.ic_shopping, true),
            Category(0, userId, "Bills", "income", R.drawable.ic_bills, true),
            Category(0, userId, "Entertainment", "income", R.drawable.ic_entertainment, true),
            Category(0, userId, "Health", "income", R.drawable.ic_health, true),
            Category(0, userId, "Education", "income", R.drawable.ic_education, true),
            Category(0, userId, "Other", "income", R.drawable.ic_other, true)
        )

        // Insert vào DB trong coroutine
        lifecycleScope.launch {
            val existing = db.categoryDao().getCategoriesByUser(userId)
            if (existing.isEmpty()) {
                category.forEach { db.categoryDao().insert(it) }
            }

            val categories: MutableList<Category> = db.categoryDao().getCategoriesByUser(userId).toMutableList()
            categoryAdapter = CategoryAdapter(
                categories = categories,
                onCategoryClick = { category ->
                    Toast.makeText(context, "Selected: ${category.name}", Toast.LENGTH_SHORT).show()
                },
                onAddCategoryClick = {
                    showAddCategoryDialog()
                }
            )
            binding.categoryRecyclerView.apply {
                layoutManager = GridLayoutManager(context, 3)
                adapter = categoryAdapter
            }
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
        val etAmount: EditText = binding.etAmount
        val etNote: EditText = binding.etNote
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
            etAmount.text.clear()  // Thiết lập giá trị của etAmount thành rỗng
            etNote.text.clear()    // Thiết lập giá trị của etNote thành rỗng

        }
    }

    private fun updateToggleButtonColors(isIncome: Boolean) {
        val incomeColor = ContextCompat.getColor(requireContext(), R.color.income_color)
        val expenseColor = ContextCompat.getColor(requireContext(), R.color.expense_color)
        val textColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
        val dividerColor = ContextCompat.getColor(requireContext(), R.color.divider_color)

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
            requireContext(),
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
            requireContext(),
            if (isIncome) R.color.expense_color else R.color.income_color
        )
        val colorTo = ContextCompat.getColor(
            requireContext(),
            if (isIncome) R.color.income_color else R.color.expense_color
        )

        ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo).apply {
            duration = 300
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
        val db = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java,
            "moneyapp.db"
        ).build()
        val sharedPref = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_add_category)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val nameInput: TextInputEditText = dialog.findViewById(R.id.categoryNameInput)
        val iconRecyclerView: RecyclerView = dialog.findViewById(R.id.iconRecyclerView)
        val addButton: MaterialButton = dialog.findViewById(R.id.addButton)

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

        iconRecyclerView.layoutManager = GridLayoutManager(context, 5)
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
                Toast.makeText(context, "Please select an icon", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val newCategory = Category(
                user_id = userId,       // truyền user_id hiện tại
                name = name,
                type = "income",           // "income" hoặc "expense"
                icon = selectedIconResId,       // ví dụ: "ic_food", "ic_gift"
                is_default = false
            )
            lifecycleScope.launch {
                val insertedId = db.categoryDao().insert(newCategory) // trả về id (Long)
                val newCategoryWithId = newCategory.copy(id = insertedId.toInt())
                categoryAdapter.addCategory(newCategoryWithId)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 