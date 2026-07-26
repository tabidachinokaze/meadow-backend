package moe.tabidachi.meadow.model.config

import kotlinx.serialization.Serializable

@Serializable
data class RedisConfig(
    val host: String,
    val port: Int,
    val username: String?,
    val password: String?
)