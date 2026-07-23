package moe.tabidachi.meadow.model.config

import kotlinx.serialization.Serializable

@Serializable
data class DatabaseConfig(
    val url: String,
    val user: String,
    val driver: String,
    val password: String
)