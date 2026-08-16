package moe.tabidachi.meadow.routing

import io.ktor.server.plugins.di.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import moe.tabidachi.meadow.ktx.callingUserId
import moe.tabidachi.meadow.ktx.getParameter
import moe.tabidachi.meadow.model.request.MapConfigRequest
import moe.tabidachi.meadow.service.MapService

/**
 * 地图路由（规划 §9.4）
 * 挂载于 JWT 认证块内（见 plugins/Routing.kt）
 */
fun Route.map() {
    val mapService: MapService by application.dependencies

    route("/servers/{id}") {
        get("/map/config") {
            val serverId = getParameter<Long>("id")
            call.respond(mapService.getConfig(serverId))
        }

        put<MapConfigRequest>("/map/config") { request ->
            val serverId = getParameter<Long>("id")
            call.respond(mapService.saveConfig(callingUserId, serverId, request))
        }

        get("/map/players") {
            val serverId = getParameter<Long>("id")
            call.respond(mapService.getPlayers(serverId))
        }
    }
}
