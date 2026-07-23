package moe.tabidachi.meadow.plugins

import aws.sdk.kotlin.services.s3.S3Client
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import aws.smithy.kotlin.runtime.collections.Attributes
import aws.smithy.kotlin.runtime.net.Host
import aws.smithy.kotlin.runtime.net.Scheme
import aws.smithy.kotlin.runtime.net.url.Url
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.plugins.di.*
import moe.tabidachi.meadow.database.table.UserRelationTable
import moe.tabidachi.meadow.database.table.UserTable
import moe.tabidachi.meadow.model.config.Argon2Config
import moe.tabidachi.meadow.model.config.DatabaseConfig
import moe.tabidachi.meadow.model.config.JwtConfig
import moe.tabidachi.meadow.model.config.S3Config
import moe.tabidachi.meadow.repository.UserRelationRepository
import moe.tabidachi.meadow.repository.UserRelationRepositoryImpl
import moe.tabidachi.meadow.repository.UserRepository
import moe.tabidachi.meadow.repository.UserRepositoryImpl
import moe.tabidachi.meadow.security.Argon2Encryptor
import moe.tabidachi.meadow.security.Encryptor
import moe.tabidachi.meadow.security.Jwt
import moe.tabidachi.meadow.security.JwtImpl
import moe.tabidachi.meadow.service.AuthService
import moe.tabidachi.meadow.service.AuthServiceImpl
import moe.tabidachi.meadow.service.UserService
import moe.tabidachi.meadow.service.UserServiceImpl
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

fun Application.configureDI() {
    val argon2Config = property<Argon2Config>("argon2")
    val jwtConfig = property<JwtConfig>("jwt")
    val s3Config = property<S3Config>("s3")
    dependencies {
        provide<Encryptor> {
            Argon2Encryptor(
                iterations = argon2Config.iterations,
                memory = argon2Config.memory,
                parallelism = argon2Config.parallelism
            )
        }
        provide<Jwt> {
            JwtImpl(
                secret = jwtConfig.secret,
                issuer = jwtConfig.issuer,
                audience = arrayOf(jwtConfig.audience)
            )
        }
        provide<S3Client> {
            S3Client {
                this.region = "us-east-1"
                this.endpointUrl = Url {
                    this.scheme = Scheme.HTTP
                    this.host = Host.Domain(s3Config.host)
                    this.port = s3Config.port
                }
                this.credentialsProvider = object : CredentialsProvider {
                    override suspend fun resolve(attributes: Attributes): Credentials {
                        return Credentials(
                            accessKeyId = s3Config.accessKey,
                            secretAccessKey = s3Config.secretKey
                        )
                    }
                }
            }
        }
    }
    val mode = if (property<Boolean>("ktor.development")) "test" else "main"
    val (url, user, driver, password) = property<DatabaseConfig>("database.$mode")
    dependencies {
        provide<Database> {
            Database.connect(url, driver, user, password).also { db ->
                transaction(db) {
                    SchemaUtils.create(UserTable, UserRelationTable)
                }
            }
        }
    }
    dependencies {
        provide<UserRepository> {
            UserRepositoryImpl(
                database = resolve(),
                encryptor = resolve()
            )
        }
        provide<UserRelationRepository> {
            UserRelationRepositoryImpl(
                database = resolve()
            )
        }
    }
    dependencies {
        provide<AuthService> {
            AuthServiceImpl(
                jwt = resolve(),
                encryptor = resolve(),
                userRepository = resolve()
            )
        }
        provide<UserService> {
            UserServiceImpl(
                userRepository = resolve(),
                userRelationRepository = resolve(),
            )
        }
    }
}
