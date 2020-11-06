package com.example.newsee

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
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
        const val ACTION_SHOW = "SHOW"
        const val ACTION_HIDE = "HIDE"

        private lateinit var feedsBinder : FeedsService.FeedsBinder
        private lateinit var onStopListener : () -> Unit

        fun start(context: Context, binder: FeedsService.FeedsBinder?, listener: () -> Unit) {
            binder ?: return

            val intent = Intent(context, OverlayService::class.java).apply {
                action = ACTION_SHOW
            }
            feedsBinder = binder
            onStopListener = listener
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Start as a foreground service
            val notification = OverlayNotification.build(this)
            startForeground(1, notification)
        }

        // setup overlay view and view pager
        overlayView = OverlayView.create(this)
        overlayView.findViewById<View>(R.id.pager)
        viewPager = overlayView.findViewById(R.id.pager)
        viewPager.adapter = MovablePagerAdapter(overlayView, feedsBinder,
            { longClicked: Boolean ->
                // viewPagerの要素が長押しされたとき / 離されたとき
                if (longClicked) {
                    viewPager.isUserInputEnabled = false
                    ViewPagerAutoScrollService.stop(this)
                } else {
                    viewPager.isUserInputEnabled = true
                    ViewPagerAutoScrollService.start(this, viewPager)
                }
            },
            { link: String ->
                val uri = Uri.parse(link)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                hideOverlayView()
            }
        )
    }

    /** Handles [ACTION_SHOW] and [ACTION_HIDE] intents. */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_SHOW -> {
                    showOverlayView()
                }
                ACTION_HIDE -> {
                    hideOverlayView()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /** Cleans up views just in case. */
    override fun onDestroy() {
        overlayView.hide()
        ViewPagerAutoScrollService.stop(this)
    }

    /** This service does not support binding. */
    override fun onBind(intent: Intent?) = null

    private fun showOverlayView() {
        isActive = true
        overlayView.show()
        ViewPagerAutoScrollService.start(this, viewPager)
    }

    private fun hideOverlayView() {
        isActive = false
        overlayView.hide()
        ViewPagerAutoScrollService.stop(this)
        onStopListener()
        stopSelf()
    }
}
