package com.kranin.application.model

import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("FCM Token: $token") // токен нужно отправить на твой Python сервер
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            val title = it.title
            val body = it.body
            Toast.makeText(applicationContext, "$title\n$body", Toast.LENGTH_LONG).show()
        }
    }
}
