package com.androiddevs.runningappyt.presentation.ui.states


data class StatisticsUiState(
    val totalAvgSpeed: Float = 0f,
    val totalDistance: Int = 0,
    val totalCaloriesBurned: Int = 0,
    val totalTimeInMillis: Long = 0L
)