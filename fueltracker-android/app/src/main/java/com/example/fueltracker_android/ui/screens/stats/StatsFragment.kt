package com.example.fueltracker_android.ui.screens.stats

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.fueltracker_android.R
import com.example.fueltracker_android.data.AppDependencies
import com.example.fueltracker_android.databinding.FragmentStatsBinding
import com.example.fueltracker_android.domain.model.Stats
import com.example.fueltracker_android.ui.UiState
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.animation.Easing
import kotlinx.coroutines.launch

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatsViewModel by viewModels {
        StatsViewModel.factory(AppDependencies.repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCharts()
        animateSummaryCards()
        observeViewModel()
    }

    private fun setupCharts() {
        binding.chartConsumption.apply {
            setBackgroundColor(Color.TRANSPARENT)
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(true)
            setScaleEnabled(false)
            xAxis.apply {
                textColor = Color.parseColor("#8892A4")
                textSize = 10f
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                setDrawAxisLine(false)
            }
            axisLeft.apply {
                textColor = Color.parseColor("#8892A4")
                textSize = 10f
                gridColor = Color.parseColor("#1E2435")
                setDrawAxisLine(false)
            }
            axisRight.isEnabled = false
        }

        binding.chartPrice.apply {
            setBackgroundColor(Color.TRANSPARENT)
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(false)
            setScaleEnabled(false)
            xAxis.apply {
                textColor = Color.parseColor("#8892A4")
                textSize = 10f
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                setDrawAxisLine(false)
                granularity = 1f
            }
            axisLeft.apply {
                textColor = Color.parseColor("#8892A4")
                textSize = 10f
                gridColor = Color.parseColor("#1E2435")
                setDrawAxisLine(false)
            }
            axisRight.isEnabled = false
        }

        binding.chartMonthly.apply {
            setBackgroundColor(Color.TRANSPARENT)
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(false)
            setScaleEnabled(false)
            xAxis.apply {
                textColor = Color.parseColor("#8892A4")
                textSize = 10f
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                setDrawAxisLine(false)
                granularity = 1f
            }
            axisLeft.apply {
                textColor = Color.parseColor("#8892A4")
                textSize = 10f
                gridColor = Color.parseColor("#1E2435")
                setDrawAxisLine(false)
            }
            axisRight.isEnabled = false
        }
    }

    private fun animateSummaryCards() {
        listOf(
            binding.cardAvgConsumption,
            binding.cardBestConsumption,
            binding.cardTotalSpent,
            binding.cardTotalLiters
        ).forEachIndexed { i, card ->
            card.alpha = 0f
            card.translationY = 30f * resources.displayMetrics.density
            card.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay(i * 80L)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.summary.collect { state ->
                if (state is UiState.Success) showSummary(state.data)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.trend.collect { state ->
                if (state is UiState.Success) showConsumptionChart(state.data)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.monthly.collect { state ->
                if (state is UiState.Success) {
                    showPriceChart(state.data)
                    showMonthlyChart(state.data)
                }
            }
        }
    }

    private fun showSummary(summary: Stats.Summary) {
        binding.tvTotalSpent.text  = "${summary.totalSpent.toInt()} ₽"
        binding.tvTotalLiters.text = "${summary.totalLiters} л"

        // avgConsumption — Double?, показываем "—" если данных ещё нет
        binding.tvAvgConsumption.text = summary.avgConsumption
            ?.let { "$it л/100" } ?: "— л/100"

        // bestConsumption — Double?
        binding.tvBestConsumption.text = summary.bestConsumption
            ?.let { "$it л/100" } ?: "— л/100"
    }

    private fun showConsumptionChart(trend: List<Stats.ConsumptionPoint>) {
        // Фильтруем точки без расхода (первая заправка всегда null)
        val validPoints = trend.filter { it.consumption != null }
        if (validPoints.isEmpty()) return

        val entries = validPoints.mapIndexed { i, point ->
            Entry(i.toFloat(), point.consumption!!.toFloat())
        }

        val dataSet = LineDataSet(entries, "").apply {
            color = Color.parseColor("#4FC3F7")
            lineWidth = 2f
            setCircleColor(Color.parseColor("#4FC3F7"))
            circleRadius = 3f
            setDrawCircleHole(false)
            setDrawValues(false)
            setDrawFilled(true)
            fillDrawable = ContextCompat.getDrawable(
                requireContext(), R.drawable.bg_chart_line_fill
            )
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        binding.chartConsumption.apply {
            data = LineData(dataSet)
            animateX(1000, Easing.EaseInOutCubic)
        }
    }

    private fun showPriceChart(monthly: List<Stats.MonthlyData>) {
        if (monthly.isEmpty()) return

        val entries = monthly.mapIndexed { i, m ->
            BarEntry(i.toFloat(), m.avgPrice.toFloat())
        }
        val labels = monthly.map { it.month }

        val dataSet = BarDataSet(entries, "").apply {
            color = Color.parseColor("#4FC3F7")
            setDrawValues(false)
        }

        binding.chartPrice.apply {
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.labelCount = labels.size
            data = BarData(dataSet).apply { barWidth = 0.6f }
            animateY(800, Easing.EaseInOutQuart)
        }
    }

    private fun showMonthlyChart(monthly: List<Stats.MonthlyData>) {
        if (monthly.isEmpty()) return

        val entries = monthly.mapIndexed { i, m ->
            BarEntry(i.toFloat(), m.totalSpent.toFloat())
        }
        val labels = monthly.map { it.month }

        val dataSet = BarDataSet(entries, "").apply {
            color = Color.parseColor("#F5A623")
            setDrawValues(false)
        }

        binding.chartMonthly.apply {
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.labelCount = labels.size
            data = BarData(dataSet).apply { barWidth = 0.6f }
            animateY(1000, Easing.EaseInOutCubic)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}