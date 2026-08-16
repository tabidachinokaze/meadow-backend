package moe.tabidachi.meadow.routing

import io.ktor.server.plugins.*
import io.ktor.server.plugins.di.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import moe.tabidachi.meadow.ktx.callingUserId
import moe.tabidachi.meadow.ktx.getParameter
import moe.tabidachi.meadow.model.request.AddMemberRequest
import moe.tabidachi.meadow.model.request.TransferOwnershipRequest
import moe.tabidachi.meadow.model.request.UpdateMemberRequest
import moe.tabidachi.meadow.service.ServerMemberService

/**
 * 服务器成员管理路由（规划 §9.10）
 * 挂载于 JWT 认证块内（见 plugins/Routing.kt）
 */
fun Route.serverMembers() {
    val serverMemberService: ServerMemberService by application.dependencies
    route("/servers/{id}") {
        get("/members") {
            val serverId = getParameter<Long>("id")
            call.respond(serverMemberService.getMembers(serverId))
        }

        post<AddMemberRequest>("/members") { request ->
            val serverId = getParameter<Long>("id")
            call.respond(serverMemberService.addMember(callingUserId, serverId, request))
        }

        patch<UpdateMemberRequest>("/members/{userId}") { request ->
            val serverId = getParameter<Long>("id")
            val userId = getParameter<Long>("userId")
            call.respond(serverMemberService.updateMember(callingUserId, serverId, userId, request))
        }

        delete("/members/{userId}") {
            val serverId = getParameter<Long>("id")
            val userId = getParameter<Long>("userId")
            call.respond(serverMemberService.removeMember(callingUserId, serverId, userId))
        }

        post<TransferOwnershipRequest>("/transfer-ownership") { request ->
            val serverId = getParameter<Long>("id")
            call.respond(serverMemberService.transferOwnership(callingUserId, serverId, request))
        }

        get("/my-role") {
            val serverId = getParameter<Long>("id")
            call.respond(serverMemberService.getMyRole(callingUserId, serverId))
        }
    }
}
