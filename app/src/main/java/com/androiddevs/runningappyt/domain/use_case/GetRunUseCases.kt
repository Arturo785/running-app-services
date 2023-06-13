package com.androiddevs.runningappyt.domain.use_case

data class GetRunUseCases(
    val deleteUseCase: DeleteUseCase,
    val getRunSortedByUseCase: GetRunSortedByUseCase,
    val getTotalAvgSpeedUseCase: GetTotalAvgSpeedUseCase,
    val getTotalCaloriesBurnedUseCase: GetTotalCaloriesBurnedUseCase,
    val getTotalDistanceUseCase: GetTotalDistanceUseCase,
    val getTotalTimeInMillisUseCase: GetTotalTimeInMillisUseCase,
    val insertRunUseCase: InsertRunUseCase
)