package moe.tabidachi.meadow.routing

import io.ktor.server.plugins.di.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import moe.tabidachi.meadow.ktx.callingUserId
import moe.tabidachi.meadow.ktx.getParameter
import moe.tabidachi.meadow.service.FavoriteService

/**
 * 收藏路由（规划 §9.1.1 / 原文档 4.2.3）
 * 挂载于 JWT 认证块内（见 plugins/Routing.kt）
 */
fun Route.favorites() {
    val favoriteService: FavoriteService by application.dependencies

    route("/servers/{id}") {
        post("/favorite") {
            val serverId = getParameter<Long>("id")
            call.respond(favoriteService.addFavorite(callingUserId, serverId))
        }

        delete("/favorite") {
            val serverId = getParameter<Long>("id")
            call.respond(favoriteService.removeFavorite(callingUserId, serverId))
        }
    }

    get("/users/me/favorites") {
        call.respond(favoriteService.getFavorites(callingUserId))
    }
}
