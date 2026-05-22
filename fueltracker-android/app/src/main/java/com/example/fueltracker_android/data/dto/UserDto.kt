package com.example.fueltracker_android.data.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id")         val id: Int,
    @SerializedName("email")      val email: String,
    @SerializedName("created_at") val createdAt: String
)
