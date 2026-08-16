package moe.tabidachi.meadow.routing

import io.ktor.server.plugins.di.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import moe.tabidachi.meadow.ktx.callingUserId
import moe.tabidachi.meadow.ktx.getParameter
import moe.tabidachi.meadow.model.request.BanPlayerRequest
import moe.tabidachi.meadow.model.request.HandleReportRequest
import moe.tabidachi.meadow.service.AdminService

/**
 * 管理员路由（规划 §9.9）
 * 挂载于 JWT 认证块内（见 plugins/Routing.kt）
 */
fun Route.admin() {
    val adminService: AdminService by application.dependencies

    get("/admin/reports") {
        val status = call.request.queryParameters["status"]
        call.respond(adminService.getReports(status))
    }

    patch<HandleReportRequest>("/admin/reports/{id}") { request ->
        val reportId = getParameter<Long>("id")
        call.respond(adminService.handleReport(callingUserId, reportId, request))
    }

    route("/servers/{id}") {
        get("/bans") {
            val serverId = getParameter<Long>("id")
            call.respond(adminService.getBans(callingUserId, serverId))
        }

        post<BanPlayerRequest>("/bans") { request ->
            val serverId = getParameter<Long>("id")
            call.respond(adminService.banPlayer(callingUserId, serverId, request))
        }

        delete("/bans/{banId}") {
            val serverId = getParameter<Long>("id")
            val banId = getParameter<Long>("banId")
            call.respond(adminService.unbanPlayer(callingUserId, serverId, banId))
        }
    }
}
