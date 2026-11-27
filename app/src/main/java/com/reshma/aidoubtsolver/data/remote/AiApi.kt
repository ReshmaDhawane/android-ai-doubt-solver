package com.reshma.aidoubtsolver.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

/** OpenAI-compatible Chat Completions API. */
interface AiApi {
    @POST("chat/completions")
    suspend fun complete(@Body request: CompletionRequest): CompletionResponse
}

@Serializable
data class ApiMessage(val role: String, val content: String)

@Serializable
data class CompletionRequest(
    val model: String,
    val messages: List<ApiMessage>,
    val temperature: Double = 0.3,
)

@Serializable
data class CompletionResponse(val choices: List<Choice>) {
    @Serializable
    data class Choice(val message: ApiMessage)
}
