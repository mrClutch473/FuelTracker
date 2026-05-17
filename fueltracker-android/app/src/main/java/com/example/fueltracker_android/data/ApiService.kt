package com.example.fueltracker_android.data

import com.example.fueltracker_android.data.dto.ConsumptionPointDto
import com.example.fueltracker_android.data.dto.MonthlyDataDto
import com.example.fueltracker_android.data.dto.RefuelDto
import com.example.fueltracker_android.data.dto.SummaryDto
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

data class CreateRefuelRequest(
    val liters: Double,
    val price: Double,
    val odometer: Int,
    val note: String?
)

data class RefuelListResponse(
    val items: List<RefuelDto>,
    val total: Int,
    val limit: Int,
    val offset: Int
)

data class MonthlyStatsResponse(
    val data: List<MonthlyDataDto>
)

data class ConsumptionTrendResponse(
    val points: List<ConsumptionPointDto>
)

data class DeleteResponse(
    val message: String,
    val id: Int
)

interface ApiService {

    @POST("refuels")
    suspend fun createRefuel(
        @Body request: CreateRefuelRequest
    ): RefuelDto

    @GET("refuels")
    suspend fun getRefuels(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("month") month: String? = null
    ): RefuelListResponse

    @DELETE("refuels/{id}")
    suspend fun deleteRefuel(
        @Path("id") id: Int
    ): DeleteResponse

    @GET("stats/summary")
    suspend fun getSummary(): SummaryDto

    @GET("stats/monthly")
    suspend fun getMonthlyStats(
        @Query("months") months: Int = 6
    ): MonthlyStatsResponse

    @GET("stats/consumption-trend")
    suspend fun getConsumptionTrend(
        @Query("limit") limit: Int = 20
    ): ConsumptionTrendResponse

    companion object {
        fun create(): ApiService {
            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl("http://192.168.0.7:8000/api/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(ApiService::class.java)
        }
    }
}