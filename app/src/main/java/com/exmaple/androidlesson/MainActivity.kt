package com.exmaple.androidlesson

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.tooling.preview.Preview
import com.exmaple.androidlesson.presentation.grid.GridStrategyEditScreen
import com.exmaple.androidlesson.presentation.grid.GridStrategyListScreen
import com.exmaple.androidlesson.presentation.login.LoginScreen
import com.exmaple.androidlesson.presentation.main.MainTabScaffold
import com.exmaple.androidlesson.ui.theme.AndroidLessonTheme
import com.google.firebase.auth.FirebaseAuth // 確保 FirebaseAuth 類別可以被解析
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

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
sealed class RootScreen {
    object MainTabs : RootScreen()
    data class GridList(val stockId: String) : RootScreen()
    data class GridEdit(val stockId: String, val strategyId: String?) : RootScreen()
}

@Composable
fun AppRoot() {
    val auth = remember { FirebaseAuth.getInstance() }
    // 初始狀態：如果 currentUser 不為空，就代表已登入
    var isLoggedIn by rememberSaveable { mutableStateOf(auth.currentUser != null) }

    // ⭐ 把 rememberSaveable 換成 remember
    var rootScreen by remember { mutableStateOf<RootScreen>(RootScreen.MainTabs) }

    if (!isLoggedIn) {
        LoginScreen(onLoginSuccess = {
            isLoggedIn = true
        })
    } else {
        when (val screen = rootScreen) {
            is RootScreen.MainTabs -> {
                MainTabScaffold(
                    onLogout = {
                        auth.signOut()     // 真的登出 Firebase
                        isLoggedIn = false // 觸發 recomposition，畫面回登入頁
                        println("登出")
                    }
                    ,
                    // 從 FavoriteTab 裡點進來某一檔股票的網格策略列表
                    onOpenGridForStock = { stockId ->
                        rootScreen = RootScreen.GridList(stockId)
                    }
                )
            }

            is RootScreen.GridList -> {
                GridStrategyListScreen(
                    stockId = screen.stockId,
                    onBack = { rootScreen = RootScreen.MainTabs },
                    onAddNew = { stockId ->
                        rootScreen = RootScreen.GridEdit(stockId, null)
                    },
                    onEditStrategy = { stockId, strategyId ->
                        rootScreen = RootScreen.GridEdit(stockId, strategyId)
                    }
                )
            }

            is RootScreen.GridEdit -> {
                GridStrategyEditScreen(
                    stockId = screen.stockId,
                    strategyId = screen.strategyId,
                    onClose = {
                        // 編輯完回到該股票的策略列表
                        rootScreen = RootScreen.GridList(screen.stockId)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    AndroidLessonTheme {
        AppRoot()
    }
}
