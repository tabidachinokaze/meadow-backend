package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.model.UserInfo
import moe.tabidachi.meadow.model.Response

interface UserService {
    suspend fun getUserInfo(uid: Long, self: Boolean): Response<UserInfo?>
    suspend fun getContracts(uid: Long): Response<List<UserInfo>>
}
