package moe.tabidachi.meadow.security

import de.mkammerer.argon2.Argon2
import de.mkammerer.argon2.Argon2Factory

interface Encryptor {
    fun hash(password: CharArray): String
    fun verify(hash: String, password: CharArray): Boolean
}

class Argon2Encryptor(
    private val iterations: Int = 8,
    private val memory: Int = 1024 * 64,
    private val parallelism: Int = 1,
) : Encryptor {
    private val argon2: Argon2 = Argon2Factory.create()

    override fun hash(password: CharArray): String {
        return argon2.hash(iterations, memory, parallelism, password)
    }

    override fun verify(hash: String, password: CharArray): Boolean {
        return argon2.verify(hash, password)
    }
}