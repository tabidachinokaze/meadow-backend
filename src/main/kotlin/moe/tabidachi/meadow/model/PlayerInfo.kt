package moe.tabidachi.meadow.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** 玩家信息（API 响应） */
@Serializable
data class PlayerInfo(
    val gameUuid: String,
    val playerName: String,
    val avatarUrl: String?,
    val isOnline: Boolean,
    val firstSeen: Instant?,
    val lastSeen: Instant?,
    val onlineDuration: Long,
)

/** 服务器玩家列表（9.2.1 响应） */
@Serializable
data class ServerPlayersResult(
    val onlineCount: Int,
    val online: List<PlayerInfo>,
    val recentOffline: List<PlayerInfo>,
)

/** 玩家跨服信息（9.2.2 响应） */
@Serializable
data class PlayerCrossServerInfo(
    val gameUuid: String,
    val playerName: String,
    val avatarUrl: String?,
    val servers: List<ServerPlayerEntry>,
) {
    @Serializable
    data class ServerPlayerEntry(
        val serverId: Long,
        val serverName: String?,
        val firstSeen: Instant?,
        val lastSeen: Instant?,
        val onlineDuration: Long,
        val isOnline: Boolean,
    )
}

fun playerAvatarUrl(name: String): String = "https://mc-heads.net/avatar/$name/64"
