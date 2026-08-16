package moe.tabidachi.meadow.service

import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import moe.tabidachi.meadow.model.ChatMessageInfo
import java.util.concurrent.ConcurrentHashMap

/**
 * 聊天 WebSocket 会话枢纽：按服务器维护在线连接并广播新消息
 * （规划 §9.5.3 / §9.11）
 */
class ChatHub {
    private val json = Json {
        ignoreUnknownKeys = true
        // 与 REST 一致：全局 snake_case
        namingStrategy = JsonNamingStrategy.SnakeCase
    }
    private val connections = ConcurrentHashMap<Long, MutableMap<String, WebSocketSession>>()

    fun connect(serverId: Long, uid: Long, session: WebSocketSession) {
        connections.computeIfAbsent(serverId) { ConcurrentHashMap() }[sessionKey(uid, session)] = session
    }

    fun disconnect(serverId: Long, uid: Long, session: WebSocketSession) {
        connections[serverId]?.remove(sessionKey(uid, session))
    }

    /** 向该服务器所有连接广播一条消息（含发送者） */
    suspend fun broadcast(serverId: Long, message: ChatMessageInfo) {
        val text = json.encodeToString(ChatMessageInfo.serializer(), message)
        val frame = Frame.Text(text)
        connections[serverId]?.values?.forEach { session ->
            runCatching { session.send(frame) }
        }
    }

    private fun sessionKey(uid: Long, session: WebSocketSession): String =
        "$uid:${System.identityHashCode(session)}"
}
