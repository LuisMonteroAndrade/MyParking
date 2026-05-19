package com.miestacionamiento.data.remote

import com.miestacionamiento.data.model.AuthResponse
import com.miestacionamiento.data.model.LoginRequest
import com.miestacionamiento.data.model.Parking
import com.miestacionamiento.data.model.RegisterRequest
import com.miestacionamiento.data.model.SaveResponse
import com.miestacionamiento.data.model.UserProfile
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // --- Autenticacion ---

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    // --- Estacionamientos ---

    @GET("parkings")
    suspend fun getParkings(
        @Header("X-User-Id") userId: Int? = null
    ): Response<List<Parking>>

    @GET("parkings/{id}")
    suspend fun getParkingById(
        @Path("id") id: Int,
        @Header("X-User-Id") userId: Int? = null
    ): Response<Parking>

    @GET("parkings/search")
    suspend fun searchParkings(
        @Query("q") query: String
    ): Response<List<Parking>>

    @GET("parkings/saved")
    suspend fun getSavedParkings(): Response<List<Parking>>

    @GET("parkings/recent")
    suspend fun getRecentParkings(): Response<List<Parking>>

    @POST("parkings/{id}/save")
    suspend fun toggleSave(@Path("id") id: Int): Response<SaveResponse>

    @POST("parkings/{id}/view")
    suspend fun markAsViewed(@Path("id") id: Int): Response<Void>

    // --- Perfil de usuario ---

    @GET("users/profile")
    suspend fun getProfile(): Response<UserProfile>

    @PUT("users/profile")
    suspend fun updateProfile(@Body profile: Map<String, String?>): Response<UserProfile>
}
