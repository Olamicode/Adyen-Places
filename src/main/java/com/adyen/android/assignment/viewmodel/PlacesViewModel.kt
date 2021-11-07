package com.adyen.android.assignment.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.adyen.android.assignment.api.model.VenueRecommendationsResponse
import com.adyen.android.assignment.repository.IPlacesRepository
import com.adyen.android.assignment.utils.callbacks.ObjectCallback
import com.adyen.android.assignment.utils.callbacks.OfflineCallBack

class PlacesViewModel(private val placesRepository: IPlacesRepository) : ViewModel() {

    fun getVenueRecommendations(
        context: Context,
        successCallBack: ObjectCallback<VenueRecommendationsResponse>,
        errorCallBack: ObjectCallback<Throwable>,
        offlineCallBack: OfflineCallBack,
        latitude: Double,
        longitude: Double
    ) {
        placesRepository.getVenueRecommendations(context, successCallBack, errorCallBack, offlineCallBack, latitude, longitude)
    }
}