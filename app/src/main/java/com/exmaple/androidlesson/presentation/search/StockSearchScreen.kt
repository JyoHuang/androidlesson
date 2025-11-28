package com.exmaple.androidlesson.presentation.search

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.exmaple.androidlesson.ui.theme.AndroidLessonTheme
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

@Composable
fun StockSearchScreen(
    viewModel: StockSearchViewModel = viewModel()
) {
    val uiState = viewModel.uiState

    // ⭐ 每 5 秒自動刷新目前查到的那一檔股票
    LaunchedEffect(uiState.result?.code) {
        // 沒有查到任何股票就不要進入自動刷新
        val currentCode = uiState.result?.code
        if (currentCode.isNullOrBlank()) return@LaunchedEffect

        while (true) {
            delay(5000L)
            viewModel.refreshQuote()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            // 頁面標題
            Text(
                text = "股票查詢",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 查詢輸入區
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChanged,
                label = { Text("股票代號（例如 2330）") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { viewModel.search() },
                enabled = !uiState.isLoading,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(text = "查詢")
            }

            // Loading 狀態
            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("取得報價中…")
                }
            }

            // 錯誤訊息
            uiState.errorMessage?.let { msg ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 查詢結果顯示區
            uiState.result?.let { quote ->
                Spacer(modifier = Modifier.height(24.dp))

                StockQuoteCard(quote = quote)

                // ⭐ 最後自動更新時間
                uiState.lastUpdatedTime?.let { last ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "最後自動更新時間：$last",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "資料來源：TWSE MIS（即時報價，約略延遲數秒，僅供參考）",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun StockQuoteCard(quote: StockQuote) {
    val priceColor: Color = when (quote.isUp) {
        true -> Color(0xFFD32F2F)   // 紅色：漲
        false -> Color(0xFF2E7D32)  // 綠色：跌
        null -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 股票代號 + 名稱
            Text(
                text = "${quote.code}  ${quote.name}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 大字顯示現價 + 漲跌
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = quote.lastPriceText,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = priceColor
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "漲跌：${quote.changeText}",
                        color = priceColor,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "漲跌幅：${quote.changePercentText}",
                        color = priceColor,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 價格區塊：開盤 / 最高 / 最低 / 昨收
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "開盤價", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        text = quote.open?.toString() ?: "-",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "最高價", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        text = quote.high?.toString() ?: "-",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "最低價", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        text = quote.low?.toString() ?: "-",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "昨收", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        text = quote.prevClose?.toString() ?: "-",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 成交量 / 更新時間
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "成交量（張）", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        text = quote.volume?.toString() ?: "-",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "更新時間", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        text = quote.time.ifBlank { "-" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StockSearchScreenPreview() {
    AndroidLessonTheme {
        StockSearchScreen()
    }
}
