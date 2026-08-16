package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.model.*
import moe.tabidachi.meadow.repository.FavoriteRepository
import moe.tabidachi.meadow.repository.ServerRepository

class FavoriteServiceImpl(
    private val favoriteRepository: FavoriteRepository,
    private val serverRepository: ServerRepository,
) : FavoriteService {

    override suspend fun addFavorite(callerId: Long, serverId: Long): Response<FavoriteResult?> {
        if (serverRepository.getById(serverId) == null) {
            return ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        }
        if (!favoriteRepository.exists(callerId, serverId)) {
            favoriteRepository.add(callerId, serverId)
        }
        return CommonStatusCode.SUCCESS.withData(FavoriteResult(isFavorited = true))
    }

    override suspend fun removeFavorite(callerId: Long, serverId: Long): Response<FavoriteResult?> {
        if (serverRepository.getById(serverId) == null) {
            return ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        }
        favoriteRepository.remove(callerId, serverId)
        return CommonStatusCode.SUCCESS.withData(FavoriteResult(isFavorited = false))
    }

    override suspend fun getFavorites(callerId: Long): Response<List<ServerInfo>?> {
        val favorites = favoriteRepository.getByUser(callerId)
        val servers = favorites.mapNotNull { favorite ->
            serverRepository.getServerInfo(favorite.serverId)
        }.map { it.desensitize() }
        return CommonStatusCode.SUCCESS.withData(servers)
    }
}
