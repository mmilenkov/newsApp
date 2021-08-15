package com.example.newsapp.api

import com.example.newsapp.models.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import com.example.newsapp.util.Constants

interface NewsAPI {

    @GET("/v2/top-headlines")
    suspend fun getTopHeadlines(@Query("country") countryCode: String = "gb",
                                @Query("page") pageNumber: Int = 1,
                                @Query("apiKey") apiKey: String = Constants.API_KEY): Response<NewsResponse>

    @GET("/v2/everything")
    suspend fun getSearchResults(@Query("q") searchTerm: String,
                                @Query("page") pageNumber: Int = 1,
                                @Query("apiKey") apiKey: String = Constants.API_KEY): Response<NewsResponse>


}