package com.fueled.search.nearbyfood.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fueled.search.nearbyfood.R
import kotlinx.android.synthetic.main.venue_item.view.*

/**
 * Created by Kiran.
 */



class RecyclerAdapter(private val venues: List<SearchViewModel.VenueItem>, private val clickHandler: ClickHandler) :
    RecyclerView.Adapter<RecyclerAdapter.VenueHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VenueHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.venue_item, parent, false)
        return VenueHolder(inflatedView, clickHandler)
    }

    override fun getItemCount(): Int = venues.size

    override fun onBindViewHolder(holder: VenueHolder, position: Int) {
        val itemPhoto = venues[position]
        holder.bindItem(itemPhoto)
    }

    class VenueHolder(v: View, private val clickHandler: ClickHandler) : RecyclerView.ViewHolder(v) {
        private var view: View = v
        private var venue: SearchViewModel.VenueItem? = null

        fun bindItem(venue: SearchViewModel.VenueItem) {
            this.venue = venue

            view.tvAddress.text = venue.address
            view.tvDistance.text = venue.distanceTitle


            view.itemName.text = venue.title
            view.tvCategory.text = venue.categoryTitle
            view.ibStarred.isSelected = venue.isSelected
            if(view.ibStarred.isSelected)
                view.tvFavorite.text = view.context.getString(R.string.add_to_fav)
            else
                view.tvFavorite.text = view.context.getString(R.string.remove_fav)
            Glide.with(view.context)
                .load(venue.imageUrl)
                .placeholder(R.drawable.ic_tag_faces_black_24dp)
                .into(view.ivVenue)

            view.llMain.setOnClickListener {
                clickHandler.onSearchItemClick(venue)
            }

            view.ibStarred.setOnClickListener {
                val isSelected = it.isSelected
                clickHandler.onStarClick(venue, isSelected)
                if(isSelected)
                    view.tvFavorite.text = "This place is added to your favorites!!!"
                else
                    view.tvFavorite.text = "Add this place to your favorites"
                it.isSelected = !isSelected
            }
        }
    }

    fun getData(): List<SearchViewModel.VenueItem> = venues
}

interface ClickHandler {

    fun onSearchItemClick(item: SearchViewModel.VenueItem)

    fun onStarClick(item: SearchViewModel.VenueItem, selected: Boolean)
}