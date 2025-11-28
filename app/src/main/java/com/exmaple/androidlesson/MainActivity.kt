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
import com.exmaple.androidlesson.presentation.login.LoginScreen
import com.exmaple.androidlesson.presentation.main.MainTabScaffold
import com.exmaple.androidlesson.ui.theme.AndroidLessonTheme
import com.google.firebase.auth.FirebaseAuth // 確保 FirebaseAuth 類別可以被解析

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

@Composable
fun AppRoot() {
    val auth = remember { FirebaseAuth.getInstance() }
    // 初始狀態：如果 currentUser 不為空，就代表已登入
    var isLoggedIn by rememberSaveable { mutableStateOf(auth.currentUser != null) }

    if (!isLoggedIn) {
        LoginScreen(
            onLoginSuccess = { isLoggedIn = true }
        )
    } else {
        MainTabScaffold(
            onLogout = {
                auth.signOut()     // 真的登出 Firebase
                isLoggedIn = false // 觸發 recomposition，畫面回登入頁
                println("登出")
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    AndroidLessonTheme {
        AppRoot()
    }
}
