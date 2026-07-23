package moe.tabidachi.meadow.mapper

import moe.tabidachi.meadow.database.entity.UserEntity
import moe.tabidachi.meadow.database.model.User
import moe.tabidachi.meadow.model.UserInfo

object UserMapper {
    fun toUser(entity: UserEntity): User {
        return User(
            uid = entity.id.value,
            username = entity.username,
            password = entity.password,
            email = entity.email,
            phone = entity.phone,
            avatar = entity.avatar,
            createTime = entity.createTime,
            updateTime = entity.updateTime
        )
    }

    fun toUserInfo(entity: UserEntity): UserInfo {
        return UserInfo(
            uid = entity.id.value,
            username = entity.username,
            email = entity.email,
            phone = entity.phone,
            avatar = entity.avatar,
            createTime = entity.createTime,
            updateTime = entity.updateTime
        )
    }
}
