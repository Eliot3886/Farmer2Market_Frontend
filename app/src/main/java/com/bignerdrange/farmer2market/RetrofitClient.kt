package com.bignerdrange.farmer2market

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    /** 
     * IMPORTANT IP CONFIGURATION:
     * - If using Android Emulator: Use "http://192.168.100.17:8000/"
     * - If using Real Device: Use your computer's local IP (e.g., "http://192.168.1.5:8000/")
     *   Make sure both devices are on the SAME Wi-Fi network.
     */
    const val BASE_URL = "https://farmer2market-78uo.onrender.com/"

    private var authToken: String? = null

    fun setToken(token: String?) {
        authToken = token
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            authToken?.let {
                requestBuilder.addHeader("Authorization", "Bearer $it")
            }
            chain.proceed(requestBuilder.build())
        }
        .addInterceptor(HttpLoggingInterceptor().apply { 
            level = HttpLoggingInterceptor.Level.BODY 
        })
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
