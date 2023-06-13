package com.androiddevs.runningappyt.domain.use_case

import com.androiddevs.runningappyt.domain.repositories.MainRepository
import kotlinx.coroutines.flow.Flow

class GetTotalCaloriesBurnedUseCase(
    private val mainRepository: MainRepository
) {
    operator fun invoke(): Flow<Int> {
        return mainRepository.getTotalCaloriesBurned()
    }
}