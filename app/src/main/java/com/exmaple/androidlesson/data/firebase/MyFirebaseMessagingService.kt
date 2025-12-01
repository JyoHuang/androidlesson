package com.exmaple.androidlesson.data.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("MyFcmService", "onNewToken: $token")

        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(user.uid)

        userRef.update(
            "fcmTokens",
            FieldValue.arrayUnion(token)
        ).addOnSuccessListener {
            Log.d("MyFcmService", "FCM token updated")
        }.addOnFailureListener { e ->
            Log.e("MyFcmService", "Failed to update FCM token", e)
        }
    }
}
