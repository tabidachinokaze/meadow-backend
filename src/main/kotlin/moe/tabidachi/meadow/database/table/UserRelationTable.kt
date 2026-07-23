package moe.tabidachi.meadow.database.table

import moe.tabidachi.meadow.model.RelationStatus
import moe.tabidachi.meadow.model.RelationType
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.timestamp

object UserRelationTable : LongIdTable("user_relation") {
    val userId = long("user_id").references(
        ref = UserTable.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )

    val targetUserId = long("target_user_id").references(
        ref = UserTable.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val type = enumeration<RelationType>("relation_type")
    val status = enumeration<RelationStatus>("relation_status")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    init {
        uniqueIndex(userId, targetUserId)
    }
}