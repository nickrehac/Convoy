package edu.temple.convoy

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.io.FileDescriptor

class FcmService : FirebaseMessagingService() {
    var token = "";
    override fun onNewToken(t: String) {
        token = t
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
    }
}