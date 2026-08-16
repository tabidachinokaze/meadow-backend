package moe.tabidachi.meadow.database.model

import kotlin.time.Instant

/** 服务器地图瓦片配置（数据模型） */
data class MapConfig(
    val id: Long,
    val serverId: Long,
    val type: String,
    val tileUrl: String?,
    val worldName: String?,
    val centerX: Int,
    val centerZ: Int,
    val zoomMin: Int,
    val zoomMax: Int,
    val zoomDefault: Int,
    val playerMarkersUrl: String?,
    val webMapUrl: String?,
    val seed: Long?,
    val updatedAt: Instant,
)

/** 玩家实时位置（数据模型） */
data class PlayerPosition(
    val id: Long,
    val serverId: Long,
    val gameUuid: String,
    val playerName: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val world: String?,
    val updatedAt: Instant,
)
