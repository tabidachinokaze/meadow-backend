package moe.tabidachi.meadow.database.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** 存档（数据库模型） */
@Serializable
data class WorldSave(
    val id: Long,
    val serverId: Long,
    val worldName: String,
    val worldType: String,
    val fileSize: Long,
    val isCurrent: Boolean,
    val lastSaved: Instant,
    val downloadCount: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
)
