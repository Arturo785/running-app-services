package com.androiddevs.runningappyt.domain.model

import android.graphics.Bitmap

//4
//
//
//Overall using bitmap can be considered as the exceptional case and it is ok to use it in domain layer.
// we can also convert it into a byteArray if we need pure kotlin class

data class RunDomain(
    var id: Int? = null,
    var img: Bitmap? = null,
    var timestamp: Long = 0L,
    var avgSpeedInKMH: Float = 0f,
    var distanceInMeters: Int = 0,
    var timeInMillis: Long = 0L,
    var caloriesBurned: Int = 0
)