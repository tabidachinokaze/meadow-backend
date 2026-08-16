package moe.tabidachi.meadow.security

import io.github.domgew.kedis.KedisClient
import io.github.domgew.kedis.arguments.value.SetOptions
import io.github.domgew.kedis.commands.KedisValueCommands
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

interface CaptchaValidator {
    suspend fun generate(key: String): String
    suspend fun validate(key: String, value: String): ValidationResult
    /** 只读取验证码（不消费、不删除），用于避免重复请求覆盖 */
    suspend fun peek(key: String): String?

    enum class ValidationResult {
        CORRECT, ERROR, EXPIRED
    }
}

class CaptchaValidatorRedisImpl(
    private val kedisClient: KedisClient,
    private val ttl: Duration = 5.minutes,
    private val codeLength: Int = 6,
) : CaptchaValidator {
    override suspend fun generate(key: String): String {
        val code = generateRandomCode(codeLength)
        kedisClient.execute(
            KedisValueCommands.set(
                key = key,
                options = SetOptions(
                    expire = SetOptions.ExpireOption.ExpiresInMilliseconds(ttl.inWholeMilliseconds)
                ),
                value = code
            )
        )
        return code
    }

    override suspend fun validate(
        key: String,
        value: String
    ): CaptchaValidator.ValidationResult {
        val code: String? = kedisClient.execute(KedisValueCommands.get(key))
        return when (code) {
            null -> CaptchaValidator.ValidationResult.EXPIRED
            value -> {
                kedisClient.execute(KedisValueCommands.del(key))
                CaptchaValidator.ValidationResult.CORRECT
            }

            else -> CaptchaValidator.ValidationResult.ERROR
        }
    }

    override suspend fun peek(key: String): String? =
        kedisClient.execute(KedisValueCommands.get(key))

    private fun generateRandomCode(length: Int): String {
        return (1..length.coerceAtLeast(1))
            .map { Random.nextInt(0, 10) }
            .joinToString("")
    }
}
