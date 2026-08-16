package moe.tabidachi.meadow.database.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** Mod（数据库模型） */
@Serializable
data class ServerMod(
    val id: Long,
    val serverId: Long,
    val modName: String,
    val modVersion: String,
    val modCategory: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
