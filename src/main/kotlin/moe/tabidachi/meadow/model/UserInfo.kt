package moe.tabidachi.meadow.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class UserInfo(
    val uid: Long,
    val username: String,
    val email: String? = null,
    val phone: String? = null,
    val avatar: String? = null,
    val createTime: Instant? = null,
    val updateTime: Instant? = null,
)
