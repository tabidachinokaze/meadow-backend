package moe.tabidachi.meadow.plugins

import io.ktor.http.HttpHeaders
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCORS() {
    // 允许的来源白名单（已知问题 #4）：开发环境允许任意来源；生产限定配置中的域名
    val allowedHosts = environment.config
        .propertyOrNull("cors.allowed_hosts")
        ?.getList()
        ?.filter { it.isNotBlank() }
        ?.toSet()
        .orEmpty()

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

        if (allowedHosts.isEmpty()) {
            // 未配置白名单（开发环境）：允许任意来源
            anyHost()
        } else {
            // Ktor 3.x：hosts 为 MutableSet<String>，逐条添加白名单域名
            hosts.addAll(allowedHosts)
        }
    }
}
