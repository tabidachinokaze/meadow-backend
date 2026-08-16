package moe.tabidachi.meadow.repository

import moe.tabidachi.meadow.database.entity.ModEntity
import moe.tabidachi.meadow.database.model.ServerMod
import moe.tabidachi.meadow.database.table.ModTable
import moe.tabidachi.meadow.ktx.withTransaction
import moe.tabidachi.meadow.mapper.ModMapper
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import kotlin.time.Clock

interface ModRepository {
    /** 按服务器查询 Mod（支持分类精确筛选） */
    suspend fun getByServer(serverId: Long, category: String?): List<ServerMod>
    /** Agent 全量上报：删除旧数据后整体替换 */
    suspend fun syncMods(serverId: Long, mods: List<Triple<String, String, String?>>): Int
}

class ModRepositoryImpl(
    private val database: Database,
) : ModRepository {
    override suspend fun getByServer(serverId: Long, category: String?): List<ServerMod> =
        database.withTransaction {
            val conditions = buildList {
                add(ModTable.serverId.eq(serverId))
                category?.let { add(ModTable.modCategory.eq(it)) }
            }
            ModEntity.find(conditions.reduce { acc, op -> acc and op })
                .map(ModMapper::toServerMod)
        }

    override suspend fun syncMods(serverId: Long, mods: List<Triple<String, String, String?>>): Int =
        database.withTransaction {
            ModTable.deleteWhere { ModTable.serverId.eq(serverId) }
            val now = Clock.System.now()
            for ((name, version, category) in mods) {
                ModEntity.new {
                    this.serverId = serverId
                    this.modName = name
                    this.modVersion = version
                    this.modCategory = category
                    this.createdAt = now
                    this.updatedAt = now
                }
            }
            mods.size
        }
}
