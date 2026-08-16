package moe.tabidachi.meadow.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** 聊天消息（API 响应） */
@Serializable
data class ChatMessageInfo(
    val id: Long,
    val serverId: Long,
    val senderName: String,
    val senderUuid: String?,
    val senderRole: String? = null,
    val content: String,
    val type: String,
    val isBroadcast: Boolean,
    val isRecalled: Boolean,
    val createdAt: Instant?,
)

/** 聊天历史（9.5.1 响应） */
@Serializable
data class ChatHistoryResult(
    val messages: List<ChatMessageInfo>,
    val hasMore: Boolean,
)
