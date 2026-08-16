package moe.tabidachi.meadow.database.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestamp

/** 服务器玩家记录表（规划 §9.1.3 落地，数据由 Agent 状态上报维护） */
object ServerPlayerTable : LongIdTable("server_player") {
    val serverId = long("server_id").references(
        ref = ServerTable.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val gameUuid = varchar("game_uuid", 64)
    val playerName = varchar("player_name", 64)
    val firstSeen = timestamp("first_seen")
    val lastSeen = timestamp("last_seen")
    val onlineDuration = long("online_duration").default(0)
    val isOnline = bool("is_online").default(false)

    init {
        uniqueIndex(serverId, gameUuid)
    }
}
