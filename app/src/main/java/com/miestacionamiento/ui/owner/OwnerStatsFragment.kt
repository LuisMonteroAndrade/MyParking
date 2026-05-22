package com.miestacionamiento.ui.owner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.snackbar.Snackbar
import com.miestacionamiento.R
import com.miestacionamiento.data.model.OwnerStats
import com.miestacionamiento.databinding.FragmentOwnerStatsBinding

class OwnerStatsFragment : Fragment() {

    private var _binding: FragmentOwnerStatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OwnerStatsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOwnerStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        viewModel.loadStats()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.stats.observe(viewLifecycleOwner) { stats ->
            stats?.let { displayStats(it) }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show() }
        }
    }

    private fun displayStats(stats: OwnerStats) {
        binding.tvTotalRevenue.text = "$ ${stats.totalRevenue.toLong()}"
        binding.tvTotalBookings.text = stats.totalBookings.toString()
        binding.tvUniqueDrivers.text = stats.uniqueDrivers.toString()

        val hasData = stats.totalBookings > 0
        binding.layoutNoData.visibility = if (!hasData) View.VISIBLE else View.GONE

        setupLineChart(stats)
        setupBarChart(stats)
    }

    private fun setupLineChart(stats: OwnerStats) {
        val chart = binding.lineChartRevenue
        val labels = stats.monthLabels.toTypedArray()

        val entries = stats.monthlyRevenue.mapIndexed { i, v ->
            Entry(i.toFloat(), v)
        }

        val primaryColor = ContextCompat.getColor(requireContext(), R.color.primary)
        val dataSet = LineDataSet(entries, "Ingresos").apply {
            color = primaryColor
            setCircleColor(primaryColor)
            lineWidth = 2.5f
            circleRadius = 4f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawValues(false)
            setDrawFilled(true)
            fillColor = primaryColor
            fillAlpha = 30
        }

        chart.data = LineData(dataSet)
        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
            textColor = ContextCompat.getColor(requireContext(), R.color.on_surface_variant)
        }
        chart.axisRight.isEnabled = false
        chart.axisLeft.apply {
            textColor = ContextCompat.getColor(requireContext(), R.color.on_surface_variant)
            setDrawGridLines(true)
            gridColor = ContextCompat.getColor(requireContext(), R.color.outline_variant)
        }
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setTouchEnabled(true)
        chart.setPinchZoom(false)
        chart.animateX(800)
        chart.invalidate()
    }

    private fun setupBarChart(stats: OwnerStats) {
        val chart = binding.barChartBookings
        val labels = stats.monthLabels.toTypedArray()

        val entries = stats.monthlyBookings.mapIndexed { i, v ->
            BarEntry(i.toFloat(), v.toFloat())
        }

        val secondaryColor = ContextCompat.getColor(requireContext(), R.color.secondary)
        val dataSet = BarDataSet(entries, "Reservas").apply {
            color = secondaryColor
            setDrawValues(false)
        }

        chart.data = BarData(dataSet).apply { barWidth = 0.6f }
        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
            setCenterAxisLabels(false)
            textColor = ContextCompat.getColor(requireContext(), R.color.on_surface_variant)
        }
        chart.axisRight.isEnabled = false
        chart.axisLeft.apply {
            textColor = ContextCompat.getColor(requireContext(), R.color.on_surface_variant)
            setDrawGridLines(true)
            gridColor = ContextCompat.getColor(requireContext(), R.color.outline_variant)
            axisMinimum = 0f
        }
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setTouchEnabled(true)
        chart.setPinchZoom(false)
        chart.animateY(800)
        chart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
