package com.example.newsee

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml


@Xml(name = "rss")
data class YahooFeedsResponse(
    @Attribute(name = "version")
    val version: String,
    @Element(name = "channel")
    val channel: YahooChannelResponse
)

@Xml(name = "channel")
data class YahooChannelResponse(
    @PropertyElement(name = "language")
    val language: String,
    @PropertyElement(name = "copyright")
    val copyright: String,
    @PropertyElement(name = "title")
    val title: String,
    @PropertyElement(name = "description")
    val description: String,
    @PropertyElement(name = "link")
    val link: String,
    @PropertyElement(name = "pubDate")
    val pubDate: String,
    @Element(name = "item")
    val items: List<FeedResponse>
)

@Xml(name = "item")
data class FeedResponse(
    @PropertyElement(name = "title")
    val title: String,
    @PropertyElement(name = "description")
    val description: String,
    @PropertyElement(name = "link")
    val link: String,
    @PropertyElement(name = "pubDate")
    val pubDate: String,
    @PropertyElement(name = "comments")
    val comments: String?
)
