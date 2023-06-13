package com.androiddevs.runningappyt.data.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.androiddevs.runningappyt.R
import com.androiddevs.runningappyt.data.other.TrackingUtility
import com.androiddevs.runningappyt.domain.constants.Constants.ACTION_PAUSE_SERVICE
import com.androiddevs.runningappyt.domain.constants.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.androiddevs.runningappyt.domain.constants.Constants.ACTION_START_OR_RESUME_SERVICE
import com.androiddevs.runningappyt.domain.constants.Constants.ACTION_STOP_SERVICE
import com.androiddevs.runningappyt.domain.constants.Constants.FASTEST_LOCATION_INTERVAL
import com.androiddevs.runningappyt.domain.constants.Constants.LOCATION_UPDATE_INTERVAL
import com.androiddevs.runningappyt.domain.constants.Constants.NOTIFICATION_CHANNEL_ID
import com.androiddevs.runningappyt.domain.constants.Constants.NOTIFICATION_CHANNEL_NAME
import com.androiddevs.runningappyt.domain.constants.Constants.NOTIFICATION_ID
import com.androiddevs.runningappyt.domain.constants.Constants.TIMER_UPDATE_INTERVAL
import com.androiddevs.runningappyt.presentation.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

/** normally is used service or intentService but in this case we use lifeCycle service
// because we use liveData data inside this service and need to observe it, and the
// lifeData observer always need a lifeCycle owner
// thanks to this we can attach this instance as lifeCycle owner*/
@AndroidEntryPoint // this service uses serviceModule and has it's own dependencies lifecycle
class TrackingService : LifecycleService() {
    // as every service needs to be registered in the manifest


    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var notificationManager: NotificationManager

    // our provider of locations from google
    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    // the one to be updated
    lateinit var curNotificationBuilder: NotificationCompat.Builder

    // our flag to control
    private var isFirstRun = true
    private var isKilled = false

    //this is for the notification to not update the notification to often
    private val timeRunInSeconds = MutableLiveData<Long>()

    /**
     * Variables for timeTracking
     * */
    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L


    companion object {
        // for the tracking fragment
        val timeRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
        // our typeAlias on top of the file
    }

    override fun onCreate() {
        super.onCreate()
        curNotificationBuilder = baseNotificationBuilder // comes from DI serviceModule
        postInitialValues()

        /*NOW PROVIDED IN THE SERVICEMODULE FILE*/
        // fusedLocationProviderClient = FusedLocationProviderClient(this)


        //this thanks to LifecycleService
        isTracking.observe(this) {
            if (!isKilled){
                updateLocationTracking(it)
                updateNotificationTrackingState(it) // this is the one that attaches the buttons to the notification
            }
        }

        // we update our notification based on the seconds observer
        // this updates the text on the notification when the observer is triggered
        timeRunInSeconds.observe(this) {
            if (!isKilled) {
                // we retrieve our currentBuilder
                val notification = curNotificationBuilder
                    .setContentText(TrackingUtility.getFormattedStopWatchTime(it * 1000L))
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        }
    }

    /**  this one will give us the location of the user
    is just an anonymous object, by itself does nothing, needs to be used in updateLocationTracking*/
    val locationCallback = object : LocationCallback() {

        // everytime gets triggered by fusedLocationProviderClient
        // the inner code will happen
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)

            if (isTracking.value == true) {
                result?.locations?.let { locations ->
                    // we receive a list of locations from  com.google.android.gms
                    // and save each of this locations in our last list of lists of polylines
                    for (location in locations) {
                        addPathPoint(location)
                        Timber.d("NEW LOCATION: ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }

    }

    // gets executed inside isTracking liveData observable
    // we already check for the permission in the if
    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermissions(this)) {
                // our request from google package
                val request = LocationRequest().apply {
                    // our constants on how fast and how often
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                // here we register our callback we did
                // contains the parameters of how often to search and our callback
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            // if we are not already tracking remove the callback to avoid storing more locations
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    // we communicate by intents
    // our communication gets sent in TrackingFragment.kt
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            // we manage the action with our constants and from our UI to give commands
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        Timber.d("Starting service...")
                        startOurForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Resuming service...")
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                    pauseService() // just post is tracking to false haha
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                    killService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }


    private fun startTimer() {
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true

        //needed like this in order to interact with UI objects
        serviceScope.launch(context = Dispatchers.Main) {
            while (isTracking.value == true) {
                // time difference between now and timeStarted
                lapTime = System.currentTimeMillis() - timeStarted
                // post the new lapTime
                timeRunInMillis.postValue(timeRun + lapTime)
                if (timeRunInMillis.value!! >= lastSecondTimestamp + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }


    // we make it a foreground service to allow the user know that there is  a service running
    // and also to avoid the android system to kill our service

    // needs a notification and a channel notification

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID, // notification ID always has to be 1 or greater but not 0
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }


    /*NOW PROVIDED IN THE SERVICEMODULE FILE*/
    //https://www.notion.so/Pending-intent-135ab88c50984116a04e65212af63fb3
    /**
    when using getActivity means that pending intent will open an activity when executed
    and as the notion explains will use our app permissions*/
    /*   private fun getMainActivityPendingIntent(): PendingIntent = PendingIntent.getActivity(
           *//* context = *//* this, // this thanks to LifecycleService
        *//* requestCode = *//*
        0, // not important here
        *//* intent = *//*
        Intent(this, MainActivity::class.java).also {
            it.action =
                ACTION_SHOW_TRACKING_FRAGMENT // we attach and action flag to tell we should navigate to our fragment
        },
        *//* flags = *//*
        FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT // to update the current activity if already exists, could also create a new one
    )*/

    /**
    By making a PendingIntent immutable, you guarantee that its intent data and extras cannot be
    altered or tampered with, even if the PendingIntent is passed to other components or stored for later use.
     * */

    /**
     *
     * It's worth noting that making a PendingIntent immutable is particularly useful when you want to share
     *  it with other components, such as passing it to a notification, starting an activity from a widget,
     *  or storing it for later use. It helps ensure the integrity and security of the PendingIntent and its associated intent data.
     *
     * */

    // we have access to the context because we are in a LifecycleService
    private fun startOurForegroundService() {
        //  triggers the mutable liveData which will trigger the observer that starts or stops
        // the fusedLocationProviderClient
        startTimer()
        isTracking.postValue(true)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        // oreo or greater we need a channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        /**
         * Now is provided in the serviceModule
         * */
        /*      // our notification that appears on the screen bar
              val notificationBuilder =
                  NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)// our ID greater than 0
                      .setAutoCancel(false) // can't be swiped
                      .setOngoing(true)
                      .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
                      .setContentTitle("Running App")
                      .setContentText("00:00:00") // data before update
                      .setContentIntent(getMainActivityPendingIntent()) // the pending intent to execute when clicked the notification*/

        //https://stackoverflow.com/questions/72664186/why-did-android-13-remove-the-foreground-service-notification


        // notification builder provided by injection
        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())
    }


    /**
     * This function takes care of updating the notification to show the current time and also attaches
     * the buttons to the notification
     * */

    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "Pause" else "Resume"

        // the last instruction of the block is the one assigned to the val
        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                // we change the action to command the service when the button is clicked in the notification
                action = ACTION_PAUSE_SERVICE
            }
            /**
            getActivity was used when the pending intent was needed to open the activity when clicked
            now we need to communicate with the service when the button is clicked but not open any thing or UI
            So that's why we use getService
            we have
            getActivity - for opening or communicating with activity
            getBroadcast - for opening or communicating with broadcastReceiver
            getService - for opening or communicating with service

             */
            PendingIntent.getService(
                this,
                1, // the request code needs to be different one from the other (we have 0 in the activity one)
                pauseIntent,
                FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
            )
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                // we change the action to command the service when the button is clicked in the notification
                action = ACTION_START_OR_RESUME_SERVICE
            }
            /**
            getActivity was used when the pending intent was needed to open the activity when clicked
            now we need to communicate with the service when the button is clicked but not open any thing or UI
            So that's why we use getService*/
            PendingIntent.getService(
                this,
                2, // the request code needs to be different one from the other (we have 0 in the activity one)
                resumeIntent,
                FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
            )
        }

        // our normal manager
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // we use our current notification builder injected
        // because we update the buttons in the notification every time we pause or resume we need to remove the
        // old action attached to the notification and set a new one
        // otherwise we stack buttons and buttons in the notification
        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(
                curNotificationBuilder,
                ArrayList<NotificationCompat.Action>()
            ) // deletes the old action
        }
        // adds the new action and creates a new notification with the same id which updates the old one
        curNotificationBuilder = baseNotificationBuilder
            .addAction(R.drawable.ic_pause_black_24dp, notificationActionText, pendingIntent)
        notificationManager.notify(NOTIFICATION_ID, curNotificationBuilder.build())
    }


    // gets a location, creates a latLng object
    // gets the list of list of polylines, takes the last list
    // adds the new latLong object to the last list and updates the live data
    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    private fun addEmptyPolyline() {
        // checks our list of polylines
        // if available creates a new empty list and adds the the list
        pathPoints.value?.apply {
            add(mutableListOf())
            pathPoints.postValue(this) // updates the value
        }
            ?: pathPoints.postValue(mutableListOf(mutableListOf())) // otherwise inits the list containing an empty list
    }

    private fun killService() {
        isKilled = true
        Timber.d("Kill service called")
        isFirstRun = true
        pauseService()
        postInitialValues()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onDestroy() {
        Timber.d("Serviced destroyed")
        super.onDestroy()
        serviceScope.cancel()
    }

}