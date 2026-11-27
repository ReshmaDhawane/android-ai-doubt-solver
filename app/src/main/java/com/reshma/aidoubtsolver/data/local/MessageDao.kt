package com.reshma.aidoubtsolver.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE subject = :subject ORDER BY createdAt ASC")
    fun observe(subject: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE subject = :subject ORDER BY createdAt ASC")
    suspend fun getAll(subject: String): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(message: MessageEntity)

    @Query("UPDATE messages SET status = :status WHERE id = :id")
    suspend fun setStatus(id: String, status: String)

    @Query("DELETE FROM messages WHERE subject = :subject")
    suspend fun clear(subject: String)
}
