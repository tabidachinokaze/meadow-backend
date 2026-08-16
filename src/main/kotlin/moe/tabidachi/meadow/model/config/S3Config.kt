package moe.tabidachi.meadow.model.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class S3Config(
    /** 后端连接 S3 的地址（容器→宿主机内网，如 host.docker.internal） */
    val host: String,
    val port: Int,
    /** 传输协议：https（默认，443 反代场景）或 http（自建 S3 直连，如 rustfs:9000） */
    val scheme: String = "https",
    /** 公开访问地址（浏览器直链用；未配置时回退到 host/port/scheme） */
    @SerialName("public_host")
    val publicHost: String? = null,
    @SerialName("public_port")
    val publicPort: Int? = null,
    @SerialName("public_scheme")
    val publicScheme: String? = null,
    @SerialName("access_key")
    val accessKey: String,
    @SerialName("secret_key")
    val secretKey: String,
) {
    /** 生成对象直链的公开基础地址（bucket/key 拼接在其后） */
    fun publicBase(): String {
        val scheme = publicScheme?.takeIf { it.isNotBlank() } ?: this.scheme
        val host = publicHost?.takeIf { it.isNotBlank() } ?: this.host
        val port = (publicPort ?: 0).takeIf { it > 0 } ?: this.port
        val portSuffix = if ((scheme == "https" && port == 443) || (scheme == "http" && port == 80)) "" else ":$port"
        return "$scheme://$host$portSuffix"
    }
}
