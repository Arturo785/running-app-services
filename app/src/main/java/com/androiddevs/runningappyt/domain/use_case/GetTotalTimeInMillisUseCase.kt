package com.androiddevs.runningappyt.domain.use_case

import com.androiddevs.runningappyt.domain.repositories.MainRepository
import kotlinx.coroutines.flow.Flow


class GetTotalTimeInMillisUseCase(
    private val mainRepository: MainRepository
) {
    operator fun invoke(): Flow<Long> {
        return mainRepository.getTotalTimeInMillis()
    }
}