package com.example.newsee

import android.app.PictureInPictureParams
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.provider.Settings
import android.util.Log
import android.util.Rational
import android.view.View
import android.view.WindowInsets
import android.widget.Button
import android.widget.ToggleButton
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity() {
    private var feedsBinder : FeedsService.FeedsBinder? = null
    private var handlerThread: HandlerThread? = null
    private lateinit var viewPager: ViewPager2

//    private val mConnection = object : ServiceConnection {
//        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
//            feedsBinder = binder as FeedsService.FeedsBinder
//        }
//
//        override fun onServiceDisconnected(name: ComponentName?) {
//            feedsBinder = null
//        }
//    }

    companion object {
        /** ID for the runtime permission dialog */
        private const val OVERLAY_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestOverlayPermission()

        viewPager = findViewById(R.id.pager)
        val adapter = ScreenSlidePagerAdapter(this)
        viewPager.adapter = adapter

        // Show/hide overlay view with a toggle button.
        findViewById<ToggleButton>(R.id.toggle_button).apply {
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    supportActionBar?.hide()
                    viewPager.visibility = View.VISIBLE
                    startViewPagerHandler()
                } else {
                    supportActionBar?.show()
                    viewPager.visibility = View.INVISIBLE
                }
            }
//            isChecked = OverlayService.isActive
//            setOnCheckedChangeListener { _, isChecked ->
//                if (isChecked)
//                    OverlayService.start(this@MainActivity)
//                else
//                    OverlayService.stop(this@MainActivity)
//            }
        }

//        val intent = Intent(applicationContext, FeedsService::class.java)
//        bindService(intent, mConnection, Context.BIND_AUTO_CREATE)

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

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onUserLeaveHint() {
        enterPictureInPictureMode()
        super.onUserLeaveHint()
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration?) {
        if (isInPictureInPictureMode) {
            findViewById<ToggleButton>(R.id.toggle_button).visibility = View.INVISIBLE
        } else {
            findViewById<ToggleButton>(R.id.toggle_button).visibility = View.VISIBLE
        }
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
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

    private fun startViewPagerHandler() {
        handlerThread = HandlerThread("carousel")
        handlerThread?.start()
        handlerThread?.let {
            Handler(it.looper).postDelayed({
                Log.d("HOGGE'", "FUGA")
                if (viewPager.currentItem < (viewPager.adapter?.itemCount ?: 0) - 1) {
                    viewPager.currentItem += 1
                }
                // 最初に戻ろうとすると落ちる
//            else {
//                viewPager.currentItem = 0
//            }
//                startViewPagerHandler()
            }, 5000)
        }
    }

    private class ScreenSlidePagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 5

        override fun createFragment(position: Int): Fragment = OverlaySlideItemFragment()
    }
}
