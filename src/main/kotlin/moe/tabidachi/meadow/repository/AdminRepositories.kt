package moe.tabidachi.meadow.repository

import moe.tabidachi.meadow.database.entity.BanEntity
import moe.tabidachi.meadow.database.entity.ReportEntity
import moe.tabidachi.meadow.database.model.Ban
import moe.tabidachi.meadow.database.model.Report
import moe.tabidachi.meadow.database.table.BanTable
import moe.tabidachi.meadow.database.table.ReportTable
import moe.tabidachi.meadow.ktx.withTransaction
import moe.tabidachi.meadow.mapper.BanMapper
import moe.tabidachi.meadow.mapper.ReportMapper
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock

interface ReportRepository {
    /** 举报列表（按创建时间倒序，可选状态筛选） */
    suspend fun getByStatus(status: String?): List<Report>
    suspend fun getById(reportId: Long): Report?
    suspend fun add(screenshotId: Long, reporterId: Long, reason: String): Report
    /** 审核处理：更新状态/处理人/备注/处理时间 */
    suspend fun handle(reportId: Long, status: String, handlerId: Long, handlerNote: String?): Boolean
}

class ReportRepositoryImpl(
    private val database: Database,
) : ReportRepository {
    override suspend fun getByStatus(status: String?): List<Report> = database.withTransaction {
        val entities = if (status != null) {
            ReportEntity.find { ReportTable.status.eq(status) }
        } else {
            ReportEntity.all()
        }
        entities.orderBy(ReportTable.createdAt to SortOrder.DESC)
            .map(ReportMapper::toReport)
    }

    override suspend fun getById(reportId: Long): Report? = database.withTransaction {
        ReportEntity.find { ReportTable.id.eq(reportId) }
            .singleOrNull()?.let(ReportMapper::toReport)
    }

    override suspend fun add(screenshotId: Long, reporterId: Long, reason: String): Report = database.withTransaction {
        val entity = ReportEntity.new {
            this.screenshotId = screenshotId
            this.reporterId = reporterId
            this.reason = reason
            this.createdAt = Clock.System.now()
        }
        ReportMapper.toReport(entity)
    }

    override suspend fun handle(reportId: Long, status: String, handlerId: Long, handlerNote: String?): Boolean =
        database.withTransaction {
            val updateCount = ReportTable.update({ ReportTable.id.eq(reportId) }) {
                it[ReportTable.status] = status
                it[ReportTable.handlerId] = handlerId
                it[ReportTable.handlerNote] = handlerNote
                it[ReportTable.handledAt] = Clock.System.now()
            }
            updateCount > 0
        }
}

interface BanRepository {
    /** 服务器生效中的禁言（含过期未解禁的，供过滤） */
    suspend fun getByServer(serverId: Long): List<Ban>
    suspend fun getById(serverId: Long, banId: Long): Ban?
    suspend fun add(
        serverId: Long,
        playerUuid: String,
        playerName: String,
        bannedBy: Long,
        reason: String,
        durationHours: Int,
        expiresAt: kotlin.time.Instant?,
    ): Ban
    suspend fun deactivate(serverId: Long, banId: Long): Boolean
}

class BanRepositoryImpl(
    private val database: Database,
) : BanRepository {
    override suspend fun getByServer(serverId: Long): List<Ban> = database.withTransaction {
        BanEntity.find { BanTable.serverId.eq(serverId) }
            .orderBy(BanTable.createdAt to SortOrder.DESC)
            .map(BanMapper::toBan)
    }

    override suspend fun getById(serverId: Long, banId: Long): Ban? = database.withTransaction {
        BanEntity.find { BanTable.id.eq(banId).and(BanTable.serverId.eq(serverId)) }
            .singleOrNull()?.let(BanMapper::toBan)
    }

    override suspend fun add(
        serverId: Long,
        playerUuid: String,
        playerName: String,
        bannedBy: Long,
        reason: String,
        durationHours: Int,
        expiresAt: kotlin.time.Instant?,
    ): Ban = database.withTransaction {
        val entity = BanEntity.new {
            this.serverId = serverId
            this.playerUuid = playerUuid
            this.playerName = playerName
            this.bannedBy = bannedBy
            this.reason = reason
            this.durationHours = durationHours
            this.expiresAt = expiresAt
            this.createdAt = Clock.System.now()
        }
        BanMapper.toBan(entity)
    }

    override suspend fun deactivate(serverId: Long, banId: Long): Boolean = database.withTransaction {
        val updateCount = BanTable.update({
            BanTable.id.eq(banId).and(BanTable.serverId.eq(serverId))
        }) {
            it[isActive] = false
        }
        updateCount > 0
    }
}
