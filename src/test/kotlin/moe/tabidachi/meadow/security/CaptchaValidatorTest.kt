package moe.tabidachi.moe.tabidachi.meadow.security

import io.github.domgew.kedis.KedisClient
import io.github.domgew.kedis.KedisConfiguration
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.coroutines.delay
import moe.tabidachi.meadow.model.config.RedisConfig
import moe.tabidachi.meadow.security.CaptchaValidator
import moe.tabidachi.meadow.security.CaptchaValidatorRedisImpl
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class CaptchaValidatorTest {
    @Test
    fun testExpire() = testApplication {
        environment {
            config = configLoaders.firstNotNullOf {
                it.load("application.yaml")
            }
        }
        val redisConfig = application.property<RedisConfig>("redis")
        val validator: CaptchaValidator = CaptchaValidatorRedisImpl(
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