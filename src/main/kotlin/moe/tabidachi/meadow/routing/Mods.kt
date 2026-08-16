package moe.tabidachi.meadow.routing

import io.ktor.server.plugins.di.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import moe.tabidachi.meadow.ktx.getParameter
import moe.tabidachi.meadow.service.ModService

/**
 * Mod 路由（规划 §9.3）
 * 挂载于 JWT 认证块内（见 plugins/Routing.kt）
 */
fun Route.mods() {
    val modService: ModService by application.dependencies
    route("/servers/{id}") {
        get("/mods") {
            val serverId = getParameter<Long>("id")
            val keyword = call.request.queryParameters["keyword"]
            val category = call.request.queryParameters["category"]
            call.respond(modService.getServerMods(serverId, keyword, category))
        }
    }
}
