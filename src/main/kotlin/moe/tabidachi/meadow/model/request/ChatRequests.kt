package moe.tabidachi.meadow.model.request

import kotlinx.serialization.Serializable

/** 发送聊天消息 */
@Serializable
data class SendChatMessageRequest(
    val content: String,
    /** 消息类型：chat 普通聊天 / announcement 系统公告（广播到游戏内） */
    val type: String = "chat",
)
