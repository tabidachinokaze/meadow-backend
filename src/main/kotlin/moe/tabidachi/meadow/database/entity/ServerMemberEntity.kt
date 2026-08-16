package moe.tabidachi.meadow.database.entity

import moe.tabidachi.meadow.database.model.ServerRole
import moe.tabidachi.meadow.database.table.ServerMemberTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class ServerMemberEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ServerMemberEntity>(ServerMemberTable)

    var serverId by ServerMemberTable.serverId
    var userId by ServerMemberTable.userId
    var role by ServerMemberTable.role
    var joinedAt by ServerMemberTable.joinedAt
    var updatedAt by ServerMemberTable.updatedAt
}
