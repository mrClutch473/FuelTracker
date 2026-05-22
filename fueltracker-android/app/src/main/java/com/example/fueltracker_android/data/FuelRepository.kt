package com.example.fueltracker_android.data

import com.example.fueltracker_android.data.dto.ConsumptionPointDto
import com.example.fueltracker_android.data.dto.MonthlyDataDto
import com.example.fueltracker_android.data.dto.RefuelDto
import com.example.fueltracker_android.data.dto.SummaryDto
import com.example.fueltracker_android.data.dto.UserDto
import com.example.fueltracker_android.domain.model.Refuel
import com.example.fueltracker_android.domain.model.Stats
import com.example.fueltracker_android.domain.model.User

class FuelRepository(private val apiService: ApiService) {

    // ── Auth ─────────────────────────────────────────────────────────────────

    suspend fun register(email: String, password: String): User {
        return apiService.register(RegisterRequest(email, password)).toDomain()
    }

    suspend fun login(email: String, password: String): User {
        return apiService.login(LoginRequest(email, password)).toDomain()
    }

    /** Выход: очищаем cookie на сервере и в локальном хранилище. */
    suspend fun logout() {
        try {
            apiService.logout()
        } finally {
            // Очищаем cookie даже если сервер не ответил
            AppDependencies.cookieJar.clear()
        }
    }

    suspend fun me(): User = apiService.me().toDomain()

    // ── Refuels ───────────────────────────────────────────────────────────────

    suspend fun getRefuels(
        limit: Int = 50,
        offset: Int = 0,
        month: String? = null
    ): List<Refuel> {
        return apiService.getRefuels(limit, offset, month).items.map { it.toDomain() }
    }

    suspend fun createRefuel(
        liters: Double,
        price: Double,
        odometer: Int,
        note: String?
    ): Refuel {
        return apiService.createRefuel(CreateRefuelRequest(liters, price, odometer, note))
            .toDomain()
    }

    suspend fun deleteRefuel(id: Int): Int {
        return apiService.deleteRefuel(id).id
    }

    // ── Stats ─────────────────────────────────────────────────────────────────

    suspend fun getSummary(): Stats.Summary = apiService.getSummary().toDomain()

    suspend fun getMonthlyStats(months: Int = 6): List<Stats.MonthlyData> {
        return apiService.getMonthlyStats(months).data.map { it.toDomain() }
    }

    suspend fun getConsumptionTrend(limit: Int = 20): List<Stats.ConsumptionPoint> {
        return apiService.getConsumptionTrend(limit).points.map { it.toDomain() }
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private fun UserDto.toDomain() = User(
        id        = id,
        email     = email,
        createdAt = createdAt
    )

    private fun RefuelDto.toDomain() = Refuel(
        id          = id,
        liters      = liters,
        price       = price,
        totalCost   = totalCost,
        odometer    = odometer,
        consumption = consumption,
        note        = note,
        createdAt   = createdAt
    )

    private fun SummaryDto.toDomain() = Stats.Summary(
        totalSpent        = totalSpent,
        totalLiters       = totalLiters,
        avgConsumption    = avgConsumption,
        bestConsumption   = bestConsumption,
        worstConsumption  = worstConsumption,
        avgPricePerLiter  = avgPricePerLiter,
        refuelsCount      = refuelsCount,
        totalKm           = totalKm
    )

    private fun MonthlyDataDto.toDomain() = Stats.MonthlyData(
        month          = month,
        totalSpent     = totalSpent,
        totalLiters    = totalLiters,
        avgConsumption = avgConsumption,
        avgPrice       = avgPrice,
        refuelsCount   = refuelsCount
    )

    private fun ConsumptionPointDto.toDomain() = Stats.ConsumptionPoint(
        date        = date,
        consumption = consumption,
        odometer    = odometer
    )
}
