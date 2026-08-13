package moe.tabidachi.meadow.security

import de.mkammerer.argon2.Argon2
import de.mkammerer.argon2.Argon2Factory
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64

interface Encryptor {
    fun encrypt(plainText: String): String
    fun decrypt(cipherText: String): String
    fun verify(cipherText: String, plainText: String): Boolean
}

class Argon2Encryptor(
    private val iterations: Int = 8,
    private val memory: Int = 1024 * 64,
    private val parallelism: Int = 1,
) : Encryptor {
    private val argon2: Argon2 = Argon2Factory.create()

    override fun encrypt(plainText: String): String {
        return argon2.hash(iterations, memory, parallelism, plainText.toCharArray())
    }

    override fun decrypt(cipherText: String): String {
        error("Argon2 is a one-way hash function, cannot decrypt")
    }

    override fun verify(cipherText: String, plainText: String): Boolean {
        return argon2.verify(cipherText, plainText.toCharArray())
    }
}

class AesEncryptor(
    secretKey: String
) : Encryptor {
    private val keyBytes = secretKey.hexToByteArray()
    private val keySpec = SecretKeySpec(keyBytes, "AES")

    companion object {
        private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
        private const val IV_SIZE = 16 // AES 块大小 16 字节
        private val RANDOM = SecureRandom()
    }

    override fun encrypt(plainText: String): String {
        // 1. 生成随机 IV
        val iv = ByteArray(IV_SIZE)
        RANDOM.nextBytes(iv)
        val ivSpec = IvParameterSpec(iv)

        // 2. 初始化加密器
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)

        // 3. 加密
        val encrypted = cipher.doFinal(plainText.encodeToByteArray())

        // 4. 合并 IV + 密文，然后 Base64 编码
        val combined = iv + encrypted
        return Base64.encode(combined)
    }

    override fun decrypt(cipherText: String): String {
        // 1. Base64 解码
        val combined = Base64.decode(cipherText)

        // 2. 提取 IV（前 16 字节）
        val iv = combined.copyOfRange(0, IV_SIZE)
        val encrypted = combined.copyOfRange(IV_SIZE, combined.size)

        // 3. 初始化解密器
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(iv))

        // 4. 解密
        val decrypted = cipher.doFinal(encrypted)
        return decrypted.decodeToString()
    }

    override fun verify(cipherText: String, plainText: String): Boolean {
        return try {
            val decrypted = decrypt(cipherText)
            decrypted == plainText
        } catch (e: Exception) {
            false
        }
    }
}

class RsaEncryptor(
    private val privateKey: PrivateKey? = null,
    private val publicKey: PublicKey? = null
) : Encryptor {

    companion object {
        private const val ALGORITHM = "RSA"
        private const val TRANSFORMATION = "RSA/ECB/PKCS1Padding"
        private const val SIGNATURE_ALGORITHM = "SHA256withRSA"
        private const val KEY_SIZE = 2048

        // 工厂方法：从字符串加载密钥
        fun fromPemStrings(privateKeyPem: String? = null, publicKeyPem: String? = null): RsaEncryptor {
            val privateKey = privateKeyPem?.let { loadPrivateKeyFromPem(it) }
            val publicKey = publicKeyPem?.let { loadPublicKeyFromPem(it) }
            return RsaEncryptor(privateKey, publicKey)
        }

        // 从 PEM 字符串加载私钥
        fun loadPrivateKeyFromPem(pemString: String): PrivateKey {
            val cleaned = pemString
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("\\s".toRegex(), "")

            val keyBytes = Base64.decode(cleaned)
            val keySpec = PKCS8EncodedKeySpec(keyBytes)
            val keyFactory = KeyFactory.getInstance(ALGORITHM)
            return keyFactory.generatePrivate(keySpec)
        }

        // 从 PEM 字符串加载公钥
        fun loadPublicKeyFromPem(pemString: String): PublicKey {
            val cleaned = pemString
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\\s".toRegex(), "")

            val keyBytes = Base64.decode(cleaned)
            val keySpec = X509EncodedKeySpec(keyBytes)
            val keyFactory = KeyFactory.getInstance(ALGORITHM)
            return keyFactory.generatePublic(keySpec)
        }

        // 生成 RSA 密钥对
        fun generateKeyPair(): KeyPair {
            val keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM)
            keyPairGenerator.initialize(KEY_SIZE)
            return keyPairGenerator.generateKeyPair()
        }

        // 将私钥转为 PEM 格式
        fun privateKeyToPem(privateKey: PrivateKey): String {
            val encoded = Base64.encode(privateKey.encoded)
            return "-----BEGIN PRIVATE KEY-----\n$encoded\n-----END PRIVATE KEY-----"
        }

        // 将公钥转为 PEM 格式
        fun publicKeyToPem(publicKey: PublicKey): String {
            val encoded = Base64.encode(publicKey.encoded)
            return "-----BEGIN PUBLIC KEY-----\n$encoded\n-----END PUBLIC KEY-----"
        }
    }

    override fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val key = publicKey ?: throw IllegalStateException("Public key is required for encryption")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encrypted = cipher.doFinal(plainText.encodeToByteArray())
        return Base64.encode(encrypted)
    }

    override fun decrypt(cipherText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val key = privateKey ?: throw IllegalStateException("Private key is required for decryption")
        cipher.init(Cipher.DECRYPT_MODE, key)
        val encrypted = Base64.decode(cipherText)
        val decrypted = cipher.doFinal(encrypted)
        return String(decrypted, Charsets.UTF_8)
    }

    override fun verify(cipherText: String, plainText: String): Boolean {
        return try {
            val decrypted = decrypt(cipherText)
            decrypted == plainText
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 使用私钥对消息进行签名
     */
    fun sign(message: String): String {
        val signature = Signature.getInstance(SIGNATURE_ALGORITHM)
        val key = privateKey ?: throw IllegalStateException("Private key is required for signing")
        signature.initSign(key)
        signature.update(message.encodeToByteArray())
        val signed = signature.sign()
        return Base64.encode(signed)
    }

    /**
     * 使用公钥验证签名
     */
    fun verifySignature(message: String, signatureBase64: String): Boolean {
        val signature = Signature.getInstance(SIGNATURE_ALGORITHM)
        val key = publicKey ?: throw IllegalStateException("Public key is required for verification")
        signature.initVerify(key)
        signature.update(message.encodeToByteArray())
        val signed = Base64.decode(signatureBase64)
        return signature.verify(signed)
    }
}