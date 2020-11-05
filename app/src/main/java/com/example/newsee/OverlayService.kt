package com.example.newsee

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2


/**
 * A foreground service for managing the life cycle of overlay view.
 */
@RequiresApi(Build.VERSION_CODES.R)
class OverlayService : Service() {
    companion object {
        private const val ACTION_SHOW = "SHOW"
        private const val ACTION_HIDE = "HIDE"

        fun start(context: Context) {
            val intent = Intent(context, OverlayService::class.java).apply {
                action = ACTION_SHOW
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, OverlayService::class.java).apply {
                action = ACTION_HIDE
            }
            context.startService(intent)
        }

        // To control toggle button in MainActivity. This is not elegant but works.
        var isActive = false
            private set
    }

    private lateinit var overlayView: OverlayView
    private lateinit var viewPager: ViewPager2

    override fun onCreate() {
        overlayView = OverlayView.create(this)
        overlayView.findViewById<View>(R.id.pager)

        viewPager = overlayView.findViewById(R.id.pager)
        viewPager.adapter = MovablePagerAdapter(overlayView, viewPager)
        refreshHandler()
    }

    /** Handles [ACTION_SHOW] and [ACTION_HIDE] intents. */
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_SHOW -> {
                    isActive = true
                    overlayView.show()
                }
                ACTION_HIDE -> {
                    isActive = false
                    overlayView.hide()
                    stopSelf()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /** Cleans up views just in case. */
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onDestroy() = overlayView.hide()

    /** This service does not support binding. */
    override fun onBind(intent: Intent?) = null

    private fun refreshHandler() {
        val handlerThread = HandlerThread("carousel")
        handlerThread.start()
        Handler(handlerThread.looper).postDelayed({
            if (viewPager.currentItem < (viewPager.adapter?.itemCount ?: 0) - 1) {
                viewPager.currentItem += 1
            }
            // 最初に戻ろうとすると落ちる
            // TODO: serviceに移行してメインスレッドで処理させる
//            else {
//                viewPager.currentItem = 0
//            }
            refreshHandler()
        }, 5000)
    }
}
