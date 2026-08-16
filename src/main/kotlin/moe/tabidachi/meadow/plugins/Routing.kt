package moe.tabidachi.meadow.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.config.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import moe.tabidachi.meadow.model.config.JwtConfig
import moe.tabidachi.meadow.routing.admin
import moe.tabidachi.meadow.routing.auth
import moe.tabidachi.meadow.routing.chat
import moe.tabidachi.meadow.routing.chatSocket
import moe.tabidachi.meadow.routing.contact
import moe.tabidachi.meadow.routing.favorites
import moe.tabidachi.meadow.routing.map
import moe.tabidachi.meadow.routing.mods
import moe.tabidachi.meadow.routing.modpacks
import moe.tabidachi.meadow.routing.players
import moe.tabidachi.meadow.routing.screenshots
import moe.tabidachi.meadow.routing.serverMembers
import moe.tabidachi.meadow.routing.serverStatus
import moe.tabidachi.meadow.routing.servers
import moe.tabidachi.meadow.routing.swagger
import moe.tabidachi.meadow.routing.user
import moe.tabidachi.meadow.routing.worlds

fun Application.configureRouting() {
    install(SSE)
    val jwtConfig = property<JwtConfig>("jwt")
    routing {
        auth()
        swagger()
        chatSocket()
        // 服务器列表/详情为公开接口（内部对写操作单独要求 JWT，见 routing/Servers.kt）
        servers()
        authenticate(jwtConfig.name) {
            user()
            serverMembers()
            favorites()
            screenshots()
            serverStatus()
            players()
            mods()
            chat()
            worlds()
            modpacks()
            admin()
            map()
            contact()
        }
    }
}