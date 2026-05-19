package com.example.mobile_lab.network

import com.example.mobile_lab.model.QuoteResponse
import retrofit2.http.GET

interface QuoteApiService {
    @GET("random")
    suspend fun getRandomQuote(): QuoteResponse
}
