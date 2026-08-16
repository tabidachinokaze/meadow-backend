package moe.tabidachi.meadow.mapper

import moe.tabidachi.meadow.database.entity.ServerMemberEntity
import moe.tabidachi.meadow.database.model.ServerMember

object ServerMemberMapper {
    fun toServerMember(entity: ServerMemberEntity): ServerMember {
        return ServerMember(
            serverId = entity.serverId,
            userId = entity.userId,
            role = entity.role,
            joinedAt = entity.joinedAt,
            updatedAt = entity.updatedAt
        )
    }
}
