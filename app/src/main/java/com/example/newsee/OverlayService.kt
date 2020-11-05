package com.example.newsee

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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

    private val displaySize: Point by lazy {
        val size = Point()
        overlayView.windowManager.currentWindowMetrics.bounds
        size
    }

    private var isLongClick: Boolean = false

    override fun onCreate() {
        overlayView = OverlayView.create(this)
        overlayView.apply(clickListener())

        viewPager = overlayView.findViewById(R.id.pager)
        viewPager.adapter = adapter
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

    private class ScreenSlidePagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 5

        override fun createFragment(position: Int): Fragment = OverlaySlideItemFragment()
    }

    private fun refreshHandler() {
        val handlerThread = HandlerThread("carousel")
        handlerThread.start()
        Handler(handlerThread.looper).postDelayed({
            if (viewPager.currentItem < (viewPager.adapter?.itemCount ?: 0) - 1) {
                viewPager.currentItem += 1
            }
            // 最初に戻ろうとすると落ちる
//            else {
//                viewPager.currentItem = 0
//            }
            refreshHandler()
        }, 5000)
    }

    private fun clickListener(): View.() -> Unit {
        return {
            setOnLongClickListener { view ->
                isLongClick = true
                // ロングタップ状態が分かりやすいように背景色を変える
                view.setBackgroundResource(R.color.white)
                false
            }.apply {
                setOnTouchListener { view, motionEvent ->
                    Log.d("OverlayView", "click " + isLongClick.toString())
                    val x = motionEvent.rawX.toInt()
                    val y = motionEvent.rawY.toInt()

                    when (motionEvent.action) {
                        MotionEvent.ACTION_MOVE -> {
                            if (isLongClick) {
                                val centerX = x - (displaySize.x / 2)
                                val centerY = y - (displaySize.y / 2)

                                // オーバーレイ表示領域の座標を移動させる
                                overlayView.layoutParams.x = centerX
                                overlayView.layoutParams.y = centerY
                                overlayView.windowManager.updateViewLayout(overlayView, layoutParams)
                            }
                        }
                        MotionEvent.ACTION_UP -> {
                            if (isLongClick) {

                                // 背景色を戻す
                                view.setBackgroundResource(android.R.color.transparent)
                            }
                            isLongClick = false
                        }
                    }
                    false
                }
            }
        }
    }
}
