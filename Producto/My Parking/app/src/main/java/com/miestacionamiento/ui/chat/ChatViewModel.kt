package com.miestacionamiento.ui.chat

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.miestacionamiento.data.model.CreateMessageRequest
import com.miestacionamiento.data.model.Message
import com.miestacionamiento.data.remote.RetrofitClient
import com.miestacionamiento.utils.PreferencesManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val api = RetrofitClient.instance
    private val prefs = PreferencesManager(application)

    private val _messages = MutableLiveData<List<Message>>(emptyList())
    val messages: LiveData<List<Message>> = _messages

    private val _isSending = MutableLiveData(false)
    val isSending: LiveData<Boolean> = _isSending

    private val _userId = MutableLiveData(0)
    val userId: LiveData<Int> = _userId

    private var conversationId = -1
    private var pollingJob: Job? = null

    init {
        viewModelScope.launch {
            _userId.value = prefs.userId.first()
        }
    }

    fun initialize(convId: Int) {
        conversationId = convId
        loadMessages()
        startPolling()
    }

    fun loadMessages() {
        viewModelScope.launch {
            try {
                val response = api.getMessages(conversationId)
                if (response.isSuccessful) {
                    _messages.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("ChatVM", "Error cargando mensajes", e)
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return
        _isSending.value = true
        viewModelScope.launch {
            try {
                val response = api.sendMessage(conversationId, CreateMessageRequest(content.trim()))
                if (response.isSuccessful) {
                    val newMessage = response.body()!!
                    val current = _messages.value?.toMutableList() ?: mutableListOf()
                    current.add(newMessage)
                    _messages.value = current
                }
            } catch (e: Exception) {
                Log.e("ChatVM", "Error enviando mensaje", e)
            } finally {
                _isSending.value = false
            }
        }
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(5000)
                try {
                    val response = api.getMessages(conversationId)
                    if (response.isSuccessful) {
                        val newList = response.body() ?: emptyList()
                        if (newList.size != (_messages.value?.size ?: 0)) {
                            _messages.value = newList
                        }
                    }
                } catch (_: Exception) {}
            }
        }
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }
}
