package com.example.fueltracker_android.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fueltracker_android.data.FuelRepository
import com.example.fueltracker_android.domain.model.Refuel
import com.example.fueltracker_android.domain.model.Stats
import com.example.fueltracker_android.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: FuelRepository) : ViewModel() {

    private val _lastRefuel = MutableStateFlow<UiState<Refuel?>>(UiState.Loading)
    val lastRefuel: StateFlow<UiState<Refuel?>> = _lastRefuel

    private val _summary = MutableStateFlow<UiState<Stats.Summary>>(UiState.Loading)
    val summary: StateFlow<UiState<Stats.Summary>> = _summary

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            try {
                val refuels = repository.getRefuels(limit = 1)
                _lastRefuel.value = UiState.Success(refuels.firstOrNull())
            } catch (e: Exception) {
                _lastRefuel.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }

            try {
                val summary = repository.getSummary()
                _summary.value = UiState.Success(summary)
            } catch (e: Exception) {
                _summary.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    companion object {
        fun factory(repository: FuelRepository) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(repository) as T
            }
        }
    }
}