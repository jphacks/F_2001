package com.example.newsee

import android.graphics.Point
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

@RequiresApi(Build.VERSION_CODES.R)
class MovablePagerAdapter(private val overlayView: OverlayView, private val targetPager: ViewPager2) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // TODO: itemsを渡す (bind?)
    val items = mutableListOf<Any>()
    private var isLongClick: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.fragment_overlay_slide_item, parent, false))

    override fun getItemCount(): Int = 5

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // bind your items
    }

    private inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var bookmarked = false

        init {
            itemView.apply(clickListener())
            itemView.findViewById<ImageButton>(R.id.detail_button).setOnClickListener {
                Log.d("Detail Button", "clicked.")
            }
            itemView.findViewById<ImageButton>(R.id.bookmark_button).setOnClickListener {
                Log.d("Bookmark Button", "clicked." + bookmarked)
                bookmarked = !bookmarked

                val src = if (bookmarked) R.drawable.ic_baseline_bookmark_24 else R.drawable.ic_baseline_bookmark_border_24
                (it as ImageButton).setImageResource(src)
            }
        }
    }

    private fun clickListener(): View.() -> Unit {
        return {
            setOnLongClickListener {
                isLongClick = true
                targetPager.isUserInputEnabled = false
                false
            }.apply {
                setOnTouchListener { view, motionEvent ->
                    when (motionEvent.action) {
                        MotionEvent.ACTION_MOVE -> {
                            if (isLongClick) {
                                // TODO: スクロールを止める

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
                                // TODO: スクロールを再開
                                isLongClick = false
                                targetPager.isUserInputEnabled = true
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
