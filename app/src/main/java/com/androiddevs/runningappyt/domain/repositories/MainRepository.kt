package com.androiddevs.runningappyt.domain.repositories

import com.androiddevs.runningappyt.domain.model.RunDomain
import kotlinx.coroutines.flow.Flow

//If you’re using LiveData in layers other than the presentation layer (like in repositories),
// the answer is definitely Yes, switch to Flow.
// The reason is that LiveData isn’t built to handle asynchronous streams and data transformations.

interface MainRepository {

    suspend fun insertRun(run: RunDomain)

    suspend fun deleteRun(run: RunDomain)

    fun getAllRunsSortedByDate(): Flow<List<RunDomain>>

    fun getAllRunsSortedByDistance(): Flow<List<RunDomain>>

    fun getAllRunsSortedByTimeInMillis(): Flow<List<RunDomain>>

    fun getAllRunsSortedByAvgSpeed(): Flow<List<RunDomain>>

    fun getAllRunsSortedByCaloriesBurned(): Flow<List<RunDomain>>

    fun getTotalAvgSpeed(): Flow<Float>

    fun getTotalDistance(): Flow<Int>

    fun getTotalCaloriesBurned(): Flow<Int>

    fun getTotalTimeInMillis(): Flow<Long>
}