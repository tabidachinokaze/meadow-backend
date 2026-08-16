package moe.tabidachi.meadow.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** 举报信息（API 响应） */
@Serializable
data class ReportInfo(
    val id: Long,
    val screenshotId: Long,
    val screenshotUrl: String?,
    val serverId: Long?,
    val serverName: String?,
    val reporterId: Long,
    val reporterName: String?,
    val reason: String,
    val status: String,
    val handlerId: Long?,
    val handlerNote: String?,
    val createdAt: Instant?,
    val handledAt: Instant?,
)

/** 禁言信息（API 响应） */
@Serializable
data class BanInfo(
    val id: Long,
    val serverId: Long,
    val playerUuid: String,
    val playerName: String,
    val reason: String,
    val durationHours: Int,
    val expiresAt: Instant?,
    val isActive: Boolean,
    val createdAt: Instant?,
)

/**
 * 管理员模块状态码（码段 410xx）
 */
enum class AdminStatusCode(
    override val code: Int,
    override val message: String,
) : StatusCode {
    REPORT_NOT_FOUND(41001, "举报不存在"),
    BAN_NOT_FOUND(41002, "禁言记录不存在"),
    INVALID_ACTION(41003, "无效的审核操作"),
    INVALID_DURATION(41004, "无效的禁言时长"),
    ALREADY_HANDLED(41005, "举报已处理"),
}
