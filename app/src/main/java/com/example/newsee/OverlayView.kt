package com.example.newsee

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.R)
class OverlayView @JvmOverloads constructor(
        ctx: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0
) : FrameLayout(ctx, attrs, defStyle) {
    companion object {
        /** Creates an instance of [OverlayView]. */
        fun create(context: Context) =
                View.inflate(context, R.layout.overlay_view, null) as OverlayView
    }

     val windowManager: WindowManager =
            ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    /** Settings for overlay view */
     val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,  // Overlay レイヤに表示
            WindowManager.LayoutParams.WRAP_CONTENT,  // Overlay レイヤに表示
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,  // Overlay レイヤに表示
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  // フォーカスを奪わない
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,  // 画面外への拡張を許可
            PixelFormat.TRANSLUCENT
    )

    /** Starts displaying this view as overlay. */
    @Synchronized
    fun show() {
        if (!this.isShown) {
            windowManager.addView(this, layoutParams)
        }
    }

    /** Hide this view. */
    @Synchronized
    fun hide() {
        if (this.isShown) {
            windowManager.removeView(this)
        }
    }
}
