package com.androiddevs.runningappyt.presentation.ui.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.androiddevs.runningappyt.R
import com.androiddevs.runningappyt.data.other.TrackingUtility
import com.androiddevs.runningappyt.databinding.FragmentRunBinding
import com.androiddevs.runningappyt.domain.constants.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.androiddevs.runningappyt.domain.constants.Constants.REQUEST_CODE_POST_NOTIFICATION
import com.androiddevs.runningappyt.domain.utils.RunOrderType
import com.androiddevs.runningappyt.presentation.ui.adapters.RunAdapter
import com.androiddevs.runningappyt.presentation.ui.viewModels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions


@AndroidEntryPoint
class RunFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private lateinit var binding: FragmentRunBinding

    private lateinit var runAdapter: RunAdapter


    private val viewModel: MainViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRunBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestPermissions()
        setupRecyclerView()
        setupListeners()

        binding.apply {
            fab.setOnClickListener {
                findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
            }


            spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}

                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    id: Long
                ) {
                    when (pos) {
                        0 -> viewModel.getRunsSorted(RunOrderType.ByDate)
                        1 -> viewModel.getRunsSorted(RunOrderType.ByTimeInMillis)
                        2 -> viewModel.getRunsSorted(RunOrderType.ByDistance)
                        3 -> viewModel.getRunsSorted(RunOrderType.ByAvgSpeed)
                        4 -> viewModel.getRunsSorted(RunOrderType.ByCaloriesBurned)
                    }
                }
            }
        }

    }

    private fun setupRecyclerView() {
        runAdapter = RunAdapter()
        binding.rvRuns.apply {
            adapter = runAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupListeners() {
        viewModel.runsSortedBy.observe(viewLifecycleOwner) {
            runAdapter.submitList(it)
        }
    }


    // goes in here because is the real screen that needs them
    private fun requestPermissions() {
        // al good
        if (TrackingUtility.hasLocationPermissions(requireContext())) {
            return
        }

        // //https://stackoverflow.com/questions/72664186/why-did-android-13-remove-the-foreground-service-notification
        // request the persmissions depending on the version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept this for notification showing up.",
                REQUEST_CODE_POST_NOTIFICATION,
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            )
        }
    }

    // this comes from EasyPermissions.PermissionCallbacks
    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        //do nothing
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            // a dialog to allow permissions in case permanently denied
            AppSettingsDialog.Builder(this).build().show()
        } else {
            // normal permissions requested
            requestPermissions()
        }
    }

    // this comes from android sdk
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}