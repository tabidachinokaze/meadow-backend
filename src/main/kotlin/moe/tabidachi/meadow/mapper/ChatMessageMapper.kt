package moe.tabidachi.meadow.mapper

import moe.tabidachi.meadow.database.entity.ChatMessageEntity
import moe.tabidachi.meadow.database.model.ChatMessage

object ChatMessageMapper {
    fun toChatMessage(entity: ChatMessageEntity): ChatMessage {
        return ChatMessage(
            id = entity.id.value,
            serverId = entity.serverId,
            senderName = entity.senderName,
            senderUuid = entity.senderUuid,
            content = entity.content,
            type = entity.type,
            isBroadcast = entity.isBroadcast,
            isRecalled = entity.isRecalled,
            createdAt = entity.createdAt,
        )
    }
}
