package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.model.ChatHistoryResult
import moe.tabidachi.meadow.model.ChatMessageInfo
import moe.tabidachi.meadow.model.Response

interface ChatService {
    /** 聊天历史（before 时间戳加载更早） */
    suspend fun getHistory(serverId: Long, limit: Int, before: kotlin.time.Instant?): Response<ChatHistoryResult?>

    /** 发送消息（入库 + WebSocket 广播） */
    suspend fun sendMessage(callerId: Long, serverId: Long, content: String): Response<ChatMessageInfo?>

    /** 撤回消息（owner / admin） */
    suspend fun recallMessage(callerId: Long, serverId: Long, messageId: Long): Response<Long?>

    /** Agent 上报聊天消息（server_key + machine_id 认证，规划 §9.12 实时事件） */
    suspend fun reportAgentMessage(
        serverId: Long,
        serverKey: String,
        machineId: String,
        senderUuid: String?,
        senderName: String,
        content: String,
        type: String,
    ): Response<ChatMessageInfo?>
}
