package com.example.grocerycompanion.util

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.grocerycompanion.model.LocationViewModel

class LocationViewModelFactory(private val app: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(model: Class<T>) : T {
        return LocationViewModel(app) as T
    }
}