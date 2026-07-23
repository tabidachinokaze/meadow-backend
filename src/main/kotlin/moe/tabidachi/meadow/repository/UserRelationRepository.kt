package moe.tabidachi.meadow.repository

import moe.tabidachi.meadow.database.entity.UserRelationEntity
import moe.tabidachi.meadow.database.model.UserRelation
import moe.tabidachi.meadow.database.table.UserRelationTable
import moe.tabidachi.meadow.ktx.withTransaction
import moe.tabidachi.meadow.mapper.UserRelationMapper
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database

interface UserRelationRepository {
    suspend fun getByUserId(userId: Long): List<UserRelation>
}

class UserRelationRepositoryImpl(
    private val database: Database
) : UserRelationRepository {
    override suspend fun getByUserId(userId: Long): List<UserRelation> = database.withTransaction {
        UserRelationEntity.find { UserRelationTable.userId.eq(userId) }
            .map(UserRelationMapper::toUserRelation)
    }
}
