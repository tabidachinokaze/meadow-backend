package moe.tabidachi.meadow.regex

val RegexEmail by lazy { "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$".toRegex() }
val RegexPhone by lazy { "^1[3|4|5|7|8][0-9]{9}\$".toRegex() }
val RegexUsernameBasic by lazy { "^[\\p{L}\\p{M}\\p{N}_.-]{2,32}$".toRegex() }
val RegexUsernameStrict by lazy { "^(?!.*[_.-]{2})[\\p{L}\\p{M}\\p{N}](?:[\\p{L}\\p{M}\\p{N}_.-]*[\\p{L}\\p{M}\\p{N}])?$".toRegex() }