package moe.tabidachi.moe.tabidachi.meadow

import io.ktor.util.generateNonceBlocking
import kotlin.test.Test

class SecretGenerator {
    @Test
    fun generateSecret() {
        val string = generateNonceBlocking(32)
        println(string)
    }
}