package com.reshma.aidoubtsolver.domain

enum class Role { USER, ASSISTANT }

enum class MessageStatus { SENT, FAILED }

data class ChatMessage(
    val id: String,
    val subject: Subject,
    val role: Role,
    val text: String,
    val createdAt: Long,
    val status: MessageStatus = MessageStatus.SENT,
) {
    val isUser: Boolean get() = role == Role.USER
}

enum class Subject(val label: String, private val styleHint: String) {
    MATHS("Maths", "Explain step by step with worked examples."),
    PHYSICS("Physics", "Use formulas, units and real-life intuition."),
    CHEMISTRY("Chemistry", "Mention reactions and balanced equations where relevant."),
    PROGRAMMING("Programming", "Give short, runnable code snippets."),
    GENERAL("General", "Answer clearly and concisely.");

    val systemPrompt: String
        get() = "You are a friendly $label tutor for students. $styleHint " +
            "If the question is ambiguous, ask one clarifying question. Keep answers under 250 words."
}

class AiException(message: String) : Exception(message)
