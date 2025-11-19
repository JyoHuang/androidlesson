package com.exmaple.androidlesson.presentation.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

enum class BottomTab {
    Home,
    Search,
    Favorite,
    Notification
}

class MainViewModel : ViewModel() {

    var currentTab by mutableStateOf(BottomTab.Home)
        private set

    fun selectTab(tab: BottomTab) {
        currentTab = tab
    }
}
