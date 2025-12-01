package com.exmaple.androidlesson.presentation.favorite

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.exmaple.androidlesson.data.favorites.FavoriteStock
import com.exmaple.androidlesson.ui.theme.AndroidLessonTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete


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

                uiState.favorites.isEmpty() -> {
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
                        items(uiState.favorites) { fav ->
                            FavoriteItemCard(
                                fav = fav,
                                onDelete = { viewModel.deleteFavorite(fav.code) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteItemCard(
    fav: FavoriteStock,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { /* 未來可以做「查看詳情」 */ },
                onLongClick = onDelete // ⭐ 長按刪除
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "${fav.code}  ${fav.name}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "最近一次記錄價格：${fav.lastPrice ?: "-"}",
                fontSize = 14.sp
            )
        }
        // ⭐ 右側刪除按鈕
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "刪除此收藏"
            )
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
