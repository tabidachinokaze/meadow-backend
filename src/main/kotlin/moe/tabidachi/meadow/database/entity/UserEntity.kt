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
    var avatarUrl by UserTable.avatarUrl
    var bannerUrl by UserTable.bannerUrl
    var gameId by UserTable.gameId
    var role by UserTable.role
    var isActive by UserTable.isActive
    var tokenVersion by UserTable.tokenVersion
    var lastLogin by UserTable.lastLogin
    var createdAt by UserTable.createdAt
    var updatedAt by UserTable.updatedAt
    var bio by UserTable.bio
    var website by UserTable.website
    var location by UserTable.location
}