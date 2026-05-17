package com.example.fueltracker_android.domain.model

object Stats {

    data class Summary(
        val totalSpent: Double,
        val totalLiters: Double,
        val avgConsumption: Double?,   // null если < 2 заправок
        val bestConsumption: Double?,
        val worstConsumption: Double?,
        val avgPricePerLiter: Double,
        val refuelsCount: Int,
        val totalKm: Int
    )

    data class MonthlyData(
        val month: String,
        val totalSpent: Double,
        val totalLiters: Double,
        val avgConsumption: Double?,   // null если нет данных расхода
        val avgPrice: Double,
        val refuelsCount: Int
    )

    data class ConsumptionPoint(
        val date: String,
        val consumption: Double?,      // null для первой заправки
        val odometer: Int
    )
}