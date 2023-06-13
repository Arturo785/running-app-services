package com.androiddevs.runningappyt.presentation.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.androiddevs.runningappyt.R
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.androiddevs.runningappyt.databinding.ActivityMainBinding
import com.androiddevs.runningappyt.domain.constants.Constants.ACTION_SHOW_TRACKING_FRAGMENT

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.findNavController()

        // in case the activity was destroyed but our service stills running
        // like if we close the app in our apps manager (as I usually do)
        navigateToTrackingFragmentIfNeeded(intent) // intent passed comes from onCreate instance


        binding.apply {
            setSupportActionBar(toolbar)
            bottomNavigationView.setupWithNavController(navController)

            // OVERRIDES THE DEFAULT BEHAVIOUR TO AVOID RECREATING FRAGMENTS ON RE-SELECT
            bottomNavigationView.setOnNavigationItemReselectedListener {
                /*NO-OP*/
            }

            // determines the visibility depending on the fragment option from the bottom_nav_menu.xml item
            // we don't want to show the bottom nav in all the fragments
            navController
                .addOnDestinationChangedListener { _, destination, _ ->
                    when (destination.id) {
                        R.id.settingsFragment, R.id.runFragment, R.id.statisticsFragment ->
                            bottomNavigationView.visibility = View.VISIBLE
                        else -> bottomNavigationView.visibility = View.GONE
                    }
                }
        }
    }


    // this is in case the pending intent gets executed but the activity has not been killed
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?) {
        // if the received intent contains the action attached means that
        // was the pendingIntent created to our notification clicked
        // and we navigate to see our current run
        if (intent?.action == ACTION_SHOW_TRACKING_FRAGMENT) {
            navController.navigate(R.id.action_global_trackingFragment)
        }
    }
}
