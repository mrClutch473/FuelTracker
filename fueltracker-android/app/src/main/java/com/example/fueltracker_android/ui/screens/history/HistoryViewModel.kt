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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Фильтры по временному периоду. */
enum class PeriodFilter { ALL, DAY, WEEK, MONTH }

class HistoryViewModel(private val repository: FuelRepository) : ViewModel() {

    // Полный список, полученный с сервера — не меняется до следующего loadRefuels()
    private val _allRefuels = MutableStateFlow<List<Refuel>>(emptyList())

    // Отфильтрованный/отсортированный список, который наблюдает Fragment
    private val _refuels = MutableStateFlow<UiState<List<Refuel>>>(UiState.Loading)
    val refuels: StateFlow<UiState<List<Refuel>>> = _refuels

    // ── Состояние фильтров ────────────────────────────────────────────────────

    var periodFilter: PeriodFilter = PeriodFilter.ALL
        private set

    /** true = новые сверху (по умолчанию), false = старые сверху */
    var sortDescending: Boolean = true
        private set

    /** true = показывать только заправки с заметкой */
    var noteFilterActive: Boolean = false
        private set

    // ── Жизненный цикл ────────────────────────────────────────────────────────

    init {
        loadRefuels()
    }

    /** Загружает заправки с сервера и применяет текущие фильтры. */
    fun loadRefuels() {
        viewModelScope.launch {
            _refuels.value = UiState.Loading
            try {
                // limit=50 — максимум, который принимает сервер
                val list = repository.getRefuels(limit = 50)
                _allRefuels.value = list
                applyFiltersAndSort()
            } catch (e: Exception) {
                _refuels.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    // ── Управление фильтрами ──────────────────────────────────────────────────

    fun setPeriodFilter(filter: PeriodFilter) {
        periodFilter = filter
        applyFiltersAndSort()
    }

    /** Переключает направление сортировки. */
    fun toggleSort() {
        sortDescending = !sortDescending
        applyFiltersAndSort()
    }

    /** Переключает фильтр «только с заметками». */
    fun toggleNoteFilter() {
        noteFilterActive = !noteFilterActive
        applyFiltersAndSort()
    }

    // ── Удаление ──────────────────────────────────────────────────────────────

    fun deleteRefuel(id: Int) {
        viewModelScope.launch {
            try {
                repository.deleteRefuel(id)
                // Убираем из локального списка, не делая лишний сетевой запрос
                _allRefuels.value = _allRefuels.value.filter { it.id != id }
                applyFiltersAndSort()
            } catch (e: Exception) {
                _refuels.value = UiState.Error(e.message ?: "Ошибка удаления")
            }
        }
    }

    // ── Приватная логика ──────────────────────────────────────────────────────

    private fun applyFiltersAndSort() {
        var list = _allRefuels.value

        // 1. Фильтр по периоду
        val cal = Calendar.getInstance()
        when (periodFilter) {
            PeriodFilter.ALL  -> { /* без фильтра */ }

            PeriodFilter.DAY  -> {
                val today = todayString(cal)
                list = list.filter { it.createdAt.startsWith(today) }
            }

            PeriodFilter.WEEK -> {
                val weekAgo = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -7)
                }.time
                list = list.filter { parseIsoDate(it.createdAt) >= weekAgo }
            }

            PeriodFilter.MONTH -> {
                val yearMonth = yearMonthString(cal)
                list = list.filter { it.createdAt.startsWith(yearMonth) }
            }
        }

        // 2. Фильтр «только с заметками»
        if (noteFilterActive) {
            list = list.filter { !it.note.isNullOrBlank() }
        }

        // 3. Сортировка по дате
        list = if (sortDescending) {
            list.sortedByDescending { it.createdAt }
        } else {
            list.sortedBy { it.createdAt }
        }

        _refuels.value = UiState.Success(list)
    }

    /** "2026-05-22" — текущая дата в формате ISO */
    private fun todayString(cal: Calendar): String {
        val y = cal.get(Calendar.YEAR)
        val m = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        val d = cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        return "$y-$m-$d"
    }

    /** "2026-05" — текущий год-месяц */
    private fun yearMonthString(cal: Calendar): String {
        val y = cal.get(Calendar.YEAR)
        val m = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        return "$y-$m"
    }

    /** "2026-05-22T18:53:42" → java.util.Date */
    private fun parseIsoDate(isoDate: String): Date {
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .parse(isoDate.substring(0, 10)) ?: Date(0)
        } catch (e: Exception) {
            Date(0)
        }
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    companion object {
        fun factory(repository: FuelRepository) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return HistoryViewModel(repository) as T
            }
        }
    }
}
