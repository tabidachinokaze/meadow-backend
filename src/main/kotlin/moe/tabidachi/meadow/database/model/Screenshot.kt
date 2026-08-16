package moe.tabidachi.meadow.database.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** 截图（数据库模型） */
@Serializable
data class Screenshot(
    val id: Long,
    val serverId: Long,
    val uploaderId: Long,
    val uploaderName: String,
    val imageUrl: String,
    val description: String?,
    val coordinates: String?,
    val status: String,
    val reportCount: Int,
    val downloadCount: Int,
    val createdAt: Instant,
)
