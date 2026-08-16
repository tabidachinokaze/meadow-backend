package moe.tabidachi.meadow.database.entity

import moe.tabidachi.meadow.database.table.MapConfigTable
import moe.tabidachi.meadow.database.table.PlayerPositionTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class MapConfigEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<MapConfigEntity>(MapConfigTable)

    var serverId by MapConfigTable.serverId
    var type by MapConfigTable.type
    var tileUrl by MapConfigTable.tileUrl
    var worldName by MapConfigTable.worldName
    var centerX by MapConfigTable.centerX
    var centerZ by MapConfigTable.centerZ
    var zoomMin by MapConfigTable.zoomMin
    var zoomMax by MapConfigTable.zoomMax
    var zoomDefault by MapConfigTable.zoomDefault
    var playerMarkersUrl by MapConfigTable.playerMarkersUrl
    var webMapUrl by MapConfigTable.webMapUrl
    var seed by MapConfigTable.seed
    var updatedAt by MapConfigTable.updatedAt
}

class PlayerPositionEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<PlayerPositionEntity>(PlayerPositionTable)

    var serverId by PlayerPositionTable.serverId
    var gameUuid by PlayerPositionTable.gameUuid
    var playerName by PlayerPositionTable.playerName
    var x by PlayerPositionTable.x
    var y by PlayerPositionTable.y
    var z by PlayerPositionTable.z
    var world by PlayerPositionTable.world
    var updatedAt by PlayerPositionTable.updatedAt
}
