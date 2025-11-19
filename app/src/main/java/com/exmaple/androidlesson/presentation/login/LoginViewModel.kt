package com.exmaple.androidlesson.presentation.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class LoginUiState(
    val account: String = "",
    val password: String = ""
)

class LoginViewModel : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onAccountChanged(newAccount: String) {
        uiState = uiState.copy(account = newAccount)
    }

    fun onPasswordChanged(newPassword: String) {
        uiState = uiState.copy(password = newPassword)
    }

    fun login(onSuccess: () -> Unit) {
        // TODO: 未來可以在這裡接 Firebase Auth 驗證
        onSuccess()
    }
}
