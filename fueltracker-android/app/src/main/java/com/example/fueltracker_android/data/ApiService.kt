package com.example.fueltracker_android.data

import com.example.fueltracker_android.data.dto.ConsumptionPointDto
import com.example.fueltracker_android.data.dto.MonthlyDataDto
import com.example.fueltracker_android.data.dto.RefuelDto
import com.example.fueltracker_android.data.dto.SummaryDto
import com.example.fueltracker_android.data.dto.UserDto
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

// ── Auth request bodies ──────────────────────────────────────────────────────

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String
)

// ── Refuel request/response bodies ──────────────────────────────────────────

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

// ── Stats response wrappers ──────────────────────────────────────────────────

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

// ── Interface ────────────────────────────────────────────────────────────────

interface ApiService {

    // ── Auth ─────────────────────────────────────────────────────────────────

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): UserDto

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): UserDto

    @POST("auth/logout")
    suspend fun logout(): Map<String, String>

    @GET("auth/me")
    suspend fun me(): UserDto

    // ── Refuels ───────────────────────────────────────────────────────────────

    @POST("refuels")
    suspend fun createRefuel(
        @Body request: CreateRefuelRequest
    ): RefuelDto

    @GET("refuels")
    suspend fun getRefuels(
        @Query("limit")  limit: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("month")  month: String? = null
    ): RefuelListResponse

    @DELETE("refuels/{id}")
    suspend fun deleteRefuel(
        @Path("id") id: Int
    ): DeleteResponse

    // ── Stats ─────────────────────────────────────────────────────────────────

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

    // ── Factory ───────────────────────────────────────────────────────────────

    companion object {
        /**
         * Принимает [cookieJar] из AppDependencies, чтобы все запросы
         * использовали одну и ту же сессионную cookie.
         */
        fun create(cookieJar: SessionCookieJar): ApiService {
            val client = OkHttpClient.Builder()
                .cookieJar(cookieJar)           // ← сессионная cookie ft_session
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
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
