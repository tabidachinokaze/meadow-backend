package moe.tabidachi.meadow.repository

import moe.tabidachi.meadow.database.entity.ChatMessageEntity
import moe.tabidachi.meadow.database.model.ChatMessage
import moe.tabidachi.meadow.database.table.ChatMessageTable
import moe.tabidachi.meadow.ktx.withTransaction
import moe.tabidachi.meadow.mapper.ChatMessageMapper
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock

interface ChatMessageRepository {
    /** 历史消息（时间倒序，before 用于加载更早；返回正序列表） */
    suspend fun getHistory(serverId: Long, limit: Int, before: kotlin.time.Instant?): List<ChatMessage>
    suspend fun getById(serverId: Long, messageId: Long): ChatMessage?
    suspend fun add(
        serverId: Long,
        senderName: String,
        senderUuid: String?,
        content: String,
        type: String,
    ): ChatMessage
    suspend fun recall(serverId: Long, messageId: Long): Boolean
}

class ChatMessageRepositoryImpl(
    private val database: Database,
) : ChatMessageRepository {
    override suspend fun getHistory(serverId: Long, limit: Int, before: kotlin.time.Instant?): List<ChatMessage> =
        database.withTransaction {
            val entities = if (before != null) {
                ChatMessageEntity.find {
                    ChatMessageTable.serverId.eq(serverId).and(ChatMessageTable.createdAt less before)
                }
            } else {
                ChatMessageEntity.find { ChatMessageTable.serverId.eq(serverId) }
            }
            val reversed = entities
                .orderBy(ChatMessageTable.createdAt to SortOrder.DESC)
                .limit(limit)
                .map(ChatMessageMapper::toChatMessage)
            reversed.reversed()
        }

    override suspend fun getById(serverId: Long, messageId: Long): ChatMessage? = database.withTransaction {
        ChatMessageEntity.find {
            ChatMessageTable.id.eq(messageId).and(ChatMessageTable.serverId.eq(serverId))
        }.singleOrNull()?.let(ChatMessageMapper::toChatMessage)
    }

    override suspend fun add(
        serverId: Long,
        senderName: String,
        senderUuid: String?,
        content: String,
        type: String,
    ): ChatMessage = database.withTransaction {
        val entity = ChatMessageEntity.new {
            this.serverId = serverId
            this.senderName = senderName
            this.senderUuid = senderUuid
            this.content = content
            this.type = type
            this.createdAt = Clock.System.now()
        }
        ChatMessageMapper.toChatMessage(entity)
    }

    override suspend fun recall(serverId: Long, messageId: Long): Boolean = database.withTransaction {
        val updateCount = ChatMessageTable.update({
            ChatMessageTable.id.eq(messageId).and(ChatMessageTable.serverId.eq(serverId))
        }) {
            it[isRecalled] = true
        }
        updateCount > 0
    }
}
