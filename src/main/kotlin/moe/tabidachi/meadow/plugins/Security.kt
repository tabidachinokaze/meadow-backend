package moe.tabidachi.meadow.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.server.plugins.di.*
import moe.tabidachi.meadow.contract.AuthenticationNames
import moe.tabidachi.meadow.model.config.JwtConfig
import moe.tabidachi.meadow.security.Jwt

fun Application.configureSecurity() {
    val jwt: Jwt by dependencies
    val jwtConfig = property<JwtConfig>("jwt")
    authentication {
        jwt(jwtConfig.name) {
            realm = jwtConfig.realm
            verifier(jwt.verifier)
            validate { credential ->
                if (credential.payload.audience.contains(jwtConfig.audience)) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
        register(EmptyAuthenticationProvider(AuthenticationNames.NONE))
    }
}

class EmptyAuthenticationProvider(name: String) : AuthenticationProvider(DynamicProviderConfig(name)) {
    override suspend fun onAuthenticate(context: AuthenticationContext) {
        context.principal("none")
    }
}
