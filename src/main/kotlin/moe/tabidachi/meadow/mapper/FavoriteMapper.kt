package moe.tabidachi.meadow.mapper

import moe.tabidachi.meadow.database.entity.FavoriteEntity
import moe.tabidachi.meadow.database.model.Favorite

object FavoriteMapper {
    fun toFavorite(entity: FavoriteEntity): Favorite {
        return Favorite(
            userId = entity.userId,
            serverId = entity.serverId,
            createdAt = entity.createdAt
        )
    }
}
