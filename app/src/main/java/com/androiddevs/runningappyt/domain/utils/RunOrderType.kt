package com.androiddevs.runningappyt.domain.utils

sealed interface RunOrderType {
    object ByDate : RunOrderType
    object ByDistance : RunOrderType
    object ByTimeInMillis : RunOrderType
    object ByAvgSpeed : RunOrderType
    object ByCaloriesBurned : RunOrderType
}