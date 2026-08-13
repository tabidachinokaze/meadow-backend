package moe.tabidachi.meadow.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import moe.tabidachi.meadow.model.ModLoader

@Serializable
data class ServerUpdateRequest(
    @SerialName("name")
    val name: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("host")
    val host: String? = null,
    @SerialName("port")
    val port: Int? = null,
    @SerialName("mod_loader")
    val modLoader: ModLoader? = null,
    @SerialName("version")
    val version: String? = null,
    @SerialName("banner_url")
    val bannerUrl: String? = null,
    @SerialName("tags")
    val tags: List<String>? = null,
    @SerialName("rcon_host")
    val rconHost: String? = null,
    @SerialName("rcon_port")
    val rconPort: Int? = null,
    @SerialName("rcon_password")
    val rconPassword: String? = null,
) {
    fun isEmpty(): Boolean = listOf(
        name,
        description,
        host,
        port,
        modLoader,
        version,
        bannerUrl,
        tags,
        rconHost,
        rconPort,
        rconPassword
    ).all { it == null }
}