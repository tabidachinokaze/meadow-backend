package moe.tabidachi.meadow.database.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** 整合包（数据库模型） */
@Serializable
data class ServerModpack(
    val id: Long,
    val serverId: Long,
    val version: String,
    val releaseDate: String,
    val downloadUrl: String,
    val fileSize: Long,
    val md5Hash: String,
    val changelog: String?,
    val downloadCount: Int,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)
