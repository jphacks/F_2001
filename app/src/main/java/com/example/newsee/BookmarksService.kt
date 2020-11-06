package com.example.newsee

import android.app.Service
import android.content.Intent
import android.os.*
import io.realm.Realm
import io.realm.RealmConfiguration


class BookmarksService : Service() {
    private val binder = BookmarksBinder()

    companion object {
        private val realmConfig = RealmConfiguration.Builder().allowWritesOnUiThread(true).build()
        val realm = Realm.getInstance(realmConfig)
        private val bookmarks = mutableListOf<Bookmark>()
        private val bookmarkLinks = mutableListOf<String>()

        fun createFromFeed(feed: Feed) {
            val bookmark = Bookmark(
                title = feed.title,
                description = feed.description,
                link = feed.link,
                pubDate = feed.pubDate
            )

            if (bookmarkLinks.contains(feed.link))
                return
            realm.executeTransaction {
                it.insert(bookmark)
            }
            bookmarks.add(bookmark)
            bookmarkLinks.add(feed.link)
        }

        fun deleteFromFeed(feed: Feed) {
            realm.executeTransaction {
                it.where(Bookmark::class.java).equalTo("link", feed.link).findAll().deleteAllFromRealm()
            }
            bookmarks.removeAll { it.link == feed.link }
            bookmarkLinks.remove(feed.link)
        }
    }

    override fun onCreate() {
        bookmarks.addAll(realm.where(Bookmark::class.java).findAll().toList())
        bookmarkLinks.addAll(bookmarks.map { it.link })
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    inner class BookmarksBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getBookmarks() = bookmarks
    }
}
