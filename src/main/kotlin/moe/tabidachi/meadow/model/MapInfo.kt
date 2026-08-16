package moe.tabidachi.meadow.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** 地图瓦片配置（9.4.1 响应） */
@Serializable
data class MapConfigInfo(
    val type: String,
    val tileUrl: String?,
    val worldName: String?,
    val center: MapCenter,
    val zoom: MapZoom,
    val playerMarkersUrl: String?,
    /** 实时 Web 地图地址（BlueMap/Dynmap 等，前端 iframe 嵌入） */
    val webMapUrl: String?,
    /** 世界种子（种子预览用） */
    val seed: Long?,
    val updatedAt: Instant?,
)

@Serializable
data class MapCenter(
    val x: Int,
    val z: Int,
)

@Serializable
data class MapZoom(
    val min: Int,
    val max: Int,
    val default: Int,
)

/** 玩家实时位置（9.4.2 列表项） */
@Serializable
data class MapPlayerPosition(
    val name: String,
    val uuid: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val world: String?,
    val avatarUrl: String?,
)

/** 地图实时玩家（9.4.2 响应） */
@Serializable
data class MapPlayersResult(
    val players: List<MapPlayerPosition>,
    val updatedAt: Instant?,
)

/**
 * 地图模块状态码（码段 411xx）
 */
enum class MapStatusCode(
    override val code: Int,
    override val message: String,
) : StatusCode {
    CONFIG_NOT_SET(41101, "地图尚未配置"),
}
