package moe.tabidachi.meadow.database.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** 服务器玩家记录（数据库模型） */
@Serializable
data class ServerPlayer(
    val serverId: Long,
    val gameUuid: String,
    val playerName: String,
    val firstSeen: Instant,
    val lastSeen: Instant,
    val onlineDuration: Long,
    val isOnline: Boolean,
)
