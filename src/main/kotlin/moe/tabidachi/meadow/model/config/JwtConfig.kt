package moe.tabidachi.meadow.model.config

import kotlinx.serialization.Serializable

@Serializable
data class JwtConfig(
    val name: String? = null,
    val domain: String,
    val audience: String,
    val realm: String,
    val secret: String,
    val issuer: String,
)