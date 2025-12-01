package com.exmaple.androidlesson.presentation.grid

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

data class GridStrategySummary(
    val id: String,
    val lowerPrice: Double,
    val upperPrice: Double,
    val gridCount: Int,
    val cooldownMinutes: Int,
    val stopLossPrice: Double?,
    val active: Boolean,
    val createdAt: Long?,
    val updatedAt: Long?
)

data class GridStrategyListUiState(
    val stockId: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val strategies: List<GridStrategySummary> = emptyList()
)

class GridStrategyListViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    var uiState by mutableStateOf(GridStrategyListUiState())
        private set

    private var initialized = false
    private var stockIdInternal: String? = null
    private var listenerRegistration: ListenerRegistration? = null

    /**
     * 在 Composable 中用 LaunchedEffect(Unit) 呼叫一次
     */
    fun start(stockId: String) {
        if (initialized) return
        initialized = true

        stockIdInternal = stockId
        uiState = uiState.copy(
            stockId = stockId,
            isLoading = true,
            errorMessage = null
        )

        val user = auth.currentUser
        if (user == null) {
            uiState = uiState.copy(
                isLoading = false,
                errorMessage = "尚未登入，無法載入網格策略。"
            )
            return
        }

        val colRef = db.collection("users")
            .document(user.uid)
            .collection("favorites")
            .document(stockId)
            .collection("gridStrategies")

        listenerRegistration = colRef
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "載入網格策略時發生錯誤。",
                        strategies = emptyList()
                    )
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = null,
                        strategies = emptyList()
                    )
                    return@addSnapshotListener
                }

                val list = snapshot.documents.map { doc ->
                    GridStrategySummary(
                        id = doc.id,
                        lowerPrice = doc.getDouble("lowerPrice") ?: 0.0,
                        upperPrice = doc.getDouble("upperPrice") ?: 0.0,
                        gridCount = (doc.getLong("gridCount") ?: 0L).toInt(),
                        cooldownMinutes = (doc.getLong("cooldownMinutes") ?: 0L).toInt(),
                        stopLossPrice = doc.getDouble("stopLossPrice"),
                        active = doc.getBoolean("active") ?: true,
                        createdAt = doc.getLong("createdAt"),
                        updatedAt = doc.getLong("updatedAt")
                    )
                }

                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = null,
                    strategies = list
                )
            }
    }

    fun toggleActive(strategyId: String, newActive: Boolean) {
        val user = auth.currentUser ?: return
        val stockId = stockIdInternal ?: return

        val docRef = db.collection("users")
            .document(user.uid)
            .collection("favorites")
            .document(stockId)
            .collection("gridStrategies")
            .document(strategyId)

        docRef.update(
            mapOf(
                "active" to newActive,
                "updatedAt" to System.currentTimeMillis()
            )
        )
    }

    fun deleteStrategy(strategyId: String) {
        val user = auth.currentUser ?: return
        val stockId = stockIdInternal ?: return

        val docRef = db.collection("users")
            .document(user.uid)
            .collection("favorites")
            .document(stockId)
            .collection("gridStrategies")
            .document(strategyId)

        docRef.delete()
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
