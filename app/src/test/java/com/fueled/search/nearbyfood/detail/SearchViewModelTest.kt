package com.fueled.search.nearbyfood.detail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.fueled.search.nearbyfood.R
import com.fueled.search.nearbyfood.network.Venue
import com.fueled.search.nearbyfood.repository.FavoritesRepository
import com.fueled.search.nearbyfood.repository.PlacesRepository
import com.fueled.search.nearbyfood.repository.PlacesResponse
import com.fueled.search.nearbyfood.repository.VenueResponse
import com.fueled.search.nearbyfood.search.SearchViewModel
import com.fueled.search.nearbyfood.util.ResourceProvider
import com.nhaarman.mockitokotlin2.*
import org.junit.Before

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.ArgumentMatchers.*


/**
 * Created by Kiran.
 */
class SearchViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    lateinit var placesRepository: PlacesRepository

    @Mock
    lateinit var favoritesRepository: FavoritesRepository

    @Mock
    lateinit var resourceProvider: ResourceProvider

    private lateinit var searchViewModel: SearchViewModel

    private val venue = Venue(
        ID, TITLE, DESCRIPTION, null, URL,
        Venue.Location(ADDRESS, null, LAT, LNG, POSTAL_CODE, "", CITY, STATE, COUNTRY), null, null,
        listOf(
            Venue.Category(
                IMAGE_ID,
                IMAGE_CATEGORY_NAME,
                Venue.Category.CategoryIcon(
                    IMAGE_PREFIX,
                    IMAGE_SUFFIX
                ),
                true
            )
        )
    )
    private val venueResponse = VenueResponse(venue, null)
    private val venueResponseLiveData = MutableLiveData<VenueResponse>()
    private val placesResponse = PlacesResponse(listOf(venue), "")
    private val placesResponseError = PlacesResponse(emptyList(), NETWORK_ERROR)

    private val favoriteResponse = MutableLiveData<Set<String>>()
    private val searchResultLiveData = MutableLiveData<PlacesResponse>()


    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        venueResponseLiveData.value = venueResponse
        searchResultLiveData.value = placesResponse
        favoriteResponse.value = SAVED_IDs

        whenever(placesRepository.getSearchResultLiveData()).thenReturn(searchResultLiveData)
        whenever(placesRepository.getDetails(anyString())).thenReturn(venueResponseLiveData)
        whenever(favoritesRepository.getItemIds()).thenReturn(favoriteResponse)
        whenever(resourceProvider.getString(R.string.no_content_msg)).thenReturn(DEFAULT_ERROR_NO_DATA)

        searchViewModel = SearchViewModel(placesRepository, favoritesRepository, resourceProvider)
    }

    @Test
    fun `test initial live data state`() {
        assertEquals(false, searchViewModel.progressLiveData.value)
        assertEquals(emptyList<SearchViewModel.VenueItem>(), searchViewModel.venuesLiveData.value)
        assertTrue(searchViewModel.errorLiveData.value.isNullOrBlank())
    }

    @Test
    fun `test view model's query params`() {
        searchViewModel.performSearch(QUERY)

        verify(placesRepository).search(QUERY, SEARCH_LOCATION, DEFAULT_ITEMS_LIMIT)
    }

    @Test
    fun `test view model's search results`() {
        whenever(placesRepository.search(anyString(), anyString(), anyInt())).then {
            placesRepository.getSearchResultLiveData() as MutableLiveData
            (placesRepository.getSearchResultLiveData() as MutableLiveData).value = placesResponse
            it
        }

        assertEquals(false, searchViewModel.progressLiveData.value)

        searchViewModel.performSearch(QUERY)

        searchViewModel.venuesLiveData.observeForever {
            assertEquals(false, searchViewModel.progressLiveData.value)
            // after mapping from network to view model
            val viewModelVenueItem = it[0]

            assertEquals(viewModelVenueItem.isSelected, true)
            assertEquals(viewModelVenueItem.id, ID)
            assertEquals(viewModelVenueItem.title, TITLE)
            assertEquals(viewModelVenueItem.description, DESCRIPTION)
            assertEquals(viewModelVenueItem.address, ADDRESS)
            assertEquals(viewModelVenueItem.categoryTitle, IMAGE_CATEGORY_NAME)
            assertEquals(viewModelVenueItem.distanceTitle, DISTANCE_TITLE)
            assertEquals(viewModelVenueItem.lat, LAT, 0.0)
            assertEquals(viewModelVenueItem.lng, LNG, 0.0)
            assertEquals(viewModelVenueItem.url, URL)
            assertEquals(viewModelVenueItem.imageUrl, IMAGE_PREFIX + IMAGE_SIZE + IMAGE_SUFFIX)
        }
    }

    @Test
    fun `test search with network error`() {
        whenever(placesRepository.search(anyString(), anyString(), anyInt())).then {
            placesRepository.getSearchResultLiveData() as MutableLiveData
            (placesRepository.getSearchResultLiveData() as MutableLiveData).value = placesResponseError
            it
        }

        searchViewModel.performSearch(QUERY)

        searchViewModel.errorLiveData.observeForever {
            assertEquals(NETWORK_ERROR, it)
        }

        searchViewModel.venuesLiveData.observeForever {
            assertEquals(emptyList<SearchViewModel.VenueItem>(), it)
        }
    }

    @Test
    fun `test map click with search results`() {
        val venueList = mutableListOf<SearchViewModel.VenueItem>()
        venueList.add(
            SearchViewModel.VenueItem(
                ID,
                TITLE,
                IMAGE_CATEGORY_NAME,
                DESCRIPTION,
                ADDRESS,
                LAT,
                LNG,
                URL,
                DISTANCE_TITLE,
                true,
                IMAGE_PREFIX
            )
        )
        searchViewModel.venuesLiveData.value = venueList

        val mock = mock<SearchViewModel.MapClickHandler>()
        searchViewModel.onMapViewClick(mock)
        argumentCaptor<List<SearchViewModel.VenueItem>>().apply {
            verify(mock, times(1)).showMap(capture())

            assertEquals(1, allValues.size)
            assertEquals(venueList, firstValue)

            verify(mock, never()).showError(anyString())
        }
    }

    @Test
    fun `test map click with empty data`() {
        searchViewModel.venuesLiveData.value = mutableListOf()

        val mock = mock<SearchViewModel.MapClickHandler>()
        searchViewModel.onMapViewClick(mock)
        argumentCaptor<String>().apply {
            verify(mock, times(1)).showError(capture())

            assertEquals(1, allValues.size)
            assertEquals(DEFAULT_ERROR_NO_DATA, firstValue)
        }
    }

    companion object {

        val SAVED_IDs = setOf("52d456c811d24128cdd7bc8b", "57e95a82498e0a3995a43e90")

        const val QUERY = "Restaurant"
        const val NETWORK_ERROR = "Something went wrong"

        const val ID = "57e95a82498e0a3995a43e90"
        const val TITLE = "Banana Tree"
        const val CITY = "Milton Keynes"
        const val STATE = ""
        const val COUNTRY = "UK"
        const val LAT = 52.04172
        const val LNG = -0.75583
        const val POSTAL_CODE = "MK9 2BS"
        const val DESCRIPTION =
            "Modern, warm Italian eatery with small plates plus pizzas from a blue-tiled wood-burning oven."
        const val URL = "http://bananatree.com"
        const val ADDRESS = "43, chesela House"

        const val IMAGE_ID = "4bf58dd8d48988d1e0931735"
        const val IMAGE_CATEGORY_NAME = "Coffee Shop"
        const val IMAGE_PREFIX = "https://ss3.4sqi.net/img/categories_v2/food/coffeeshop_"
        const val IMAGE_SUFFIX = ".png"
        const val IMAGE_SIZE = 88

        const val DISTANCE_TITLE = "0.51 miles of the city center"
        const val SEARCH_LOCATION = "Milton Keynes,+UK"
        const val DEFAULT_ITEMS_LIMIT = 30
        const val DEFAULT_ERROR_NO_DATA = "Try to search first"
    }
}