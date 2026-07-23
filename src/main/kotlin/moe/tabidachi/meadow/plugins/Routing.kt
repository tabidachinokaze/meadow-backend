package moe.tabidachi.meadow.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.config.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import moe.tabidachi.meadow.model.config.JwtConfig
import moe.tabidachi.meadow.routing.authenticate
import moe.tabidachi.meadow.routing.contact
import moe.tabidachi.meadow.routing.user

fun Application.configureRouting() {
    install(SSE)
    routing {
        sse("/sse") {

        }
    }
    val jwtConfig = property<JwtConfig>("jwt")
    routing {
        authenticate()
        authenticate(jwtConfig.name) {
            user()
            contact()
        }
    }
}