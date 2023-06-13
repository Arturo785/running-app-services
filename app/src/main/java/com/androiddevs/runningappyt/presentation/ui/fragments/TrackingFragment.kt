package com.androiddevs.runningappyt.presentation.ui.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isNotEmpty
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.androiddevs.runningappyt.R
import com.androiddevs.runningappyt.data.other.TrackingUtility
import com.androiddevs.runningappyt.data.services.Polyline
import com.androiddevs.runningappyt.data.services.TrackingService
import com.androiddevs.runningappyt.databinding.FragmentTrackingBinding
import com.androiddevs.runningappyt.domain.constants.Constants.ACTION_PAUSE_SERVICE
import com.androiddevs.runningappyt.domain.constants.Constants.ACTION_START_OR_RESUME_SERVICE
import com.androiddevs.runningappyt.domain.constants.Constants.ACTION_STOP_SERVICE
import com.androiddevs.runningappyt.domain.constants.Constants.CANCEL_TAG
import com.androiddevs.runningappyt.domain.constants.Constants.MAP_ZOOM
import com.androiddevs.runningappyt.domain.constants.Constants.POLYLINE_COLOR
import com.androiddevs.runningappyt.domain.constants.Constants.POLYLINE_WIDTH
import com.androiddevs.runningappyt.domain.model.RunDomain
import com.androiddevs.runningappyt.presentation.ui.viewModels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Math.round
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class TrackingFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: FragmentTrackingBinding

    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()

    private var map: GoogleMap? = null

    private var curTimeInMillis = 0L


    // do not do it like this, this is just for sake of time and quickness
    // do it like this https://github.com/Arturo785/multimodule-new-version/blob/main/core/src/main/java/com/example/core/domain/preferences/Preferences.kt

    @set:Inject
    var weight = 80f

    private var menu: Menu? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = FragmentTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            val cancelTrackingDialog =
                parentFragmentManager.findFragmentByTag(CANCEL_TAG) as CancelTrackingDialog?

            cancelTrackingDialog?.setYesListener {
                stopRun()
            }
        }

        binding.apply {
            mapView.onCreate(savedInstanceState)

            mapView.getMapAsync {
                map = it
                addAllPolylines() // in case rotation or recreation of fragment
            }

            btnToggleRun.setOnClickListener {
                // uses our fun to communicate with our service
                toggleRun()
            }

            btnFinishRun.setOnClickListener {
                if (pathPoints.isNotEmpty()) {
                    zoomToSeeWholeTrack()
                    endRunAndSaveToDb()
                }
            }
        }

        subscribeToObservers()
    }


    /**   // is able to observe from the service because they are "static"
     *    companion object {
    val isTracking = MutableLiveData<Boolean>()
    val pathPoints = MutableLiveData<Polylines>()
    // our typeAlias on top of the file
    }*/
    // companion object of the service*/
    private fun subscribeToObservers() {
        // also reacts to the mutableLive data inside the service
        TrackingService.isTracking.observe(viewLifecycleOwner) {
            updateTracking(it)
        }

        // reacts to every new emission of the polyLines
        TrackingService.pathPoints.observe(viewLifecycleOwner) {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        }

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner) {
            curTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(curTimeInMillis, true)
            binding.tvTimer.text = formattedTime
        }
    }

    // controls the sending of actions to our service through an intent in the inner code
    private fun toggleRun() {
        if (isTracking) {// our local boolean
            if (menu?.isNotEmpty() == true) {
                menu?.getItem(0)?.isVisible = true
            }
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    /**
    // used to send commands to our service in data layer
    // we communicate through intents
    // the action parameter comes from our constants file
    // returns an intent that is prepared to be started
    // by itself is just an intent, needs to be executed
    // gets executed in the also*/
    private fun sendCommandToService(action: String): Intent {
        // create an intent and populate the action field with our constant of the command
        return Intent(
            requireContext(),
            TrackingService::class.java
        ) // the service to which we want to communicate
            .also {
                it.action = action
                requireContext().startService(it) // triggers the intent
                /**   // does not really starts the service every time is called
                // instead delivers the intent to the service
                // sends our command to the service */
            }
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking

        binding.apply {
            if (!isTracking && curTimeInMillis > 0L) {
                btnToggleRun.text = "Start"
                btnFinishRun.visibility = View.VISIBLE
            } else if (isTracking) {
                btnToggleRun.text = "Stop"
                if (menu?.isNotEmpty() == true) {
                    menu?.getItem(0)?.isVisible = true
                }
                btnFinishRun.visibility = View.GONE
            }
        }
    }

    // this one only connects the last two polyLines
    private fun addLatestPolyline() {
        // if we have polylines and the last list of polyLines has more than 1
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2] // the penultimate
            val lastLatLng = pathPoints.last().last() // the last one

            // we connect our penultimate to the lastOne
            val polylineOptions = PolylineOptions()
                .color(Color.parseColor(POLYLINE_COLOR))
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)

            // paints the connection
            map?.addPolyline(polylineOptions)
        }
    }

    // this is used when the fragment gets recreated and all lines should be re-drawn
    private fun addAllPolylines() {
        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(Color.parseColor(POLYLINE_COLOR))
                .width(POLYLINE_WIDTH)
                .addAll(polyline)

            map?.addPolyline(polylineOptions)
        }
    }

    private fun moveCameraToUser() {
        // we must have at least one coordinate
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                // goes into the last coordinate of the last list
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints) {
            for (pos in polyline) {
                bounds.include(pos)
            }
        }

        // points to the whole run route to make the snapshot
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                binding.mapView.width,
                binding.mapView.height,
                (binding.mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun endRunAndSaveToDb() {
        // gives us the bitmap of the current map
        map?.snapshot { bmp ->

            // formulas to calculate distance based on the polyLines
            var distanceInMeters = 0
            for (polyline in pathPoints) {
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }

            // formulas for getting data
            val avgSpeed =
                round((distanceInMeters / 1000f) / (curTimeInMillis / 1000f / 60 / 60) * 10) / 10f
            val dateTimestamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()

            val run = RunDomain(
                img = bmp,
                timestamp = dateTimestamp,
                avgSpeedInKMH = avgSpeed,
                distanceInMeters = distanceInMeters,
                timeInMillis = curTimeInMillis,
                caloriesBurned = caloriesBurned
            )
            viewModel.insertRun(run)

            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run saved successfully",
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()
        }
    }


    // when we click our menu item for cancelling
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miCancelTracking -> {
                CancelTrackingDialog().apply {
                    setYesListener {
                        stopRun()
                    }
                }.show(parentFragmentManager, CANCEL_TAG)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * We create a dialog to show in case the user wants to cancel the run
     * the AlertDialogTheme is made by us in the styles.xml
     * */
    // was transformed into a DialogFragment in this package
    /* private fun showCancelTrackingDialog() {
         val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
             .setTitle("Cancel the Run?")
             .setMessage("Are you sure to cancel the current run and delete all its data?")
             .setIcon(R.drawable.ic_delete)
             .setPositiveButton("Yes") { _, _ ->
                 stopRun()
             }
             .setNegativeButton("No") { dialogInterface, _ ->
                 dialogInterface.cancel()
             }
             .create()

         dialog.show()
     }*/

    private fun stopRun() {
        sendCommandToService(ACTION_STOP_SERVICE) // the intent to stop our service and destroy it
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        // means that we have started a run
        if (curTimeInMillis > 0L && this.menu?.isNotEmpty() == true) {
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView?.onSaveInstanceState(outState)
    }

}