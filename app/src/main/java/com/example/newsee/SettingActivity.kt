package com.example.newsee

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setting_activity)
        val settingsFragment = SettingsFragment()
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, settingsFragment)
                .commit()

        val toolbar = supportActionBar
        //set actionbar title
        // toolbar.setTitle('hoge')
        // actionbar!!.title = "設定"
        //set back button
        // actionbar.setDisplayHomeAsUpEnabled(true)
        // actionbar.setDisplayHomeAsUpEnabled(true)
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }
}
