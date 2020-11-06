package com.example.newsee

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.viewpager2.widget.ViewPager2
import java.util.*


/**
 * A foreground service for managing the life cycle of overlay view.
 */
@RequiresApi(Build.VERSION_CODES.R)
class ViewPagerAutoScrollService : Service() {
    companion object {
        private const val ACTION_START = "START"
        private const val ACTION_STOP = "STOP"
        private lateinit var viewPager: ViewPager2

        fun start(context: Context, viewPager: ViewPager2) {
            val intent = Intent(context, ViewPagerAutoScrollService::class.java).apply {
                action = ACTION_START
            }
            this.viewPager = viewPager
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, ViewPagerAutoScrollService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private var timer: Timer? = null
    private val scrollHandler = Handler(Looper.getMainLooper())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START -> {
                    startTimer()
                }
                ACTION_STOP -> {
                    stopTimer()
                    stopSelf()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /** This service does not support binding. */
    override fun onBind(intent: Intent?) = null

    private fun startTimer() {
        timer = Timer(true)
        timer?.schedule(object : TimerTask() {
            override fun run() {
                scrollHandler.post(Runnable {
                    if (viewPager.currentItem < (viewPager.adapter?.itemCount ?: 0) - 1) {
                        viewPager.currentItem += 1
                    } else {
                        viewPager.currentItem = 0
                    }
                })
            }
        }, 10000, 10000)
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }
}
