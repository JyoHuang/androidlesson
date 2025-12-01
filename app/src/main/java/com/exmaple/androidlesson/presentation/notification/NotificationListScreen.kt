package com.exmaple.androidlesson.presentation.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.exmaple.androidlesson.ui.theme.AndroidLessonTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationListScreen(
    viewModel: NotificationListViewModel = viewModel()
) {
    val uiState = viewModel.uiState

    LaunchedEffect(Unit) {
        viewModel.start()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "通知列表",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // 清空所有通知
                    if (uiState.items.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearAll() }) {
                            Text("全部清除")
                        }
                    }
                    // ⭐ 暫時加一顆「測試」按鈕
                    IconButton(onClick = { viewModel.createDummyNotification() }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "新增測試通知"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
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
                        text = uiState.errorMessage ?: "讀取通知失敗",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            uiState.items.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "目前沒有任何通知。",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(uiState.items) { item ->
                        NotificationCard(
                            item = item,
                            onClick = {
                                if (!item.read) {
                                    viewModel.markAsRead(item.id)
                                }
                                // 之後如果要點了跳到該股票 / 詳細頁可以在這裡加 callback
                            },
                            onDelete = { viewModel.deleteNotification(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    item: NotificationItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val bgColor =
        if (item.read) MaterialTheme.colorScheme.surface
        else MaterialTheme.colorScheme.primaryContainer

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.title.ifBlank { "系統通知" },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    val stockLabel = listOfNotNull(item.stockId, item.stockName)
                        .joinToString(" ")

                    if (stockLabel.isNotBlank()) {
                        Text(
                            text = stockLabel,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "刪除通知"
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.body,
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.timeText(),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                if (!item.read) {
                    Text(
                        text = "未讀",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationListPreview() {
    AndroidLessonTheme {
        val demo = NotificationItem(
            id = "demo",
            stockId = "2330",
            stockName = "台積電",
            title = "2330 觸發第 3 格，考慮買入",
            body = "現價：603.0，區間：600~650，漲幅 2.5%。",
            type = "gridHit",
            createdAt = System.currentTimeMillis(),
            read = false
        )
        Scaffold { padding ->
            Box(modifier = Modifier.padding(padding)) {
                NotificationCard(
                    item = demo,
                    onClick = {},
                    onDelete = {}
                )
            }
        }
    }
}
