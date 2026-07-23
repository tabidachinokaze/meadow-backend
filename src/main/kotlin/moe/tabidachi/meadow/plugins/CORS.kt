package moe.tabidachi.meadow.plugins

import io.ktor.http.HttpHeaders
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCORS() {
    install(CORS) {
        allowNonSimpleContentTypes = true
        allowCredentials = true
        allowSameOrigin = true
        anyMethod()
        allowXHttpMethodOverride()

        // Allow headers
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.Upgrade)
        allowHeader(HttpHeaders.Connection)
        allowHeader("Sec-WebSocket-Key")
        allowHeader("Sec-WebSocket-Version")
        allowHeader("Sec-WebSocket-Extensions")
        allowHeader("Sec-WebSocket-Protocol")
        allowHeader("Sec-WebSocket-Accept") // Important for WebSocket handshake
        allowHeaders { true }
        anyHost()
    }
}
