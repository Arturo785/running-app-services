package com.androiddevs.runningappyt.di

import com.androiddevs.runningappyt.data.db.RunDAO
import com.androiddevs.runningappyt.data.repositories.MainRepositoryImpl
import com.androiddevs.runningappyt.domain.repositories.MainRepository
import com.androiddevs.runningappyt.domain.use_case.DeleteUseCase
import com.androiddevs.runningappyt.domain.use_case.GetRunSortedByUseCase
import com.androiddevs.runningappyt.domain.use_case.GetRunUseCases
import com.androiddevs.runningappyt.domain.use_case.GetTotalAvgSpeedUseCase
import com.androiddevs.runningappyt.domain.use_case.GetTotalCaloriesBurnedUseCase
import com.androiddevs.runningappyt.domain.use_case.GetTotalDistanceUseCase
import com.androiddevs.runningappyt.domain.use_case.GetTotalTimeInMillisUseCase
import com.androiddevs.runningappyt.domain.use_case.InsertRunUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent


@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    @Provides
    fun provideMainRepository(dao: RunDAO): MainRepository {
        return MainRepositoryImpl(dao)
    }

    @Provides
    fun provideRunUseCases(mainRepository: MainRepository) = GetRunUseCases(
        deleteUseCase = DeleteUseCase(mainRepository),
        getRunSortedByUseCase = GetRunSortedByUseCase(mainRepository),
        getTotalAvgSpeedUseCase = GetTotalAvgSpeedUseCase(mainRepository),
        getTotalCaloriesBurnedUseCase = GetTotalCaloriesBurnedUseCase(mainRepository),
        getTotalDistanceUseCase = GetTotalDistanceUseCase(mainRepository),
        getTotalTimeInMillisUseCase = GetTotalTimeInMillisUseCase(mainRepository),
        insertRunUseCase = InsertRunUseCase(mainRepository)
    )
}