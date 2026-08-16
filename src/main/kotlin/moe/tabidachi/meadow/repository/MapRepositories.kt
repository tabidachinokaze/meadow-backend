package moe.tabidachi.meadow.repository

import moe.tabidachi.meadow.database.entity.MapConfigEntity
import moe.tabidachi.meadow.database.entity.PlayerPositionEntity
import moe.tabidachi.meadow.database.model.MapConfig
import moe.tabidachi.meadow.database.model.PlayerPosition
import moe.tabidachi.meadow.database.table.MapConfigTable
import moe.tabidachi.meadow.database.table.PlayerPositionTable
import moe.tabidachi.meadow.ktx.withTransaction
import moe.tabidachi.meadow.mapper.MapConfigMapper
import moe.tabidachi.meadow.mapper.PlayerPositionMapper
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock
import kotlin.time.Instant

interface MapConfigRepository {
    suspend fun getByServer(serverId: Long): MapConfig?
    /** upsert：每服务器至多一条配置 */
    suspend fun upsert(
        serverId: Long,
        type: String,
        tileUrl: String?,
        worldName: String?,
        centerX: Int,
        centerZ: Int,
        zoomMin: Int,
        zoomMax: Int,
        zoomDefault: Int,
        playerMarkersUrl: String?,
    ): MapConfig
}

class MapConfigRepositoryImpl(
    private val database: Database,
) : MapConfigRepository {
    override suspend fun getByServer(serverId: Long): MapConfig? = database.withTransaction {
        MapConfigEntity.find { MapConfigTable.serverId.eq(serverId) }
            .singleOrNull()
            ?.let(MapConfigMapper::toMapConfig)
    }

    override suspend fun upsert(
        serverId: Long,
        type: String,
        tileUrl: String?,
        worldName: String?,
        centerX: Int,
        centerZ: Int,
        zoomMin: Int,
        zoomMax: Int,
        zoomDefault: Int,
        playerMarkersUrl: String?,
    ): MapConfig = database.withTransaction {
        val existing = MapConfigEntity.find { MapConfigTable.serverId.eq(serverId) }.singleOrNull()
        if (existing != null) {
            MapConfigTable.update({ MapConfigTable.serverId.eq(serverId) }) {
                it[MapConfigTable.type] = type
                it[MapConfigTable.tileUrl] = tileUrl
                it[MapConfigTable.worldName] = worldName
                it[MapConfigTable.centerX] = centerX
                it[MapConfigTable.centerZ] = centerZ
                it[MapConfigTable.zoomMin] = zoomMin
                it[MapConfigTable.zoomMax] = zoomMax
                it[MapConfigTable.zoomDefault] = zoomDefault
                it[MapConfigTable.playerMarkersUrl] = playerMarkersUrl
                it[MapConfigTable.updatedAt] = Clock.System.now()
            }
            MapConfigMapper.toMapConfig(
                MapConfigEntity.find { MapConfigTable.serverId.eq(serverId) }.single()
            )
        } else {
            val entity = MapConfigEntity.new {
                this.serverId = serverId
                this.type = type
                this.tileUrl = tileUrl
                this.worldName = worldName
                this.centerX = centerX
                this.centerZ = centerZ
                this.zoomMin = zoomMin
                this.zoomMax = zoomMax
                this.zoomDefault = zoomDefault
                this.playerMarkersUrl = playerMarkersUrl
                this.updatedAt = Clock.System.now()
            }
            MapConfigMapper.toMapConfig(entity)
        }
    }
}

interface PlayerPositionRepository {
    /** 某服务器全部玩家位置 */
    suspend fun getByServer(serverId: Long): List<PlayerPosition>
    /** upsert 玩家位置 */
    suspend fun upsert(
        serverId: Long,
        gameUuid: String,
        playerName: String,
        x: Double,
        y: Double,
        z: Double,
        world: String?,
        now: Instant,
    ): PlayerPosition
}

class PlayerPositionRepositoryImpl(
    private val database: Database,
) : PlayerPositionRepository {
    override suspend fun getByServer(serverId: Long): List<PlayerPosition> = database.withTransaction {
        PlayerPositionEntity.find { PlayerPositionTable.serverId.eq(serverId) }
            .map(PlayerPositionMapper::toPlayerPosition)
    }

    override suspend fun upsert(
        serverId: Long,
        gameUuid: String,
        playerName: String,
        x: Double,
        y: Double,
        z: Double,
        world: String?,
        now: Instant,
    ): PlayerPosition = database.withTransaction {
        val existing = PlayerPositionEntity.find {
            PlayerPositionTable.serverId.eq(serverId).and(PlayerPositionTable.gameUuid.eq(gameUuid))
        }.singleOrNull()
        if (existing != null) {
            PlayerPositionTable.update({
                PlayerPositionTable.serverId.eq(serverId).and(PlayerPositionTable.gameUuid.eq(gameUuid))
            }) {
                it[PlayerPositionTable.playerName] = playerName
                it[PlayerPositionTable.x] = x
                it[PlayerPositionTable.y] = y
                it[PlayerPositionTable.z] = z
                it[PlayerPositionTable.world] = world
                it[PlayerPositionTable.updatedAt] = now
            }
            PlayerPositionMapper.toPlayerPosition(
                PlayerPositionEntity.find {
                    PlayerPositionTable.serverId.eq(serverId).and(PlayerPositionTable.gameUuid.eq(gameUuid))
                }.single()
            )
        } else {
            val px = x
            val py = y
            val pz = z
            val pw = world
            val entity = PlayerPositionEntity.new {
                this.serverId = serverId
                this.gameUuid = gameUuid
                this.playerName = playerName
                this.x = px
                this.y = py
                this.z = pz
                this.world = pw
                this.updatedAt = now
            }
            PlayerPositionMapper.toPlayerPosition(entity)
        }
    }
}
