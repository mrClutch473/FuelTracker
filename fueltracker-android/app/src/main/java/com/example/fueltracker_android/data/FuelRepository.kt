package com.example.fueltracker_android.data

import com.example.fueltracker_android.data.dto.ConsumptionPointDto
import com.example.fueltracker_android.data.dto.MonthlyDataDto
import com.example.fueltracker_android.data.dto.RefuelDto
import com.example.fueltracker_android.data.dto.SummaryDto
import com.example.fueltracker_android.data.ApiService
import com.example.fueltracker_android.domain.model.Refuel
import com.example.fueltracker_android.domain.model.Stats

class FuelRepository(private val apiService: ApiService) {

    suspend fun getRefuels(limit: Int = 50, offset: Int = 0, month: String? = null): List<Refuel> {
        val response = apiService.getRefuels(limit, offset, month)
        return response.items.map { it.toDomain() }
    }

    suspend fun createRefuel(liters: Double, price: Double, odometer: Int, note: String?): Refuel {
        val request = CreateRefuelRequest(liters, price, odometer, note)
        return apiService.createRefuel(request).toDomain()
    }

    suspend fun deleteRefuel(id: Int): Int {
        val response = apiService.deleteRefuel(id)
        return response.id
    }

    suspend fun getSummary(): Stats.Summary {
        return apiService.getSummary().toDomain()
    }

    suspend fun getMonthlyStats(months: Int = 6): List<Stats.MonthlyData> {
        val response = apiService.getMonthlyStats(months)
        return response.data.map { it.toDomain() }
    }

    suspend fun getConsumptionTrend(limit: Int = 20): List<Stats.ConsumptionPoint> {
        val response = apiService.getConsumptionTrend(limit)
        return response.points.map { it.toDomain() }
    }

    private fun RefuelDto.toDomain(): Refuel = Refuel(
        id = id,
        liters = liters,
        price = price,
        totalCost = totalCost,
        odometer = odometer,
        consumption = consumption,
        note = note,
        createdAt = createdAt
    )

    private fun SummaryDto.toDomain(): Stats.Summary = Stats.Summary(
        totalSpent = totalSpent,
        totalLiters = totalLiters,
        avgConsumption = avgConsumption,
        bestConsumption = bestConsumption,
        worstConsumption = worstConsumption,
        avgPricePerLiter = avgPricePerLiter,
        refuelsCount = refuelsCount,
        totalKm = totalKm
    )

    private fun MonthlyDataDto.toDomain(): Stats.MonthlyData = Stats.MonthlyData(
        month = month,
        totalSpent = totalSpent,
        totalLiters = totalLiters,
        avgConsumption = avgConsumption,
        avgPrice = avgPrice,
        refuelsCount = refuelsCount
    )

    private fun ConsumptionPointDto.toDomain(): Stats.ConsumptionPoint = Stats.ConsumptionPoint(
        date = date,
        consumption = consumption,
        odometer = odometer
    )
}