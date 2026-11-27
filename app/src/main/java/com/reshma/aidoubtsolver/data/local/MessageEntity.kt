package com.reshma.aidoubtsolver.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.reshma.aidoubtsolver.domain.ChatMessage
import com.reshma.aidoubtsolver.domain.MessageStatus
import com.reshma.aidoubtsolver.domain.Role
import com.reshma.aidoubtsolver.domain.Subject

@Entity(tableName = "messages", indices = [Index("subject")])
data class MessageEntity(
    @PrimaryKey val id: String,
    val subject: String,
    val role: String,
    val text: String,
    val createdAt: Long,
    val status: String,
)

fun MessageEntity.toDomain() = ChatMessage(
    id = id,
    subject = Subject.valueOf(subject),
    role = Role.valueOf(role),
    text = text,
    createdAt = createdAt,
    status = MessageStatus.valueOf(status),
)

fun ChatMessage.toEntity() = MessageEntity(
    id = id,
    subject = subject.name,
    role = role.name,
    text = text,
    createdAt = createdAt,
    status = status.name,
)
