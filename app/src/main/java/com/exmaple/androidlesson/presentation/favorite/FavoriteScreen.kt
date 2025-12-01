package com.exmaple.androidlesson.presentation.favorite

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.exmaple.androidlesson.ui.theme.AndroidLessonTheme

@Composable
fun FavoriteScreen(
    viewModel: FavoriteViewModel = viewModel()
) {
    val uiState = viewModel.uiState

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "我的最愛",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("載入中…")
                    }
                }

                uiState.errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "讀取失敗",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                uiState.items.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("目前沒有任何收藏，可以在『股票查詢』頁面加入我的最愛。")
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.items) { item ->
                            FavoriteQuoteCard(
                                item = item,
                                onDelete = { viewModel.deleteFavorite(item.base.code) }
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun FavoriteQuoteCard(
    item: FavoriteQuoteItem,
    onDelete: () -> Unit
) {
    val quote = item.quote

    val priceColor: Color = when (quote?.isUp) {
        true -> Color(0xFFD32F2F)   // 漲：紅
        false -> Color(0xFF2E7D32)  // 跌：綠
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

            // 第一排：股票代號 + 名稱 + 刪除 icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.base.code}  ${item.base.name}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "刪除此收藏"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when {
                item.isLoading -> {
                    Text(
                        text = "取得即時報價中…",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                item.errorMessage != null -> {
                    Text(
                        text = "無法取得即時報價：${item.errorMessage}",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                quote != null -> {
                    // --------------- 現價 + 漲跌區塊（跟查價頁一樣） ---------------
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = quote.lastPriceText,
                            fontSize = 30.sp,
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

                    // --------------- 開盤 / 高 / 低 / 昨收（跟查價頁一致） ---------------
                    Row(modifier = Modifier.fillMaxWidth()) {

                        Column(modifier = Modifier.weight(1f)) {
                            Text("開盤價", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = quote.open?.toString() ?: "-",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("最高價", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = quote.high?.toString() ?: "-",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("最低價", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = quote.low?.toString() ?: "-",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("昨收", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = quote.prevClose?.toString() ?: "-",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --------------- 成交量 + 更新時間 ---------------
                    Row(modifier = Modifier.fillMaxWidth()) {

                        Column(modifier = Modifier.weight(1f)) {
                            Text("成交量（張）", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = quote.volume?.toString() ?: "-",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("更新時間", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = quote.time.ifBlank { "-" },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } // end when
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FavoriteScreenPreview() {
    AndroidLessonTheme {
        FavoriteScreen()
    }
}
