package moe.tabidachi.meadow.database.entity

import moe.tabidachi.meadow.database.table.ChatMessageTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class ChatMessageEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ChatMessageEntity>(ChatMessageTable)

    var serverId by ChatMessageTable.serverId
    var senderName by ChatMessageTable.senderName
    var senderUuid by ChatMessageTable.senderUuid
    var content by ChatMessageTable.content
    var type by ChatMessageTable.type
    var isBroadcast by ChatMessageTable.isBroadcast
    var isRecalled by ChatMessageTable.isRecalled
    var createdAt by ChatMessageTable.createdAt
}
