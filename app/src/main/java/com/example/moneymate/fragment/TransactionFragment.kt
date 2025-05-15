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
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneymate.R
import com.example.moneymate.adapter.CategoryAdapter
import com.example.moneymate.adapter.IconAdapter
import com.example.moneymate.database.DatabaseProvider
import com.example.moneymate.databinding.FragmentTransactionBinding
import com.example.moneymate.model.Category
import com.example.moneymate.model.Transaction
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
    private var selectedCategoryId: Int? = null

    // Add variables for editing mode
    private var isEditing = false
    private var editingTransactionId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionBinding.inflate(inflater, container, false)

        // Get editing information from arguments
        arguments?.let { args ->
            isEditing = args.getBoolean("isEditing", false)
            if (isEditing) {
                editingTransactionId = args.getInt("transactionId")
                selectedCategoryId = args.getInt("categoryId")

                // Pre-fill the form with transaction data
                binding.etAmount.setText(formatAmount(args.getDouble("amount")))
                binding.etNote.setText(args.getString("note", ""))
                binding.etDate.setText(args.getString("date", ""))

                // Set the correct toggle button based on transaction type
                val isIncome = args.getString("type") == "income"
                binding.toggleGroup.check(if (isIncome) binding.btnIncome.id else binding.btnExpense.id)

                // Update button text for editing mode
                binding.btnSubmit.text = "Cập nhật"
            }
        }

        return binding.root
    }

    private fun formatAmount(amount: Double): String {
        val formatter = java.text.DecimalFormat("#,###")
        return formatter.format(amount)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategoryGrid()
        setupViews()
        setupListeners()
    }

    @SuppressLint("SuspiciousIndentation")
    private fun setupCategoryGrid() {
        val db = DatabaseProvider.getInstance(requireContext())

        val sharedPref = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

//        val category = mutableListOf(
//            // Income categories
//            Category(0, userId, "Tiền lương", "income", R.drawable.ic_salary, true),
//            Category(0, userId, "Thưởng", "income", R.drawable.ic_gift, true),
//            Category(0, userId, "Lãi ngân hàng", "income", R.drawable.ic_investment, true),
//            Category(0, userId, "Bán hàng", "income", R.drawable.ic_selling, true),
//
//            // Expense categories
//            Category(0, userId, "Ăn uống", "expense", R.drawable.ic_food, true),
//            Category(0, userId, "Giải trí", "expense", R.drawable.ic_entertainment, true),
//            Category(0, userId, "Mua sắm", "expense", R.drawable.ic_shopping, true),
//            Category(0, userId, "Đi lại", "expense", R.drawable.ic_transport, true),
//            Category(0, userId, "Y tế", "expense", R.drawable.ic_health, true)
//        )
        val category = mutableListOf(
            Category(0, userId, "category_salary", "income", R.drawable.ic_salary, true),
            Category(0, userId, "category_bonus", "income", R.drawable.ic_gift, true),
            Category(0, userId, "category_interest", "income", R.drawable.ic_investment, true),
            Category(0, userId, "category_sales", "income", R.drawable.ic_selling, true),

            Category(0, userId, "category_food", "expense", R.drawable.ic_food, true),
            Category(0, userId, "category_entertainment", "expense", R.drawable.ic_entertainment, true),
            Category(0, userId, "category_shopping", "expense", R.drawable.ic_shopping, true),
            Category(0, userId, "category_transport", "expense", R.drawable.ic_transport, true),
            Category(0, userId, "category_health", "expense", R.drawable.ic_health, true)
        )


        // Insert vào DB trong coroutine
        lifecycleScope.launch {
            val existing = db.categoryDao().getCategoriesByUser(userId)
            if (existing.isEmpty()) {
                category.forEach { db.categoryDao().insert(it) }
            }

            val categories: MutableList<Category> =
                db.categoryDao().getCategoriesByUser(userId).toMutableList()
            categoryAdapter = CategoryAdapter(
                categories = categories,
                onCategoryClick = { category ->
                    selectedCategoryId = category.id
                },
                onAddCategoryClick = {
                    showAddCategoryDialog()
                },
                userId = userId,
                context = requireContext()
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

    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupListeners() {
        val etAmount: EditText = binding.etAmount
        val etNote: EditText = binding.etNote

        // Add TextWatcher for amount formatting
        etAmount.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s == null || isFormatting) return

                isFormatting = true
                try {
                    // Remove all non-digit characters
                    val value = s.toString().replace(Regex("[^0-9]"), "")

                    // If the string is not empty, format it
                    if (value.isNotEmpty()) {
                        val longval = value.toLong()
                        val formatter = java.text.DecimalFormat("#,###")
                        val formatted = formatter.format(longval)

                        // Only update if the formatted value is different
                        if (formatted != s.toString()) {
                            etAmount.setText(formatted)
                            etAmount.setSelection(formatted.length)
                        }
                    } else {
                        etAmount.setText("")
                    }
                } catch (e: Exception) {
                    // If there's any error, keep the original string
                    etAmount.setText(s.toString())
                    etAmount.setSelection(s.toString().length)
                } finally {
                    isFormatting = false
                }
            }
        })

        // Toggle group listener
        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val isIncome = checkedId == binding.btnIncome.id
                updateToggleButtonColors(isIncome)
                updateSubmitButtonWithAnimation(isIncome)
                categoryAdapter.updateType(if (isIncome) "income" else "expense")
            }
        }

        // Date picker
        binding.etDate.setOnClickListener {
            showDatePicker()
        }

        // Submit button
        binding.btnSubmit.setOnClickListener {
            val db = DatabaseProvider.getInstance(requireContext())

            val sharedPref =
                requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val userId = sharedPref.getInt("user_id", -1)

            val amountText = binding.etAmount.text.toString().replace(Regex("[^0-9]"), "")
            val note = binding.etNote.text.toString()
            val categoryId = selectedCategoryId

            if (amountText.isBlank() || categoryId == null) {
                Toast.makeText(
                    context,
                    "Vui lòng nhập số tiền và chọn danh mục",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(context, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val now = java.time.LocalDateTime.now()
            val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val createdAt = now.format(formatter)
            val dateOnly = binding.etDate.text.toString()

            lifecycleScope.launch {
                if (isEditing) {
                    // Update existing transaction
                    val transaction = Transaction(
                        id = editingTransactionId,
                        user_id = userId,
                        category_id = categoryId,
                        amount = amount,
                        note = if (note.isBlank()) null else note,
                        transaction_date = dateOnly,
                        created_at = createdAt
                    )
                    db.transactionDao().update(transaction)
                    Toast.makeText(context, "Đã cập nhật giao dịch", Toast.LENGTH_SHORT).show()
                } else {
                    // Create new transaction
                    val transaction = Transaction(
                        user_id = userId,
                        category_id = categoryId,
                        amount = amount,
                        note = if (note.isBlank()) null else note,
                        transaction_date = dateOnly,
                        created_at = createdAt
                    )
                    db.transactionDao().insert(transaction)
                    Toast.makeText(context, "Đã thêm giao dịch", Toast.LENGTH_SHORT).show()
                }

                // Clear form and go back if editing
                binding.etAmount.text.clear()
                binding.etNote.text.clear()
                if (isEditing) {
                    parentFragmentManager.popBackStack()
                }
            }
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

        // Update amount label text
        binding.tvAmountLabel.text =
            getString(if (isIncome) R.string.tien_thu else R.string.tien_chi)
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.SpinnerDatePickerTheme,
            { _, year, month, day ->
                calendar.set(year, month, day)
                binding.etDate.setText(dateFormatter.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.apply {
            calendarViewShown = false
            spinnersShown = true
        }
        datePickerDialog.show()
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

        val db = DatabaseProvider.getInstance(requireContext())

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
            val isIncome = binding.toggleGroup.checkedButtonId == binding.btnIncome.id
            val newCategory = Category(
                user_id = userId,
                labelKey = name,
                type = if (isIncome) "income" else "expense",
                icon = selectedIconResId,
                is_default = false
            )
            lifecycleScope.launch {
                val insertedId = db.categoryDao().insert(newCategory)
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