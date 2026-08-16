package moe.tabidachi.meadow.repository

import moe.tabidachi.meadow.database.entity.ModpackEntity
import moe.tabidachi.meadow.database.model.ServerModpack
import moe.tabidachi.meadow.database.table.ModpackTable
import moe.tabidachi.meadow.ktx.withTransaction
import moe.tabidachi.meadow.mapper.ModpackMapper
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.core.plus
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock

interface ModpackRepository {
    /** 当前激活的整合包 */
    suspend fun getActive(serverId: Long): ServerModpack?
    /** 全部版本（新→旧） */
    suspend fun getByServer(serverId: Long): List<ServerModpack>
    suspend fun getById(serverId: Long, modpackId: Long): ServerModpack?
    suspend fun add(
        serverId: Long,
        version: String,
        releaseDate: String,
        downloadUrl: String,
        fileSize: Long,
        md5Hash: String,
        changelog: String?,
    ): ServerModpack
    suspend fun deactivateOthers(serverId: Long, modpackId: Long): Boolean
    suspend fun incrementDownload(serverId: Long, modpackId: Long): Boolean
}

class ModpackRepositoryImpl(
    private val database: Database,
) : ModpackRepository {
    override suspend fun getActive(serverId: Long): ServerModpack? = database.withTransaction {
        ModpackEntity.find { ModpackTable.serverId.eq(serverId).and(ModpackTable.isActive.eq(true)) }
            .firstOrNull()?.let(ModpackMapper::toServerModpack)
    }

    override suspend fun getByServer(serverId: Long): List<ServerModpack> = database.withTransaction {
        ModpackEntity.find { ModpackTable.serverId.eq(serverId) }
            .map(ModpackMapper::toServerModpack)
    }

    override suspend fun getById(serverId: Long, modpackId: Long): ServerModpack? = database.withTransaction {
        ModpackEntity.find { ModpackTable.id.eq(modpackId).and(ModpackTable.serverId.eq(serverId)) }
            .singleOrNull()?.let(ModpackMapper::toServerModpack)
    }

    override suspend fun add(
        serverId: Long,
        version: String,
        releaseDate: String,
        downloadUrl: String,
        fileSize: Long,
        md5Hash: String,
        changelog: String?,
    ): ServerModpack = database.withTransaction {
        val now = Clock.System.now()
        val entity = ModpackEntity.new {
            this.serverId = serverId
            this.version = version
            this.releaseDate = releaseDate
            this.downloadUrl = downloadUrl
            this.fileSize = fileSize
            this.md5Hash = md5Hash
            this.changelog = changelog
            this.isActive = true
            this.createdAt = now
            this.updatedAt = now
        }
        ModpackMapper.toServerModpack(entity)
    }

    override suspend fun deactivateOthers(serverId: Long, modpackId: Long): Boolean = database.withTransaction {
        val updateCount = ModpackTable.update({
            ModpackTable.serverId.eq(serverId).and(ModpackTable.isActive.eq(true))
                .and(ModpackTable.id.neq(modpackId))
        }) {
            it[isActive] = false
            it[updatedAt] = Clock.System.now()
        }
        true
    }

    override suspend fun incrementDownload(serverId: Long, modpackId: Long): Boolean = database.withTransaction {
        val updateCount = ModpackTable.update({
            ModpackTable.id.eq(modpackId).and(ModpackTable.serverId.eq(serverId))
        }) {
            it[downloadCount] = downloadCount + 1
        }
        updateCount > 0
    }
}
