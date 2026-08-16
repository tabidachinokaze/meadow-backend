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
    /** 邮箱换绑：向新邮箱发送验证码（校验格式 + 未被占用） */
    suspend fun sendEmailRebindCode(email: String): Response<String?>
    /** 密码重置：向邮箱发送验证码（校验邮箱已注册） */
    suspend fun sendResetPasswordCode(email: String): Response<String?>
    /** 密码重置：验证码校验后更新密码 */
    suspend fun resetPassword(email: String, verificationCode: String, newPassword: String): Response<String?>
}