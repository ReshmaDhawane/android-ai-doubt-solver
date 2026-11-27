package com.reshma.aidoubtsolver.data.remote

import com.reshma.aidoubtsolver.domain.AiException
import com.reshma.aidoubtsolver.domain.ChatMessage
import com.reshma.aidoubtsolver.domain.Subject
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException

interface AiDataSource {
    suspend fun ask(subject: Subject, history: List<ChatMessage>): String
}

class RemoteAiDataSource(
    private val api: AiApi,
    private val model: String,
) : AiDataSource {
    override suspend fun ask(subject: Subject, history: List<ChatMessage>): String {
        val request = CompletionRequest(
            model = model,
            messages = listOf(ApiMessage("system", subject.systemPrompt)) +
                history.takeLast(MAX_CONTEXT).map {
                    ApiMessage(if (it.isUser) "user" else "assistant", it.text)
                },
        )
        return try {
            api.complete(request).choices.first().message.content.trim()
        } catch (e: HttpException) {
            throw AiException(
                when (e.code()) {
                    401 -> "Invalid API key."
                    429 -> "Rate limited. Please try again shortly."
                    else -> "Server error (${e.code()})."
                },
            )
        } catch (e: IOException) {
            throw AiException("Network error. Check your connection.")
        }
    }

    private companion object { const val MAX_CONTEXT = 12 }
}

/** Offline tutor used when no API key is configured. */
class MockAiDataSource : AiDataSource {
    override suspend fun ask(subject: Subject, history: List<ChatMessage>): String {
        delay(700)
        val q = history.last { it.isUser }.text
        return "(${subject.label} demo tutor) Great question: \"$q\".\n\n" +
            "1. Identify what is given and what is asked.\n" +
            "2. Recall the core concept or formula.\n" +
            "3. Apply it step by step and check units.\n\n" +
            "Add AI_API_KEY to local.properties for real answers."
    }
}
