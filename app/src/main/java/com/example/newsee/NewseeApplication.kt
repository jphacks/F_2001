package com.example.newsee

import android.app.Application
import io.realm.Realm

class NewseeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }
}
