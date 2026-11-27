package com.reshma.aidoubtsolver.data.repository

import com.reshma.aidoubtsolver.data.local.MessageDao
import com.reshma.aidoubtsolver.data.local.toDomain
import com.reshma.aidoubtsolver.data.local.toEntity
import com.reshma.aidoubtsolver.data.remote.AiDataSource
import com.reshma.aidoubtsolver.domain.AiException
import com.reshma.aidoubtsolver.domain.ChatMessage
import com.reshma.aidoubtsolver.domain.ChatRepository
import com.reshma.aidoubtsolver.domain.MessageStatus
import com.reshma.aidoubtsolver.domain.Role
import com.reshma.aidoubtsolver.domain.Subject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val dao: MessageDao,
    private val ai: AiDataSource,
) : ChatRepository {

    override fun observe(subject: Subject): Flow<List<ChatMessage>> =
        dao.observe(subject.name).map { list -> list.map { it.toDomain() } }

    override suspend fun addUserQuestion(subject: Subject, text: String): ChatMessage {
        val msg = ChatMessage(
            id = UUID.randomUUID().toString(),
            subject = subject,
            role = Role.USER,
            text = text,
            createdAt = System.currentTimeMillis(),
        )
        dao.upsert(msg.toEntity())
        return msg
    }

    override suspend fun answer(question: ChatMessage) {
        dao.setStatus(question.id, MessageStatus.SENT.name)
        val history = dao.getAll(question.subject.name).map { it.toDomain() }
        try {
            val reply = ai.ask(question.subject, history)
            dao.upsert(
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    subject = question.subject,
                    role = Role.ASSISTANT,
                    text = reply,
                    createdAt = System.currentTimeMillis(),
                ).toEntity(),
            )
        } catch (e: AiException) {
            dao.setStatus(question.id, MessageStatus.FAILED.name)
            throw e
        }
    }

    override suspend fun clear(subject: Subject) = dao.clear(subject.name)
}
