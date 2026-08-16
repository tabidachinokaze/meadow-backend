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

@Suppress("UNCHECKED_CAST")
inline fun <reified T> RoutingContext.getParameter(name: String): T {
    val raw = call.parameters[name] ?: throw MissingRequestParameterException(name, "path")
    return when (T::class) {
        String::class -> raw as T
        Int::class -> (raw.toIntOrNull()
            ?: throw BadRequestException("参数 ${name} 必须是整数")) as T
        Long::class -> (raw.toLongOrNull()
            ?: throw BadRequestException("参数 ${name} 必须是整数")) as T
        Double::class -> (raw.toDoubleOrNull()
            ?: throw BadRequestException("参数 ${name} 必须是数字")) as T
        Float::class -> (raw.toFloatOrNull()
            ?: throw BadRequestException("参数 ${name} 必须是数字")) as T
        Boolean::class -> (raw.toBooleanStrictOrNull()
            ?: throw BadRequestException("参数 ${name} 必须是布尔值")) as T
        else -> raw as T
    }
}
