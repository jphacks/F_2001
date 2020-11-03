package com.example.newsee

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

/**
 * A foreground service for managing the life cycle of overlay view.
 */
class OverlayService : Service() {
    companion object {
        private const val ACTION_SHOW = "SHOW"
        private const val ACTION_HIDE = "HIDE"
        private lateinit var adapter: ScreenSlidePagerAdapter

        fun start(context: Context) {
            val intent = Intent(context, OverlayService::class.java).apply {
                action = ACTION_SHOW
            }

            adapter = ScreenSlidePagerAdapter(context as AppCompatActivity)
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
        viewPager = overlayView.findViewById(R.id.pager)
        viewPager.adapter = adapter
    }

    /** Handles [ACTION_SHOW] and [ACTION_HIDE] intents. */
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
    override fun onDestroy() = overlayView.hide()

    /** This service does not support binding. */
    override fun onBind(intent: Intent?) = null

    private class ScreenSlidePagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 5

        override fun createFragment(position: Int): Fragment = OverlaySlideItemFragment()
    }
}
