package com.example.fueltracker_android.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fueltracker_android.data.FuelRepository
import com.example.fueltracker_android.domain.model.Stats
import com.example.fueltracker_android.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StatsViewModel(private val repository: FuelRepository) : ViewModel() {

    private val _summary = MutableStateFlow<UiState<Stats.Summary>>(UiState.Loading)
    val summary: StateFlow<UiState<Stats.Summary>> = _summary

    private val _monthly = MutableStateFlow<UiState<List<Stats.MonthlyData>>>(UiState.Loading)
    val monthly: StateFlow<UiState<List<Stats.MonthlyData>>> = _monthly

    private val _trend = MutableStateFlow<UiState<List<Stats.ConsumptionPoint>>>(UiState.Loading)
    val trend: StateFlow<UiState<List<Stats.ConsumptionPoint>>> = _trend

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            try {
                _summary.value = UiState.Success(repository.getSummary())
            } catch (e: Exception) {
                _summary.value = UiState.Error(e.message ?: "Ошибка")
            }

            try {
                _monthly.value = UiState.Success(repository.getMonthlyStats())
            } catch (e: Exception) {
                _monthly.value = UiState.Error(e.message ?: "Ошибка")
            }

            try {
                _trend.value = UiState.Success(repository.getConsumptionTrend())
            } catch (e: Exception) {
                _trend.value = UiState.Error(e.message ?: "Ошибка")
            }
        }
    }

    companion object {
        fun factory(repository: FuelRepository) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return StatsViewModel(repository) as T
            }
        }
    }
}