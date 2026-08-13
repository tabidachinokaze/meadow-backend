package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.model.Response
import moe.tabidachi.meadow.model.UserInfo
import moe.tabidachi.meadow.model.request.UpdatePasswordRequest
import moe.tabidachi.meadow.model.request.UpdateUserInfoRequest

interface UserService {
    suspend fun getUserInfo(callingUserId: Long?, targetUserId: Long): Response<UserInfo?>
    suspend fun getContracts(callingUserId: Long): Response<List<UserInfo>>
    suspend fun updateUserInfo(
        callingUserId: Long,
        targetUserId: Long,
        request: UpdateUserInfoRequest
    ): Response<UserInfo?>

    suspend fun updatePassword(
        callingUserId: Long,
        targetUserId: Long,
        request: UpdatePasswordRequest
    ): Response<String?>

    suspend fun updateEmail(
        callingUserId: Long,
        targetUserId: Long,
        request: UpdatePasswordRequest
    ): Response<UserInfo?>
}
