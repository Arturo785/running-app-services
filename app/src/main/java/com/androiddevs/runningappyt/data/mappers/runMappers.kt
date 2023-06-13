package com.androiddevs.runningappyt.data.mappers

import com.androiddevs.runningappyt.data.db.models.Run
import com.androiddevs.runningappyt.domain.model.RunDomain


fun Run.toRunDomain(): RunDomain {
    return RunDomain(
        id = this.id,
        img = this.img,
        timestamp = this.timestamp,
        avgSpeedInKMH = this.avgSpeedInKMH,
        distanceInMeters = this.distanceInMeters,
        timeInMillis = this.timeInMillis,
        caloriesBurned = this.caloriesBurned
    )
}

fun RunDomain.toRunDb(): Run {
    return Run(
        img = this.img,
        timestamp = this.timestamp,
        avgSpeedInKMH = this.avgSpeedInKMH,
        distanceInMeters = this.distanceInMeters,
        timeInMillis = this.timeInMillis,
        caloriesBurned = this.caloriesBurned,
        id = this.id,
    )
}