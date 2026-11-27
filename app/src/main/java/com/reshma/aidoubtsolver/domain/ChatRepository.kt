package com.reshma.aidoubtsolver.domain

import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observe(subject: Subject): Flow<List<ChatMessage>>
    suspend fun addUserQuestion(subject: Subject, text: String): ChatMessage
    /** Asks the AI about [question]; on failure marks it FAILED and rethrows [AiException]. */
    suspend fun answer(question: ChatMessage)
    suspend fun clear(subject: Subject)
}
