package com.androiddevs.runningappyt.data.repositories

import com.androiddevs.runningappyt.data.db.RunDAO
import com.androiddevs.runningappyt.data.db.models.Run
import com.androiddevs.runningappyt.data.mappers.toRunDb
import com.androiddevs.runningappyt.data.mappers.toRunDomain
import com.androiddevs.runningappyt.domain.model.RunDomain
import com.androiddevs.runningappyt.domain.repositories.MainRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MainRepositoryImpl @Inject constructor(
    private val runDao: RunDAO
) : MainRepository {

    override suspend fun insertRun(run: RunDomain) = runDao.insertRun(run.toRunDb())

    override suspend fun deleteRun(run: RunDomain) = runDao.deleteRun(run.toRunDb())

    override fun getAllRunsSortedByDate(): Flow<List<RunDomain>> {
        return runDao.getAllRunsSortedByDate().map { container ->
            container.toDomainRunList()
        }
    }

    override fun getAllRunsSortedByDistance(): Flow<List<RunDomain>> {
        return runDao.getAllRunsSortedByDistance().map { container ->
            container.toDomainRunList()
        }
    }

    override fun getAllRunsSortedByTimeInMillis(): Flow<List<RunDomain>> {
        return runDao.getAllRunsSortedByTimeInMillis().map { container ->
            container.toDomainRunList()
        }
    }

    override fun getAllRunsSortedByAvgSpeed(): Flow<List<RunDomain>> {
        return runDao.getAllRunsSortedByAvgSpeed().map { container ->
            container.toDomainRunList()
        }
    }

    override fun getAllRunsSortedByCaloriesBurned(): Flow<List<RunDomain>> {
        return runDao.getAllRunsSortedByCaloriesBurned().map { container ->
            container.toDomainRunList()
        }
    }

    override fun getTotalAvgSpeed() = runDao.getTotalAvgSpeed()

    override fun getTotalDistance() = runDao.getTotalDistance()

    override fun getTotalCaloriesBurned() = runDao.getTotalCaloriesBurned()

    override fun getTotalTimeInMillis() = runDao.getTotalTimeInMillis()

    private fun List<Run>.toDomainRunList(): List<RunDomain> {
        return this.map { it.toRunDomain() }
    }
}