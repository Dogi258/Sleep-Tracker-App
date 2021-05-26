/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleepquality

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.SleepDatabase
import com.example.android.trackmysleepquality.databinding.FragmentSleepQualityBinding

/**
 * Fragment that displays a list of clickable icons,
 * each representing a sleep quality rating.
 * Once the user taps an icon, the quality is set in the current sleepNight
 * and the database is updated.
 */
class SleepQualityFragment : Fragment() {

    /**
     * Called when the Fragment is ready to display content to the screen.
     *
     * This function uses DataBindingUtil to inflate R.layout.fragment_sleep_quality.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentSleepQualityBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_sleep_quality, container, false)

        // Get the application from this activity
        val application = requireNotNull(this.activity).application

        // Get arguments from the arguments bundle
        val arguments = SleepQualityFragmentArgs.fromBundle(arguments!!)

        // Get the SleepData DAO
        val dataSource = SleepDatabase.getInstance(application).sleepDatabaseDao

        //Create an instance of the ViewModelFactory passing in the dataSource and sleepNightKey:
        val viewModelFactory = SleepQualityViewModelFactory(arguments.sleepNightKey, dataSource)

        //Get the SleepQualityViewModel reference
        val viewModel = ViewModelProvider(this, viewModelFactory)
                .get(SleepQualityViewModel::class.java)

        // Add the sleepQualityViewModel to the binding object
        binding.sleepQualityViewModel = viewModel


        // Observes the state of navigateToSleepTracker event variable in the ViewModel
        viewModel.navigateToSleepTracker.observe(this.viewLifecycleOwner, Observer {
            if (it == true) { // Observed state is true.

                // Navigate to the sleep tracker fragment
                this.findNavController().navigate(
                        SleepQualityFragmentDirections.actionSleepQualityFragmentToSleepTrackerFragment())

                // Reset the state of navigateToSleepTracker
                viewModel.doneNavigating()
            }
        })

        return binding.root
    }
}
