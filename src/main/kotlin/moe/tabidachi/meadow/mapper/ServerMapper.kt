package moe.tabidachi.meadow.mapper

import moe.tabidachi.meadow.database.entity.ServerEntity
import moe.tabidachi.meadow.database.model.Server
import moe.tabidachi.meadow.model.ServerInfo
import moe.tabidachi.meadow.security.Encryptor

class ServerMapper(
    private val encryptor: Encryptor
) {
    fun toServer(entity: ServerEntity): Server {
        return Server(
            id = entity.id.value,
            name = entity.name,
            description = entity.description,
            host = entity.host,
            port = entity.port,
            modLoader = entity.modLoader,
            version = entity.version,
            bannerUrl = entity.bannerUrl,
            tags = entity.tags,
            ownerId = entity.ownerId,
            rconHost = entity.rconHost,
            rconPort = entity.rconPort,
            rconPassword = entity.rconPassword?.let(encryptor::decrypt),
            isVerified = entity.isVerified,
            serverKey = entity.serverKey,
            machineId = entity.machineId,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toServerInfo(entity: ServerEntity): ServerInfo {
        return ServerInfo(
            id = entity.id.value,
            name = entity.name,
            description = entity.description,
            host = entity.host,
            port = entity.port,
            modLoader = entity.modLoader,
            version = entity.version,
            bannerUrl = entity.bannerUrl,
            tags = entity.tags,
            rconHost = entity.rconHost,
            rconPort = entity.rconPort,
            isVerified = entity.isVerified,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            ownerId = entity.ownerId,
            rconPassword = entity.rconPassword?.let(encryptor::decrypt),
            serverKey = entity.serverKey,
            machineId = entity.machineId
        )
    }
}