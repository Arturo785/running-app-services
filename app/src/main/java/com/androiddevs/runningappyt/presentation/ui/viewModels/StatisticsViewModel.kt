package com.androiddevs.runningappyt.presentation.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.androiddevs.runningappyt.domain.repositories.MainRepository
import com.androiddevs.runningappyt.domain.use_case.GetRunUseCases
import com.androiddevs.runningappyt.presentation.ui.states.StatisticsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val runUseCases: GetRunUseCases
) : ViewModel() {

    private val _staticsUiState = MutableStateFlow(StatisticsUiState())

    /**     will trigger the inner code in case any of the varargs changes,
    and will take the value of the last block in this case  StatisticsUiState*/
    /*  val statisticsState = combine(
          mainRepository.getTotalAvgSpeed(),
          mainRepository.getTotalDistance(),
          mainRepository.getTotalCaloriesBurned(),
          mainRepository.getTotalTimeInMillis(),
          _staticsUiState
      ) { avgSpeed, totalDistance, totalCalories, timeInMillis, uiState ->
          uiState.copy(
              totalAvgSpeed = avgSpeed,
              totalDistance = totalDistance,
              totalCaloriesBurned = totalCalories,
              totalTimeInMillis = timeInMillis,
          )
      }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _staticsUiState.value)*/


    /** The stateIn operator in Kotlin Flows is used to transform a Flow into a StateFlow.
    by specifying an initial value and a CoroutineScope.
    The initial value represents the starting value of the StateFlow,
    and the CoroutineScope is used to control the lifespan of the StateFlow.

    SharingStarted.WhileSubscribed
    will be executed as long there is a subscriber
    then the last subscribers stops listening will wait 5 more seconds, if no one resubscribes then gets cleaned
    helps in case of rotations and change of lifecycle

    The stateIn operator takes three parameters:

    The CoroutineScope (this) represents the scope in which the StateFlow will be created.
    The SharingStarted.Eagerly parameter specifies the sharing mode for the StateFlow. In this case, it eagerly shares the StateFlow to all collectors.
    The initial value 0 is the starting value of the StateFlow. */


    // Will try to use the useCases instead of the repository

    val statisticsState = combine(
        runUseCases.getTotalAvgSpeedUseCase(),
        runUseCases.getTotalDistanceUseCase(),
        runUseCases.getTotalCaloriesBurnedUseCase(),
        runUseCases.getTotalTimeInMillisUseCase(),
        _staticsUiState
    ) { avgSpeed, totalDistance, totalCalories, timeInMillis, uiState ->
        uiState.copy(
            totalAvgSpeed = avgSpeed,
            totalDistance = totalDistance,
            totalCaloriesBurned = totalCalories,
            totalTimeInMillis = timeInMillis,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _staticsUiState.value)
        .asLiveData()


}