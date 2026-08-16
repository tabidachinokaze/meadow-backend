package moe.tabidachi.meadow.database.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestamp

/** 服务器地图瓦片配置（规划 §9.4，数据由 Agent 上报 / 管理员配置维护） */
object MapConfigTable : LongIdTable("map_config") {
    val serverId = long("server_id").references(
        ref = ServerTable.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    ).uniqueIndex()
    val type = varchar("type", 32).default("dynmap")
    val tileUrl = text("tile_url").nullable()
    val worldName = varchar("world_name", 64).nullable()
    val centerX = integer("center_x").default(0)
    val centerZ = integer("center_z").default(0)
    val zoomMin = integer("zoom_min").default(0)
    val zoomMax = integer("zoom_max").default(3)
    val zoomDefault = integer("zoom_default").default(1)
    val playerMarkersUrl = text("player_markers_url").nullable()
    /** 实时 Web 地图地址（BlueMap/Dynmap/Pl3xMap 等，前端 iframe 嵌入） */
    val webMapUrl = text("web_map_url").nullable()
    /** 世界种子（种子预览用，Chunkbase 等） */
    val seed = long("seed").nullable()
    val updatedAt = timestamp("updated_at")
}

/** 玩家实时位置（规划 §9.4.2，数据由 Agent 状态上报携带坐标维护） */
object PlayerPositionTable : LongIdTable("player_position") {
    val serverId = long("server_id").references(
        ref = ServerTable.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val gameUuid = varchar("game_uuid", 64)
    val playerName = varchar("player_name", 64)
    val x = double("x").default(0.0)
    val y = double("y").default(0.0)
    val z = double("z").default(0.0)
    val world = varchar("world", 64).nullable()
    val updatedAt = timestamp("updated_at")

    init {
        uniqueIndex(serverId, gameUuid)
    }
}
