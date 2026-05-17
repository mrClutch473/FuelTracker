package com.example.fueltracker_android.ui.screens.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fueltracker_android.data.FuelRepository
import com.example.fueltracker_android.domain.model.Refuel
import com.example.fueltracker_android.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AddRefuelViewModel(private val repository: FuelRepository) : ViewModel() {

    private val _addState = MutableStateFlow<UiState<Refuel>?>(null)
    val addState: StateFlow<UiState<Refuel>?> = _addState

    fun addRefuel(liters: Double, price: Double, odometer: Int, note: String?) {
        viewModelScope.launch {
            _addState.value = UiState.Loading
            try {
                val refuel = repository.createRefuel(liters, price, odometer, note)
                _addState.value = UiState.Success(refuel)
            } catch (e: Exception) {
                _addState.value = UiState.Error(e.message ?: "Ошибка сохранения")
            }
        }
    }

    companion object {
        fun factory(repository: FuelRepository) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AddRefuelViewModel(repository) as T
            }
        }
    }
}