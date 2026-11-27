package com.reshma.aidoubtsolver.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reshma.aidoubtsolver.domain.AiException
import com.reshma.aidoubtsolver.domain.ChatMessage
import com.reshma.aidoubtsolver.domain.ChatRepository
import com.reshma.aidoubtsolver.domain.Subject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val subject: Subject = Subject.GENERAL,
    val messages: List<ChatMessage> = emptyList(),
    val isThinking: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository,
) : ViewModel() {

    private val subject = MutableStateFlow(Subject.GENERAL)
    private val thinking = MutableStateFlow(false)

    private val _errors = Channel<String>(Channel.BUFFERED)
    val errors = _errors.receiveAsFlow()

    val state: StateFlow<ChatUiState> = combine(
        subject,
        subject.flatMapLatest { repository.observe(it) },
        thinking,
    ) { s, msgs, t -> ChatUiState(s, msgs, t) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatUiState())

    fun selectSubject(s: Subject) { subject.value = s }

    fun send(raw: String) {
        val text = raw.trim()
        if (text.isEmpty() || thinking.value) return
        viewModelScope.launch {
            val q = repository.addUserQuestion(subject.value, text)
            ask(q)
        }
    }

    fun retry(question: ChatMessage) {
        if (thinking.value) return
        viewModelScope.launch { ask(question) }
    }

    fun clear() {
        viewModelScope.launch { repository.clear(subject.value) }
    }

    private suspend fun ask(question: ChatMessage) {
        thinking.value = true
        try {
            repository.answer(question)
        } catch (e: AiException) {
            _errors.send(e.message ?: "Something went wrong.")
        } finally {
            thinking.value = false
        }
    }
}
