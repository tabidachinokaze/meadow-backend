package moe.tabidachi.moe.tabidachi.meadow.security

import io.ktor.util.*
import moe.tabidachi.meadow.security.AesEncryptor
import org.junit.Assert.assertEquals
import kotlin.test.Test

class AesEncryptorTest {
    private val encryptor = AesEncryptor(generateNonceBlocking(32))

    @Test
    fun testAesEncryptor() {
        val plainText = "114514"
        val encrypted = encryptor.encrypt(plainText)
        val decrypted = encryptor.decrypt(encrypted)
        println("encrypted: $encrypted")
        println("decrypted: $decrypted")
        println("plainText: $plainText")
        println(encryptor.verify(encrypted, plainText))
        assertEquals(plainText, decrypted)
    }
}