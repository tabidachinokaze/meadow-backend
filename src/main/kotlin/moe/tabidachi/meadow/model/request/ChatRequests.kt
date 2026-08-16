package moe.tabidachi.meadow.model.request

import kotlinx.serialization.Serializable

/** 发送聊天消息 */
@Serializable
data class SendChatMessageRequest(
    val content: String,
)
