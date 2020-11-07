package com.example.newsee

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import io.realm.Realm
import io.realm.RealmConfiguration


class BookmarksService : Service() {
    companion object {
        private val realmConfig = RealmConfiguration.Builder().allowWritesOnUiThread(true).build()
        val realm = Realm.getInstance(realmConfig)
        val bookmarkResults = realm.where(Bookmark::class.java).findAll()
        private val bookmarkLinks = mutableListOf<String>()
        private lateinit var onBookmarksChanged: () -> Unit

        fun start(context: Context, onBookmarksChanged: () -> Unit) {
            val intent = Intent(context, BookmarksService::class.java)
            this.onBookmarksChanged = onBookmarksChanged
            context.startService(intent)
        }

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
            if (existsBookmarkOnRealm(bookmark.link))
                return
            realm.executeTransaction {
                it.insert(bookmark)
            }
            bookmarkLinks.add(bookmark.link)
            onBookmarksChanged()
        }

        fun delete(link: String) {
            if (!existsBookmarkOnRealm(link))
                return
            realm.executeTransaction {
                it.where(Bookmark::class.java).equalTo("link", link).findAll().deleteAllFromRealm()
            }
            bookmarkLinks.remove(link)
            onBookmarksChanged()
        }

        fun existsBookmarkOnRealm(link: String): Boolean {
            return bookmarkLinks.contains(link) && realm.where(Bookmark::class.java).equalTo("link", link).findFirst() != null
        }
    }

    override fun onCreate() {
        bookmarkLinks.addAll(bookmarkResults.toList().map { it.link })
        super.onCreate()
    }

    override fun onBind(intent: Intent?) = null
}
