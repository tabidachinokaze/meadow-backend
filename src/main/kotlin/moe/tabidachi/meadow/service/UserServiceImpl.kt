package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.model.*
import moe.tabidachi.meadow.repository.UserRelationRepository
import moe.tabidachi.meadow.repository.UserRepository

class UserServiceImpl(
    private val userRepository: UserRepository,
    private val userRelationRepository: UserRelationRepository
) : UserService {
    override suspend fun getUserInfo(uid: Long, self: Boolean): Response<UserInfo?> {
        val userInfo = userRepository.getUserInfo(uid)
        return if (userInfo == null) {
            UserStatusCode.USER_NOT_FOUND.withData(userInfo)
        } else {
            val sensitiveUserInfo = if (self) {
                userInfo
            } else {
                userInfo.desensitize()
            }
            CommonStatusCode.SUCCESS.withData(sensitiveUserInfo)
        }
    }

    override suspend fun getContracts(uid: Long): Response<List<UserInfo>> {
        val userInfos = userRelationRepository.getByUserId(uid).filter {
            it.status == RelationStatus.ACTIVE
        }.mapNotNull {
            userRepository.getUserInfo(it.targetUserId)
        }
        return CommonStatusCode.SUCCESS.withData(userInfos)
    }
}