package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.model.*
import moe.tabidachi.meadow.repository.ModRepository
import moe.tabidachi.meadow.repository.ServerRepository

class ModServiceImpl(
    private val modRepository: ModRepository,
    private val serverRepository: ServerRepository,
) : ModService {

    override suspend fun getServerMods(
        serverId: Long,
        keyword: String?,
        category: String?,
    ): Response<ServerModsResult?> {
        if (serverRepository.getById(serverId) == null) {
            return ServerStatusCode.SERVER_NOT_EXISTS.emptyData()
        }
        val mods = modRepository.getByServer(serverId, category)
            .filter { keyword == null || it.modName.contains(keyword, ignoreCase = true) }
            .map { ModInfo(id = it.id, modName = it.modName, modVersion = it.modVersion, category = it.modCategory) }
        return CommonStatusCode.SUCCESS.withData(ServerModsResult(total = mods.size, mods = mods))
    }
}
