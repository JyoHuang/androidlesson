package com.exmaple.androidlesson.data.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

object FcmTokenManager {

    private const val TAG = "FcmTokenManager"

    fun registerTokenForCurrentUser() {
        val user = FirebaseAuth.getInstance().currentUser ?: run {
            Log.w(TAG, "No current user, skip FCM token register")
            return
        }

        // 取得 FCM token
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                if (token.isNullOrBlank()) {
                    Log.w(TAG, "FCM token is blank, skip")
                    return@addOnSuccessListener
                }

                val db = FirebaseFirestore.getInstance()
                val userRef = db.collection("users").document(user.uid)

                // 確保 user 文件存在，順便把 email 存進去也可以
                val baseData = mapOf(
                    "email" to (user.email ?: ""),
                    "displayName" to (user.displayName ?: ""),
                )

                userRef.set(baseData, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener {
                        // 用 arrayUnion 把 token 加進 fcmTokens 陣列（避免重複）
                        userRef.update(
                            "fcmTokens",
                            FieldValue.arrayUnion(token)
                        ).addOnSuccessListener {
                            Log.d(TAG, "FCM token registered to user doc")
                        }.addOnFailureListener { e ->
                            Log.e(TAG, "Failed to update fcmTokens", e)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to create user doc", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to get FCM token", e)
            }
    }
}
