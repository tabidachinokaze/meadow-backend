package moe.tabidachi.meadow.repository

import moe.tabidachi.meadow.database.entity.ScreenshotEntity
import moe.tabidachi.meadow.database.model.Screenshot
import moe.tabidachi.meadow.database.table.ScreenshotTable
import moe.tabidachi.meadow.ktx.withTransaction
import moe.tabidachi.meadow.mapper.ScreenshotMapper
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.plus
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock

interface ScreenshotRepository {
    suspend fun getByServer(serverId: Long, uploaderId: Long? = null, status: String? = null): List<Screenshot>
    suspend fun getById(serverId: Long, screenshotId: Long): Screenshot?
    /** 仅按截图 id 查询（用于举报/审核场景，此时未知 serverId） */
    suspend fun getByScreenshotId(screenshotId: Long): Screenshot?
    suspend fun getByUploader(uploaderId: Long): List<Screenshot>
    suspend fun add(
        serverId: Long,
        uploaderId: Long,
        uploaderName: String,
        imageUrl: String,
        description: String?,
        coordinates: String?,
    ): Screenshot
    suspend fun markReported(serverId: Long, screenshotId: Long): Boolean
    /** 审核下架：status → deleted（保留证据，不物理删除） */
    suspend fun markDeleted(serverId: Long, screenshotId: Long): Boolean
    suspend fun incrementDownload(serverId: Long, screenshotId: Long): Boolean
    suspend fun delete(serverId: Long, screenshotId: Long): Boolean
}

class ScreenshotRepositoryImpl(
    private val database: Database,
) : ScreenshotRepository {
    override suspend fun getByServer(
        serverId: Long,
        uploaderId: Long?,
        status: String?,
    ): List<Screenshot> = database.withTransaction {
        val conditions = buildList {
            add(ScreenshotTable.serverId.eq(serverId))
            uploaderId?.let { add(ScreenshotTable.uploaderId.eq(it)) }
            status?.let { add(ScreenshotTable.status.eq(it)) }
        }
        ScreenshotEntity.find(conditions.reduce { acc, op -> acc and op })
            .map(ScreenshotMapper::toScreenshot)
    }

    override suspend fun getById(serverId: Long, screenshotId: Long): Screenshot? = database.withTransaction {
        ScreenshotEntity.find {
            ScreenshotTable.id.eq(screenshotId).and(ScreenshotTable.serverId.eq(serverId))
        }.singleOrNull()?.let(ScreenshotMapper::toScreenshot)
    }

    override suspend fun getByScreenshotId(screenshotId: Long): Screenshot? = database.withTransaction {
        ScreenshotEntity.find { ScreenshotTable.id.eq(screenshotId) }
            .singleOrNull()?.let(ScreenshotMapper::toScreenshot)
    }

    override suspend fun getByUploader(uploaderId: Long): List<Screenshot> = database.withTransaction {
        ScreenshotEntity.find { ScreenshotTable.uploaderId.eq(uploaderId) }
            .map(ScreenshotMapper::toScreenshot)
    }

    override suspend fun add(
        serverId: Long,
        uploaderId: Long,
        uploaderName: String,
        imageUrl: String,
        description: String?,
        coordinates: String?,
    ): Screenshot = database.withTransaction {
        val entity = ScreenshotEntity.new {
            this.serverId = serverId
            this.uploaderId = uploaderId
            this.uploaderName = uploaderName
            this.imageUrl = imageUrl
            this.description = description
            this.coordinates = coordinates
            this.createdAt = Clock.System.now()
        }
        ScreenshotMapper.toScreenshot(entity)
    }

    override suspend fun markReported(serverId: Long, screenshotId: Long): Boolean = database.withTransaction {
        val updateCount = ScreenshotTable.update({
            ScreenshotTable.id.eq(screenshotId).and(ScreenshotTable.serverId.eq(serverId))
        }) {
            it[status] = "reported"
            it[reportCount] = reportCount + 1
        }
        updateCount > 0
    }

    override suspend fun markDeleted(serverId: Long, screenshotId: Long): Boolean = database.withTransaction {
        val updateCount = ScreenshotTable.update({
            ScreenshotTable.id.eq(screenshotId).and(ScreenshotTable.serverId.eq(serverId))
        }) {
            it[status] = "deleted"
        }
        updateCount > 0
    }

    override suspend fun incrementDownload(serverId: Long, screenshotId: Long): Boolean = database.withTransaction {
        val updateCount = ScreenshotTable.update({
            ScreenshotTable.id.eq(screenshotId).and(ScreenshotTable.serverId.eq(serverId))
        }) {
            it[downloadCount] = downloadCount + 1
        }
        updateCount > 0
    }

    override suspend fun delete(serverId: Long, screenshotId: Long): Boolean = database.withTransaction {
        val deleteCount = ScreenshotTable.deleteWhere {
            ScreenshotTable.id.eq(screenshotId).and(ScreenshotTable.serverId.eq(serverId))
        }
        deleteCount > 0
    }
}
