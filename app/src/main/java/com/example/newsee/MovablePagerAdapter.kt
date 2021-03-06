package com.example.newsee

import android.graphics.Point
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import org.w3c.dom.Text

@RequiresApi(Build.VERSION_CODES.R)
class MovablePagerAdapter(private val overlayView: OverlayView, private val binder: FeedsService.FeedsBinder, private val notifyLongClick: ((longClicked: Boolean) -> Unit)?, private val moveBrowser: ((link: String) -> Unit)?) :
        RecyclerView.Adapter<MovablePagerAdapter.ItemViewHolder>() {

    private var isLongClick: Boolean = false
    private var isFirstMove: Boolean = false
    private var moveOffsetX: Int = 0
    private var moveOffsetY: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovablePagerAdapter.ItemViewHolder =
            ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.overlay_slide_item, parent, false), parent)

    override fun getItemCount(): Int = binder.getFeeds().size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        binder.getFeeds()[position].let { feed ->
            holder.titleText.text = feed.title
            holder.descriptionText.text = feed.description
            holder.linkButton.setOnClickListener {
                moveBrowser?.invoke(feed.link)
            }
            holder.bookmarkButton.setOnClickListener {
                if (!feed.bookmarked) {
                    FeedsService.bookmark(feed)
                } else {
                    FeedsService.unBookmark(feed.link)
                }

                (it as ImageButton).setImageResource(getBookmarkButtonResource(feed))
            }
            holder.bookmarkButton.setImageResource(getBookmarkButtonResource(feed))
        }
    }

    private fun getBookmarkButtonResource(feed : Feed) = if (feed.bookmarked) R.drawable.ic_baseline_bookmark_24 else R.drawable.ic_baseline_bookmark_border_24

    inner class ItemViewHolder(itemView: View, parent: ViewGroup) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView
        val descriptionText: TextView
        val linkButton: ImageButton
        val bookmarkButton: ImageButton

        init {
            itemView.apply {
                titleText = findViewById(R.id.feed_title)
                descriptionText = findViewById(R.id.feed_description)
                linkButton = findViewById(R.id.detail_button)
                bookmarkButton = findViewById(R.id.bookmark_button)
            }
            itemView.apply(clickListener())
        }
    }

    private fun clickListener(): View.() -> Unit {
        return {
            setOnLongClickListener {
                isLongClick = true
                isFirstMove = true

                notifyLongClick?.invoke(true)

                false
            }.apply {
                setOnTouchListener { view, motionEvent ->
                    when (motionEvent.action) {
                        MotionEvent.ACTION_MOVE -> {
                            if (isLongClick) {
                                // NOTE: deprecatedですが, Android 8で動くようにするためです
                                val display = overlayView.windowManager.defaultDisplay
                                val size = Point()
                                display.getSize(size)

                                if (isFirstMove) {
                                    moveOffsetX = motionEvent.rawX.toInt() - size.x / 2 - overlayView.layoutParams.x
                                    moveOffsetY = motionEvent.rawY.toInt() - size.y / 2 - overlayView.layoutParams.y
                                }

                                // オーバーレイ表示領域の座標を移動させる
                                overlayView.layoutParams.x = motionEvent.rawX.toInt() - size.x / 2 - moveOffsetX
                                overlayView.layoutParams.y = motionEvent.rawY.toInt() - size.y / 2 - moveOffsetY
                                overlayView.windowManager.updateViewLayout(overlayView, overlayView.layoutParams)

                                overlayView.findViewById<ViewPager2>(R.id.pager).setBackgroundResource(R.drawable.border_line_bold)

                                isFirstMove = false
                            }
                        }
                        MotionEvent.ACTION_UP -> {
                            if (isLongClick) {
                                isLongClick = false
                                notifyLongClick?.invoke(false)

                                overlayView.findViewById<ViewPager2>(R.id.pager).setBackgroundResource(0)
                            }

                            view.performClick()
                        }
                    }
                    false
                }
            }
        }
    }
}
