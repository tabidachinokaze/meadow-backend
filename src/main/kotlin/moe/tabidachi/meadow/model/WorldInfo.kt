package moe.tabidachi.meadow.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** 存档信息（API 响应） */
@Serializable
data class WorldInfo(
    val id: Long,
    val serverId: Long,
    val worldName: String,
    val worldType: String,
    val fileSize: Long,
    val isCurrent: Boolean,
    val lastSaved: Instant?,
    val downloadCount: Int,
    val downloadUrl: String?,
    val createdAt: Instant?,
    val updatedAt: Instant?,
)

/**
 * 存档模块状态码（码段 408xx）
 */
enum class WorldStatusCode(
    override val code: Int,
    override val message: String,
) : StatusCode {
    WORLD_NOT_FOUND(40801, "存档不存在"),
    INVALID_FILE(40802, "文件无效或超过大小限制"),
}
