package moe.tabidachi.meadow.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** 截图信息（API 响应，camelCase 与 UserInfo 风格一致） */
@Serializable
data class ScreenshotInfo(
    val id: Long,
    val serverId: Long,
    val serverName: String?,
    val uploaderId: Long,
    val uploaderName: String,
    val imageUrl: String,
    val description: String?,
    val coordinates: String?,
    val status: String,
    val reportCount: Int,
    val downloadCount: Int,
    val createdAt: Instant?,
)
