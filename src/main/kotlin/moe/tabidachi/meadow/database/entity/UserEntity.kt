package moe.tabidachi.meadow.database.entity

import moe.tabidachi.meadow.database.table.UserTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class UserEntity(uid: EntityID<Long>) : LongEntity(uid) {
    companion object : LongEntityClass<UserEntity>(UserTable)

    var username by UserTable.username
    var password by UserTable.password
    var email by UserTable.email
    var phone by UserTable.phone
    var avatar by UserTable.avatar
    var createTime by UserTable.createTime
    var updateTime by UserTable.updateTime
}