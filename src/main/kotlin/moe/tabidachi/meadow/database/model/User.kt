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
    val avatar: String?,
    val createTime: Instant,
    val updateTime: Instant,
)
