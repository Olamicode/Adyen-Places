package com.adyen.android.assignment.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adyen.android.assignment.repository.IPlacesRepository

class PlacesViewModelFactory(private val placesRepository: IPlacesRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(IPlacesRepository::class.java)
            .newInstance(placesRepository)
    }
}

