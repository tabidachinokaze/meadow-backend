package moe.tabidachi.meadow.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.server.plugins.di.*
import moe.tabidachi.meadow.contract.AuthenticationNames
import moe.tabidachi.meadow.jwt.Claims
import moe.tabidachi.meadow.model.config.JwtConfig
import moe.tabidachi.meadow.repository.UserRepository
import moe.tabidachi.meadow.security.Jwt

fun Application.configureSecurity() {
    val jwt: Jwt by dependencies
    val userRepository: UserRepository by dependencies
    val jwtConfig = property<JwtConfig>("jwt")
    authentication {
        jwt(jwtConfig.name) {
            realm = jwtConfig.realm
            verifier(jwt.verifier)
            validate { credential ->
                // 用户必须存在、激活，且 token_version / created_at 与数据库一致
                // （删库重建后 uid 可能复用但 createdAt 不同、注销后 isActive=false、改密后版本递增 → 一律拒绝）
                val uid = credential.payload.getClaim(Claims.UID).asLong()
                val tokenVersion = credential.payload.getClaim(Claims.TOKEN_VERSION).asInt() ?: 0
                val createdAt = credential.payload.getClaim(Claims.CREATED_AT).asLong() ?: -1
                if (credential.payload.audience.contains(jwtConfig.audience) &&
                    isUserTokenValid(userRepository, uid, tokenVersion, createdAt)
                ) {
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
                userRepository = userRepository,
            )
        )
    }
}

/**
 * 校验用户 token：用户存在 + 激活 + token_version 匹配 + created_at 匹配。
 * 删库重建后用户不存在或 uid 复用但 createdAt 不同、注销后 isActive=false、
 * 改密后版本号递增 → 一律拒绝。
 */
suspend fun isUserTokenValid(
    userRepository: UserRepository,
    uid: Long,
    tokenVersion: Int,
    createdAtEpoch: Long,
): Boolean {
    val user = userRepository.getByUid(uid) ?: return false
    return user.isActive &&
        user.tokenVersion == tokenVersion &&
        user.createdAt.epochSeconds == createdAtEpoch
}

/**
 * 可选认证 provider：
 * - 未携带 token → 视为匿名（principal = null，`callingUserIdOrNull` 返回 null）
 * - 携带 token → 必须有效（用户存在/激活/版本/创建时间匹配），否则拒绝（无效 token 不再被当作匿名放行，已知问题 #14）
 *
 * 用于公开路由（如 GET /servers、GET /users/{uid}）的双 provider 认证。
 */
class OptionalJwtProvider(
    name: String,
    private val verifier: com.auth0.jwt.interfaces.JWTVerifier,
    private val audience: String,
    private val userRepository: UserRepository,
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
        // 有 token：必须有效（签名 + 用户存在/激活/版本/创建时间匹配）
        val principal = try {
            val decoded = verifier.verify(token)
            val uid = decoded.getClaim(Claims.UID).asLong()
            val tokenVersion = decoded.getClaim(Claims.TOKEN_VERSION).asInt() ?: 0
            val createdAt = decoded.getClaim(Claims.CREATED_AT).asLong() ?: -1
            if (decoded.audience.contains(audience) &&
                isUserTokenValid(userRepository, uid, tokenVersion, createdAt)
            ) {
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
