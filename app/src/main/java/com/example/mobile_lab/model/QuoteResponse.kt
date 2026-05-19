package com.example.mobile_lab.model

import com.google.gson.annotations.SerializedName

data class QuoteResponse(
    @SerializedName("_id") val id: String?,
    @SerializedName("content") val content: String?,
    @SerializedName("author") val author: String?,
    @SerializedName("tags") val tags: List<String>?
)
