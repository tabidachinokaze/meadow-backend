package moe.tabidachi.meadow.mapper

import moe.tabidachi.meadow.database.entity.BanEntity
import moe.tabidachi.meadow.database.entity.ReportEntity
import moe.tabidachi.meadow.database.model.Ban
import moe.tabidachi.meadow.database.model.Report

object ReportMapper {
    fun toReport(entity: ReportEntity): Report {
        return Report(
            id = entity.id.value,
            screenshotId = entity.screenshotId,
            reporterId = entity.reporterId,
            reason = entity.reason,
            status = entity.status,
            handlerId = entity.handlerId,
            handlerNote = entity.handlerNote,
            createdAt = entity.createdAt,
            handledAt = entity.handledAt,
        )
    }
}

object BanMapper {
    fun toBan(entity: BanEntity): Ban {
        return Ban(
            id = entity.id.value,
            serverId = entity.serverId,
            playerUuid = entity.playerUuid,
            playerName = entity.playerName,
            bannedBy = entity.bannedBy,
            reason = entity.reason,
            durationHours = entity.durationHours,
            expiresAt = entity.expiresAt,
            isActive = entity.isActive,
            createdAt = entity.createdAt,
        )
    }
}
