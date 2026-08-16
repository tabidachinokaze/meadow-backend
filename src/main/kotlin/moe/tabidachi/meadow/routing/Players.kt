package moe.tabidachi.meadow.routing

import io.ktor.server.plugins.di.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import moe.tabidachi.meadow.ktx.getParameter
import moe.tabidachi.meadow.service.PlayerService

/**
 * 玩家路由（规划 §9.2）
 * 挂载于 JWT 认证块内（见 plugins/Routing.kt）
 */
fun Route.players() {
    val playerService: PlayerService by application.dependencies

    route("/servers/{id}") {
        get("/players") {
            val serverId = getParameter<Long>("id")
            val filter = call.request.queryParameters["filter"]
            val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceIn(1, 100) ?: 50
            call.respond(playerService.getServerPlayers(serverId, filter, limit))
        }
    }

    get("/players/{uuid}") {
        val uuid = getParameter<String>("uuid")
        call.respond(playerService.getPlayer(uuid))
    }
}
