package com.androiddevs.runningappyt.presentation.ui.events

sealed interface SortedRunsEvent {
    object ByAvgSpeed : SortedRunsEvent
    object ByCaloriesBurned : SortedRunsEvent
    object ByDate : SortedRunsEvent
    object ByDistance : SortedRunsEvent
    object ByTimeInMillis : SortedRunsEvent
}