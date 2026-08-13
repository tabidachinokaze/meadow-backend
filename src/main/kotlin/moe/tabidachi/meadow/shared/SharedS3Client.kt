package moe.tabidachi.meadow.shared

import aws.sdk.kotlin.services.s3.S3Client
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import aws.smithy.kotlin.runtime.collections.Attributes
import aws.smithy.kotlin.runtime.net.Host
import aws.smithy.kotlin.runtime.net.Scheme
import aws.smithy.kotlin.runtime.net.url.Url
import moe.tabidachi.meadow.model.config.S3Config

@Suppress("FunctionName")
fun SharedS3Client(s3Config: S3Config): S3Client {
    return S3Client {
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