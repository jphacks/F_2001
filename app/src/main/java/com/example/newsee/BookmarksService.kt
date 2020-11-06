package com.example.newsee

import android.app.Service
import android.content.Context
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
           create(
               Bookmark(
                    title = feed.title,
                    description = feed.description,
                    link = feed.link,
                    pubDate = feed.pubDate
               )
           )
        }

        fun create(bookmark: Bookmark) {
            if (bookmarkLinks.contains(bookmark.link))
                return
            realm.executeTransaction {
                it.insert(bookmark)
            }
            bookmarks.add(bookmark)
            bookmarkLinks.add(bookmark.link)
            onBookmarksChanged()
        }

        fun delete(link: String) {
            realm.executeTransaction {
                it.where(Bookmark::class.java).equalTo("link", link).findAll().deleteAllFromRealm()
            }
            bookmarks.removeAll { it.link == link }
            bookmarkLinks.remove(link)
            onBookmarksChanged()
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
