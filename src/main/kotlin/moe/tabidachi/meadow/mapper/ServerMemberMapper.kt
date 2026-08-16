package moe.tabidachi.meadow.mapper

import moe.tabidachi.meadow.database.entity.ServerMemberEntity
import moe.tabidachi.meadow.database.model.MemberPermissions
import moe.tabidachi.meadow.database.model.ServerMember
import moe.tabidachi.meadow.database.model.ServerRole

object ServerMemberMapper {
    fun toServerMember(entity: ServerMemberEntity): ServerMember {
        return ServerMember(
            serverId = entity.serverId,
            userId = entity.userId,
            role = entity.role,
            permissions = if (entity.role == ServerRole.OWNER) {
                // OWNER 恒为全权限（数据库记录即使被误改也以全权限为准）
                MemberPermissions.OWNER_ALL
            } else {
                MemberPermissions(
                    canEditServer = entity.canEditServer,
                    canManageRcon = entity.canManageRcon,
                    canManageMembers = entity.canManageMembers,
                    canManageScreenshots = entity.canManageScreenshots,
                    canManageChat = entity.canManageChat,
                    canManageWorlds = entity.canManageWorlds,
                    canManageModpack = entity.canManageModpack,
                    canDeleteServer = entity.canDeleteServer,
                )
            },
            joinedAt = entity.joinedAt,
            updatedAt = entity.updatedAt
        )
    }
}
