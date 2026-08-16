package moe.tabidachi.meadow.repository

import moe.tabidachi.meadow.database.entity.FavoriteEntity
import moe.tabidachi.meadow.database.model.Favorite
import moe.tabidachi.meadow.database.table.FavoriteTable
import moe.tabidachi.meadow.ktx.withTransaction
import moe.tabidachi.meadow.mapper.FavoriteMapper
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import kotlin.time.Clock

interface FavoriteRepository {
    suspend fun getByUser(userId: Long): List<Favorite>
    suspend fun exists(userId: Long, serverId: Long): Boolean
    suspend fun add(userId: Long, serverId: Long): Favorite
    suspend fun remove(userId: Long, serverId: Long): Boolean
}

class FavoriteRepositoryImpl(
    private val database: Database,
) : FavoriteRepository {
    override suspend fun getByUser(userId: Long): List<Favorite> = database.withTransaction {
        FavoriteEntity.find { FavoriteTable.userId.eq(userId) }
            .map(FavoriteMapper::toFavorite)
    }

    override suspend fun exists(userId: Long, serverId: Long): Boolean = database.withTransaction {
        FavoriteEntity.find {
            FavoriteTable.userId.eq(userId).and(FavoriteTable.serverId.eq(serverId))
        }.count() > 0
    }

    override suspend fun add(userId: Long, serverId: Long): Favorite = database.withTransaction {
        val entity = FavoriteEntity.new {
            this.userId = userId
            this.serverId = serverId
            this.createdAt = Clock.System.now()
        }
        FavoriteMapper.toFavorite(entity)
    }

    override suspend fun remove(userId: Long, serverId: Long): Boolean = database.withTransaction {
        val deleteCount = FavoriteTable.deleteWhere {
            FavoriteTable.userId.eq(userId).and(FavoriteTable.serverId.eq(serverId))
        }
        deleteCount > 0
    }
}
