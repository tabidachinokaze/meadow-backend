package moe.tabidachi.meadow.repository

import moe.tabidachi.meadow.database.entity.WorldEntity
import moe.tabidachi.meadow.database.model.WorldSave
import moe.tabidachi.meadow.database.table.WorldTable
import moe.tabidachi.meadow.ktx.withTransaction
import moe.tabidachi.meadow.mapper.WorldMapper
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.plus
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock

interface WorldRepository {
    suspend fun getByServer(serverId: Long): List<WorldSave>
    suspend fun getById(serverId: Long, worldId: Long): WorldSave?
    suspend fun add(
        serverId: Long,
        worldName: String,
        worldType: String,
        fileSize: Long,
        isCurrent: Boolean,
    ): WorldSave
    suspend fun incrementDownload(serverId: Long, worldId: Long): Boolean
    suspend fun setCurrent(serverId: Long, worldId: Long): Boolean
    suspend fun clearCurrent(serverId: Long): Boolean
    suspend fun delete(serverId: Long, worldId: Long): Boolean
}

class WorldRepositoryImpl(
    private val database: Database,
) : WorldRepository {
    override suspend fun getByServer(serverId: Long): List<WorldSave> = database.withTransaction {
        WorldEntity.find { WorldTable.serverId.eq(serverId) }
            .map(WorldMapper::toWorldSave)
    }

    override suspend fun getById(serverId: Long, worldId: Long): WorldSave? = database.withTransaction {
        WorldEntity.find { WorldTable.id.eq(worldId).and(WorldTable.serverId.eq(serverId)) }
            .singleOrNull()?.let(WorldMapper::toWorldSave)
    }

    override suspend fun add(
        serverId: Long,
        worldName: String,
        worldType: String,
        fileSize: Long,
        isCurrent: Boolean,
    ): WorldSave = database.withTransaction {
        val now = Clock.System.now()
        val entity = WorldEntity.new {
            this.serverId = serverId
            this.worldName = worldName
            this.worldType = worldType
            this.fileSize = fileSize
            this.isCurrent = isCurrent
            this.lastSaved = now
            this.createdAt = now
            this.updatedAt = now
        }
        WorldMapper.toWorldSave(entity)
    }

    override suspend fun incrementDownload(serverId: Long, worldId: Long): Boolean = database.withTransaction {
        val updateCount = WorldTable.update({
            WorldTable.id.eq(worldId).and(WorldTable.serverId.eq(serverId))
        }) {
            it[downloadCount] = downloadCount + 1
        }
        updateCount > 0
    }

    override suspend fun setCurrent(serverId: Long, worldId: Long): Boolean = database.withTransaction {
        // 先清除该服务器所有 current，再设置目标
        WorldTable.update({ WorldTable.serverId.eq(serverId).and(WorldTable.isCurrent.eq(true)) }) {
            it[isCurrent] = false
            it[updatedAt] = Clock.System.now()
        }
        val updateCount = WorldTable.update({
            WorldTable.id.eq(worldId).and(WorldTable.serverId.eq(serverId))
        }) {
            it[isCurrent] = true
            it[updatedAt] = Clock.System.now()
        }
        updateCount > 0
    }

    override suspend fun clearCurrent(serverId: Long): Boolean = database.withTransaction {
        val updateCount = WorldTable.update({
            WorldTable.serverId.eq(serverId).and(WorldTable.isCurrent.eq(true))
        }) {
            it[isCurrent] = false
            it[updatedAt] = Clock.System.now()
        }
        updateCount > 0
    }

    override suspend fun delete(serverId: Long, worldId: Long): Boolean = database.withTransaction {
        val deleteCount = WorldTable.deleteWhere {
            WorldTable.id.eq(worldId).and(WorldTable.serverId.eq(serverId))
        }
        deleteCount > 0
    }
}
