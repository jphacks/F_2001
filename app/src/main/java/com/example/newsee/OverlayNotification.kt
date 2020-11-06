package com.example.newsee

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.R)
object OverlayNotification {
    private const val CHANNEL_ID = "channel_id_overlay_sample"
    private const val CHANNEL_NAME = "オーバーレイ表示の切り替え"
    private const val CHANNEL_IMPORTANCE = NotificationManager.IMPORTANCE_DEFAULT
    private const val FIRST_LINE = "オーバーレイ表示中"
    private const val SECOND_LINE = "ここから表示・非表示を切り替えられます。"

    fun build(context: Context): Notification {
        // Create a notification channel
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, CHANNEL_IMPORTANCE)
        )

        val intent = Intent(context, OverlayService::class.java).apply { action = OverlayService.ACTION_HIDE }
        val pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val notificationAction = Notification.Action(R.drawable.ic_baseline_close_24, "非表示にする", pendingIntent)

        return Notification.Builder(context, CHANNEL_ID)
            .setAutoCancel(false)  // don't dismiss when touched
            .setContentIntent(pendingIntent)  // The intent to send when the entry is clicked
            .setContentTitle(FIRST_LINE)  // the label of the entry
            .setContentText(SECOND_LINE)  // the contents of the entry
            .setSmallIcon(R.drawable.shoebill)  // the status icon
            .setTicker(context.getText(R.string.app_name))  // the status text
            .setWhen(System.currentTimeMillis())  // the time stamp
            .addAction(notificationAction)
            .build()
    }
}
