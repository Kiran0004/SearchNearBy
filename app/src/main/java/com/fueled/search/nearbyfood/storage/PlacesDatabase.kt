package com.fueled.search.nearbyfood.storage

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Created by Kiran.
 */


const val DB_NAME = "places.db"

@Database(entities = [Venue::class], version = 1)
abstract class PlacesDatabase : RoomDatabase() {
    abstract fun venueDao(): VenueDao
}