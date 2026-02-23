package edu.temple.convoy

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class ConvoyService : Service() {
    class ConvoyBinder(val service: ConvoyService) : Binder()

    override fun onBind(intent: Intent?): IBinder {
        return ConvoyBinder(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }
}