package com.exmaple.androidlesson.presentation.search

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import kotlin.math.abs
import kotlin.math.roundToInt

data class StockQuote(
    val code: String,
    val name: String,
    val lastPrice: Double?,       // 成交價 z
    val open: Double?,            // 開盤 o
    val high: Double?,            // 最高 h
    val low: Double?,             // 最低 l
    val prevClose: Double?,       // 昨收 y
    val change: Double?,          // 漲跌價差 = last - prevClose
    val changePercent: Double?,   // 漲跌幅 %
    val volume: Long?,            // 成交量（張）
    val time: String              // 成交時間 t (HH:MM:SS)
) {
    val isUp: Boolean?
        get() = change?.let { it > 0 } // true=漲, false=跌, null=無法判斷

    // 給 UI 用的格式化字串
    val lastPriceText: String
        get() = lastPrice?.toString() ?: "-"

    val changeText: String
        get() = change?.let {
            (if (it > 0) "+" else "") + String.format("%.2f", it)
        } ?: "-"

    val changePercentText: String
        get() = changePercent?.let {
            val sign = if (it > 0) "+" else ""
            sign + String.format("%.2f", it) + "%"
        } ?: "-"
}

data class StockSearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val result: StockQuote? = null
)

class StockSearchViewModel : ViewModel() {

    var uiState by mutableStateOf(StockSearchUiState())
        private set

    fun onQueryChanged(newQuery: String) {
        uiState = uiState.copy(
            query = newQuery,
            errorMessage = null,
            result = null
        )
    }

    fun search() {
        val stockId = uiState.query.trim()

        if (stockId.isBlank()) {
            uiState = uiState.copy(errorMessage = "請先輸入股票代號，例如 2330。")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null, result = null)

            try {
                val quote = fetchStockQuote(stockId)
                uiState = uiState.copy(
                    isLoading = false,
                    result = quote,
                    errorMessage = null
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    result = null,
                    errorMessage = e.message ?: "查詢失敗，請稍後再試。"
                )
            }
        }
    }

    /**
     * 呼叫 TWSE MIS API
     * 範例：
     * https://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=tse_2330.tw&json=1&delay=0
     */
    private suspend fun fetchStockQuote(stockId: String): StockQuote =
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

            // TWSE MIS 常見欄位：z=成交價, y=昨收, o=開, h=高, l=低, v=成交量, t=時間
            val lastPrice = obj.optString("z").toDoubleOrNull()
            val open = obj.optString("o").toDoubleOrNull()
            val high = obj.optString("h").toDoubleOrNull()
            val low = obj.optString("l").toDoubleOrNull()
            val prevClose = obj.optString("y").toDoubleOrNull()
            val volume = obj.optString("v").toLongOrNull()
            val time = obj.optString("t", "")

            // 如果 API 有內建的漲跌與漲幅可以直接用，這裡我們自己算一份，比較直覺
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
    /**
     * 自動刷新用：如果目前有查到的股票，就再抓一次最新報價。
     * 不會改動 isLoading，避免畫面一直閃。
     */
    fun refreshQuote() {
        val stockId = uiState.result?.code ?: uiState.query.trim()
        if (stockId.isBlank()) return

        viewModelScope.launch {
            try {
                val quote = fetchStockQuote(stockId)
                uiState = uiState.copy(result = quote)
                Log.d("refreshQuote", "更新成功")
            } catch (e: Exception) {
                // 自動刷新失敗就先忽略，不要打斷使用者
                // 如果你想顯示錯誤，也可以在這邊更新 errorMessage
                Log.e("refreshQuote", "錯誤: ${e.message}")
            }
        }
    }

}

