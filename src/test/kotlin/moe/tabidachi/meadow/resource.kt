package moe.tabidachi.moe.tabidachi.meadow

import java.io.InputStream

fun resource(filename: String): InputStream {
    return object {}.javaClass.classLoader.getResourceAsStream(filename)
}