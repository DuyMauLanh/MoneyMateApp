package com.example.moneymate.fragment

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneymate.R
import com.example.moneymate.adapter.ReportAdapter
import com.example.moneymate.databinding.FragmentReportBinding
import com.example.moneymate.model.ReportItem
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.text.SimpleDateFormat
import java.util.*

class ReportFragment : Fragment() {
    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private lateinit var reportAdapter: ReportAdapter

    // Sample data for demonstration
    private val sampleIncomeData = listOf(
        ReportItem("Salary", R.drawable.ic_salary, 6000000, 40),
        ReportItem("Freelance", R.drawable.ic_freelance, 3000000, 20),
        ReportItem("Investment", R.drawable.ic_investment, 4000000, 27),
        ReportItem("Gifts", R.drawable.ic_gift, 2000000, 13)
    )

    private val sampleExpenseData = listOf(
        ReportItem("Food", R.drawable.ic_food, 3000000, 30),
        ReportItem("Transport", R.drawable.ic_transport, 2000000, 20),
        ReportItem("Shopping", R.drawable.ic_shopping, 2500000, 25),
        ReportItem("Bills", R.drawable.ic_bills, 1500000, 15),
        ReportItem("Entertainment", R.drawable.ic_entertainment, 1000000, 10)
    )

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

        // Set initial date
        updateDateDisplay()

        // Set initial time selection
        binding.timeToggleGroup.check(binding.btnMonthly.id)

        // Set initial income/expense selection
        binding.toggleGroup.check(binding.btnIncome.id)
        updateToggleButtonColors(true)
    }

    private fun setupListeners() {
        // Income/Expense toggle
        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val isIncome = checkedId == binding.btnIncome.id
                updateToggleButtonColors(isIncome)
                updateData(isIncome)
            }
        }

        // Time period toggle
        binding.timeToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val isMonthly = checkedId == binding.btnMonthly.id
                updateDateDisplay()
                // TODO: Update data based on time period
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
            // TODO: Update data for new period
        }

        binding.btnNext.setOnClickListener {
            if (binding.btnMonthly.isChecked) {
                calendar.add(Calendar.MONTH, 1)
            } else {
                calendar.add(Calendar.YEAR, 1)
            }
            updateDateDisplay()
            // TODO: Update data for new period
        }
    }

    private fun updateData(isIncome: Boolean) {
        val data = if (isIncome) sampleIncomeData else sampleExpenseData
        val total = data.sumOf { it.amount }
        
        // Update total amount
        binding.tvTotalAmount.text = "Total: ${formatAmount(total)}đ"

        // Update pie chart
        setupPieChart(data)

        // Update list
        reportAdapter.submitList(data)
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