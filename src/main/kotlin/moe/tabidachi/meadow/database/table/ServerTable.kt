package moe.tabidachi.meadow.database.table

import moe.tabidachi.meadow.model.ModLoader
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestamp

object ServerTable : LongIdTable(name = "server") {
    val name = varchar("name", 255).nullable()
    val description = text("description").nullable()
    val host = varchar("host", 255)
    val port = integer("port")
    val modLoader = enumeration<ModLoader>("mod_loader").nullable()
    val version = text("version").nullable()
    val bannerUrl = text("banner_url").nullable()
    val tags = array<String>("tags").nullable()
    val ownerId = long("owner_id").references(UserTable.id)
    val rconHost = varchar("rcon_host", 255).nullable()
    val rconPort = integer("rcon_port").nullable()
    val rconPassword = varchar("rcon_password", 255).nullable()
    val isVerified = bool("is_verified").default(false)
    val serverKey = text("server_key")
    val machineId = varchar("machine_id", 255).nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    init {
        uniqueIndex(host, port)
    }
}