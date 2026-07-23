package moe.tabidachi.meadow.model.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class S3Config(
    val host: String,
    val port: Int,
    @SerialName("access_key")
    val accessKey: String,
    @SerialName("secret_key")
    val secretKey: String,
)
