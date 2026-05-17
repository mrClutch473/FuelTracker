package com.example.fueltracker_android.domain.model

data class Refuel(
    val id: Int,
    val liters: Double,
    val price: Double,
    val totalCost: Double,
    val odometer: Int,
    val consumption: Double?,
    val note: String?,
    val createdAt: String
)
