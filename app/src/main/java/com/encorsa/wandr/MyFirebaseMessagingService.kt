package com.encorsa.wandr

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.encorsa.wandr.utils.Prefs


import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONArray
import org.json.JSONException

class MyFirebaseMessagingService: FirebaseMessagingService() {
    val TAG = "FirebaseMessagingService"
    private val CHANNEL_ID = "WandrChannel"
    val prefs = Prefs(applicationContext)

    @SuppressLint("LongLogTag")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.size > 0) {
            Log.i(TAG, "Message data payload: ${remoteMessage.data}")
        }
        showNotification(remoteMessage)
    }

    private fun showNotification(remoteMessage: RemoteMessage) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

//        var title = ""
//        var msg = ""
//        val data = remoteMessage.data
//        msg = if (data["message"] != null) data["message"] else ""
//        val id = data["id"]
//
//
//        var curLanguage = prefs.currentLanguage.toUpperCase()
//        if (curLanguage == "")
//            curLanguage = "RO"
//        when (curLanguage) {
//            "RO" -> title = "Eveniment nou"
//            "EN" -> title = "New event"
//            "BG" -> title = "Ново събитие"
//        }
//
//        try {
//            val descriptions = JSONArray(data["descriptions"])
//            for (i in 0 until descriptions.length()) {
//                if (descriptions.getJSONObject(i).getString("Language").toUpperCase() == curLanguage) {
//                    if (!descriptions.getJSONObject(i).isNull("Name")) {
//                        msg = descriptions.getJSONObject(i).getString("Name")
//                        if (!descriptions.getJSONObject(i).isNull("Address"))
//                            msg = msg + " - " + descriptions.getJSONObject(i).getString("Address")
//                    }
//                    break
//                }
//            }
//        } catch (e: JSONException) {
//            e.printStackTrace()
//        }
//
//        if (msg != "" && id != null) {
//
//            val intentMain = Intent(this, DetailActivity::class.java)
//            intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            intentMain.putExtra("objectiveId", id)
//            val intents = arrayOf(intentMain)//, intentView};
//
//
//            val pendingIntent =
//                PendingIntent.getActivities(this, 0, intents, PendingIntent.FLAG_ONE_SHOT)
//            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//
//
//            val notificationManager = NotificationManagerCompat.from(applicationContext)
//            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
//            notification.setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE)
//            notification.setContentTitle(title)
//            notification.setSound(defaultSoundUri)
//            notification.setContentText(msg)
//            notification.setSmallIcon(R.drawable.ic_launcher_foreground)
//            notification.setContentIntent(pendingIntent)
//            notification.setAutoCancel(true)
//            notificationManager.notify(1078, notification.build())
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Wandr notifications"
            val description = "Notifications sent to Wandr when new event is available"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            channel.setSound(soundUri, audioAttributes)
            channel.enableVibration(true)
            channel.description = description

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(channel)
        }
    }
}