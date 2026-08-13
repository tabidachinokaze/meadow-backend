package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.model.*
import moe.tabidachi.meadow.model.request.CodeLoginRequest
import moe.tabidachi.meadow.model.request.PasswordLoginRequest
import moe.tabidachi.meadow.model.request.RegisterRequest
import moe.tabidachi.meadow.regex.RegexEmail
import moe.tabidachi.meadow.regex.RegexUsernameStrict
import moe.tabidachi.meadow.repository.UserRepository
import moe.tabidachi.meadow.security.CaptchaValidator
import moe.tabidachi.meadow.security.Encryptor
import moe.tabidachi.meadow.security.Jwt
import moe.tabidachi.meadow.system.Postman
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaInstant

class AuthServiceImpl(
    private val jwt: Jwt,
    private val encryptor: Encryptor,
    private val userRepository: UserRepository,
    private val postman: Postman,
    private val captchaValidator: CaptchaValidator
) : AuthService {
    override suspend fun register(request: RegisterRequest): Response<String?> {
        return when {
            !RegexEmail.matches(request.email) -> ValidStatusCode.INVALID_EMAIL.emptyData()
            request.password.length < 8 -> UserStatusCode.PASSWORD_TOO_WEAK.emptyData()
            request.username.length !in 2..32 || !RegexUsernameStrict.matches(request.username) -> ValidStatusCode.INVALID_USERNAME.emptyData()
            userRepository.getByEmail(request.email) != null -> UserStatusCode.EMAIL_ALREADY_EXISTS.emptyData()
            userRepository.getByUsername(request.username) != null -> UserStatusCode.USERNAME_ALREADY_EXISTS.emptyData()
            else -> when (captchaValidator.validate("email:code:${request.email}", request.verificationCode)) {
                CaptchaValidator.ValidationResult.ERROR -> UserStatusCode.VERIFICATION_CODE_ERROR.emptyData()
                CaptchaValidator.ValidationResult.EXPIRED -> UserStatusCode.VERIFICATION_CODE_EXPIRED.emptyData()
                CaptchaValidator.ValidationResult.CORRECT -> {
                    val uid = userRepository.create(
                        username = request.username,
                        email = request.email,
                        password = request.password,
                        //gameId = request.gameId
                    )
                    val token = jwt.sign(uid) {
                        withExpiresAt(Clock.System.now().plus(7.days).toJavaInstant())
                    }
                    UserStatusCode.SIGN_UP_SUCCESS.withData(token)
                }
            }
        }
    }

    override suspend fun loginByPassword(request: PasswordLoginRequest): Response<String?> {
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
                    request.password
                ) -> UserStatusCode.PASSWORD_INCORRECT.emptyData()

                else -> {
                    val token = jwt.sign(user.uid) {
                        withExpiresAt(Clock.System.now().plus(7.days).toJavaInstant())
                    }
                    userRepository.updateLastLogin(user.uid)
                    UserStatusCode.LOGIN_SUCCESS.withData(token)
                }
            }
        }
    }

    override suspend fun loginByCode(request: CodeLoginRequest): Response<String?> {
        return when {
            !RegexEmail.matches(request.email) -> ValidStatusCode.INVALID_EMAIL.emptyData()
            else -> when (captchaValidator.validate("email:code:${request.email}", request.verificationCode)) {
                CaptchaValidator.ValidationResult.ERROR -> UserStatusCode.VERIFICATION_CODE_ERROR.emptyData()
                CaptchaValidator.ValidationResult.EXPIRED -> UserStatusCode.VERIFICATION_CODE_EXPIRED.emptyData()
                CaptchaValidator.ValidationResult.CORRECT -> {
                    val user = userRepository.getByEmail(request.email)
                    if (user == null) {
                        UserStatusCode.USER_NOT_REGISTERED.emptyData()
                    } else {
                        val token = jwt.sign(user.uid) {
                            withExpiresAt(Clock.System.now().plus(7.days).toJavaInstant())
                        }
                        userRepository.updateLastLogin(user.uid)
                        UserStatusCode.LOGIN_SUCCESS.withData(token)
                    }
                }
            }
        }
    }

    override suspend fun sendRegisterCode(email: String): Response<String?> {
        return when {
            !RegexEmail.matches(email) -> ValidStatusCode.INVALID_EMAIL.emptyData()
            userRepository.getByEmail(email) != null -> UserStatusCode.EMAIL_ALREADY_EXISTS.emptyData()
            else -> sendCode(email)
        }
    }

    override suspend fun sendLoginCode(email: String): Response<String?> {
        return when {
            !RegexEmail.matches(email) -> ValidStatusCode.INVALID_EMAIL.emptyData()
            userRepository.getByEmail(email) == null -> UserStatusCode.USER_NOT_REGISTERED.emptyData()
            else -> sendCode(email)
        }
    }

    private suspend fun sendCode(email: String): Response<String?> {
        val code = captchaValidator.generate("email:code:${email}")
        val result = postman.sendVerificationCode(
            recipient = email,
            code = code
        )
        return if (result.isSuccess) {
            CommonStatusCode.SUCCESS.withData(result.getOrNull())
        } else {
            CommonStatusCode.INTERNAL_SERVER_ERROR.emptyData(message = result.exceptionOrNull()?.message)
        }
    }
}