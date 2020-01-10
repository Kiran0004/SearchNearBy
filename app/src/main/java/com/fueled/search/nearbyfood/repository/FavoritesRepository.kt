package com.fueled.search.nearbyfood.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.fueled.search.nearbyfood.storage.PlacesDatabase
import com.fueled.search.nearbyfood.storage.Venue
import java.util.concurrent.*

/**
 * Created by Kiran
 *
 */
interface FavoritesRepository {
    fun getItemIds(): LiveData<Set<String>>
    fun add(id: String)
    fun remove(id: String)
    fun isInFavorites(id: String): LiveData<Venue?>
}


class FavoritesRepositoryImpl(private val placesDatabase: PlacesDatabase, private val executor: Executor) :
    FavoritesRepository {

    override fun isInFavorites(id: String): LiveData<Venue?> = placesDatabase.venueDao().isFavorite(id)

    override fun add(id: String) {
        executor.execute { placesDatabase.venueDao().insertVenue(Venue(id)) }
    }

    override fun remove(id: String) {
        executor.execute { placesDatabase.venueDao().deleteById(id) }
    }

    override fun getItemIds(): LiveData<Set<String>> {
        return Transformations.map(placesDatabase.venueDao().getVenues(), ::toSet)
    }

    private fun toSet(venues: List<Venue>): Set<String> {
        val set = hashSetOf<String>()
        venues.forEach {
            set.add(it.id)
        }
        return set
    }
}