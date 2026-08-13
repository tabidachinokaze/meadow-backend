package moe.tabidachi.meadow.ktx

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.statements.UpdateStatement

fun <S> UpdateStatement.setIfNotNull(column: Column<S>, value: S?) {
    this[column] = value ?: return
}