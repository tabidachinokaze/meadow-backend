package moe.tabidachi.meadow.database.model

import kotlinx.serialization.Serializable
import moe.tabidachi.meadow.model.RelationStatus
import moe.tabidachi.meadow.model.RelationType
import kotlin.time.Instant

@Serializable
data class UserRelation(
    val userId: Long,
    val targetUserId: Long,
    val type: RelationType,
    val status: RelationStatus,
    val createdAt: Instant,
    val updatedAt: Instant
)
