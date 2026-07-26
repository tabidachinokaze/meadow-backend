package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.model.Response
import moe.tabidachi.meadow.model.request.CodeLoginRequest
import moe.tabidachi.meadow.model.request.PasswordLoginRequest
import moe.tabidachi.meadow.model.request.RegisterRequest

interface AuthService {
    suspend fun register(request: RegisterRequest): Response<String?>
    suspend fun loginByPassword(request: PasswordLoginRequest): Response<String?>
    suspend fun loginByCode(request: CodeLoginRequest): Response<String?>
    suspend fun sendRegisterCode(email: String): Response<String?>
    suspend fun sendLoginCode(email: String): Response<String?>
}