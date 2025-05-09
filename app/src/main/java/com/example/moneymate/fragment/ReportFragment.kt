package com.example.moneymate.fragment

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.DatePickerDialog
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.moneymate.R
import com.example.moneymate.adapter.ReportAdapter
import com.example.moneymate.database.AppDatabase
import com.example.moneymate.databinding.FragmentReportBinding
import com.example.moneymate.model.ReportItem
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ReportFragment : Fragment() {
    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private lateinit var reportAdapter: ReportAdapter
    private var isIncome = true


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupListeners()
        updateData(true) // Show income data by default
    }

    private fun setupViews() {
        // Setup RecyclerView
        reportAdapter = ReportAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = reportAdapter
        }

        // Set initial time selection first
        binding.timeToggleGroup.check(binding.btnMonthly.id)
        
        // Set initial date display based on Monthly selection
        updateDateDisplay()

        // Set initial income/expense selection
        binding.toggleGroup.check(binding.btnIncome.id)
        updateToggleButtonColors(true)
    }

    private fun setupListeners() {
        // Income/Expense toggle
        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isIncome = checkedId == binding.btnIncome.id
                updateToggleButtonColors(isIncome)
                updateData(isIncome)
            }
        }

        // Time period toggle
        binding.timeToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                updateDateDisplay()
                updateData(isIncome)
            }
        }

        // Date navigation
        binding.btnPrevious.setOnClickListener {
            if (binding.btnMonthly.isChecked) {
                calendar.add(Calendar.MONTH, -1)
            } else {
                calendar.add(Calendar.YEAR, -1)
            }
            updateDateDisplay()
            updateData(isIncome)
        }

        binding.btnNext.setOnClickListener {
            if (binding.btnMonthly.isChecked) {
                calendar.add(Calendar.MONTH, 1)
            } else {
                calendar.add(Calendar.YEAR, 1)
            }
            updateDateDisplay()
            updateData(isIncome)
        }

        // Add date picker on click
        binding.tvCurrentPeriod.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.SpinnerDatePickerTheme,
            { _, year, month, _ ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                updateDateDisplay()
                updateData(isIncome)
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

    private fun updateData(isIncome: Boolean) {
        val db = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java,
            "moneyapp.db"
        ).build()

        val type = if (isIncome) "income" else "expense"
        val datePrefix = if (binding.btnMonthly.isChecked) {
            SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(calendar.time)
        } else {
            SimpleDateFormat("yyyy", Locale.getDefault()).format(calendar.time)
        }

        val sharedPref = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        Log.d("debug","$userId  $type $datePrefix")

        lifecycleScope.launch {
            val reportList = if (binding.btnMonthly.isChecked) {
                db.transactionDao()
                    .getMonthlyReport(userId, type, "%$datePrefix")

            } else {
                db.transactionDao()
                    .getYearlyReport(userId, type, "%$datePrefix")
            }

            if (reportList.isEmpty()) {
                Toast.makeText(requireContext(), "No data available", Toast.LENGTH_SHORT).show()
                binding.pieChart.clear()
                reportAdapter.submitList(emptyList())
                binding.tvTotalAmount.text = "Total: 0đ"
                return@launch
            }

            val total = reportList.sumOf { it.totalAmount }
            val reportItems = reportList.map {
                ReportItem(
                    name = it.categoryName,
                    iconResId = it.categoryIcon ?: 0, // fallback icon nếu null
                    amount = it.totalAmount.toInt(),
                    percentage = ((it.totalAmount * 100f) / total).toInt()
                )
            }

            binding.tvTotalAmount.text = "Total: ${formatAmount(total.toInt())}đ"
            setupPieChart(reportItems)
            reportAdapter.submitList(reportItems)
        }

        // Update total amount
//        binding.tvTotalAmount.text = "Total: ${formatAmount(total)}đ"
//
//        // Update pie chart
//        setupPieChart(data)
//
//        // Update list
//        reportAdapter.submitList(data)
    }

    private fun setupPieChart(data: List<ReportItem>) {
        val entries = data.map { PieEntry(it.percentage.toFloat(), it.name) }
        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                ContextCompat.getColor(requireContext(), R.color.chart_color_1),
                ContextCompat.getColor(requireContext(), R.color.chart_color_2),
                ContextCompat.getColor(requireContext(), R.color.chart_color_3),
                ContextCompat.getColor(requireContext(), R.color.chart_color_4),
                ContextCompat.getColor(requireContext(), R.color.chart_color_5)
            )
            valueTextSize = 12f
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
        }

        binding.pieChart.apply {
            setData(PieData(dataSet))
            description.isEnabled = false
            legend.isEnabled = false
            setHoleColor(ContextCompat.getColor(requireContext(), R.color.background_color))
            setTransparentCircleColor(ContextCompat.getColor(requireContext(), R.color.background_color))
            animateY(1000)
            invalidate()
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

    private fun updateDateDisplay() {
        val format = if (binding.btnMonthly.isChecked) "MMMM yyyy" else "yyyy"
        binding.tvCurrentPeriod.text = SimpleDateFormat(format, Locale.getDefault())
            .format(calendar.time)
    }

    private fun formatAmount(amount: Int): String {
        return String.format("%,d", amount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 