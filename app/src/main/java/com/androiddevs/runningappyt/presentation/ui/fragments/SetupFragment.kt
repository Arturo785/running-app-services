package com.androiddevs.runningappyt.presentation.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.androiddevs.runningappyt.R
import com.androiddevs.runningappyt.databinding.FragmentSetupBinding
import com.androiddevs.runningappyt.domain.constants.Constants.KEY_FIRST_TIME_TOGGLE
import com.androiddevs.runningappyt.domain.constants.Constants.KEY_NAME
import com.androiddevs.runningappyt.domain.constants.Constants.KEY_WEIGHT
import com.androiddevs.runningappyt.presentation.ui.MainActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment() {

    private lateinit var binding: FragmentSetupBinding


    // do not do it like this, this is just for sake of time and quickness
    // do it like this https://github.com/Arturo785/multimodule-new-version/blob/main/core/src/main/java/com/example/core/domain/preferences/Preferences.kt

    @Inject
    lateinit var sharedPref: SharedPreferences


    // do not do it like this, this is just for sake of time and quickness
    @set:Inject
    var isFirstAppOpen = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /**
         * @param destinationId The destination to pop up to, clearing all intervening destinations.
         * @param inclusive true to also pop the given destination from the back stack.
         */

        if (!isFirstAppOpen) {
            // we programmatically make the popUp to inclusive true
            // which means that when we click "back action" when in the nextFragment we will not
            // return to this one
            // in a nutshell does not put this fragment in the backstack or removes it right away
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment, true)
                .build()
            findNavController().navigate(
                R.id.action_setupFragment_to_runFragment,
                savedInstanceState,
                navOptions
            )
        }

        binding.apply {
            tvContinue.setOnClickListener {
                val success = writePersonalDataToSharedPref()
                if (success) {
                    findNavController().navigate(R.id.action_setupFragment_to_runFragment)
                } else {
                    Snackbar.make(
                        requireView(),
                        "Please enter all the fields",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }

    }

    // do not do it like this, this is just for sake of time and quickness
    // do it like this https://github.com/Arturo785/multimodule-new-version/blob/main/core/src/main/java/com/example/core/domain/preferences/Preferences.kt
    private fun writePersonalDataToSharedPref(): Boolean {
        binding.apply {
            val name = etName.text.toString()
            val weight = etWeight.text.toString()
            if (name.isEmpty() || weight.isEmpty()) {
                return false
            }

            sharedPref.edit()
                .putString(KEY_NAME, name)
                .putFloat(KEY_WEIGHT, weight.toFloat())
                .putBoolean(KEY_FIRST_TIME_TOGGLE, false)
                .apply()

            val toolbarText = "Let's go, $name!"

            (requireActivity() as MainActivity).binding.tvToolbarTitle.text = toolbarText
            return true
        }
    }

}