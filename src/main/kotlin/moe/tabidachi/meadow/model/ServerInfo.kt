package moe.tabidachi.meadow.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ServerInfo(
    @SerialName("id")
    val id: Long,
    @SerialName("name")
    val name: String?,
    @SerialName("description")
    val description: String?,
    @SerialName("host")
    val host: String,
    @SerialName("port")
    val port: Int,
    @SerialName("mod_loader")
    val modLoader: ModLoader?,
    @SerialName("version")
    val version: String?,
    @SerialName("banner_url")
    val bannerUrl: String?,
    @SerialName("tags")
    val tags: List<String>?,
    @SerialName("rcon_host")
    val rconHost: String?,
    @SerialName("rcon_port")
    val rconPort: Int?,
    @SerialName("rcon_password")
    val rconPassword: String?,
    @SerialName("is_verified")
    val isVerified: Boolean,
    @SerialName("server_key")
    val serverKey: String?,
    @SerialName("machine_id")
    val machineId: String?,
    @SerialName("created_at")
    val createdAt: Instant?,
    @SerialName("updated_at")
    val updatedAt: Instant?,
    @SerialName("owner_id")
    val ownerId: Long,
) {
    fun desensitize(): ServerInfo = copy(
        rconHost = null,
        rconPort = null,
        rconPassword = null,
        serverKey = null,
        machineId = null,
    )
}