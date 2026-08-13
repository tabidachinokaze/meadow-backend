package moe.tabidachi.meadow.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.config.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import moe.tabidachi.meadow.model.config.JwtConfig
import moe.tabidachi.meadow.routing.auth
import moe.tabidachi.meadow.routing.servers
import moe.tabidachi.meadow.routing.swagger
import moe.tabidachi.meadow.routing.user

fun Application.configureRouting() {
    install(SSE)
    val jwtConfig = property<JwtConfig>("jwt")
    routing {
        auth()
        swagger()
        authenticate(jwtConfig.name) {
            user()
            servers()
            //contact()
        }
    }
}