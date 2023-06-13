package com.androiddevs.runningappyt.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.androiddevs.runningappyt.data.db.converters.Converters
import com.androiddevs.runningappyt.data.db.models.Run

@Database(
    entities = [Run::class],
    version = 1
)
@TypeConverters(Converters::class) // our converters we made
abstract class RunningDatabase : RoomDatabase() {
    abstract fun getRunDao(): RunDAO
}