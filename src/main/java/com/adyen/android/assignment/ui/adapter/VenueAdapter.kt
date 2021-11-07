package com.adyen.android.assignment.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adyen.android.assignment.R
import com.adyen.android.assignment.api.model.RecommendedItem
import com.adyen.android.assignment.utils.flattenListToString
import kotlinx.android.synthetic.main.venue_item.view.*
import java.util.*
import kotlin.collections.ArrayList

class VenueAdapter :
    ListAdapter<RecommendedItem, VenueAdapter.VenueAdapterViewHolder>(VenueComparator()),
    Filterable {

    private var recommendedItemList = arrayListOf<RecommendedItem>()

    class VenueAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val addressTextView: TextView = itemView.address_tv
        private val categoryTextView: TextView = itemView.category_tv
        private val nameTextView: TextView = itemView.name_tv

        fun bind(recommendedItem: RecommendedItem) {
            val address = flattenListToString(recommendedItem.venue.location.formattedAddress)

            addressTextView.text = address
            categoryTextView.text = recommendedItem.venue.categories.first().name
            nameTextView.text = recommendedItem.venue.name.toUpperCase(Locale.getDefault())

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VenueAdapterViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.venue_item, parent, false)
        return VenueAdapterViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: VenueAdapterViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }


    fun submitRecommendedItemList(list: ArrayList<RecommendedItem>) {
        this.recommendedItemList = list
        submitList(list)
    }

    override fun getFilter(): Filter {
        return searchFilter
    }

    private val searchFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList: ArrayList<RecommendedItem> = ArrayList()
            if (constraint == null || constraint.isEmpty()) {
                filteredList.addAll(recommendedItemList)
            } else {
                val filterPattern = constraint.toString().toLowerCase(Locale.getDefault()).trim()
                for (item in recommendedItemList) {

                    if (!item.venue.name.isNullOrEmpty()) {
                        if (item.venue.name.toLowerCase(Locale.getDefault())
                                .contains(filterPattern)
                        ) {
                            filteredList.add(item)
                        }
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            submitList(results.values as ArrayList<RecommendedItem>)
        }
    }


}