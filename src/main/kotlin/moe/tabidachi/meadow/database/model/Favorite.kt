package moe.tabidachi.meadow.database.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** 收藏（数据库模型） */
@Serializable
data class Favorite(
    val userId: Long,
    val serverId: Long,
    val createdAt: Instant,
)
