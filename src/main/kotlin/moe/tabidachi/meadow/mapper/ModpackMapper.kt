package moe.tabidachi.meadow.mapper

import moe.tabidachi.meadow.database.entity.ModpackEntity
import moe.tabidachi.meadow.database.model.ServerModpack

object ModpackMapper {
    fun toServerModpack(entity: ModpackEntity): ServerModpack {
        return ServerModpack(
            id = entity.id.value,
            serverId = entity.serverId,
            version = entity.version,
            releaseDate = entity.releaseDate,
            downloadUrl = entity.downloadUrl,
            fileSize = entity.fileSize,
            md5Hash = entity.md5Hash,
            changelog = entity.changelog,
            downloadCount = entity.downloadCount,
            isActive = entity.isActive,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )
    }
}
