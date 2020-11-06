package com.example.newsee

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.ToggleButton
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2


@RequiresApi(Build.VERSION_CODES.R)
class MainActivity : AppCompatActivity() {
    // bookmarks
    private var bookmarkListAdapter : BookmarkListAdapter? = null
    // feeds
    private var feedsBinder : FeedsService.FeedsBinder? = null
    private val feedsConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            feedsBinder = binder as FeedsService.FeedsBinder
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            feedsBinder = null
        }
    }
    // toolbar
    private lateinit var toolbar: Toolbar

    companion object {
        /** ID for the runtime permission dialog */
        private const val OVERLAY_PERMISSION_REQUEST_CODE = 1
    }

    override fun onStart() {
        super.onStart()

        BookmarksService.start(this@MainActivity) {
            // bookmarkに変化があったときにnotifyする
            bookmarkListAdapter?.notifyDataSetChanged()
            OverlayService.notifyToAdapter()
        }

        // TODO: 「インターネットにつないでください」的なアラートを出す
        val feedIntent = Intent(applicationContext, FeedsService::class.java)
        bindService(feedIntent, feedsConnection, Context.BIND_AUTO_CREATE)
        FeedsService.start(this@MainActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestOverlayPermission()

        // Show/hide overlay view with a toggle button.
        findViewById<ToggleButton>(R.id.toggle_button).apply {
            isChecked = OverlayService.isActive
            setOnCheckedChangeListener { view, isChecked ->
                if (isChecked)
                    OverlayService.start(this@MainActivity, feedsBinder) {
                        // ボタン以外でオーバーレイが閉じられたときに切り替え
                        view.isChecked = false
                    }
                else
                    OverlayService.stop(this@MainActivity)
            }
        }

        initTutorialSlide()
        initBookmarkList()

        findViewById<ImageView>(R.id.action_settings).setOnClickListener {
            val intent = Intent(applicationContext, SettingActivity::class.java)
            startActivity(intent)
        }
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

    override fun onDestroy() {
        super.onDestroy()
        unbindService(feedsConnection)
    }

    private fun initTutorialSlide() {
        val tutorialViewPager = findViewById<ViewPager2>(R.id.tutorial_pager)
        val pagerAdapter = ScreenSlidePagerAdapter(this)
        tutorialViewPager.adapter = pagerAdapter
    }

    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment = ScreenSlidePagerFragment()
    }

    private fun initBookmarkList() {
            bookmarkListAdapter = BookmarkListAdapter(this, R.layout.bookmark_list_item, BookmarksService.bookmarkResults) { link: String ->
                val uri = Uri.parse(link)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            findViewById<NonScrollListView>(R.id.bookmark_list).adapter = bookmarkListAdapter
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
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            // User chose the "Settings" item, show the app settings UI...
            val intent = Intent(applicationContext, SettingActivity::class.java)
            startActivity(intent)
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // super.onCreateOptionsMenu(menu)
        // menuInflater.inflate(R.menu.setting_icon, menu)
        return true
    }
}
