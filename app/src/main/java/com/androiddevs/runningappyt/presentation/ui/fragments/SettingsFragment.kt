package com.androiddevs.runningappyt.presentation.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.androiddevs.runningappyt.databinding.FragmentSettingsBinding
import com.androiddevs.runningappyt.domain.constants.Constants.KEY_NAME
import com.androiddevs.runningappyt.domain.constants.Constants.KEY_WEIGHT
import com.androiddevs.runningappyt.presentation.ui.MainActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {


    // do not do it like this, this is just for sake of time and quickness
    // do it like this https://github.com/Arturo785/multimodule-new-version/blob/main/core/src/main/java/com/example/core/domain/preferences/Preferences.kt
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadFieldsFromSharedPref()


        binding.btnApplyChanges.setOnClickListener {
            val success = applyChangesToSharedPref()
            if (success) {
                Snackbar.make(view, "Saved changes", Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(view, "Please fill out all the fields", Snackbar.LENGTH_LONG).show()
            }
        }
    }


    // do not do it like this, this is just for sake of time and quickness
    // do it like this https://github.com/Arturo785/multimodule-new-version/blob/main/core/src/main/java/com/example/core/domain/preferences/Preferences.kt
    private fun loadFieldsFromSharedPref() {
        val name = sharedPreferences.getString(KEY_NAME, "")
        val weight = sharedPreferences.getFloat(KEY_WEIGHT, 80f)
        binding.etName.setText(name)
        binding.etWeight.setText(weight.toString())
    }


    // do not do it like this, this is just for sake of time and quickness
    // do it like this https://github.com/Arturo785/multimodule-new-version/blob/main/core/src/main/java/com/example/core/domain/preferences/Preferences.kt
    private fun applyChangesToSharedPref(): Boolean {
        val nameText = binding.etName.text.toString()
        val weightText = binding.etWeight.text.toString()
        if (nameText.isEmpty() || weightText.isEmpty()) {
            return false
        }
        sharedPreferences.edit()
            .putString(KEY_NAME, nameText)
            .putFloat(KEY_WEIGHT, weightText.toFloat())
            .apply()

        val toolbarText = "Let's go $nameText"
        (requireActivity() as MainActivity).binding.tvToolbarTitle.text = toolbarText
        return true
    }
}