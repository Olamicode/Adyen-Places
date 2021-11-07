package com.adyen.android.assignment.repository

import android.content.Context
import com.adyen.android.assignment.api.model.VenueRecommendationsResponse
import com.adyen.android.assignment.utils.callbacks.ObjectCallback
import com.adyen.android.assignment.utils.callbacks.OfflineCallBack

interface IPlacesRepository {
    fun getVenueRecommendations(
        context: Context,
        successCallBack: ObjectCallback<VenueRecommendationsResponse>,
        errorCallBack: ObjectCallback<Throwable>,
        offlineCallBack: OfflineCallBack,
        latitude: Double,
        longitude: Double
    )
}