package moe.tabidachi.meadow.database.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestamp

/** 举报记录表（规划 §9.1.7 落地） */
object ReportTable : LongIdTable("report") {
    val screenshotId = long("screenshot_id").references(
        ref = ScreenshotTable.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val reporterId = long("reporter_id").references(
        ref = UserTable.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val reason = varchar("reason", 256)
    val status = varchar("status", 20).default("pending")
    val handlerId = long("handler_id").references(UserTable.id).nullable()
    val handlerNote = varchar("handler_note", 256).nullable()
    val createdAt = timestamp("created_at")
    val handledAt = timestamp("handled_at").nullable()
}

/** 禁言记录表（规划 §9.1.10 落地） */
object BanTable : LongIdTable("ban") {
    val serverId = long("server_id").references(
        ref = ServerTable.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val playerUuid = varchar("player_uuid", 64)
    val playerName = varchar("player_name", 64)
    val bannedBy = long("banned_by").references(
        ref = UserTable.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val reason = varchar("reason", 256).default("违反聊天规则")
    val durationHours = integer("duration_hours")
    val expiresAt = timestamp("expires_at").nullable()
    val isActive = bool("is_active").default(true)
    val createdAt = timestamp("created_at")
}
