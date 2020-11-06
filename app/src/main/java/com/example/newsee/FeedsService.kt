package com.example.newsee

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.util.*


/**
 * A background service for fetching RSS feeds.
 */
class FeedsService : Service() {
    private var timer: Timer? = null
    private val fetchHandler = Handler(Looper.getMainLooper())
    private val binder = FeedsBinder()
    private val feeds = mutableListOf<Feed>()
    private val existingFeedLinks = mutableListOf<String>()

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, FeedsService::class.java)
            context.startService(intent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startTimer()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onDestroy() {
        stopTimer()
    }

    private fun startTimer() {
        timer = Timer(true)
        timer?.schedule(object : TimerTask() {
            override fun run() {
                fetchHandler.post(Runnable {
                    fetchFeeds(currentUrl(), currentXml())
                })
            }
        }, 0, 10000)
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
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
                            if (existingFeedLinks.contains(item.link))
                                return

                            // TODO: 先頭に追加する? 後にたす?
                            feeds.add(
                                Feed(
                                    title = item.title,
                                    description = item.description,
                                    pubDate = item.pubDate,
                                    link = item.link,
                                    comments = item.comments
                                )
                            )
                            existingFeedLinks.add(item.link)
                        }
                        Log.d("FETCHED", it.channel.items.toString())
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

    // FIXME: 今だけ
    private var currentXmlIndex: Int = -1
    private val xmls: List<String> = listOf<String>("domestic.xml", "world.xml", "business.xml", "entertainment.xml", "sports.xml", "it.xml", "science.xml", "local.xml")
    private fun currentUrl(): String {
        return "https://news.yahoo.co.jp/rss/topics/"
    }
    private fun currentXml(): String {
        currentXmlIndex++
        if (currentXmlIndex >= xmls.size)
            currentXmlIndex = 0
        return xmls[currentXmlIndex]
    }
}
