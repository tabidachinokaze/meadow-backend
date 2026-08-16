package moe.tabidachi.meadow.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 地图配置保存请求（管理员配置，规划 §9.4.1） */
@Serializable
data class MapConfigRequest(
    @SerialName("type")
    val type: String = "dynmap",
    @SerialName("tile_url")
    val tileUrl: String? = null,
    @SerialName("world_name")
    val worldName: String? = null,
    @SerialName("center")
    val center: MapCenterRequest? = null,
    @SerialName("zoom")
    val zoom: MapZoomRequest? = null,
    @SerialName("player_markers_url")
    val playerMarkersUrl: String? = null,
) {
    @Serializable
    data class MapCenterRequest(
        @SerialName("x")
        val x: Int,
        @SerialName("z")
        val z: Int,
    )

    @Serializable
    data class MapZoomRequest(
        @SerialName("min")
        val min: Int = 0,
        @SerialName("max")
        val max: Int = 3,
        @SerialName("default")
        val default: Int = 1,
    )
}
