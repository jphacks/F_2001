package com.example.newsee

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface FeedApi {
    @GET
    fun fetchFeeds(
        @Url url: String
    ): Call<YahooFeedsResponse>
}
