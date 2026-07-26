package moe.tabidachi.meadow.contract

import io.ktor.server.plugins.ratelimit.*

object RateLimitNames {
    val email = RateLimitName("email-rate-limit")
}