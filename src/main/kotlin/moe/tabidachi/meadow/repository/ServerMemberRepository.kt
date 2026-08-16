package moe.tabidachi.meadow.repository

import moe.tabidachi.meadow.database.entity.ServerMemberEntity
import moe.tabidachi.meadow.database.model.ServerMember
import moe.tabidachi.meadow.database.model.ServerRole
import moe.tabidachi.meadow.database.table.ServerMemberTable
import moe.tabidachi.meadow.ktx.withTransaction
import moe.tabidachi.meadow.mapper.ServerMemberMapper
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock

interface ServerMemberRepository {
    suspend fun getByServer(serverId: Long): List<ServerMember>
    suspend fun getByServerAndUser(serverId: Long, userId: Long): ServerMember?
    suspend fun getByUser(userId: Long): List<ServerMember>
    suspend fun add(serverId: Long, userId: Long, role: ServerRole): ServerMember
    suspend fun updateRole(serverId: Long, userId: Long, role: ServerRole): Boolean
    suspend fun remove(serverId: Long, userId: Long): Boolean
}

class ServerMemberRepositoryImpl(
    private val database: Database,
) : ServerMemberRepository {
    override suspend fun getByServer(serverId: Long): List<ServerMember> = database.withTransaction {
        ServerMemberEntity.find { ServerMemberTable.serverId.eq(serverId) }
            .map(ServerMemberMapper::toServerMember)
    }

    override suspend fun getByServerAndUser(serverId: Long, userId: Long): ServerMember? = database.withTransaction {
        ServerMemberEntity.find {
            ServerMemberTable.serverId.eq(serverId).and(ServerMemberTable.userId.eq(userId))
        }.singleOrNull()?.let(ServerMemberMapper::toServerMember)
    }

    override suspend fun getByUser(userId: Long): List<ServerMember> = database.withTransaction {
        ServerMemberEntity.find { ServerMemberTable.userId.eq(userId) }
            .map(ServerMemberMapper::toServerMember)
    }

    override suspend fun add(serverId: Long, userId: Long, role: ServerRole): ServerMember =
        database.withTransaction {
            val now = Clock.System.now()
            val entity = ServerMemberEntity.new {
                this.serverId = serverId
                this.userId = userId
                this.role = role
                this.joinedAt = now
                this.updatedAt = now
            }
            ServerMemberMapper.toServerMember(entity)
        }

    override suspend fun updateRole(serverId: Long, userId: Long, role: ServerRole): Boolean =
        database.withTransaction {
            val updateCount = ServerMemberTable.update({
                ServerMemberTable.serverId.eq(serverId).and(ServerMemberTable.userId.eq(userId))
            }) {
                it[ServerMemberTable.role] = role
                it[ServerMemberTable.updatedAt] = Clock.System.now()
            }
            updateCount > 0
        }

    override suspend fun remove(serverId: Long, userId: Long): Boolean = database.withTransaction {
        val deleteCount = ServerMemberTable.deleteWhere {
            ServerMemberTable.serverId.eq(serverId).and(ServerMemberTable.userId.eq(userId))
        }
        deleteCount > 0
    }
}
