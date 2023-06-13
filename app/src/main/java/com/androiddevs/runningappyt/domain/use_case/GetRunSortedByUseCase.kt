package com.androiddevs.runningappyt.domain.use_case

import com.androiddevs.runningappyt.domain.model.RunDomain
import com.androiddevs.runningappyt.domain.repositories.MainRepository
import com.androiddevs.runningappyt.domain.utils.RunOrderType
import kotlinx.coroutines.flow.Flow

class GetRunSortedByUseCase(
    private val mainRepository: MainRepository
) {
    operator fun invoke(selectedSort: RunOrderType = RunOrderType.ByDate): Flow<List<RunDomain>> {
        return when (selectedSort) {
            RunOrderType.ByAvgSpeed -> mainRepository.getAllRunsSortedByAvgSpeed()
            RunOrderType.ByCaloriesBurned -> mainRepository.getAllRunsSortedByCaloriesBurned()
            RunOrderType.ByDate -> mainRepository.getAllRunsSortedByDate()
            RunOrderType.ByDistance -> mainRepository.getAllRunsSortedByDistance()
            RunOrderType.ByTimeInMillis -> mainRepository.getAllRunsSortedByTimeInMillis()
        }
    }
}