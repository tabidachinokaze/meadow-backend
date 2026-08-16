package moe.tabidachi.meadow.routing

import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.di.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import moe.tabidachi.meadow.contract.AuthenticationNames
import moe.tabidachi.meadow.ktx.getParameter
import moe.tabidachi.meadow.model.request.AgentChatReportRequest
import moe.tabidachi.meadow.model.request.ServerStatusRequest
import moe.tabidachi.meadow.service.ChatService
import moe.tabidachi.meadow.service.ServerStatusService

/**
 * Agent 上报路由（规划 §9.12）
 * 免认证（面向游戏服 Agent，通过 server_key + machine_id 认证）
 */
fun Route.serverStatus() {
    val serverStatusService: ServerStatusService by application.dependencies
    val chatService: ChatService by application.dependencies
    authenticate(AuthenticationNames.NONE) {
        post<ServerStatusRequest>("/servers/{id}/sync/status") { request ->
            val serverId = getParameter<Long>("id")
            call.respond(serverStatusService.report(serverId, request))
        }

        // Agent 上报聊天消息（聊天/死亡/成就等实时事件）
        post<AgentChatReportRequest>("/servers/{id}/sync/chat") { request ->
            val serverId = getParameter<Long>("id")
            call.respond(
                chatService.reportAgentMessage(
                    serverId = serverId,
                    serverKey = request.serverKey,
                    machineId = request.machineId,
                    senderUuid = request.senderUuid,
                    senderName = request.senderName,
                    content = request.content,
                    type = request.type,
                )
            )
        }
    }
}
