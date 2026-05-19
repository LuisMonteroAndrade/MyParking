package com.miestacionamiento.data.remote

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private var token: String = "") : Interceptor {

    fun setToken(newToken: String) {
        token = newToken
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val request = if (token.isNotEmpty()) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        return chain.proceed(request)
    }
}
