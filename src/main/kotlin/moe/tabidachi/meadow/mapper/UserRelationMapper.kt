package moe.tabidachi.meadow.mapper

import moe.tabidachi.meadow.database.entity.UserRelationEntity
import moe.tabidachi.meadow.database.model.UserRelation

object UserRelationMapper {
    fun toUserRelation(entity: UserRelationEntity): UserRelation {
        return UserRelation(
            userId = entity.userId,
            targetUserId = entity.targetUserId,
            type = entity.type,
            status = entity.status,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}