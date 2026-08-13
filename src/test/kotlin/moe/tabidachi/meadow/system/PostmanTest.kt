package moe.tabidachi.moe.tabidachi.meadow.system

import io.ktor.server.config.*
import io.ktor.server.plugins.di.*
import io.ktor.server.testing.*
import moe.tabidachi.meadow.plugins.configureDI
import moe.tabidachi.meadow.system.Postman
import kotlin.test.Test

class PostmanTest {
    @Test
    fun testSendEmail() = testApplication {
        environment {
            config = configLoaders.firstNotNullOf {
                it.load("application.yaml")
            }
        }
        application.configureDI()
        val postman = application.dependencies.resolve<Postman>()
        val result = postman.sendVerificationCode("test@example.com", "114514")
        println(result)
    }
}