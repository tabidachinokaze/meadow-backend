package moe.tabidachi.meadow.ktx

import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.routing.*
import moe.tabidachi.meadow.jwt.Claims

val RoutingContext.callingUserId: Long
    get() = call.principal<JWTPrincipal>()?.getClaim(Claims.UID, Long::class) ?: error("require user id")

val RoutingContext.callingUserIdOrNull: Long?
    get() = call.principal<JWTPrincipal>()?.getClaim(Claims.UID, Long::class)

inline fun <reified T> RoutingContext.getParameter(name: String): T {
    return when (T::class) {
        String::class -> call.parameters[name] as T
        Int::class -> call.parameters[name]?.toIntOrNull() as T
        Long::class -> call.parameters[name]?.toLongOrNull() as T
        Double::class -> call.parameters[name]?.toDoubleOrNull() as T
        Float::class -> call.parameters[name]?.toFloatOrNull() as T
        Boolean::class -> call.parameters[name]?.toBoolean() as T
        else -> call.parameters[name]?.let { return it as T }
    } ?: throw MissingRequestParameterException(name, "path")
}
