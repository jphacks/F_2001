package com.example.newsee

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


/**
 * A background service for fetching RSS feeds.
 */
class FeedsService : Service() {
    private val binder = FeedsBinder()
    private val feeds = mutableListOf<Feed>()

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, FeedsService::class.java)
            context.startService(intent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        fetchFeeds("https://news.yahoo.co.jp/rss/topics/", "top-picks.xml")

        return 0
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    private fun fetchFeeds(url : String, endpoint: String) {
        val retrofit : Retrofit = Retrofit.Builder()
                .baseUrl(url)
                .client(OkHttpClient.Builder().build())
                .addConverterFactory(TikXmlConverterFactory.create())
                .build()

        retrofit.create(FeedApi::class.java).fetchFeeds(endpoint).enqueue(object : Callback<YahooFeedsResponse> {
            //非同期処理
            override fun onResponse(call: Call<YahooFeedsResponse>, response: Response<YahooFeedsResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        it.channel.items.forEach { item ->
                            feeds.add(
                                Feed(
                                    title = item.title,
                                    description = item.description,
                                    pubDate = item.pubDate,
                                    link = item.link,
                                    comments = item.comments
                                )
                            )
                        }
                    }
                } else {
                    Log.d("NOT SUCCESS", response.toString())
                }
            }
            override fun onFailure(call: Call<YahooFeedsResponse>, t: Throwable) {
                Log.d("FAILURE", t.toString())
            }
        })
    }

    inner class FeedsBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getFeeds() = feeds
    }
}
