package com.exmaple.androidlesson.presentation.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class LoginViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onEmailChanged(newEmail: String) {
        uiState = uiState.copy(email = newEmail)
    }

    fun onPasswordChanged(newPassword: String) {
        uiState = uiState.copy(password = newPassword)
    }

    fun login(onSuccess: () -> Unit) {
        val email = uiState.email.trim()
        val password = uiState.password

        if (email.isBlank() || password.isBlank()) {
            uiState = uiState.copy(
                errorMessage = "Email and password must not be empty."
            )
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    uiState = uiState.copy(isLoading = false, errorMessage = null)
                    onSuccess()
                } else {
                    val msg = task.exception?.localizedMessage ?: "Login failed."
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = msg
                    )
                }
            }
    }

    // 如果你想順便提供「註冊」也可以加一個：
    fun register(onSuccess: () -> Unit) {
        val email = uiState.email.trim()
        val password = uiState.password

        if (email.isBlank() || password.isBlank()) {
            uiState = uiState.copy(
                errorMessage = "Email and password must not be empty."
            )
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    uiState = uiState.copy(isLoading = false, errorMessage = null)
                    onSuccess()
                } else {
                    val msg = task.exception?.localizedMessage ?: "Register failed."
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = msg
                    )
                }
            }
    }
}
