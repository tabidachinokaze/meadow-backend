package moe.tabidachi.meadow.ktx

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

suspend fun <T> Database.withTransaction(block: suspend () -> T): T =
    withContext(Dispatchers.IO) {
        try {
            suspendTransaction(db = this@withTransaction) { block() }
        } catch (e: Exception) {
            throw e
        }
    }