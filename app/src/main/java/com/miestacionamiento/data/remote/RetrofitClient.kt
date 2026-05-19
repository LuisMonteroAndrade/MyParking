package com.miestacionamiento.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Para conectar el telefono via USB (adb reverse tcp:3000 tcp:3000):
    //   BASE_URL = "http://10.0.2.2:3000/api/"
    // Para conectar via WiFi (misma red):
    //   BASE_URL = "http://192.168.X.X:3000/api/"  <- cambiar a tu IP local
    private const val BASE_URL = "http://192.168.1.34:3000/api/"

    val authInterceptor = AuthInterceptor()

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
