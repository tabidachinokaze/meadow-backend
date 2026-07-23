package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.model.*
import moe.tabidachi.meadow.model.request.LoginRequest
import moe.tabidachi.meadow.model.request.SignupRequest
import moe.tabidachi.meadow.regex.RegexEmail
import moe.tabidachi.meadow.regex.RegexUsernameStrict
import moe.tabidachi.meadow.repository.UserRepository
import moe.tabidachi.meadow.security.Encryptor
import moe.tabidachi.meadow.security.Jwt
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaInstant

class AuthServiceImpl(
    private val jwt: Jwt,
    private val encryptor: Encryptor,
    private val userRepository: UserRepository
) : AuthService {
    override suspend fun signup(request: SignupRequest): Response<String?> {
        return when {
            !RegexEmail.matches(request.email) -> ValidStatusCode.INVALID_EMAIL.emptyData()
            request.password.length < 8 -> UserStatusCode.PASSWORD_TOO_WEAK.emptyData()
            request.username.length !in 2..32 || !RegexUsernameStrict.matches(request.username) -> ValidStatusCode.INVALID_USERNAME.emptyData()
            userRepository.getByEmail(request.email) != null -> UserStatusCode.EMAIL_ALREADY_EXISTS.emptyData()
            userRepository.getByUsername(request.username) != null -> UserStatusCode.USERNAME_ALREADY_EXISTS.emptyData()
            else -> {
                val uid = userRepository.create(
                    username = request.username,
                    email = request.email,
                    password = request.password
                )
                val token = jwt.sign(uid) {
                    withExpiresAt(Clock.System.now().plus(7.days).toJavaInstant())
                }
                UserStatusCode.SIGN_UP_SUCCESS.withData(token)
            }
        }
    }

    override suspend fun login(request: LoginRequest): Response<String?> {
        val isEmail = RegexEmail.matches(request.account)
        val user = if (isEmail) {
            userRepository.getByEmail(request.account)
        } else {
            userRepository.getByUsername(request.account)
        }
        return when (user) {
            null -> UserStatusCode.USER_NOT_REGISTERED.emptyData()
            else -> when {
                // !RegexEmail.matches(request.account) -> StatusCode.InvalidEmail.emptyData()
                request.password.length < 8 -> UserStatusCode.PASSWORD_TOO_WEAK.emptyData()
                !encryptor.verify(
                    user.password,
                    request.password.toCharArray()
                ) -> UserStatusCode.PASSWORD_INCORRECT.emptyData()

                else -> {
                    val token = jwt.sign(user.uid) {
                        withExpiresAt(Clock.System.now().plus(7.days).toJavaInstant())
                    }
                    UserStatusCode.LOGIN_SUCCESS.withData(token)
                }
            }
        }
    }
}