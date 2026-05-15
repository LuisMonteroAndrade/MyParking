package com.miestacionamiento.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.miestacionamiento.utils.PreferencesManager
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PreferencesManager(application)

    val isDarkMode = prefs.isDarkMode.asLiveData()
    val language = prefs.language.asLiveData()
    val userName = prefs.userName.asLiveData()
    val userEmail = prefs.userEmail.asLiveData()
    val notificationsEnabled = prefs.notificationsEnabled.asLiveData()
    val profileImageUri = prefs.profileImageUri.asLiveData()

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { prefs.setDarkMode(enabled) }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch { prefs.setLanguage(lang) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.setNotificationsEnabled(enabled) }
    }

    fun saveProfile(name: String, email: String, photoUri: String) {
        viewModelScope.launch { prefs.updateProfile(name, email, photoUri) }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            prefs.clearSession()
            onDone()
        }
    }
}
