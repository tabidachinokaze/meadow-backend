package moe.tabidachi.meadow.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import moe.tabidachi.meadow.contract.RateLimitNames
import moe.tabidachi.meadow.exception.MissingParameterException
import kotlin.time.Duration.Companion.minutes

fun Application.configureRateLimit() {
    install(RateLimit) {
        register(RateLimitNames.email) {
            rateLimiter(limit = 1, refillPeriod = 1.minutes)
            requestKey { call ->
                call.request.queryParameters["email"] ?: throw MissingParameterException("email")
            }
        }
    }
}