package com.example.fueltracker_android.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fueltracker_android.data.FuelRepository
import com.example.fueltracker_android.domain.model.Refuel
import com.example.fueltracker_android.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: FuelRepository) : ViewModel() {

    private val _refuels = MutableStateFlow<UiState<List<Refuel>>>(UiState.Loading)
    val refuels: StateFlow<UiState<List<Refuel>>> = _refuels

    // Текущий активный фильтр — сохраняем чтобы не сбросить после удаления
    private var currentMonth: String? = null

    init {
        loadRefuels()
    }

    fun loadRefuels(month: String? = currentMonth) {
        currentMonth = month
        viewModelScope.launch {
            _refuels.value = UiState.Loading
            try {
                val list = repository.getRefuels(month = month)
                _refuels.value = UiState.Success(list)
            } catch (e: Exception) {
                _refuels.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun deleteRefuel(id: Int) {
        viewModelScope.launch {
            try {
                repository.deleteRefuel(id)
                // Перезагружаем с тем же фильтром что был активен
                loadRefuels(currentMonth)
            } catch (e: Exception) {
                _refuels.value = UiState.Error(e.message ?: "Ошибка удаления")
            }
        }
    }

    companion object {
        fun factory(repository: FuelRepository) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return HistoryViewModel(repository) as T
            }
        }
    }
}