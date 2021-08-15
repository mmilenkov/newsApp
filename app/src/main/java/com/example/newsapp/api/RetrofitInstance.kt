package com.example.newsapp.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.newsapp.util.Constants
import okhttp3.logging.HttpLoggingInterceptor

class RetrofitInstance {

    companion object {
        private val retrofit by lazy {
            val interceptor = HttpLoggingInterceptor()
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build()
            Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }

        val apiFascade by lazy {
            retrofit.create(NewsAPI::class.java)
        }
    }
}