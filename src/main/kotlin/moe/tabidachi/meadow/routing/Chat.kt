package moe.tabidachi.meadow.routing

import io.ktor.server.plugins.di.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import moe.tabidachi.meadow.jwt.Claims
import moe.tabidachi.meadow.ktx.callingUserId
import moe.tabidachi.meadow.ktx.getParameter
import moe.tabidachi.meadow.model.request.SendChatMessageRequest
import moe.tabidachi.meadow.security.Jwt
import moe.tabidachi.meadow.service.ChatHub
import moe.tabidachi.meadow.service.ChatService

/**
 * 聊天路由（规划 §9.5）
 * REST 挂载于 JWT 认证块内；WebSocket 端点 /ws/chat 通过 query token 认证
 */
fun Route.chat() {
    val chatService: ChatService by application.dependencies

    route("/servers/{id}") {
        get("/chat/history") {
            val serverId = getParameter<Long>("id")
            val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceIn(1, 200) ?: 50
            val before = call.request.queryParameters["before"]?.let { runCatching { kotlin.time.Instant.parse(it) }.getOrNull() }
            call.respond(chatService.getHistory(serverId, limit, before))
        }

        post<SendChatMessageRequest>("/chat/messages") { request ->
            val serverId = getParameter<Long>("id")
            call.respond(chatService.sendMessage(callingUserId, serverId, request.content, request.type))
        }

        delete("/chat/messages/{mid}") {
            val serverId = getParameter<Long>("id")
            val messageId = getParameter<Long>("mid")
            call.respond(chatService.recallMessage(callingUserId, serverId, messageId))
        }
    }
}

/** WebSocket 客户端消息 */
@Serializable
private data class WsClientMessage(
    val type: String,
    val payload: WsPayload? = null,
) {
    @Serializable
    data class WsPayload(
        val content: String = "",
    )
}

/** WebSocket 实时聊天端点（规划 §9.5.3） */
fun Route.chatSocket() {
    val chatHub: ChatHub by application.dependencies
    val chatService: ChatService by application.dependencies
    val jwt: Jwt by application.dependencies
    val serverRepository: moe.tabidachi.meadow.repository.ServerRepository by application.dependencies
    val json = Json {
        ignoreUnknownKeys = true
        // 与 REST 一致：全局 snake_case
        namingStrategy = JsonNamingStrategy.SnakeCase
    }

    webSocket("/ws/chat") {
        val serverId = call.request.queryParameters["server_id"]?.toLongOrNull()
        if (serverId == null) {
            close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "missing server_id"))
            return@webSocket
        }
        val token = call.request.queryParameters["token"]
        val decoded = token?.let { runCatching { jwt.verifier.verify(it) }.getOrNull() }
        val uid = decoded?.getClaim(Claims.UID)?.asLong()
        if (uid == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "unauthorized"))
            return@webSocket
        }

        chatHub.connect(serverId, uid, this)
        try {
            for (frame in incoming) {
                if (frame !is Frame.Text) continue
                val text = frame.readText()
                val msg = runCatching { json.decodeFromString<WsClientMessage>(text) }.getOrNull() ?: continue
                when (msg.type) {
                    "message" -> {
                        val content = msg.payload?.content?.trim().orEmpty()
                        if (content.isNotEmpty()) {
                            // 入库 + 广播（含发送者自己的连接）
                            chatService.sendMessage(uid, serverId, content)
                        }
                    }

                    "ping" -> send(Frame.Text("""{"type":"pong"}"""))
                }
            }
        } finally {
            chatHub.disconnect(serverId, uid, this)
        }
    }

    // Agent 专用 WS：server_key + machine_id 认证，接收该服务器全部消息广播（规划 §9.12）
    // 凭据走请求头（X-Server-Key / X-Machine-Id），避免进 URL 造成日志泄露
    webSocket("/ws/agent") {
        val serverId = call.request.queryParameters["server_id"]?.toLongOrNull()
        if (serverId == null) {
            close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "missing server_id"))
            return@webSocket
        }
        val serverKey = call.request.headers["X-Server-Key"]
        val machineId = call.request.headers["X-Machine-Id"]
        val server = serverRepository.getById(serverId)
        if (server == null || server.serverKey != serverKey || server.machineId != machineId) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "unauthorized"))
            return@webSocket
        }
        // Agent 用固定虚拟 uid（负数）接入同一 ChatHub，接收广播
        chatHub.connect(serverId, -serverId, this)
        try {
            for (frame in incoming) {
                if (frame !is Frame.Text) continue
                val text = frame.readText()
                val msg = runCatching { json.decodeFromString<WsClientMessage>(text) }.getOrNull() ?: continue
                when (msg.type) {
                    "ping" -> send(Frame.Text("""{"type":"pong"}"""))
                }
            }
        } finally {
            chatHub.disconnect(serverId, -serverId, this)
        }
    }
}
