package moe.tabidachi.meadow.database.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** 聊天消息（数据库模型） */
@Serializable
data class ChatMessage(
    val id: Long,
    val serverId: Long,
    val senderName: String,
    val senderUuid: String?,
    val content: String,
    val type: String,
    val isBroadcast: Boolean,
    val isRecalled: Boolean,
    val createdAt: Instant,
)
