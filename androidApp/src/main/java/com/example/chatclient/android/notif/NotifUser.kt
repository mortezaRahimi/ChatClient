package com.example.chatclient.android.notif

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.chatclient.android.R
import kotlin.random.Random

class NotifUser(
    private val context: Context
) {

    fun showNotif(context: Context,message:String){

        val idChannel = "OMNI"
        val mNotificationManager = context.getSystemService(Application.NOTIFICATION_SERVICE) as NotificationManager

        var mChannel: NotificationChannel? = null
        // The id of the channel.

        val importance = NotificationManager.IMPORTANCE_HIGH
        val builder = NotificationCompat.Builder(context, expandableNotification(message))
        builder.setContentTitle("OMNI CHAT")
            .setContentText(message)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel =
                NotificationChannel(idChannel,"OMNI Chat", importance)
            // Configure the notification channel.
            mChannel.description = "new message received"
            mChannel.enableLights(true)
            mChannel.lightColor = Color.RED
            mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            mNotificationManager.createNotificationChannel(mChannel)
        } else {
            builder.setContentTitle("OMNI Chat")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(ContextCompat.getColor(context, android.R.color.transparent))
                .setVibrate(longArrayOf(100, 250))
                .setLights(Color.YELLOW, 500, 5000)
                .setAutoCancel(true)
        }
        mNotificationManager.notify(1, builder.build())
    }
    fun expandableNotification(message: String):Notification {
        return NotificationCompat.Builder(context, "OMNI")
            .setContentTitle("Message received")
            .setSmallIcon(com.google.accompanist.permissions.R.drawable.notification_bg)
            .setContentText(message)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setAutoCancel(true)
            .build()
    }

    private fun Context.bitmapFromResource(
        @DrawableRes resId: Int
    ) = BitmapFactory.decodeResource(
        resources,
        resId
    )
}