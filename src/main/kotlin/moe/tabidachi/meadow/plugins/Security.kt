package moe.tabidachi.meadow.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.server.plugins.di.*
import moe.tabidachi.meadow.contract.AuthenticationNames
import moe.tabidachi.meadow.model.config.JwtConfig
import moe.tabidachi.meadow.security.Jwt

fun Application.configureSecurity() {
    val jwt: Jwt by dependencies
    val jwtConfig = property<JwtConfig>("jwt")
    authentication {
        jwt(jwtConfig.name) {
            realm = jwtConfig.realm
            verifier(jwt.verifier)
            validate { credential ->
                if (credential.payload.audience.contains(jwtConfig.audience)) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
        register(
            OptionalJwtProvider(
                name = AuthenticationNames.NONE,
                verifier = jwt.verifier,
                audience = jwtConfig.audience,
            )
        )
    }
}

/**
 * 可选认证 provider：
 * - 未携带 token → 视为匿名（principal = null，`callingUserIdOrNull` 返回 null）
 * - 携带 token → 必须有效，否则拒绝（无效 token 不再被当作匿名放行，已知问题 #14）
 *
 * 用于公开路由（如 GET /servers、GET /users/{uid}）的双 provider 认证。
 */
class OptionalJwtProvider(
    name: String,
    private val verifier: com.auth0.jwt.interfaces.JWTVerifier,
    private val audience: String,
) : AuthenticationProvider(DynamicProviderConfig(name)) {
    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val header = context.call.request.headers["Authorization"] ?: ""
        if (!header.startsWith("Bearer ")) {
            // 无 token：匿名放行
            context.principal("none")
            return
        }
        val token = header.removePrefix("Bearer ").trim()
        if (token.isEmpty()) {
            context.principal("none")
            return
        }
        // 有 token：必须有效
        val principal = try {
            val decoded = verifier.verify(token)
            if (decoded.audience.contains(audience)) {
                JWTPrincipal(decoded)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
        if (principal != null) {
            context.principal(principal)
        } else {
            context.challenge("InvalidBearerToken", AuthenticationFailedCause.Error("Invalid token")) { _, _ ->
                // 拒绝：不设置 principal，调用方（authenticate 块）将按未认证处理 → 401
            }
        }
    }
}
