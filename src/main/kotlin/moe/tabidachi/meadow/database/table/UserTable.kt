package moe.tabidachi.meadow.database.table

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestamp

object UserTable : LongIdTable(name = "user") {
    val username = varchar("username", 32).uniqueIndex()
    val password = varchar("password", 128)
    val email = varchar("email", 254).uniqueIndex().nullable()
    val phone = varchar("phone", 16).uniqueIndex().nullable()
    val avatar = text("avatar").nullable()
    val createTime = timestamp("create_time")
    val updateTime = timestamp("update_time")
}