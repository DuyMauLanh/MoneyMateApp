package com.example.moneymate.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.moneymate.R
import com.example.moneymate.databinding.FragmentCalendarBinding
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
    private val transactions = mutableMapOf<String, List<Transaction>>()
    
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
        updateSelectedDateDisplay()
        loadSampleTransactions()
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
    
    private fun updateMonthYearDisplay() {
        val monthYear = "tháng ${currentDate.get(Calendar.MONTH) + 1} ${currentDate.get(Calendar.YEAR)}"
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
            val isSelected = isSameDay(selectedDate, currentDate.get(Calendar.YEAR), 
                                    currentDate.get(Calendar.MONTH), day)
            addDayToGrid(day.toString(), isSelected, params)
        }
        
        // Add empty cells for remaining days to complete 6 rows
        val totalCells = 6 * 7 // 6 rows, 7 days per row
        val remainingCells = totalCells - (offset + daysInMonth)
        for (i in 0 until remainingCells) {
            addDayToGrid("", false, params)
        }
    }
    
    private fun addDayToGrid(dayText: String, isSelected: Boolean, params: GridLayout.LayoutParams) {
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
                    selectedDate.set(currentDate.get(Calendar.YEAR), 
                                   currentDate.get(Calendar.MONTH), day)
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
    }
    
    private fun loadSampleTransactions() {
        // Sample transactions for demonstration
        val today = dateFormat.format(Calendar.getInstance().time)
        val yesterday = dateFormat.format(Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }.time)
        
        transactions[today] = listOf(
            Transaction("Ăn uống", 150000, "Tiền ăn trưa", false),
            Transaction("Lương", 5000000, "Lương tháng 1", true)
        )
        
        transactions[yesterday] = listOf(
            Transaction("Di chuyển", 50000, "Tiền taxi", false),
            Transaction("Thưởng", 1000000, "Thưởng cuối năm", true)
        )
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
        var monthlyIncome = 0
        var monthlyExpense = 0
        
        // Calculate monthly totals
        val currentMonth = "${currentDate.get(Calendar.MONTH) + 1}/${currentDate.get(Calendar.YEAR)}"
        
        transactions.forEach { (date, transactionList) ->
            if (date.endsWith(currentMonth)) {
                transactionList.forEach { transaction ->
                    if (transaction.isIncome) {
                        monthlyIncome += transaction.amount
                    } else {
                        monthlyExpense += transaction.amount
                    }
                }
            }
        }
        
        val monthlyTotal = monthlyIncome - monthlyExpense
        
        binding.tvMonthlyIncome.text = formatCurrency(monthlyIncome)
        binding.tvMonthlyExpense.text = formatCurrency(monthlyExpense)
        binding.tvMonthlyTotal.text = formatCurrency(monthlyTotal)
    }
    
    private fun addTransactionItem(transaction: Transaction) {
        val transactionView = layoutInflater.inflate(R.layout.item_transaction, binding.transactionsContainer, false)
        
        val iconView = transactionView.findViewById<ImageView>(R.id.ivTransactionIcon)
        val amountView = transactionView.findViewById<TextView>(R.id.tvTransactionAmount)
        val noteView = transactionView.findViewById<TextView>(R.id.tvTransactionNote)
        val detailsContainer = transactionView.findViewById<LinearLayout>(R.id.transactionDetails)
        val expandButton = transactionView.findViewById<ImageButton>(R.id.btnExpandTransaction)
        
        // Set transaction icon (in a real app, you would use actual icons)
        iconView.setImageResource(if (transaction.isIncome) R.drawable.ic_income else R.drawable.ic_expense)
        
        // Set amount with color
        amountView.text = formatCurrency(transaction.amount)
        amountView.setTextColor(ContextCompat.getColor(
            requireContext(),
            if (transaction.isIncome) R.color.income_color else R.color.expense_color
        ))
        
        // Set note with color
        noteView.text = transaction.note
        noteView.setTextColor(ContextCompat.getColor(
            requireContext(),
            if (transaction.isIncome) R.color.income_color else R.color.expense_color
        ))
        
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
        
        binding.transactionsContainer.addView(transactionView)
    }
    
    private fun formatCurrency(amount: Int): String {
        return String.format("%,d đ", amount)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    // Data class for transactions
    data class Transaction(
        val category: String,
        val amount: Int,
        val note: String,
        val isIncome: Boolean
    )
} 