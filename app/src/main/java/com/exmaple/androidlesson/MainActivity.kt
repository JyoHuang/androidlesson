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
    var isLoggedIn by rememberSaveable { mutableStateOf(false) }

    if (!isLoggedIn) {
        LoginScreen(
            onLoginSuccess = { isLoggedIn = true }
        )
    } else {
        MainTabScaffold()
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    AndroidLessonTheme {
        AppRoot()
    }
}
