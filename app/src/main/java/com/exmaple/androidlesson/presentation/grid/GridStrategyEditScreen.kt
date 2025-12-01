package com.exmaple.androidlesson.presentation.grid

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun GridStrategyEditScreen(
    stockId: String,
    strategyId: String? = null,
    onClose: () -> Unit = {},
    viewModel: GridStrategyEditViewModel = viewModel()
) {
    val uiState = viewModel.uiState

    // 初始化（只跑一次）
    LaunchedEffect(Unit) {
        viewModel.start(stockId, strategyId)
    }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            // 標題列
            Text(
                text = "網格策略設定 - ${uiState.stockId}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading && !uiState.isNew) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            // 1. 價格區間
            Text(
                text = "價格區間",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = uiState.lowerPrice,
                    onValueChange = viewModel::onLowerPriceChange,
                    label = { Text("最低價") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = uiState.upperPrice,
                    onValueChange = viewModel::onUpperPriceChange,
                    label = { Text("最高價") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. 網格數量
            Text(
                text = "網格設定",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.gridCount,
                onValueChange = viewModel::onGridCountChange,
                label = { Text("網格數量") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // 小提示：顯示大概的格距
            val lower = uiState.lowerPrice.toDoubleOrNull()
            val upper = uiState.upperPrice.toDoubleOrNull()
            val grids = uiState.gridCount.toIntOrNull()
            if (lower != null && upper != null && grids != null && grids > 0) {
                val spacing = (upper - lower) / grids.toDouble()
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "每格約 ${(spacing).toBigDecimal().setScale(2, java.math.RoundingMode.HALF_UP)} 單位價差",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. 通知參數
            Text(
                text = "通知參數",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.cooldownMinutes,
                onValueChange = viewModel::onCooldownMinutesChange,
                label = { Text("通知間隔（分鐘）") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.stopLossPrice,
                onValueChange = viewModel::onStopLossPriceChange,
                label = { Text("止損價（可選）") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "啟用此網格策略",
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = uiState.active,
                    onCheckedChange = viewModel::onActiveChange
                )
            }

            // 錯誤 / 提示訊息
            uiState.errorMessage?.let { msg ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            uiState.infoMessage?.let { msg ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 底部按鈕列
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onClose,
                ) {
                    Text("返回")
                }

                Button(
                    onClick = { viewModel.save(onSuccess = onClose) },
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("儲存中…")
                    } else {
                        Text("儲存策略")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GridStrategyEditScreenPreview() {
    AndroidLessonTheme {
        GridStrategyEditScreen(
            stockId = "2330",
            strategyId = null
        )
    }
}
