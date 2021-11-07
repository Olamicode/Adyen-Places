package com.adyen.android.assignment.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.adyen.android.assignment.api.model.RecommendedItem

class VenueComparator : DiffUtil.ItemCallback<RecommendedItem>() {
    override fun areItemsTheSame(oldItem: RecommendedItem, newItem: RecommendedItem): Boolean =
        oldItem.venue.id == newItem.venue.id

    override fun areContentsTheSame(oldItem: RecommendedItem, newItem: RecommendedItem): Boolean =
        oldItem == newItem

}