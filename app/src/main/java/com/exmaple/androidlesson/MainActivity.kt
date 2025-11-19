package com.exmaple.androidlesson

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exmaple.androidlesson.ui.theme.AndroidLessonTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidLessonTheme {
                AppRoot()
            }
        }
    }
}

/**
 * AppRoot：決定顯示「登入頁」還是「主畫面（有 TabBar）」。
 */
@Composable
fun AppRoot() {
    var isLoggedIn by rememberSaveable { mutableStateOf(false) }

    if (!isLoggedIn) {
        LoginScreen(
            onLoginSuccess = {
                // 這邊先不做真正驗證，按下就當作登入成功
                isLoggedIn = true
            }
        )
    } else {
        MainTabScaffold()
    }
}

/**
 * 登入畫面：帳號 + 密碼 + 登入按鈕
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "股票價格通知系統",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = account,
                onValueChange = { account = it },
                label = { Text("帳號") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("密碼") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    // 未來可以在這裡接 Firebase Auth
                    onLoginSuccess()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("登入")
            }
        }
    }
}

/**
 * 底部 Tab 切換的主畫面
 */
@Composable
fun MainTabScaffold() {
    var currentTab by rememberSaveable { mutableStateOf(BottomTab.Home) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                BottomTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                BottomTab.Home -> HomeScreen()
                BottomTab.Search -> StockSearchScreen()
                BottomTab.Favorite -> FavoriteScreen()
                BottomTab.Notification -> NotificationListScreen()
            }
        }
    }
}

/**
 * 底部四個 Tab 的定義
 */
enum class BottomTab(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Home("首頁", Icons.Filled.Home),
    Search("股票查詢", Icons.Filled.Search),
    Favorite("我的最愛", Icons.Filled.Favorite),
    Notification("通知列表", Icons.Filled.List)
}

/* ------- 以下是四個頁面的簡單 Title 畫面 ------- */

@Composable
fun HomeScreen() {
    CenterTitle(title = "首頁 - 今日股市最新消息 / 推薦網格操作的股票（之後再做）")
}

@Composable
fun StockSearchScreen() {
    CenterTitle(title = "股票查詢 - 依名稱 / 代號查詢，並可加入我的最愛")
}

@Composable
fun FavoriteScreen() {
    CenterTitle(title = "我的最愛 - 設定通知機制與觀看股票")
}

@Composable
fun NotificationListScreen() {
    CenterTitle(title = "通知列表 - 範例：XX 股票在 2025/12/3 18:00 達到 150 元")
}

/**
 * 共用的置中 Title 元件
 */
@Composable
fun CenterTitle(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 18.sp
        )
    }
}

/**
 * 預覽用
 */
@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    AndroidLessonTheme {
        LoginScreen(onLoginSuccess = {})
    }
}

@Preview(showBackground = true)
@Composable
fun MainTabPreview() {
    AndroidLessonTheme {
        MainTabScaffold()
    }
}
