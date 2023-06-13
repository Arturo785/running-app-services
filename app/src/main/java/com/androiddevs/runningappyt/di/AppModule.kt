package com.androiddevs.runningappyt.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.androiddevs.runningappyt.data.db.RunDAO
import com.androiddevs.runningappyt.data.db.RunningDatabase
import com.androiddevs.runningappyt.domain.constants.Constants.KEY_FIRST_TIME_TOGGLE
import com.androiddevs.runningappyt.domain.constants.Constants.KEY_NAME
import com.androiddevs.runningappyt.domain.constants.Constants.KEY_WEIGHT
import com.androiddevs.runningappyt.domain.constants.Constants.RUNNING_DATABASE_NAME
import com.androiddevs.runningappyt.domain.constants.Constants.SHARED_PREFERENCES_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
The component determines when the dependencies will be created and destroyed**
The scope determines the number of instances at the same time (only one or many)**
 * */

// refer to this https://www.notion.so/Dagger-hilt-scopes-c41d6a443d964817ae4aa5ae7948d05e
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    //the provided objects in here will be available during the the lifecycle
// of the installIn component

    @Singleton
    @Provides
    fun provideRunningDatabase(
        @ApplicationContext app: Context
    ): RunningDatabase {
        return Room.databaseBuilder(
            app,
            RunningDatabase::class.java,
            RUNNING_DATABASE_NAME
        ).build()
    }

    @Singleton
    @Provides
    fun provideRunDao(db: RunningDatabase): RunDAO {
        return db.getRunDao()
    }

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext app: Context) =
        app.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideName(sharedPref: SharedPreferences) = sharedPref.getString(KEY_NAME, "") ?: ""

    @Singleton
    @Provides
    fun provideWeight(sharedPref: SharedPreferences) = sharedPref.getFloat(KEY_WEIGHT, 80f)

    @Singleton
    @Provides
    fun provideFirstTimeToggle(sharedPref: SharedPreferences) =
        sharedPref.getBoolean(KEY_FIRST_TIME_TOGGLE, true)
}
