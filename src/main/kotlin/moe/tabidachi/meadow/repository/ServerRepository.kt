package moe.tabidachi.meadow.repository

import io.ktor.util.*
import moe.tabidachi.meadow.database.entity.ServerEntity
import moe.tabidachi.meadow.database.model.Server
import moe.tabidachi.meadow.database.table.ServerTable
import moe.tabidachi.meadow.ktx.setIfNotNull
import moe.tabidachi.meadow.ktx.withTransaction
import moe.tabidachi.meadow.mapper.ServerMapper
import moe.tabidachi.meadow.model.ModLoader
import moe.tabidachi.meadow.model.ServerInfo
import moe.tabidachi.meadow.security.Encryptor
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock

interface ServerRepository {
    suspend fun getServers(): List<ServerInfo>
    suspend fun getByAddress(host: String, port: Int): Server?
    suspend fun getById(serverId: Long): Server?
    suspend fun getServerInfo(serverId: Long): ServerInfo?
    suspend fun create(
        name: String?,
        description: String?,
        host: String,
        port: Int,
        modLoader: ModLoader?,
        version: String?,
        bannerUrl: String?,
        tags: List<String>?,
        ownerId: Long,
        rconHost: String?,
        rconPort: Int?,
        rconPassword: String?,
        serverKey: String?,
        machineId: String?,
    ): Long

    suspend fun update(
        serverId: Long,
        name: String? = null,
        description: String? = null,
        host: String? = null,
        port: Int? = null,
        modLoader: ModLoader? = null,
        version: String? = null,
        bannerUrl: String? = null,
        tags: List<String>? = null,
        rconHost: String? = null,
        rconPort: Int? = null,
        rconPassword: String? = null,
        isVerified: Boolean? = null,
        serverKey: String? = null,
        machineId: String? = null,
        ownerId: Long? = null,
        onlinePlayers: Int? = null,
        maxPlayers: Int? = null,
        uptimeSeconds: Long? = null,
        lastStatusAt: kotlin.time.Instant? = null,
    ): Boolean

    suspend fun delete(serverId: Long): Boolean
}

class ServerRepositoryImpl(
    private val database: Database,
    private val encryptor: Encryptor
) : ServerRepository {
    private val serverMapper = ServerMapper(encryptor)
    override suspend fun getServers(): List<ServerInfo> = database.withTransaction {
        ServerEntity.all().map(serverMapper::toServerInfo)
    }

    override suspend fun getByAddress(host: String, port: Int): Server? = database.withTransaction {
        ServerEntity.find { ServerTable.host.eq(host).and(ServerTable.port.eq(port)) }
            .singleOrNull()
            ?.let(serverMapper::toServer)
    }

    override suspend fun getById(serverId: Long): Server? = database.withTransaction {
        ServerEntity.find { ServerTable.id.eq(serverId) }
            .singleOrNull()
            ?.let(serverMapper::toServer)
    }

    override suspend fun getServerInfo(serverId: Long): ServerInfo? = database.withTransaction {
        ServerEntity.find { ServerTable.id.eq(serverId) }
            .singleOrNull()
            ?.let(serverMapper::toServerInfo)
    }

    override suspend fun create(
        name: String?,
        description: String?,
        host: String,
        port: Int,
        modLoader: ModLoader?,
        version: String?,
        bannerUrl: String?,
        tags: List<String>?,
        ownerId: Long,
        rconHost: String?,
        rconPort: Int?,
        rconPassword: String?,
        serverKey: String?,
        machineId: String?
    ): Long = database.withTransaction {
        ServerEntity.new {
            this.name = name
            this.description = description
            this.host = host
            this.port = port
            this.modLoader = modLoader
            this.version = version
            this.bannerUrl = bannerUrl
            this.tags = tags
            this.ownerId = ownerId
            this.rconHost = rconHost
            this.rconPort = rconPort
            this.rconPassword = rconPassword?.let(encryptor::encrypt)
            this.serverKey = serverKey ?: generateNonceBlocking(16)
            this.machineId = machineId
            val now = Clock.System.now()
            this.createdAt = now
            this.updatedAt = now
        }.id.value
    }

    override suspend fun update(
        serverId: Long,
        name: String?,
        description: String?,
        host: String?,
        port: Int?,
        modLoader: ModLoader?,
        version: String?,
        bannerUrl: String?,
        tags: List<String>?,
        rconHost: String?,
        rconPort: Int?,
        rconPassword: String?,
        isVerified: Boolean?,
        serverKey: String?,
        machineId: String?,
        ownerId: Long?,
        onlinePlayers: Int?,
        maxPlayers: Int?,
        uptimeSeconds: Long?,
        lastStatusAt: kotlin.time.Instant?
    ): Boolean = database.withTransaction {
        val updateCount = ServerTable.update({ ServerTable.id.eq(serverId) }) { statement ->
            statement.setIfNotNull(ServerTable.name, name)
            statement.setIfNotNull(ServerTable.description, description)
            statement.setIfNotNull(ServerTable.host, host)
            statement.setIfNotNull(ServerTable.port, port)
            statement.setIfNotNull(ServerTable.modLoader, modLoader)
            statement.setIfNotNull(ServerTable.version, version)
            statement.setIfNotNull(ServerTable.bannerUrl, bannerUrl)
            statement.setIfNotNull(ServerTable.tags, tags)
            statement.setIfNotNull(ServerTable.rconHost, rconHost)
            statement.setIfNotNull(ServerTable.rconPort, rconPort)
            statement.setIfNotNull(ServerTable.rconPassword, rconPassword?.let(encryptor::encrypt))
            statement.setIfNotNull(ServerTable.isVerified, isVerified)
            statement.setIfNotNull(ServerTable.serverKey, serverKey)
            statement.setIfNotNull(ServerTable.machineId, machineId)
            statement.setIfNotNull(ServerTable.ownerId, ownerId)
            statement.setIfNotNull(ServerTable.onlinePlayers, onlinePlayers)
            statement.setIfNotNull(ServerTable.maxPlayers, maxPlayers)
            statement.setIfNotNull(ServerTable.uptimeSeconds, uptimeSeconds)
            statement.setIfNotNull(ServerTable.lastStatusAt, lastStatusAt)
            statement[ServerTable.updatedAt] = Clock.System.now()
        }
        updateCount > 0
    }

    override suspend fun delete(serverId: Long): Boolean = database.withTransaction {
        val deleteCount = ServerTable.deleteWhere { ServerTable.id.eq(serverId) }
        deleteCount > 0
    }
}