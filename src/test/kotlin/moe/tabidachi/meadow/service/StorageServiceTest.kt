package moe.tabidachi.moe.tabidachi.meadow.service

import io.ktor.server.config.*
import io.ktor.server.plugins.di.*
import io.ktor.server.testing.*
import moe.tabidachi.meadow.plugins.configureDI
import moe.tabidachi.meadow.service.StorageService
import moe.tabidachi.moe.tabidachi.meadow.resource
import kotlin.test.Test

class StorageServiceTest {
    @Test
    fun testUploadAvatar() = testApplication {
        environment {
            config = configLoaders.firstNotNullOf {
                it.load("application.yaml")
            }
        }
        application.configureDI()
        val storageService = application.dependencies.resolve<StorageService>()
        val bytes = resource("transparent_akkarin.jpg").readBytes()
        val uploadAvatar = storageService.uploadAvatar(
            bytes = bytes,
            fileName = "transparent_akkarin.jpg",
        )
        println(uploadAvatar)
    }
}