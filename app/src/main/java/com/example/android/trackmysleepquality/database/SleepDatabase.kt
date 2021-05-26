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

package com.example.android.trackmysleepquality.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The purpose of this class is to provide create or get the database(s)
 */

// Tell the database what entity(s) to use along with a version number
@Database(entities = [SleepNight::class], version = 1, exportSchema = false)
abstract class SleepDatabase : RoomDatabase() {

    // Declare the Dao(s) that will be associated with this database
    abstract val sleepDatabaseDao: SleepDatabaseDao

    // The companion object lets clients create or get the database without instantiating the class
    companion object {

        // INSTANCE will keep a reference to the database once we have one
        @Volatile  // This annotation will ensure that this variable is up to date across all execution threads
        private var INSTANCE: SleepDatabase? = null

        // Gets an instance of the database
        fun getInstance(context: Context): SleepDatabase {
            // Only one execution thread can enter this block at once, ensuring that this database
            // initialized once
            synchronized(this) {
                var instance = INSTANCE

                // If there is no instance of the database, create it with room databaseBuilder
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            SleepDatabase::class.java,
                            "sleep_history_database"
                    )
                            .fallbackToDestructiveMigration()
                            .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
