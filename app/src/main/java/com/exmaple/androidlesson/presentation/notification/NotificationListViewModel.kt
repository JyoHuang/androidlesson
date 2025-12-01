package com.exmaple.androidlesson.presentation.notification

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class NotificationItem(
    val id: String,
    val stockId: String?,
    val stockName: String?,
    val title: String,
    val body: String,
    val type: String?,
    val createdAt: Long?,
    val read: Boolean
) {
    fun timeText(): String {
        if (createdAt == null) return ""
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.TAIWAN)
        return sdf.format(Date(createdAt))
    }
}

data class NotificationUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val items: List<NotificationItem> = emptyList()
)

class NotificationListViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    var uiState by mutableStateOf(NotificationUiState())
        private set

    private var listenerRegistration: ListenerRegistration? = null
    private var started = false

    fun start() {
        if (started) return
        started = true

        val user = auth.currentUser
        if (user == null) {
            uiState = uiState.copy(
                isLoading = false,
                errorMessage = "尚未登入，無法讀取通知列表。",
                items = emptyList()
            )
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null)

        val colRef = db.collection("users")
            .document(user.uid)
            .collection("notifications")

        // 最新的排在最前面
        listenerRegistration = colRef
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "讀取通知失敗。",
                        items = emptyList()
                    )
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = null,
                        items = emptyList()
                    )
                    return@addSnapshotListener
                }

                val list = snapshot.documents.map { doc ->
                    NotificationItem(
                        id = doc.id,
                        stockId = doc.getString("stockId"),
                        stockName = doc.getString("stockName"),
                        title = doc.getString("title") ?: "",
                        body = doc.getString("body") ?: "",
                        type = doc.getString("type"),
                        createdAt = doc.getLong("createdAt"),
                        read = doc.getBoolean("read") ?: false
                    )
                }

                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = null,
                    items = list
                )
            }
    }

    fun markAsRead(notificationId: String) {
        val user = auth.currentUser ?: return
        val docRef = db.collection("users")
            .document(user.uid)
            .collection("notifications")
            .document(notificationId)

        docRef.update("read", true)
    }

    fun deleteNotification(notificationId: String) {
        val user = auth.currentUser ?: return
        val docRef = db.collection("users")
            .document(user.uid)
            .collection("notifications")
            .document(notificationId)

        docRef.delete()
    }

    fun clearAll() {
        val user = auth.currentUser ?: return
        val colRef = db.collection("users")
            .document(user.uid)
            .collection("notifications")

        // 小專案用：一次抓全部刪掉
        colRef.get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()
                snapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit()
            }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }

    fun createDummyNotification() {
        val user = auth.currentUser ?: return

        val now = System.currentTimeMillis()
        val docRef = db.collection("users")
            .document(user.uid)
            .collection("notifications")
            .document() // 自動 ID

        val data = mapOf(
            "stockId" to "2330",
            "stockName" to "台積電",
            "title" to "測試通知：網格觸發示範",
            "body" to "這是一筆測試通知，用來確認通知列表是否正常顯示。",
            "type" to "test",
            "createdAt" to now,
            "read" to false
        )

        docRef.set(data)
    }

}
