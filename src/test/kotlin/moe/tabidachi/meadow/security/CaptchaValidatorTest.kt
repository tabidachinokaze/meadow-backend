package moe.tabidachi.moe.tabidachi.meadow.security

import io.github.domgew.kedis.KedisClient
import io.github.domgew.kedis.KedisConfiguration
import io.ktor.test.dispatcher.*
import kotlinx.coroutines.delay
import moe.tabidachi.meadow.model.config.RedisConfig
import moe.tabidachi.meadow.security.CaptchaValidator
import moe.tabidachi.meadow.security.CaptchaValidatorRedisImpl
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class CaptchaValidatorTest {
    private val redisConfig: RedisConfig = RedisConfig(
        host = "mac.lan",
        port = 6379,
        username = null,
        password = "12345678"
    )
    private val validator: CaptchaValidator = CaptchaValidatorRedisImpl(
        kedisClient = KedisClient(
            configuration = KedisConfiguration(
                endpoint = KedisConfiguration.Endpoint.HostPort(
                    host = redisConfig.host,
                    port = redisConfig.port
                ),
                authentication = if (redisConfig.password != null) {
                    KedisConfiguration.Authentication.AutoAuth(
                        password = redisConfig.password,
                        username = redisConfig.username,
                    )
                } else {
                    KedisConfiguration.Authentication.NoAutoAuth
                },
                connectionTimeout = 1.seconds
            )
        ),
        ttl = 5.seconds,
    )

    @Test
    fun testExpire() = testSuspend {
        val key = "email:code:user@example.com"
        val code = validator.generate(key)
        println("code: $code")
        delay(2.seconds)
        validator.generate(key)
        val result = validator.validate(key, code)
        println(result)
        println(validator.validate(key, code))
    }
}