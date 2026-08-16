package moe.tabidachi.meadow.repository

import moe.tabidachi.meadow.database.entity.ServerMemberEntity
import moe.tabidachi.meadow.database.model.MemberPermissions
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
    suspend fun add(serverId: Long, userId: Long, role: ServerRole, permissions: MemberPermissions): ServerMember
    suspend fun updateRole(serverId: Long, userId: Long, role: ServerRole): Boolean
    suspend fun updatePermissions(serverId: Long, userId: Long, permissions: MemberPermissions): Boolean
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

    override suspend fun add(serverId: Long, userId: Long, role: ServerRole, permissions: MemberPermissions): ServerMember =
        database.withTransaction {
            val now = Clock.System.now()
            // OWNER 恒为全权限
            val effective = if (role == ServerRole.OWNER) MemberPermissions.OWNER_ALL else permissions
            val entity = ServerMemberEntity.new {
                this.serverId = serverId
                this.userId = userId
                this.role = role
                this.canEditServer = effective.canEditServer
                this.canManageRcon = effective.canManageRcon
                this.canManageMembers = effective.canManageMembers
                this.canManageScreenshots = effective.canManageScreenshots
                this.canManageChat = effective.canManageChat
                this.canManageWorlds = effective.canManageWorlds
                this.canManageModpack = effective.canManageModpack
                this.canDeleteServer = effective.canDeleteServer
                this.joinedAt = now
                this.updatedAt = now
            }
            ServerMemberMapper.toServerMember(entity)
        }

    override suspend fun updateRole(serverId: Long, userId: Long, role: ServerRole): Boolean =
        database.withTransaction {
            val effective = if (role == ServerRole.OWNER) MemberPermissions.OWNER_ALL else null
            val updateCount = ServerMemberTable.update({
                ServerMemberTable.serverId.eq(serverId).and(ServerMemberTable.userId.eq(userId))
            }) {
                it[ServerMemberTable.role] = role
                if (effective != null) {
                    it[ServerMemberTable.canEditServer] = effective.canEditServer
                    it[ServerMemberTable.canManageRcon] = effective.canManageRcon
                    it[ServerMemberTable.canManageMembers] = effective.canManageMembers
                    it[ServerMemberTable.canManageScreenshots] = effective.canManageScreenshots
                    it[ServerMemberTable.canManageChat] = effective.canManageChat
                    it[ServerMemberTable.canManageWorlds] = effective.canManageWorlds
                    it[ServerMemberTable.canManageModpack] = effective.canManageModpack
                    it[ServerMemberTable.canDeleteServer] = effective.canDeleteServer
                }
                it[ServerMemberTable.updatedAt] = Clock.System.now()
            }
            updateCount > 0
        }

    override suspend fun updatePermissions(serverId: Long, userId: Long, permissions: MemberPermissions): Boolean =
        database.withTransaction {
            val updateCount = ServerMemberTable.update({
                ServerMemberTable.serverId.eq(serverId).and(ServerMemberTable.userId.eq(userId))
            }) {
                it[ServerMemberTable.canEditServer] = permissions.canEditServer
                it[ServerMemberTable.canManageRcon] = permissions.canManageRcon
                it[ServerMemberTable.canManageMembers] = permissions.canManageMembers
                it[ServerMemberTable.canManageScreenshots] = permissions.canManageScreenshots
                it[ServerMemberTable.canManageChat] = permissions.canManageChat
                it[ServerMemberTable.canManageWorlds] = permissions.canManageWorlds
                it[ServerMemberTable.canManageModpack] = permissions.canManageModpack
                it[ServerMemberTable.canDeleteServer] = permissions.canDeleteServer
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
