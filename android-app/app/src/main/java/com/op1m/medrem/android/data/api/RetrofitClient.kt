package com.op1m.medrem.android.data.api

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/api/"

    private var retrofit: Retrofit? = null

    private var authToken: String? = null

    fun getApiService(): ApiService {
        return getRetrofit().create(ApiService::class.java)
    }

    fun setAuthCredentials(username: String, password: String) {
        authToken = Credentials.basic(username, password)
    }

    fun clearAuth() {
        authToken = null
    }

    private fun getRetrofit(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(createOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()

            val newRequest = authToken?.let { token ->
                originalRequest.newBuilder()
                    .header("Authorization", token)
                    .build()
            } ?: originalRequest

            try {
                chain.proceed(newRequest)
            } catch (e: Exception) {
                android.util.Log.e("RetrofitClient", "Network error: ${e.message}")
                throw e
            }
        }

        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .build()
    }
}