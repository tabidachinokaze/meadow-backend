package moe.tabidachi.meadow.database.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** 举报记录（数据库模型） */
@Serializable
data class Report(
    val id: Long,
    val screenshotId: Long,
    val reporterId: Long,
    val reason: String,
    val status: String,
    val handlerId: Long?,
    val handlerNote: String?,
    val createdAt: Instant,
    val handledAt: Instant?,
)

/** 禁言记录（数据库模型） */
@Serializable
data class Ban(
    val id: Long,
    val serverId: Long,
    val playerUuid: String,
    val playerName: String,
    val bannedBy: Long,
    val reason: String,
    val durationHours: Int,
    val expiresAt: Instant?,
    val isActive: Boolean,
    val createdAt: Instant,
)
