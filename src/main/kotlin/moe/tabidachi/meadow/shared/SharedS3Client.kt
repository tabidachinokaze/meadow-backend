package moe.tabidachi.meadow.shared

import aws.sdk.kotlin.services.s3.S3Client
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import aws.smithy.kotlin.runtime.collections.Attributes
import aws.smithy.kotlin.runtime.net.Host
import aws.smithy.kotlin.runtime.net.Scheme
import aws.smithy.kotlin.runtime.net.url.Url
import moe.tabidachi.meadow.model.config.S3Config

private fun credentialProvider(s3Config: S3Config) = object : CredentialsProvider {
    override suspend fun resolve(attributes: Attributes): Credentials {
        return Credentials(
            accessKeyId = s3Config.accessKey,
            secretAccessKey = s3Config.secretKey
        )
    }
}

/**
 * 实际上传/下载用的 S3Client（连接 endpoint）。
 * - 生产 443 反代：host=storage.tabidachi.moe, port=443, scheme=https
 * - 自建内网直连：host=storage.tabidachi.moe, port=9000, scheme=http（extra_hosts 解析到内网 IP）
 */
@Suppress("FunctionName")
fun SharedS3Client(s3Config: S3Config): S3Client {
    return S3Client {
        this.region = "us-east-1"
        this.endpointUrl = Url {
            this.scheme = if (s3Config.scheme.equals("http", ignoreCase = true)) Scheme.HTTP else Scheme.HTTPS
            this.host = Host.Domain(s3Config.host)
            this.port = s3Config.port
        }
        // 自建 S3 兼容服务（RustFS/MinIO）使用 path-style 寻址
        this.forcePathStyle = true
        this.credentialsProvider = credentialProvider(s3Config)
    }
}

/**
 * 预签名专用 S3Client：使用公开访问地址（public_* 或回退连接地址）。
 *
 * 关键：AWS SigV4 签名的 Host 头包含端口（非标准端口），若用内网 endpoint（如 :9000）签名，
 * 浏览器访问公网域名（:443，标准端口省略）时 Host 不一致 → SignatureDoesNotMatch。
 * 因此预签名必须用与浏览器完全一致的公开地址（标准端口 → Host 头无端口）。
 * presign 为纯本地计算（不实际发请求），即使容器无法直连公网也不影响。
 */
@Suppress("FunctionName")
fun SharedPresignS3Client(s3Config: S3Config): S3Client {
    val scheme = s3Config.publicScheme?.takeIf { it.isNotBlank() } ?: s3Config.scheme
    val host = s3Config.publicHost?.takeIf { it.isNotBlank() } ?: s3Config.host
    val port = (s3Config.publicPort ?: 0).takeIf { it > 0 } ?: s3Config.port
    return S3Client {
        this.region = "us-east-1"
        this.endpointUrl = Url {
            this.scheme = if (scheme.equals("http", ignoreCase = true)) Scheme.HTTP else Scheme.HTTPS
            this.host = Host.Domain(host)
            this.port = port
        }
        this.forcePathStyle = true
        this.credentialsProvider = credentialProvider(s3Config)
    }
}
