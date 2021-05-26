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

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import android.provider.SyncStateContract.Helpers.insert
import android.provider.SyncStateContract.Helpers.update
import androidx.lifecycle.*
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment. It takes in the SleepDatabaseDao and the application
 * context as parameters
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

    // Create an instance of Job for this ViewModel
    private var viewModelJob = Job()

    // Define a UI Scope for the coroutines
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // LiveData for the current SleepNight
    private var tonight = MutableLiveData<SleepNight?>()

    // nights will hold all the nights in the database
    private val nights = database.getAllNights()

    // This LiveData will be observed by the fragment to trigger navigation
    private val _navigateToSleepQuality = MutableLiveData<SleepNight>()
    val navigateToSleepQuality: LiveData<SleepNight>
        get() = _navigateToSleepQuality

    // Transforms nights into a nightsString using the formatNight() function from Util.kt
    val nightsString = Transformations.map(nights) { nights ->
        formatNights(nights, application.resources)
    }

    // Shows start button when tonight is null
    val startButtonVisible = Transformations.map(tonight) {
        null == it
    }

    // Shows stop button when tonight is not null
    val stopButtonVisible = Transformations.map(tonight) {
        null != it
    }

    // Shows clear button when there is data in the nights
    val clearButtonVisible = Transformations.map(nights) {
        it?.isNotEmpty()
    }

    // LiveData event for showing the snackbar when the data is cleared
    private var _showSnackbarEvent = MutableLiveData<Boolean>()
    val showSnackBarEvent: LiveData<Boolean>
        get() = _showSnackbarEvent

    // Rests the state of showSnackbarEvent
    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = false
    }

    // init block will run when the ViewModel gets created
    init {
        // Get the value for tonight upon creation
        initializeTonight()
    }

    // Resets the event navigateToSleepQuality
    fun doneNavigating() {
        _navigateToSleepQuality.value = null
    }

    // A Coroutine is used to get tonight from the database so that the UI does not get blocked
    // waiting for the result
    private fun initializeTonight() {
        uiScope.launch {
            tonight.value = getTonightFromDatabase()
        }
    }

    // Returns the most recent SleepNight from the database
    // Will get called within a coroutine so that we do not block UI
    private suspend fun getTonightFromDatabase(): SleepNight? {
        // IO Dispatcher is a coroutine will run on a separate thread
        return withContext(Dispatchers.IO) {
            // Get tonight from the database with the dao
            var night = database.getTonight()

            // If the start time and end time of tonight are different, return null because a new
            // night has not been started
            if (night?.endTimeMilli != night?.startTimeMilli) {
                night = null
            }
            // Return the night
            night
        }
    }

    // Click handler for the start button
    fun onStartTracking() {
        // Start a coroutine
        uiScope.launch {
            // Create a new SleepNight
            val newNight = SleepNight()

            // Insert this new SleepNight into the database
            insert(newNight)

            // Set tonight to the new SleepNight we inserted into the databse
            tonight.value = getTonightFromDatabase()
        }
    }

    // Inserts a new night into the database
    // Runs within a coroutine
    private suspend fun insert(night: SleepNight) {
        // launch a coroutine in the IO context
        withContext(Dispatchers.IO) {
            // Insert night into the database using the DAO
            database.insert(night)
        }
    }


    // Click handler for the stop button
    fun onStopTracking() {
        // Start a coroutine in the UI scope
        uiScope.launch {
            // If the value of oldNight is null, return from this coroutine
            val oldNight = tonight.value ?: return@launch

            // Set the value of end time to the current system time
            oldNight.endTimeMilli = System.currentTimeMillis()

            // Update the oldNight to the database
            update(oldNight)

            // Trigger navigation when we are done updating to the database
            _navigateToSleepQuality.value = oldNight
        }
    }

    // Updates a night from the database
    // Runs within a coroutine
    private suspend fun update(night: SleepNight) {
        // launch a coroutine in the IO context
        withContext(Dispatchers.IO) {
            // Update the SleepNight with the DAO
            database.update(night)
        }
    }

    // Click handler for the clear button
    fun onClear() {
        // Start a coroutine in the UI Scope
        uiScope.launch {
            // Clear the database
            clear()

            // Reset tonight to null to indicate that no night has started yet
            tonight.value = null

            // Trigger event for showing the snackbar by event state value to truea
            _showSnackbarEvent.value = true
        }
    }

    // clears the database
    // runs within a coroutine
    private suspend fun clear() {
        withContext(Dispatchers.IO) {
            database.clear()
        }
    }

    // onClear gets called when this ViewModel is destroyed
    override fun onCleared() {
        super.onCleared()
        // Cancels all coroutines if this ViewModel gets destroyed
        viewModelJob.cancel()
    }
}

