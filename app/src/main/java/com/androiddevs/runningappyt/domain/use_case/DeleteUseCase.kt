package com.androiddevs.runningappyt.domain.use_case

import com.androiddevs.runningappyt.domain.model.RunDomain
import com.androiddevs.runningappyt.domain.repositories.MainRepository

class DeleteUseCase(
    private val mainRepository: MainRepository
) {
    suspend operator fun invoke(runToDelete: RunDomain) {
        mainRepository.deleteRun(runToDelete)
    }
}