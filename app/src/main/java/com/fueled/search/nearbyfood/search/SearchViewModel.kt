package com.fueled.search.nearbyfood.search

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.*
import com.fueled.search.nearbyfood.R
import com.fueled.search.nearbyfood.repository.PlacesRepository
import com.fueled.search.nearbyfood.network.Venue
import com.fueled.search.nearbyfood.repository.PlacesResponse
import com.fueled.search.nearbyfood.repository.FavoritesRepository
import com.fueled.search.nearbyfood.util.ResourceProvider
import com.fueled.search.nearbyfood.util.zip

/**
 * Created by Kiran.
 */



class SearchViewModel(
    private val placesRepository: PlacesRepository,
    private val favoritesRepository: FavoritesRepository,
    private val resourceProvider: ResourceProvider
) :
    ViewModel() {


    companion object {
         var LOCATION_DTL_LAT = 51.509865
         var LOCATION_DTL_LNG = -0.118092
         var DEFAULT_IMAGE_SIZE = "88"
         var SEARCH_LOCATION = "Milton Keynes,+UK"
         var DEFAULT_ITEMS_LIMIT = 300

    }

    private var query: String = ""

    val venuesLiveData = MutableLiveData<List<VenueItem>>()
    val progressLiveData = MutableLiveData<Boolean>()
    val errorLiveData = MutableLiveData<String>()

    private val searchResultObserver: Observer<Pair<PlacesResponse, Set<String>>> = Observer {
        val places = it.first
        val favorites = it.second

        progressLiveData.value = false
        if (query.isEmpty()) {
            venuesLiveData.value = emptyList()
        } else {
            venuesLiveData.value = mapToUiModel(places.venues, favorites)
        }

        if (places.errorMsg != null) {
            errorLiveData.value = places.errorMsg
        }
    }

    private val searchResultLiveData = placesRepository
        .getSearchResultLiveData()
        .zip(favoritesRepository.getItemIds())

    init {
        searchResultLiveData.observeForever(searchResultObserver)
    }

    fun performSearch(query: String) {
        this.query = query

        progressLiveData.value = true

        placesRepository.search(query, LOCATION_DTL_LAT.toString() +","+ LOCATION_DTL_LNG.toString(), DEFAULT_ITEMS_LIMIT)
    }

    fun onMapViewClick(listener: MapClickHandler) {
        if (venuesLiveData.value.isNullOrEmpty()) {
            listener.showError(resourceProvider.getString(R.string.no_content_msg))
        } else {
            listener.showMap(venuesLiveData.value!!)
        }
    }

    fun addToFavorites(id: String) {
        favoritesRepository.add(id)
    }

    fun removeFromFavorites(id: String) {
        favoritesRepository.remove(id)
    }

    private fun mapToUiModel(values: List<Venue>, favorites: Set<String>): List<VenueItem> {
        val venuesItemList = mutableListOf<VenueItem>()
        values.forEach {
            val venueItem =
                VenueItem(
                    it.id,
                    it.name,
                    getCategoryTitle(it.categories),
                    it.description ?: "",
                    it.location?.address ?: "",
                    it.location?.lat ?: 0.0,
                    it.location?.lng ?: 0.0,
                    it.canonicalUrl ?: "",
                    formatDistanceToString(getDistanceMiles(it.location?.lat, it.location?.lng)),
                    favorites.contains(it.id),
                    getImageUrl(it.categories)
                )
            venuesItemList.add(venueItem)
        }

        return venuesItemList
    }

    private fun getImageUrl(categories: List<Venue.Category>?): String {
        val primaryCategories = categories?.filter { it.primary }
        return if (primaryCategories.isNullOrEmpty()) {
            ""
        } else {
            primaryCategories[0].icon?.prefix + DEFAULT_IMAGE_SIZE + primaryCategories[0].icon?.suffix
        }
    }


    private fun getCategoryTitle(categories: List<Venue.Category>?): String {
        val items = categories?.filter { it.primary }
        return if (items.isNullOrEmpty()) {
            ""
        } else {
            items[0].name
        }
    }

    private fun formatDistanceToString(distanceMiles: Double): String {
        return String.format("%.2f miles from your location", distanceMiles)
    }

    private fun getDistanceMiles(lat: Double?, lng: Double?): Double {
        return if (lat == null || lng == null || LOCATION_DTL_LAT == lat && LOCATION_DTL_LNG == lng) {
            0.0
        } else {
            val theta = LOCATION_DTL_LNG - lng
            var dist = Math.sin(Math.toRadians(LOCATION_DTL_LAT)) * Math.sin(Math.toRadians(lat)) +
                    Math.cos(Math.toRadians(LOCATION_DTL_LAT)) * Math.cos(Math.toRadians(lat)) * Math.cos(
                Math.toRadians(theta)
            )
            dist = Math.acos(dist)
            dist = Math.toDegrees(dist)
            dist *= 60.0 * 1.1515
            dist
        }
    }

    data class VenueItem(
        val id: String,
        val title: String,
        val categoryTitle: String,
        val description: String,
        val address: String,
        val lat: Double,
        val lng: Double,
        val url: String,
        val distanceTitle: String,
        val isSelected: Boolean,
        val imageUrl: String
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readString(),
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            parcel.readString()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(id)
            parcel.writeString(title)
            parcel.writeString(categoryTitle)
            parcel.writeString(description)
            parcel.writeString(address)
            parcel.writeDouble(lat)
            parcel.writeDouble(lng)
            parcel.writeString(url)
            parcel.writeString(distanceTitle)
            parcel.writeByte(if (isSelected) 1 else 0)
            parcel.writeString(imageUrl)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<VenueItem> {
            override fun createFromParcel(parcel: Parcel): VenueItem {
                return VenueItem(parcel)
            }

            override fun newArray(size: Int): Array<VenueItem?> {
                return arrayOfNulls(size)
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        searchResultLiveData.removeObserver(searchResultObserver)
    }

    interface MapClickHandler {
        fun showMap(list: List<VenueItem>)
        fun showError(msg: String)
    }
}