package moe.tabidachi.meadow.database.entity

import moe.tabidachi.meadow.database.table.FavoriteTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class FavoriteEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<FavoriteEntity>(FavoriteTable)

    var userId by FavoriteTable.userId
    var serverId by FavoriteTable.serverId
    var createdAt by FavoriteTable.createdAt
}
