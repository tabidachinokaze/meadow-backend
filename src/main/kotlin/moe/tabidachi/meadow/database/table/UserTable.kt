package moe.tabidachi.meadow.database.table

import moe.tabidachi.meadow.database.model.SystemRole
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestamp

object UserTable : LongIdTable(name = "user") {
    val username = varchar("username", 32).uniqueIndex()
    val password = varchar("password", 128)
    val email = varchar("email", 254).uniqueIndex()
    val phone = varchar("phone", 16).uniqueIndex().nullable()
    val avatarUrl = text("avatar_url").nullable()
    /** 个人主页 Banner 图（自定义链接或上传） */
    val bannerUrl = text("banner_url").nullable()
    val gameId = varchar("game_id", 64).uniqueIndex().nullable()
    val role = enumeration<SystemRole>("role").default(SystemRole.USER)
    val isActive = bool("is_active").default(true)
    val lastLogin = timestamp("last_login").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val bio = text("bio").nullable()
    val website = text("website").nullable()
    val location = text("location").nullable()
}