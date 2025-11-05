package com.example.lab_week_8

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class SecondNotificationService : Service() {

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var serviceHandler: Handler

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notificationBuilder = startForegroundService()
        val handlerThread = HandlerThread("ThirdThread").apply { start() }
        serviceHandler = Handler(handlerThread.looper)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val id = intent?.getStringExtra(EXTRA_ID) ?: "002"
        serviceHandler.post {
            countDown(notificationBuilder)
            notifyCompletion(id)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService(): NotificationCompat.Builder {
        val channelId = createNotificationChannel()
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Third worker process is done")
            .setContentText("Final check!")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setTicker("Third worker process is done!")
            .setOngoing(true)

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
        return notificationBuilder
    }

    private fun createNotificationChannel(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "002"
            val channelName = "002 Channel"
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
            return channelId
        }
        return ""
    }

    private fun countDown(notificationBuilder: NotificationCompat.Builder) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        for (i in 5 downTo 0) { // Hitung mundur lebih cepat (5 detik) agar tidak terlalu lama
            Thread.sleep(1000L)
            notificationBuilder.setContentText("$i seconds until finishing assignment")
                .setSilent(true)
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        }
    }

    private fun notifyCompletion(id: String) {
        Handler(Looper.getMainLooper()).post {
            mutableID.value = id
        }
    }

    companion object {
        const val NOTIFICATION_ID = 0xCA8 // ID berbeda
        const val EXTRA_ID = "Id"
        private val mutableID = MutableLiveData<String>()
        val trackingCompletion: LiveData<String> = mutableID
    }
}