package moe.tabidachi.meadow.model.config

import kotlinx.serialization.Serializable

@Serializable
data class Argon2Config(
    val iterations: Int,
    val memory: Int,
    val parallelism: Int,
)
