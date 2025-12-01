package com.exmaple.androidlesson.presentation.favorite

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.exmaple.androidlesson.data.favorites.FavoriteStock
import com.exmaple.androidlesson.data.favorites.FavoritesRepository
import com.google.firebase.firestore.ListenerRegistration

data class FavoriteUiState(
    val favorites: List<FavoriteStock> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class FavoriteViewModel : ViewModel() {

    var uiState by mutableStateOf(FavoriteUiState(isLoading = true))
        private set

    private var listenerRegistration: ListenerRegistration? = null

    init {
        subscribeFavorites()
    }

    private fun subscribeFavorites() {
        listenerRegistration = FavoritesRepository.observeFavorites { list, error ->
            uiState = if (error != null) {
                FavoriteUiState(
                    favorites = emptyList(),
                    isLoading = false,
                    errorMessage = error
                )
            } else {
                FavoriteUiState(
                    favorites = list,
                    isLoading = false,
                    errorMessage = null
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }

    fun deleteFavorite(code: String) {
        // 這裡我們不特別去改 uiState，因為 Firestore snapshot 會自動更新列表
        FavoritesRepository.deleteFavorite(code) { ok, error ->
            if (!ok && error != null) {
                // 簡單做一個錯誤提示（可選）
                uiState = uiState.copy(errorMessage = error)
            }
        }
    }

}
