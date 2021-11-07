package com.adyen.android.assignment.repository

import android.content.Context
import com.adyen.android.assignment.R
import com.adyen.android.assignment.api.PlacesService
import com.adyen.android.assignment.api.VenueRecommendationsQueryBuilder
import com.adyen.android.assignment.api.model.ResponseWrapper
import com.adyen.android.assignment.api.model.VenueRecommendationsResponse
import com.adyen.android.assignment.utils.Utility
import com.adyen.android.assignment.utils.callbacks.ObjectCallback
import com.adyen.android.assignment.utils.callbacks.OfflineCallBack
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PlacesRepository(private val placesService: PlacesService) : IPlacesRepository {

    override fun getVenueRecommendations(
        context: Context,
        successCallBack: ObjectCallback<VenueRecommendationsResponse>,
        errorCallBack: ObjectCallback<Throwable>,
        offlineCallBack: OfflineCallBack,
        latitude: Double,
        longitude: Double
    ) {
        if (Utility.hasInternet(context)) {
            val query = VenueRecommendationsQueryBuilder()
                .setLatitudeLongitude(latitude, longitude)
                .build()
            placesService.getVenueRecommendations(query)
                .enqueue(object : Callback<ResponseWrapper<VenueRecommendationsResponse>> {
                    override fun onResponse(
                        call: Call<ResponseWrapper<VenueRecommendationsResponse>>,
                        response: Response<ResponseWrapper<VenueRecommendationsResponse>>
                    ) {
                        if (response.isSuccessful) {
                            val successResponse = response.body().let {
                                it?.response
                            }
                            if (successResponse != null) {
                                successCallBack.onCallback(successResponse)
                            }
                        }
                    }

                    override fun onFailure(
                        call: Call<ResponseWrapper<VenueRecommendationsResponse>>,
                        throwable: Throwable
                    ) {
                        errorCallBack.onCallback(throwable)
                    }

                })
        } else {
            offlineCallBack.onCallback(
                context.getString(
                    R.string.could_not_load_search_results,
                    context.getString(R.string.no_internet_connection)
                )
            )
        }
    }
}

