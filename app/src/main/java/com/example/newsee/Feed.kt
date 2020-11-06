package com.example.newsee

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required


data class Feed(
    var title: String,
    var description: String,
    var link: String,
    var pubDate: String
)

open class Bookmark(
    @PrimaryKey var link: String = "",
    @Required var title: String = "",
    @Required var description: String = "",
    @Required var pubDate: String = ""
) : RealmObject()
