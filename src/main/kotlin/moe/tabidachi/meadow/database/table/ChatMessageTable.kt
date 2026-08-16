package moe.tabidachi.meadow.database.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestamp

/** 聊天消息表（规划 §9.1.5 落地） */
object ChatMessageTable : LongIdTable("chat_message") {
    val serverId = long("server_id").references(
        ref = ServerTable.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val senderName = varchar("sender_name", 64)
    val senderUuid = varchar("sender_uuid", 64).nullable()
    val content = text("content")
    val type = varchar("type", 20).default("chat")
    val isBroadcast = bool("is_broadcast").default(false)
    val isRecalled = bool("is_recalled").default(false)
    val createdAt = timestamp("created_at")

    init {
        index(false, serverId)
        index(false, senderUuid)
    }
}
