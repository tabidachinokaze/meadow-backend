package moe.tabidachi.meadow.repository

import moe.tabidachi.meadow.database.entity.ServerPlayerEntity
import moe.tabidachi.meadow.database.model.ServerPlayer
import moe.tabidachi.meadow.database.table.ServerPlayerTable
import moe.tabidachi.meadow.ktx.withTransaction
import moe.tabidachi.meadow.mapper.ServerPlayerMapper
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.notInList
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock

interface ServerPlayerRepository {
    /** 某服务器在线的玩家 */
    suspend fun getOnline(serverId: Long): List<ServerPlayer>
    /** 某服务器最近离线玩家（last_seen 倒序） */
    suspend fun getRecentOffline(serverId: Long, limit: Int = 20): List<ServerPlayer>
    /** 玩家在所有服务器上的记录 */
    suspend fun getByUuid(uuid: String): List<ServerPlayer>
    /** 上报在线玩家：upsert（首次记录 / 更新 last_seen），并标记不在线的玩家为离线 */
    suspend fun syncOnline(serverId: Long, online: List<Pair<String, String>>, now: kotlin.time.Instant): List<ServerPlayer>
}

class ServerPlayerRepositoryImpl(
    private val database: Database,
) : ServerPlayerRepository {
    override suspend fun getOnline(serverId: Long): List<ServerPlayer> = database.withTransaction {
        ServerPlayerEntity.find { ServerPlayerTable.serverId.eq(serverId).and(ServerPlayerTable.isOnline.eq(true)) }
            .map(ServerPlayerMapper::toServerPlayer)
    }

    override suspend fun getRecentOffline(serverId: Long, limit: Int): List<ServerPlayer> = database.withTransaction {
        ServerPlayerEntity.find { ServerPlayerTable.serverId.eq(serverId).and(ServerPlayerTable.isOnline.eq(false)) }
            .orderBy(ServerPlayerTable.lastSeen to SortOrder.DESC)
            .limit(limit)
            .map(ServerPlayerMapper::toServerPlayer)
    }

    override suspend fun getByUuid(uuid: String): List<ServerPlayer> = database.withTransaction {
        ServerPlayerEntity.find { ServerPlayerTable.gameUuid.eq(uuid) }
            .map(ServerPlayerMapper::toServerPlayer)
    }

    override suspend fun syncOnline(
        serverId: Long,
        online: List<Pair<String, String>>,
        now: kotlin.time.Instant,
    ): List<ServerPlayer> = database.withTransaction {
        val onlineUuids = online.map { it.first }
        // 标记不再在线的玩家为离线
        if (onlineUuids.isNotEmpty()) {
            ServerPlayerTable.update({
                ServerPlayerTable.serverId.eq(serverId).and(ServerPlayerTable.isOnline.eq(true))
                    .and(ServerPlayerTable.gameUuid.notInList(onlineUuids))
            }) {
                it[isOnline] = false
                it[lastSeen] = now
            }
        } else {
            ServerPlayerTable.update({
                ServerPlayerTable.serverId.eq(serverId).and(ServerPlayerTable.isOnline.eq(true))
            }) {
                it[isOnline] = false
                it[lastSeen] = now
            }
        }
        // upsert 在线玩家
        val result = mutableListOf<ServerPlayer>()
        for ((uuid, name) in online) {
            val existing = ServerPlayerEntity.find {
                ServerPlayerTable.serverId.eq(serverId).and(ServerPlayerTable.gameUuid.eq(uuid))
            }.singleOrNull()
            if (existing != null) {
                ServerPlayerTable.update({
                    ServerPlayerTable.serverId.eq(serverId).and(ServerPlayerTable.gameUuid.eq(uuid))
                }) {
                    it[playerName] = name
                    it[lastSeen] = now
                    it[isOnline] = true
                }
                result.add(
                    ServerPlayer(
                        serverId = serverId,
                        gameUuid = uuid,
                        playerName = name,
                        firstSeen = existing.firstSeen,
                        lastSeen = now,
                        onlineDuration = existing.onlineDuration,
                        isOnline = true,
                    )
                )
            } else {
                val entity = ServerPlayerEntity.new {
                    this.serverId = serverId
                    this.gameUuid = uuid
                    this.playerName = name
                    this.firstSeen = now
                    this.lastSeen = now
                    this.onlineDuration = 0
                    this.isOnline = true
                }
                result.add(ServerPlayerMapper.toServerPlayer(entity))
            }
        }
        result
    }
}
