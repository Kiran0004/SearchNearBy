package com.fueled.search.nearbyfood.network

/**
 * Created by Kiran.
 */



data class SearchResponse(val meta: Meta, val response: Body) {
    data class Body(val venues: List<Venue>)
}

data class Meta(val code: Int, val requestId: String)

data class Venue(
    val id: String,
    val name: String,
    val description: String?,
    val contact: Contact?,
    val canonicalUrl: String?,
    val location: Location?,
    val price: Price?,
    val hours: Hours?,
    val categories: List<Category>?
) {
    data class Contact(val phone: String, val formattedPhone: String)

    data class Price(val tier: String, val message: String, val currency: String)

    data class Hours(val status: String, val isOpen: Boolean)

    data class Location(
        val address: String,
        val crossStreet: String?,
        val lat: Double,
        val lng: Double,
        val postalCode: String,
        val cc: String,
        val city: String,
        val state: String,
        val country: String
    )

    data class Category(
        val id: String,
        val name: String,
        val icon: CategoryIcon?,
        val primary: Boolean
    ) {
        data class CategoryIcon(val prefix: String, val suffix: String)
    }
}

data class DetailsApiResponse(val meta: Meta, val response: DetailsResponseBody) {
    data class DetailsResponseBody(val venue: Venue)
}