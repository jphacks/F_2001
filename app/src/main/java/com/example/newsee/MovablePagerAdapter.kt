package com.example.newsee

import android.graphics.Point
import android.os.Binder
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

@RequiresApi(Build.VERSION_CODES.R)
class MovablePagerAdapter(private val overlayView: OverlayView, private val binder: FeedsService.FeedsBinder, private val notifyLongClick: ((longClicked: Boolean) -> Unit)?) :
        RecyclerView.Adapter<MovablePagerAdapter.ItemViewHolder>() {

    private var isLongClick: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovablePagerAdapter.ItemViewHolder =
            ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.overlay_slide_item, parent, false))

    override fun getItemCount(): Int = binder.getFeeds().size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val feed = binder.getFeeds()[position]
        holder.titleText.text = feed.title
        holder.descriptionText.text = feed.description
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var bookmarked = false
        val titleText: TextView
        val descriptionText: TextView

        init {
            itemView.apply {
                titleText = findViewById(R.id.feed_title)
                descriptionText =findViewById(R.id.feed_description)

                findViewById<ImageButton>(R.id.detail_button).setOnClickListener {
                    Log.d("Detail Button", "clicked.")
                }
                findViewById<ImageButton>(R.id.bookmark_button).setOnClickListener {
                    Log.d("Bookmark Button", "clicked." + bookmarked)
                    bookmarked = !bookmarked

                    val src = if (bookmarked) R.drawable.ic_baseline_bookmark_24 else R.drawable.ic_baseline_bookmark_border_24
                    (it as ImageButton).setImageResource(src)
                }
            }
            itemView.apply(clickListener())
        }
    }

    private fun clickListener(): View.() -> Unit {
        return {
            setOnLongClickListener {
                isLongClick = true
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

                                val x = motionEvent.rawX.toInt()
                                val y = motionEvent.rawY.toInt()

                                val centerX = x - size.x / 2
                                val centerY = y - size.y / 2

                                // オーバーレイ表示領域の座標を移動させる
                                overlayView.layoutParams.x = centerX
                                overlayView.layoutParams.y = centerY
                                overlayView.windowManager.updateViewLayout(overlayView, overlayView.layoutParams)
                            }
                        }
                        MotionEvent.ACTION_UP -> {
                            if (isLongClick) {
                                isLongClick = false
                                notifyLongClick?.invoke(false)
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
