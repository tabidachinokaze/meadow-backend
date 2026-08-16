package moe.tabidachi.meadow.service

import moe.tabidachi.meadow.model.*
import moe.tabidachi.meadow.model.request.RebindEmailRequest
import moe.tabidachi.meadow.model.request.UpdatePasswordRequest
import moe.tabidachi.meadow.model.request.UpdateUserInfoRequest
import moe.tabidachi.meadow.regex.RegexEmail
import moe.tabidachi.meadow.regex.RegexUsernameStrict
import moe.tabidachi.meadow.repository.UserRelationRepository
import moe.tabidachi.meadow.repository.UserRepository
import moe.tabidachi.meadow.security.CaptchaValidator
import moe.tabidachi.meadow.security.Encryptor

class UserServiceImpl(
    private val userRepository: UserRepository,
    private val userRelationRepository: UserRelationRepository,
    private val encryptor: Encryptor,
    private val captchaValidator: CaptchaValidator,
) : UserService {
    override suspend fun getUserInfo(callingUserId: Long?, targetUserId: Long): Response<UserInfo?> {
        val userInfo = userRepository.getUserInfo(targetUserId)
        return if (userInfo == null) {
            UserStatusCode.USER_NOT_FOUND.withData(userInfo)
        } else {
            val sensitiveUserInfo = if (callingUserId == targetUserId) {
                userInfo
            } else {
                userInfo.desensitize()
            }
            CommonStatusCode.SUCCESS.withData(sensitiveUserInfo)
        }
    }

    override suspend fun getContracts(callingUserId: Long): Response<List<UserInfo>> {
        val userInfos = userRelationRepository.getByUserId(callingUserId).filter {
            it.status == RelationStatus.ACTIVE
        }.mapNotNull {
            userRepository.getUserInfo(it.targetUserId)
        }
        return CommonStatusCode.SUCCESS.withData(userInfos)
    }

    override suspend fun updateUserInfo(
        callingUserId: Long,
        targetUserId: Long,
        request: UpdateUserInfoRequest
    ): Response<UserInfo?> {
        return if (callingUserId == targetUserId) {
            if (request.isEmpty()) {
                return CommonStatusCode.FAILURE.emptyData()
            }
            // 更新路径复用注册校验规则（已知问题 #15）
            if (request.email != null && !RegexEmail.matches(request.email)) {
                return ValidStatusCode.INVALID_EMAIL.emptyData()
            }
            if (request.username != null &&
                (request.username.length !in 2..32 || !RegexUsernameStrict.matches(request.username))
            ) {
                return ValidStatusCode.INVALID_USERNAME.emptyData()
            }
            val user = userRepository.getByUid(targetUserId) ?: return UserStatusCode.USER_NOT_FOUND.emptyData()
            val userByUsername = request.username?.let { userRepository.getByUsername(it) }
            if (userByUsername != null && user.uid != userByUsername.uid && user.username == userByUsername.username) {
                return UserStatusCode.USERNAME_ALREADY_EXISTS.emptyData()
            }
            val userByEmail = request.email?.let { userRepository.getByEmail(it) }
            if (userByEmail != null && user.uid != userByEmail.uid && user.email == userByEmail.email) {
                return UserStatusCode.EMAIL_ALREADY_EXISTS.emptyData()
            }
            val userByPhone = request.phone?.let { userRepository.getByPhone(it) }
            if (userByPhone != null && user.uid != userByPhone.uid && user.phone == userByPhone.phone) {
                return UserStatusCode.PHONE_ALREADY_REGISTERED.emptyData()
            }
            val result = userRepository.updateUserInfo(
                callingUserId = callingUserId,
                username = request.username,
                email = request.email,
                phone = request.phone,
                avatarUrl = request.avatarUrl,
                bio = request.bio,
                website = request.website,
                location = request.location
            )
            if (result) {
                getUserInfo(callingUserId, targetUserId)
            } else {
                CommonStatusCode.FAILURE.emptyData()
            }
        } else {
            CommonStatusCode.FORBIDDEN.emptyData()
        }
    }

    override suspend fun updatePassword(
        callingUserId: Long,
        targetUserId: Long,
        request: UpdatePasswordRequest
    ): Response<String?> {
        return when {
            callingUserId != targetUserId -> CommonStatusCode.FORBIDDEN.emptyData()
            else -> {
                val user = userRepository.getByUid(targetUserId)
                when {
                    user == null -> UserStatusCode.USER_NOT_FOUND.emptyData()
                    encryptor.verify(user.password, request.oldPassword) -> {
                        userRepository.updatePassword(targetUserId, request.newPassword)
                        CommonStatusCode.SUCCESS.emptyData()
                    }

                    else -> UserStatusCode.PASSWORD_INCORRECT.emptyData()
                }
            }
        }
    }

    override suspend fun updateEmail(
        callingUserId: Long,
        targetUserId: Long,
        request: RebindEmailRequest
    ): Response<UserInfo?> {
        return when {
            callingUserId != targetUserId -> CommonStatusCode.FORBIDDEN.emptyData()
            !RegexEmail.matches(request.newEmail) -> ValidStatusCode.INVALID_EMAIL.emptyData()
            userRepository.getByEmail(request.newEmail) != null ->
                UserStatusCode.EMAIL_ALREADY_EXISTS.emptyData()

            else -> when (
                captchaValidator.validate("email:code:REBIND:${request.newEmail}", request.verificationCode)
            ) {
                CaptchaValidator.ValidationResult.ERROR ->
                    UserStatusCode.VERIFICATION_CODE_ERROR.emptyData()

                CaptchaValidator.ValidationResult.EXPIRED ->
                    UserStatusCode.VERIFICATION_CODE_EXPIRED.emptyData()

                CaptchaValidator.ValidationResult.CORRECT -> {
                    if (userRepository.updateUserInfo(callingUserId, email = request.newEmail)) {
                        getUserInfo(callingUserId, targetUserId)
                    } else {
                        CommonStatusCode.FAILURE.emptyData()
                    }
                }
            }
        }
    }

    override suspend fun deactivateAccount(callingUserId: Long, targetUserId: Long): Response<Long?> {
        return when {
            callingUserId != targetUserId -> CommonStatusCode.FORBIDDEN.emptyData()
            userRepository.getByUid(targetUserId) == null -> UserStatusCode.USER_NOT_FOUND.emptyData()
            userRepository.deactivate(targetUserId) -> CommonStatusCode.SUCCESS.withData(targetUserId)
            else -> CommonStatusCode.FAILURE.emptyData()
        }
    }
}