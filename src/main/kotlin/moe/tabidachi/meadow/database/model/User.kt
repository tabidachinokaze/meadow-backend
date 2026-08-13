package moe.tabidachi.meadow.database.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class User(
    val uid: Long,
    val username: String,
    val password: String,
    val email: String?,
    val phone: String?,
    val avatarUrl: String?,
    val gameId: String?,
    val role: SystemRole,
    val isActive: Boolean,
    val lastLogin: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val bio: String?,
    val website: String?,
    val location: String?,
)
