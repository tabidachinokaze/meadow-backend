package moe.tabidachi.moe.tabidachi.meadow.system

import com.resend.Resend
import kotlinx.coroutines.test.runTest
import moe.tabidachi.meadow.system.PostmanResendImpl
import kotlin.test.Test

class PostmanTest {
    private val postman = PostmanResendImpl(
        resend = Resend("")
    )
    @Test
    fun testSendEmail() = runTest {
        val result = postman.sendVerificationCode("", "114514")
        println(result)
    }
}