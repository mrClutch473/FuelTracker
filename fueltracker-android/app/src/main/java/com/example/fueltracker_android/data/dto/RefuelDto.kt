package com.example.fueltracker_android.data.dto

import com.google.gson.annotations.SerializedName

data class RefuelDto(
    @SerializedName("id") val id: Int,
    @SerializedName("liters") val liters: Double,
    @SerializedName("price") val price: Double,
    @SerializedName("total_cost") val totalCost: Double,
    @SerializedName("odometer") val odometer: Int,
    @SerializedName("consumption") val consumption: Double?,
    @SerializedName("note") val note: String?,
    @SerializedName("created_at") val createdAt: String
)
