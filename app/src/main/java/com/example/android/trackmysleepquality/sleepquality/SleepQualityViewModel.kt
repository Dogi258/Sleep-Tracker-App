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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import kotlinx.coroutines.*

/**
 * ViewModel for SleepQualityFragment. It takes in the SleepNight key and the database DAO as
 * argumnets
 */
class SleepQualityViewModel(
        private val sleepNightKey: Long = 0L,
        val database: SleepDatabaseDao) : ViewModel() {

    // Create an instance of Job for this ViewModel
    private val viewModelJob = Job()

    // Define a UI Scope for the coroutines
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // navigateToSleepTracker LiveData will be observed by the fragment to trigger navigation
    private val _navigateToSleepTracker = MutableLiveData<Boolean?>()
    val navigateToSleepTracker: LiveData<Boolean?>
        get() = _navigateToSleepTracker


    // Resets the state of navigateToSleepTracker
    fun doneNavigating() {
        _navigateToSleepTracker.value = null
    }

    // Click handler for the smiley sleep quality images
    fun onSetSleepQuality(quality: Int) {
        // Start a coroutine in the UI Scope
        uiScope.launch {
            // Start a coroutine that will run in background thread
            withContext(Dispatchers.IO) {
                // Get the value of tonight  from the database by passing in the sleepNightKey
                // If the value is null, return from this coroutine
                val tonight = database.get(sleepNightKey) ?: return@withContext

                // Set the quality of tonight
                tonight.sleepQuality = quality

                // Update the database with tonight
                database.update(tonight)
            }
            // When we are done updating, trigger navigation by setting navigateToSleepTracker event
            // state to true
            _navigateToSleepTracker.value = true
        }
    }

    // Gets called when the ViewModel is destroyed
    override fun onCleared() {
        super.onCleared()
        // Cancels all coroutines if this ViewModel gets destroyed
        viewModelJob.cancel()
    }

}