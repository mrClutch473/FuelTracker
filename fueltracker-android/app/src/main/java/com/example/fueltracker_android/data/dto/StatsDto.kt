package com.example.fueltracker_android.data.dto

import com.google.gson.annotations.SerializedName

data class SummaryDto(
    @SerializedName("total_spent") val totalSpent: Double,
    @SerializedName("total_liters") val totalLiters: Double,
    @SerializedName("avg_consumption") val avgConsumption: Double?,   // null если < 2 заправок
    @SerializedName("best_consumption") val bestConsumption: Double?, // null если < 2 заправок
    @SerializedName("worst_consumption") val worstConsumption: Double?,
    @SerializedName("avg_price_per_liter") val avgPricePerLiter: Double,
    @SerializedName("refuels_count") val refuelsCount: Int,
    @SerializedName("total_km") val totalKm: Int
)

data class MonthlyDataDto(
    @SerializedName("month") val month: String,
    @SerializedName("total_spent") val totalSpent: Double,
    @SerializedName("total_liters") val totalLiters: Double,
    @SerializedName("avg_consumption") val avgConsumption: Double?, // null если нет consumption
    @SerializedName("avg_price") val avgPrice: Double,
    @SerializedName("refuels_count") val refuelsCount: Int
)

data class ConsumptionPointDto(
    @SerializedName("date") val date: String,
    @SerializedName("consumption") val consumption: Double?, // null для первой заправки
    @SerializedName("odometer") val odometer: Int
)