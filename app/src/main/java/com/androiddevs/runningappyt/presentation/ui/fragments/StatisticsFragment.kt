package com.androiddevs.runningappyt.presentation.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.androiddevs.runningappyt.R
import com.androiddevs.runningappyt.data.other.TrackingUtility
import com.androiddevs.runningappyt.databinding.FragmentStatisticsBinding
import com.androiddevs.runningappyt.presentation.ui.viewModels.StatisticsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Math.round

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

    private val viewModel: StatisticsViewModel by viewModels()

    private lateinit var binding: FragmentStatisticsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        viewModel.statisticsState.observe(viewLifecycleOwner) { statisticsState ->
            binding.apply {
                val totalTimeRun =
                    TrackingUtility.getFormattedStopWatchTime(statisticsState.totalTimeInMillis)
                val km = statisticsState.totalDistance / 1000f
                val totalDistance = round(km * 10f) / 10f
                val totalDistanceString = "${totalDistance}km"
                val avgSpeed = round(statisticsState.totalAvgSpeed * 10f) / 10f
                val avgSpeedString = "${avgSpeed}km/h"
                val totalCalories = "${statisticsState.totalCaloriesBurned}kcal"


                tvTotalTime.text = totalTimeRun
                tvTotalDistance.text = totalDistanceString
                tvAverageSpeed.text = avgSpeedString
                tvTotalCalories.text = totalCalories
            }
        }
    }

}