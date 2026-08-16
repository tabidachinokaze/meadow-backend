package moe.tabidachi.meadow.model.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class S3Config(
    val host: String,
    val port: Int,
    /** 传输协议：https（默认，443 反代场景）或 http（自建 S3 直连，如 rustfs:9000） */
    val scheme: String = "https",
    @SerialName("access_key")
    val accessKey: String,
    @SerialName("secret_key")
    val secretKey: String,
)
