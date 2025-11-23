package com.example.grocerycompanion.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory<T : ViewModel>(private val creator: () -> T) : ViewModelProvider.Factory {
    override fun <T2 : ViewModel> create(modelClass: Class<T2>): T2 = creator.invoke() as T2
}
