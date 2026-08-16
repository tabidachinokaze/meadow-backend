package moe.tabidachi.meadow.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Agent 定时状态上报（规划 §9.12） */
@Serializable
data class ServerStatusRequest(
    @SerialName("server_key")
    val serverKey: String,
    @SerialName("machine_id")
    val machineId: String,
    @SerialName("online_players")
    val onlinePlayers: Int,
    @SerialName("max_players")
    val maxPlayers: Int,
    @SerialName("uptime_seconds")
    val uptimeSeconds: Long,
    @SerialName("tps")
    val tps: Double? = null,
    @SerialName("players")
    val players: List<PlayerStatus> = emptyList(),
    @SerialName("mods")
    val mods: List<ModStatus> = emptyList(),
) {
    @Serializable
    data class PlayerStatus(
        @SerialName("uuid")
        val uuid: String,
        @SerialName("name")
        val name: String,
        @SerialName("x")
        val x: Double? = null,
        @SerialName("y")
        val y: Double? = null,
        @SerialName("z")
        val z: Double? = null,
        @SerialName("world")
        val world: String? = null,
    )

    @Serializable
    data class ModStatus(
        @SerialName("name")
        val name: String,
        @SerialName("version")
        val version: String,
        @SerialName("category")
        val category: String? = null,
    )
}
