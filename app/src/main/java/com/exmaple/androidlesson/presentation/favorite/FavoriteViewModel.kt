package com.exmaple.androidlesson.presentation.favorite

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exmaple.androidlesson.data.favorites.FavoriteStock
import com.exmaple.androidlesson.data.favorites.FavoritesRepository
import com.exmaple.androidlesson.presentation.search.StockQuote
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FavoriteQuoteItem(
    val base: FavoriteStock,
    val quote: StockQuote? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

data class FavoriteUiState(
    val items: List<FavoriteQuoteItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val lastUpdatedTime: String? = null      // ⭐ 新增：最後自動更新時間
)

class FavoriteViewModel : ViewModel() {

    var uiState by mutableStateOf(FavoriteUiState(isLoading = true))
        private set

    private var listenerRegistration: ListenerRegistration? = null

    init {
        subscribeFavorites()
    }

    private fun subscribeFavorites() {
        listenerRegistration = FavoritesRepository.observeFavorites { list, error ->
            if (error != null) {
                uiState = FavoriteUiState(
                    items = emptyList(),
                    isLoading = false,
                    errorMessage = error
                )
                return@observeFavorites
            }

            if (list.isEmpty()) {
                uiState = FavoriteUiState(
                    items = emptyList(),
                    isLoading = false,
                    errorMessage = null
                )
                return@observeFavorites
            }

            // 先建立「空的」 item，顯示 loading 狀態
            val initialItems = list.map { fav ->
                FavoriteQuoteItem(
                    base = fav,
                    quote = null,
                    isLoading = true,
                    errorMessage = null
                )
            }
            uiState = FavoriteUiState(
                items = initialItems,
                isLoading = false,
                errorMessage = null
            )

            // 幫每一檔股票抓一次即時報價
            list.forEach { fav ->
                viewModelScope.launch {
                    try {
                        val quote = fetchStockQuoteForFavorite(fav.code)
                        updateItemQuote(
                            code = fav.code,
                            quote = quote,
                            error = null
                        )
                    } catch (e: Exception) {
                        updateItemQuote(
                            code = fav.code,
                            quote = null,
                            error = e.message ?: "取得報價失敗"
                        )
                    }
                }
            }
        }
    }

    private fun updateItemQuote(
        code: String,
        quote: StockQuote?,
        error: String?
    ) {
        uiState = uiState.copy(
            items = uiState.items.map { item ->
                if (item.base.code == code) {
                    item.copy(
                        quote = quote,
                        isLoading = false,
                        errorMessage = error
                    )
                } else {
                    item
                }
            },
            // ⭐ 只要有一檔成功更新報價，就更新最後更新時間
            lastUpdatedTime = if (quote != null && error == null) nowTimeString() else uiState.lastUpdatedTime

        )
    }

    /**
     * 給「刪除收藏」用，之前你已經有這個功能，沿用它
     */
    fun deleteFavorite(code: String) {
        FavoritesRepository.deleteFavorite(code) { ok, error ->
            if (!ok && error != null) {
                uiState = uiState.copy(errorMessage = error)
            }
        }
    }
    /**
     * ⭐ 自動刷新全部收藏的即時報價
     * 每次會針對目前 uiState 裡的所有股票 code 再抓一次 TWSE MIS 報價
     */
    fun refreshAllQuotes() {
        // 目前收藏的所有股票代號
        val codes = uiState.items.map { it.base.code }.distinct()
        if (codes.isEmpty()) return

        codes.forEach { code ->
            viewModelScope.launch {
                try {
                    val quote = fetchStockQuoteForFavorite(code)
                    updateItemQuote(
                        code = code,
                        quote = quote,
                        error = null
                    )
                } catch (e: Exception) {
                    updateItemQuote(
                        code = code,
                        quote = null,
                        error = e.message ?: "取得報價失敗"
                    )
                }
            }
        }
    }
    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }

    /**
     * 給「我的最愛」用的 TWSE MIS 抓報價，邏輯跟查價頁類似
     */
    private suspend fun fetchStockQuoteForFavorite(stockId: String): StockQuote =
        withContext(Dispatchers.IO) {
            val url =
                "https://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=tse_${stockId}.tw&json=1&delay=0"

            val jsonStr = URL(url).readText()

            val root = JSONObject(jsonStr)
            val rtMessage = root.optString("rtmessage", "")
            if (rtMessage != "OK") {
                throw Exception("伺服器回傳訊息：$rtMessage")
            }

            val arr = root.optJSONArray("msgArray")
                ?: throw Exception("查無資料。")

            if (arr.length() == 0) {
                throw Exception("找不到代號 $stockId 的報價。")
            }

            val obj = arr.getJSONObject(0)

            val code = obj.optString("c", stockId)
            val name = obj.optString("n", "")

            val lastPrice = obj.optString("z").toDoubleOrNull()
            val open = obj.optString("o").toDoubleOrNull()
            val high = obj.optString("h").toDoubleOrNull()
            val low = obj.optString("l").toDoubleOrNull()
            val prevClose = obj.optString("y").toDoubleOrNull()
            val volume = obj.optString("v").toLongOrNull()
            val time = obj.optString("t", "")

            val change = if (lastPrice != null && prevClose != null) {
                (lastPrice - prevClose)
            } else null

            val changePercent = if (change != null && prevClose != null && prevClose != 0.0) {
                (change / prevClose) * 100.0
            } else null

            StockQuote(
                code = code,
                name = name,
                lastPrice = lastPrice,
                open = open,
                high = high,
                low = low,
                prevClose = prevClose,
                change = change,
                changePercent = changePercent,
                volume = volume,
                time = time
            )
        }

    private fun nowTimeString(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.TAIWAN)
        return sdf.format(Date())
    }
}

