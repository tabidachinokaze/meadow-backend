package moe.tabidachi.meadow.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTCreator
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import moe.tabidachi.meadow.jwt.Claims

interface Jwt {
    val verifier: JWTVerifier
    fun sign(uid: Long, tokenVersion: Int = 0, block: JWTCreator.Builder.() -> Unit = {}): String
}

class JwtImpl(
    secret: String,
    private val issuer: String? = null,
    private val audience: Array<out String?> = arrayOf()
) : Jwt {
    private val algorithm = Algorithm.HMAC256(secret)

    override val verifier: JWTVerifier = JWT.require(algorithm)
        .apply { issuer?.let(::withIssuer) }
        .withAudience(*audience)
        .build()

    override fun sign(
        uid: Long,
        tokenVersion: Int,
        block: JWTCreator.Builder.() -> Unit
    ): String = JWT.create()
        .withClaim(Claims.UID, uid)
        .withClaim(Claims.TOKEN_VERSION, tokenVersion)
        .apply {
            block()
            issuer?.let(::withIssuer)
            withAudience(*audience)
        }
        .sign(algorithm)
}
