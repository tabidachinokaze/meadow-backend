package moe.tabidachi.meadow.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** 整合包信息（API 响应） */
@Serializable
data class ModpackInfo(
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
    val createdAt: Instant?,
    val updatedAt: Instant?,
)

/**
 * 整合包模块状态码（码段 409xx）
 */
enum class ModpackStatusCode(
    override val code: Int,
    override val message: String,
) : StatusCode {
    MODPACK_NOT_FOUND(40901, "整合包不存在"),
    INVALID_FILE(40902, "文件无效或超过大小限制"),
    INVALID_VERSION(40903, "无效的版本号"),
}
