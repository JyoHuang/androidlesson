package com.exmaple.androidlesson.presentation.grid

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.exmaple.androidlesson.ui.theme.AndroidLessonTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GridStrategyListScreen(
    stockId: String,
    onBack: () -> Unit = {},
    onAddNew: (stockId: String) -> Unit = {},
    onEditStrategy: (stockId: String, strategyId: String) -> Unit = { _, _ -> },
    viewModel: GridStrategyListViewModel = viewModel()
) {
    val uiState = viewModel.uiState

    LaunchedEffect(Unit) {
        viewModel.start(stockId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "網格策略列表 - $stockId",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onAddNew(stockId) }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "新增策略"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        // 內容區
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.errorMessage ?: "讀取失敗",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            uiState.strategies.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("目前尚未設定任何網格策略。")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { onAddNew(uiState.stockId) }) {
                            Text("建立第一個網格策略")
                        }
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.strategies) { strategy ->
                        GridStrategyCard(
                            stockId = uiState.stockId,
                            strategy = strategy,
                            onToggleActive = { newActive ->
                                viewModel.toggleActive(strategy.id, newActive)
                            },
                            onDelete = {
                                viewModel.deleteStrategy(strategy.id)
                            },
                            onClick = {
                                onEditStrategy(uiState.stockId, strategy.id)
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun GridStrategyCard(
    stockId: String,
    strategy: GridStrategySummary,
    onToggleActive: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // 第一列：標題 + 啟用 Switch + 刪除
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "$stockId 網格策略",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "價格區間：${strategy.lowerPrice} ~ ${strategy.upperPrice}",
                        fontSize = 13.sp
                    )
                }

                Switch(
                    checked = strategy.active,
                    onCheckedChange = onToggleActive
                )

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "刪除策略"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 第二列：網格數量 + 間距概念
            val spacing =
                if (strategy.gridCount > 0) (strategy.upperPrice - strategy.lowerPrice) / strategy.gridCount.toDouble()
                else null

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("網格數量", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Text(
                        text = "${strategy.gridCount}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("每格價差（約）", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Text(
                        text = spacing?.let {
                            String.format("%.2f", it)
                        } ?: "-",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 第三列：通知間隔 + 止損
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("通知間隔（分鐘）", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Text(
                        text = "${strategy.cooldownMinutes}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("止損價", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Text(
                        text = strategy.stopLossPrice?.toString() ?: "-",
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
fun GridStrategyListScreenPreview() {
    AndroidLessonTheme {
        GridStrategyListScreen(
            stockId = "2330"
        )
    }
}
