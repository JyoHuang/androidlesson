package com.exmaple.androidlesson.presentation.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.exmaple.androidlesson.data.firebase.FcmTokenManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null     // ⭐ 新增：一般提示訊息
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

    /**
     * 共用欄位檢查：不空、長度夠
     */
    private fun validateInput(): Boolean {
        val email = uiState.email.trim()
        val password = uiState.password

        if (email.isBlank() || password.isBlank()) {
            uiState = uiState.copy(errorMessage = "Email and password must not be empty.")
            return false
        }

        // Firebase 要求密碼至少 6 碼
        if (password.length < 6) {
            uiState = uiState.copy(errorMessage = "Password must be at least 6 characters.")
            return false
        }

        return true
    }

    fun login(onSuccess: () -> Unit) {
        if (!validateInput()) return

        val email = uiState.email.trim()
        val password = uiState.password

        uiState = uiState.copy(isLoading = true, errorMessage = null)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // ✅ 登入成功後清空欄位
                    uiState = LoginUiState()
                    onSuccess()
                    updateUserToken(email)
                } else {
                    val msg = mapAuthErrorMessage(task.exception)
                    uiState = uiState.copy(isLoading = false, errorMessage = msg)
                }
            }
    }

    // 如果你想順便提供「註冊」也可以加一個：
    /**
     * 註冊新帳號
     * 用目前輸入的 email / password 呼叫 Firebase
     */
    fun register(onSuccess: () -> Unit) {
        if (!validateInput()) return

        val email = uiState.email.trim()
        val password = uiState.password

        uiState = uiState.copy(isLoading = true, errorMessage = null)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // ✅ 註冊成功後一樣清空欄位並視為已登入
                    uiState = LoginUiState()
                    onSuccess()
                    updateUserToken(email)
                } else {
                    val msg = mapAuthErrorMessage(task.exception)
                    uiState = uiState.copy(isLoading = false, errorMessage = msg)
                }
            }
    }
    private fun mapAuthErrorMessage(e: Exception?): String {
        val raw = e?.message ?: return "Something went wrong."
        return when {
            raw.contains("email address is badly formatted", ignoreCase = true) ->
                "Email 格式好像怪怪的，請再檢查一次。"
            raw.contains("password is invalid", ignoreCase = true) ->
                "密碼錯誤，請再試一次。"
            raw.contains("address is already in use", ignoreCase = true) ->
                "這個 Email 已經被註冊過囉，可以直接登入。"
            else -> raw
        }
    }

    /**
     * ⭐ 忘記密碼：寄送重設密碼信到目前輸入的 Email
     */
    fun resetPassword() {
        val email = uiState.email.trim()

        if (email.isBlank()) {
            uiState = uiState.copy(
                errorMessage = "請先輸入你的電子信箱",
                infoMessage = null
            )
            return
        }

        uiState = uiState.copy(
            isLoading = true,
            errorMessage = null,
            infoMessage = null
        )

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = null,
                        infoMessage = "重設密碼的信件已經寄出"
                    )
                } else {
                    val msg = task.exception?.localizedMessage ?: "寄信失敗"
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = msg,
                        infoMessage = null
                    )
                }
            }
    }
    private fun updateUserToken(email : String){
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser!!.uid

        db.collection("users").document(uid).set(
            mapOf(
                "email" to email,
                "createdAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        )
        FcmTokenManager.registerTokenForCurrentUser()
    }

}
