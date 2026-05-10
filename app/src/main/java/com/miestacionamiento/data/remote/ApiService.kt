package com.miestacionamiento.data.remote

import com.miestacionamiento.data.model.Parking
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("parkings")
    suspend fun getParkings(): Response<List<Parking>>

    @GET("parkings/{id}")
    suspend fun getParkingById(@Path("id") id: Int): Response<Parking>

    @GET("parkings/search")
    suspend fun searchParkings(@Query("q") query: String): Response<List<Parking>>
}
