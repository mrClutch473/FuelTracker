package com.example.fueltracker_android.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fueltracker_android.data.FuelRepository
import com.example.fueltracker_android.domain.model.User
import com.example.fueltracker_android.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException

class AuthViewModel(private val repository: FuelRepository) : ViewModel() {

    private val _authState = MutableStateFlow<UiState<User>?>(null)
    val authState: StateFlow<UiState<User>?> = _authState

    // ── Login ─────────────────────────────────────────────────────────────────

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            try {
                val user = repository.login(email, password)
                _authState.value = UiState.Success(user)
            } catch (e: HttpException) {
                _authState.value = UiState.Error(loginHttpError(e.code()))
            } catch (e: ConnectException) {
                _authState.value = UiState.Error("Сервер недоступен. Проверьте соединение")
            } catch (e: SocketTimeoutException) {
                _authState.value = UiState.Error("Превышено время ожидания")
            } catch (e: Exception) {
                _authState.value = UiState.Error("Ошибка: ${e.message}")
            }
        }
    }

    // ── Register ──────────────────────────────────────────────────────────────

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            try {
                val user = repository.register(email, password)
                _authState.value = UiState.Success(user)
            } catch (e: HttpException) {
                _authState.value = UiState.Error(registerHttpError(e.code()))
            } catch (e: ConnectException) {
                _authState.value = UiState.Error("Сервер недоступен. Проверьте соединение")
            } catch (e: SocketTimeoutException) {
                _authState.value = UiState.Error("Превышено время ожидания")
            } catch (e: Exception) {
                _authState.value = UiState.Error("Ошибка: ${e.message}")
            }
        }
    }

    /** Сброс состояния — вызывать при переходе между экранами, чтобы старое
     *  состояние не «протекало» на новый экран. */
    fun resetState() {
        _authState.value = null
    }

    // ── Error messages ────────────────────────────────────────────────────────

    private fun loginHttpError(code: Int) = when (code) {
        401  -> "Неверный email или пароль"
        422  -> "Проверьте правильность введённых данных"
        500  -> "Внутренняя ошибка сервера"
        else -> "Ошибка сервера ($code)"
    }

    private fun registerHttpError(code: Int) = when (code) {
        409  -> "Этот email уже зарегистрирован"
        422  -> "Проверьте правильность введённых данных"
        500  -> "Внутренняя ошибка сервера"
        else -> "Ошибка сервера ($code)"
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    companion object {
        fun factory(repository: FuelRepository) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(repository) as T
            }
        }
    }
}
