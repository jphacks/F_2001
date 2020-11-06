package com.example.newsee

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CloseOverlayBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        OverlayService.stop(context)
    }
}
