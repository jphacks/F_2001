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

@RequiresApi(Build.VERSION_CODES.N)
class FeedsService : Service() {
    private var timer: Timer? = null
    private val fetchHandler = Handler(Looper.getMainLooper())
    private val binder = FeedsBinder()
    private val existingFeedLinks = mutableListOf<String>()

    companion object {
        private val feeds = mutableListOf<Feed>()

        fun start(context: Context) {
            val intent = Intent(context, FeedsService::class.java)
            context.startService(intent)
        }

        fun bookmark(feed: Feed) {
            BookmarksService.createFromFeed(feed)
            feeds.replaceAll {
                if (it.link == feed.link) it.apply { bookmarked = true } else it
            }
        }

        fun unBookmark(link: String) {
            BookmarksService.delete(link)
            feeds.replaceAll {
                if (it.link == link) it.apply { bookmarked = false } else it
            }
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
                    response.body()?.let { response ->
                        val bookmarkLinks = BookmarksService.bookmarkResults.toList().map { it.link }
                        val newFeeds = response.channel.items.filter {
                            !existingFeedLinks.contains(it.link)
                        }.map { item ->
                            Feed(
                                title = item.title,
                                description = item.description,
                                pubDate = item.pubDate,
                                link = item.link,
                                bookmarked = bookmarkLinks.contains(item.link)
                            )
                        }
                        feeds.addAll(newFeeds)
                        existingFeedLinks.addAll(newFeeds.map { it.link })
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

    // FIXME: Yahooのみ対応
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
