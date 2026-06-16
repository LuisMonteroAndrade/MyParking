package com.miestacionamiento.ui.chat

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.miestacionamiento.data.model.Conversation
import com.miestacionamiento.data.remote.RetrofitClient
import com.miestacionamiento.utils.PreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ChatListViewModel(application: Application) : AndroidViewModel(application) {

    private val api = RetrofitClient.instance
    private val prefs = PreferencesManager(application)

    private val _conversations = MutableLiveData<List<Conversation>>(emptyList())
    val conversations: LiveData<List<Conversation>> = _conversations

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _userId = MutableLiveData(0)
    val userId: LiveData<Int> = _userId

    init {
        viewModelScope.launch {
            _userId.value = prefs.userId.first()
            loadConversations()
        }
    }

    fun loadConversations() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getConversations()
                if (response.isSuccessful) {
                    _conversations.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("ChatListVM", "Error cargando conversaciones", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
