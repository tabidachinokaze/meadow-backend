package moe.tabidachi.meadow.ktx

import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import moe.tabidachi.meadow.jwt.Claims

fun RoutingContext.requireUserId(): Long {
    return call.principal<JWTPrincipal>()?.getClaim(Claims.UID, Long::class) ?: error("require user id")
}