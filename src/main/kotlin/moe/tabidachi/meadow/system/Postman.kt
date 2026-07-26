package moe.tabidachi.meadow.system

import com.resend.Resend
import com.resend.services.emails.model.CreateEmailOptions

interface Postman {
    fun sendVerificationCode(
        recipient: String,
        code: String,
    ): Result<String>
}

class PostmanResendImpl(
    private val resend: Resend
) : Postman {
    override fun sendVerificationCode(recipient: String, code: String): Result<String> = runCatching {
        val htmlContent = (object {}.javaClass.classLoader)
            ?.getResource("email-verification.html")
            ?.readText()
            ?.replace("{{code}}", code) ?: error("email-verification.html not found")
        val options = CreateEmailOptions.builder()
            .from("Meadow <meadow@tabidachi.moe>")
            .to(recipient)
            .subject("[Meadow] 您的验证码")
            .html(htmlContent)
            .build()

        val response = resend.emails().send(options)
        response.id
    }
}

class PostmanTestImpl : Postman {
    override fun sendVerificationCode(recipient: String, code: String): Result<String> {
        return Result.success(code)
    }
}