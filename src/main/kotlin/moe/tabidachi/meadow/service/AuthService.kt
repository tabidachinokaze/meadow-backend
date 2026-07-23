package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.model.request.LoginRequest
import moe.tabidachi.meadow.model.request.SignupRequest
import moe.tabidachi.meadow.model.Response

interface AuthService {
    suspend fun signup(request: SignupRequest): Response<String?>
    suspend fun login(request: LoginRequest): Response<String?>
}