package moe.tabidachi.meadow.mapper

import moe.tabidachi.meadow.database.entity.ScreenshotEntity
import moe.tabidachi.meadow.database.model.Screenshot

object ScreenshotMapper {
    fun toScreenshot(entity: ScreenshotEntity): Screenshot {
        return Screenshot(
            id = entity.id.value,
            serverId = entity.serverId,
            uploaderId = entity.uploaderId,
            uploaderName = entity.uploaderName,
            imageUrl = entity.imageUrl,
            description = entity.description,
            coordinates = entity.coordinates,
            status = entity.status,
            reportCount = entity.reportCount,
            downloadCount = entity.downloadCount,
            createdAt = entity.createdAt
        )
    }
}
