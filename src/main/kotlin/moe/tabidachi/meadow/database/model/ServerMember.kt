package moe.tabidachi.meadow.database.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** 服务器成员（数据库模型） */
@Serializable
data class ServerMember(
    val serverId: Long,
    val userId: Long,
    val role: ServerRole,
    /** 细粒度权限位（OWNER 记录恒为全权限） */
    val permissions: MemberPermissions,
    val joinedAt: Instant,
    val updatedAt: Instant,
)
