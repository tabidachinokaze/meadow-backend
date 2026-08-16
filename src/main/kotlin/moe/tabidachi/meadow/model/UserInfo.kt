package moe.tabidachi.meadow.model

import kotlinx.serialization.Serializable
import moe.tabidachi.meadow.database.model.SystemRole
import kotlin.time.Instant

@Serializable
data class UserInfo(
    val uid: Long,
    val username: String,
    val email: String?,
    val phone: String?,
    val avatarUrl: String?,
    val bannerUrl: String?,
    val gameId: String?,
    val role: SystemRole,
    val isActive: Boolean,
    val lastLogin: Instant?,
    val createdAt: Instant?,
    val updatedAt: Instant?,
    val bio: String?,
    val website: String?,
    val location: String?,
) {
    fun desensitize(): UserInfo = copy(
        email = null,
        phone = null,
        createdAt = null,
        updatedAt = null,
    )
}
