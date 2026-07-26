package moe.tabidachi.meadow.routing

import io.ktor.openapi.OpenApiInfo
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.Route

fun Route.swagger() {
    swaggerUI("swagger") {
        info = OpenApiInfo(
            title = "Meadow api",
            version = "1.0.0"
        )
    }
}