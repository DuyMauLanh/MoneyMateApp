package com.example.moneymate.fragment

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.moneymate.R
import com.example.moneymate.database.AppDatabase
import com.example.moneymate.databinding.FragmentCalendarBinding
import com.example.moneymate.model.TransactionWithCategory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private var currentDate = Calendar.getInstance()
    private var selectedDate = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale("vi"))


    // Sample transaction data (in a real app, this would come from a database)
    private var transactions = mutableMapOf<String, List<TransactionWithCategory>>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCalendar()
        setupNavigationButtons()
        setupMonthYearPicker()
        updateSelectedDateDisplay()
        displayTransactionsForSelectedDate()
    }

    private fun setupCalendar() {
        updateMonthYearDisplay()
        populateCalendarGrid()
    }

    private fun setupNavigationButtons() {
        binding.btnPreviousMonth.setOnClickListener {
            currentDate.add(Calendar.MONTH, -1)
            updateMonthYearDisplay()
            populateCalendarGrid()
            updateMonthlySummary()
        }

        binding.btnNextMonth.setOnClickListener {
            currentDate.add(Calendar.MONTH, 1)
            updateMonthYearDisplay()
            populateCalendarGrid()
            updateMonthlySummary()
        }
    }

    private fun setupMonthYearPicker() {
        binding.tvMonthYear.setOnClickListener {
            showMonthYearPicker()
        }
    }

    private fun showMonthYearPicker() {
        val calendar = Calendar.getInstance().apply {
            time = currentDate.time
        }

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.SpinnerDatePickerTheme,
            { _, year, month, _ ->
                currentDate.set(Calendar.YEAR, year)
                currentDate.set(Calendar.MONTH, month)
                updateMonthYearDisplay()
                populateCalendarGrid()
                updateMonthlySummary()
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

    private fun updateMonthYearDisplay() {
        val monthYear =
            "tháng ${currentDate.get(Calendar.MONTH) + 1} ${currentDate.get(Calendar.YEAR)}"
        binding.tvMonthYear.text = monthYear
    }

    private fun populateCalendarGrid() {
        binding.calendarGrid.removeAllViews()

        // Get the first day of the month
        val calendar = Calendar.getInstance().apply {
            set(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), 1)
            firstDayOfWeek = Calendar.MONDAY
        }

        // Get the day of week for the first day (1 = Sunday, 2 = Monday, etc.)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        // Calculate offset (if firstDayOfWeek is Sunday(1), we want 6, if Monday(2) we want 0, etc)
        val offset = (firstDayOfWeek + 5) % 7

        // Get the number of days in the month
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Create layout parameters for day cells
        val params = GridLayout.LayoutParams().apply {
            width = 0
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            setMargins(4, 4, 4, 4)
        }

        // Add empty cells for days before the first day of the month
        for (i in 0 until offset) {
            addDayToGrid("", false, params)
        }

        // Add cells for each day of the month
        for (day in 1..daysInMonth) {
            val isSelected = isSameDay(
                selectedDate, currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH), day
            )
            addDayToGrid(day.toString(), isSelected, params)
        }

        // Add empty cells for remaining days to complete 6 rows
        val totalCells = 6 * 7 // 6 rows, 7 days per row
        val remainingCells = totalCells - (offset + daysInMonth)
        for (i in 0 until remainingCells) {
            addDayToGrid("", false, params)
        }
    }

    private fun addDayToGrid(
        dayText: String,
        isSelected: Boolean,
        params: GridLayout.LayoutParams
    ) {
        val dayView = TextView(requireContext()).apply {
            text = dayText
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(8, 8, 8, 8)

            // Set layout parameters
            layoutParams = GridLayout.LayoutParams(params).apply {
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            }

            if (isSelected) {
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_color))
                setTextColor(Color.WHITE)
            } else if (dayText.isNotEmpty()) {
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                background = null
            }

            if (dayText.isNotEmpty()) {
                setOnClickListener {
                    val day = dayText.toInt()
                    selectedDate.set(
                        currentDate.get(Calendar.YEAR),
                        currentDate.get(Calendar.MONTH), day
                    )
                    updateSelectedDateDisplay()
                    populateCalendarGrid()
                    displayTransactionsForSelectedDate()
                }
            }
        }

        binding.calendarGrid.addView(dayView)
    }

    private fun isSameDay(cal1: Calendar, year: Int, month: Int, day: Int): Boolean {
        return cal1.get(Calendar.YEAR) == year &&
                cal1.get(Calendar.MONTH) == month &&
                cal1.get(Calendar.DAY_OF_MONTH) == day
    }

    private fun updateSelectedDateDisplay() {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("vi"))
        binding.tvSelectedDate.text = dateFormat.format(selectedDate.time)
        loadSampleTransactions()
    }

    private fun loadSampleTransactions() {
        // Sample transactions for demonstration
        val formattedDate = dateFormat.format(selectedDate.time)

        val db = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java,
            "moneyapp.db"
        ).build()

        val sharedPref = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)
        lifecycleScope.launch {
            Log.d("Load", "Đang truy vấn ngày: $formattedDate")

            val transactionsForDate =
                db.transactionDao().getTransactionsWithCategoryByDate(formattedDate, userId)

            transactions[formattedDate] = transactionsForDate

            displayTransactionsForSelectedDate()

        }
    }

    private fun displayTransactionsForSelectedDate() {
        val dateKey = dateFormat.format(selectedDate.time)
        val dateTransactions = transactions[dateKey] ?: emptyList()

        binding.transactionsContainer.removeAllViews()

        if (dateTransactions.isEmpty()) {
            val noTransactionsText = TextView(requireContext()).apply {
                text = "Không có giao dịch nào cho ngày này"
                textSize = 16f
                gravity = android.view.Gravity.CENTER
                setPadding(16, 16, 16, 16)
            }
            binding.transactionsContainer.addView(noTransactionsText)
            return
        }

        // Update monthly summary
        updateMonthlySummary()

        // Add transaction items
        dateTransactions.forEach { transaction ->
            addTransactionItem(transaction)
        }
    }

    private fun updateMonthlySummary() {
        var monthlyIncome: Double = 0.0
        var monthlyExpense: Double = 0.0

        // Calculate monthly totals
        val currentMonth =
            "${currentDate.get(Calendar.MONTH) + 1}/${currentDate.get(Calendar.YEAR)}"



        transactions.forEach { (date, transactionList) ->
            if (date.endsWith(currentMonth)) {
                transactionList.forEach { transaction ->
                    if (transaction.category.type == "income") {
                        monthlyIncome += transaction.transaction.amount
                    } else {
                        monthlyExpense += transaction.transaction.amount
                    }
                }
            }
        }

        val monthlyTotal = monthlyIncome - monthlyExpense

        binding.tvMonthlyIncome.text = formatCurrency(monthlyIncome)
        binding.tvMonthlyExpense.text = formatCurrency(monthlyExpense)
        binding.tvMonthlyTotal.text = formatCurrency(monthlyTotal)
    }

    private fun addTransactionItem(transaction: TransactionWithCategory) {
        val transactionView =
            layoutInflater.inflate(R.layout.item_transaction, binding.transactionsContainer, false)

        val iconView = transactionView.findViewById<ImageView>(R.id.ivTransactionIcon)
        val amountView = transactionView.findViewById<TextView>(R.id.tvTransactionAmount)
        val categoryView = transactionView.findViewById<TextView>(R.id.tvTransactionCategory)
        val noteView = transactionView.findViewById<TextView>(R.id.tvTransactionNote)
        val detailsContainer = transactionView.findViewById<LinearLayout>(R.id.transactionDetails)
        val expandButton = transactionView.findViewById<ImageButton>(R.id.btnExpandTransaction)

        // Set category name
        categoryView.text = transaction.category.name

        // Set amount with color
        amountView.text = formatCurrency(transaction.transaction.amount)
        amountView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (transaction.category.type == "income") R.color.income_color else R.color.expense_color
            )
        )

        // Set note with color
        noteView.text = transaction.transaction.note
        noteView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (transaction.category.type == "income") R.color.income_color else R.color.expense_color
            )
        )

        // Set icon based on category's icon field
        transaction.category.icon?.let { iconResource ->
            iconView.setImageResource(iconResource)
        } ?: run {
            // Fallback icon if category doesn't have an icon
            iconView.setImageResource(
                if (transaction.category.type == "income") R.drawable.ic_income 
                else R.drawable.ic_expense
            )
        }

        // Setup expand/collapse functionality
        expandButton.setOnClickListener {
            if (detailsContainer.visibility == View.VISIBLE) {
                detailsContainer.visibility = View.GONE
                expandButton.setImageResource(R.drawable.ic_expand)
            } else {
                detailsContainer.visibility = View.VISIBLE
                expandButton.setImageResource(R.drawable.ic_collapse)
            }
        }

        // Add long press functionality
        var isLongPressed = false
        transactionView.setOnLongClickListener {
            if (!isLongPressed) {
                isLongPressed = true
                // Show delete confirmation dialog
                showDeleteConfirmationDialog(transaction) {
                    // Remove transaction from view
                    binding.transactionsContainer.removeView(transactionView)
                    // Update monthly summary
                    updateMonthlySummary()
                }
            }
            true
        }

        // Add touch listener for visual feedback
        transactionView.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    // Start scale animation
                    v.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(100)
                        .start()
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    // Reset scale animation
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                    isLongPressed = false
                }
            }
            false
        }

        binding.transactionsContainer.addView(transactionView)
    }

    private fun showDeleteConfirmationDialog(
        transaction: TransactionWithCategory,
        onDeleteConfirmed: () -> Unit
    ) {
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("Tùy chọn giao dịch")
            .setItems(arrayOf("Sửa", "Xóa", "Hủy")) { _, which ->
                when (which) {
                    0 -> { // Sửa
                        navigateToEdit(transaction)
                    }
                    1 -> { // Xóa
                        showDeleteConfirmation(transaction, onDeleteConfirmed)
                    }
                    2 -> { // Hủy
                        // Do nothing, dialog will dismiss automatically
                    }
                }
            }
            .create()

        dialog.show()
    }

    private fun showDeleteConfirmation(
        transaction: TransactionWithCategory,
        onDeleteConfirmed: () -> Unit
    ) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Xóa giao dịch")
            .setMessage("Bạn có chắc chắn muốn xóa giao dịch này?")
            .setPositiveButton("Xóa") { _, _ ->
                // Delete transaction from database
                val db = Room.databaseBuilder(
                    requireContext(),
                    AppDatabase::class.java,
                    "moneyapp.db"
                ).build()
                lifecycleScope.launch {
                    db.transactionDao().delete(transaction.transaction)
                    onDeleteConfirmed()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun navigateToEdit(transaction: TransactionWithCategory) {
        // Create a new instance of TransactionFragment
        val transactionFragment = TransactionFragment().apply {
            arguments = Bundle().apply {
                putBoolean("isEditing", true)
                putInt("transactionId", transaction.transaction.id)
                putInt("categoryId", transaction.transaction.category_id)
                putDouble("amount", transaction.transaction.amount)
                putString("note", transaction.transaction.note)
                putString("date", transaction.transaction.transaction_date)
                putString("type", transaction.category.type)
            }
        }

        // Replace current fragment with TransactionFragment
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, transactionFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun formatCurrency(amount: Double): String {
        val formatter = java.text.DecimalFormat("#,###")
        return formatter.format(amount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

} 