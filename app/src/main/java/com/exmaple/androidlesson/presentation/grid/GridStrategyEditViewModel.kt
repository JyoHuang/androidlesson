package com.exmaple.androidlesson.presentation.grid

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class GridStrategyEditUiState(
    val stockId: String = "",
    val isNew: Boolean = true,

    val lowerPrice: String = "",
    val upperPrice: String = "",
    val gridCount: String = "10",
    val cooldownMinutes: String = "10",
    val stopLossPrice: String = "",

    val active: Boolean = true,

    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

class GridStrategyEditViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    var uiState by mutableStateOf(GridStrategyEditUiState())
        private set

    private var initialized = false
    private var internalStockId: String? = null
    private var internalStrategyId: String? = null

    /**
     * 在 Composable 裡用 LaunchedEffect 呼叫一次
     * stockId: 股票代號（例如 "2330"）
     * strategyId: 若為 null 則建立新策略；若有值則載入既有策略
     */
    fun start(stockId: String, strategyId: String?) {
        if (initialized) return
        initialized = true

        internalStockId = stockId
        internalStrategyId = strategyId

        uiState = uiState.copy(
            stockId = stockId,
            isNew = strategyId == null,
            isLoading = strategyId != null,
            errorMessage = null,
            infoMessage = null
        )

        if (strategyId != null) {
            loadExistingStrategy(stockId, strategyId)
        }
    }

    private fun loadExistingStrategy(stockId: String, strategyId: String) {
        val user = auth.currentUser ?: run {
            uiState = uiState.copy(
                isLoading = false,
                errorMessage = "尚未登入，無法載入策略。"
            )
            return
        }

        val ref = db.collection("users")
            .document(user.uid)
            .collection("favorites")
            .document(stockId)
            .collection("gridStrategies")
            .document(strategyId)

        ref.get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = "找不到這個網格策略。"
                    )
                    return@addOnSuccessListener
                }

                val lower = doc.getDouble("lowerPrice") ?: 0.0
                val upper = doc.getDouble("upperPrice") ?: 0.0
                val grids = doc.getLong("gridCount") ?: 10L
                val cooldown = doc.getLong("cooldownMinutes") ?: 10L
                val stopLoss = doc.getDouble("stopLossPrice") ?: 0.0
                val active = doc.getBoolean("active") ?: true

                uiState = uiState.copy(
                    isLoading = false,
                    lowerPrice = if (lower == 0.0) "" else lower.toString(),
                    upperPrice = if (upper == 0.0) "" else upper.toString(),
                    gridCount = grids.toString(),
                    cooldownMinutes = cooldown.toString(),
                    stopLossPrice = if (stopLoss == 0.0) "" else stopLoss.toString(),
                    active = active,
                    errorMessage = null
                )
            }
            .addOnFailureListener { e ->
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "載入策略失敗。"
                )
            }
    }

    // ------- UI 綁定用的欄位更新 -------

    fun onLowerPriceChange(value: String) {
        uiState = uiState.copy(lowerPrice = value, errorMessage = null, infoMessage = null)
    }

    fun onUpperPriceChange(value: String) {
        uiState = uiState.copy(upperPrice = value, errorMessage = null, infoMessage = null)
    }

    fun onGridCountChange(value: String) {
        uiState = uiState.copy(gridCount = value, errorMessage = null, infoMessage = null)
    }

    fun onCooldownMinutesChange(value: String) {
        uiState = uiState.copy(cooldownMinutes = value, errorMessage = null, infoMessage = null)
    }

    fun onStopLossPriceChange(value: String) {
        uiState = uiState.copy(stopLossPrice = value, errorMessage = null, infoMessage = null)
    }

    fun onActiveChange(value: Boolean) {
        uiState = uiState.copy(active = value, errorMessage = null, infoMessage = null)
    }

    // ------- 儲存（新增 / 更新） -------

    fun save(onSuccess: () -> Unit) {
        val user = auth.currentUser ?: run {
            uiState = uiState.copy(errorMessage = "尚未登入，無法儲存策略。")
            return
        }

        val stockId = internalStockId ?: run {
            uiState = uiState.copy(errorMessage = "內部錯誤：缺少 stockId。")
            return
        }

        // 驗證 & 轉型
        val lower = uiState.lowerPrice.toDoubleOrNull()
        val upper = uiState.upperPrice.toDoubleOrNull()
        val grids = uiState.gridCount.toIntOrNull()
        val cooldown = uiState.cooldownMinutes.toIntOrNull()
        val stopLoss = uiState.stopLossPrice.toDoubleOrNull()

        if (lower == null || upper == null) {
            uiState = uiState.copy(errorMessage = "請輸入有效的價格區間（上下界）。")
            return
        }
        if (upper <= lower) {
            uiState = uiState.copy(errorMessage = "最高價必須大於最低價。")
            return
        }
        if (grids == null || grids <= 0) {
            uiState = uiState.copy(errorMessage = "請輸入大於 0 的網格數量。")
            return
        }
        if (cooldown == null || cooldown < 0) {
            uiState = uiState.copy(errorMessage = "請輸入正確的通知間隔（分鐘，可為 0）。")
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null, infoMessage = null)

        val strategiesRef = db.collection("users")
            .document(user.uid)
            .collection("favorites")
            .document(stockId)
            .collection("gridStrategies")

        val now = System.currentTimeMillis()

        val data = hashMapOf(
            "lowerPrice" to lower,
            "upperPrice" to upper,
            "gridCount" to grids,
            "cooldownMinutes" to cooldown,
            "stopLossPrice" to (stopLoss ?: 0.0),
            "active" to uiState.active,
            "updatedAt" to now
        )

        val docRef = if (internalStrategyId == null) {
            val newDoc = strategiesRef.document()
            internalStrategyId = newDoc.id
            data["createdAt"] = now
            newDoc
        } else {
            strategiesRef.document(internalStrategyId!!)
        }

        docRef.set(data, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                uiState = uiState.copy(
                    isLoading = false,
                    isNew = false,
                    infoMessage = "已成功儲存網格策略。",
                    errorMessage = null
                )
                onSuccess()
            }
            .addOnFailureListener { e ->
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "儲存失敗，請稍後再試。"
                )
            }
    }
}
