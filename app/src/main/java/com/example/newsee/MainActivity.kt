package com.example.newsee

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity() {
    private var feedsBinder : FeedsService.FeedsBinder? = null

    private lateinit var overlayView: OverlayView
    private lateinit var viewPager: ViewPager2

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            feedsBinder = binder as FeedsService.FeedsBinder
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            feedsBinder = null
        }
    }

    companion object {
        /** ID for the runtime permission dialog */
        private const val OVERLAY_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestOverlayPermission()
        createOverlayView()

        viewPager.setOnClickListener {
            if (viewPager.currentItem > 0) {
                viewPager.currentItem = viewPager.currentItem - 1
            }
        }

        // Show/hide overlay view with a toggle button.
        findViewById<ToggleButton>(R.id.toggle_button).apply {
            isChecked = OverlayService.isActive
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked)
                    OverlayService.start(this@MainActivity, overlayView, viewPager)
                else
                    OverlayService.stop(this@MainActivity)
            }
        }

        val intent = Intent(applicationContext, FeedsService::class.java)
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE)

        // FeedsService.start(this@MainActivity)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // ユーザから許可が得られなかったらアプリを終了させる
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (!isOverlayGranted()) {
                finish()  // Cannot continue if not granted
            }
        }
    }
    override fun onBackPressed() {
        if (viewPager.currentItem == 4) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else {
            // Otherwise, select the previous step.
            viewPager.currentItem = viewPager.currentItem + 1
        }
    }

    /* 必要に応じてユーザに権限をリクエスト */
    private fun requestOverlayPermission() {
        if (isOverlayGranted()) return
        val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
    }

    /** オーバーレイの権限があるかどうかチェック */
    private fun isOverlayGranted() =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                    Settings.canDrawOverlays(this)

    private fun createOverlayView() {
        overlayView = OverlayView.create(this)
        viewPager = overlayView.findViewById<ViewPager2>(R.id.pager)
        viewPager.adapter = ScreenSlidePagerAdapter(this)
    }

    private class ScreenSlidePagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 5

        override fun createFragment(position: Int): Fragment = OverlaySlideItemFragment()
    }
}
