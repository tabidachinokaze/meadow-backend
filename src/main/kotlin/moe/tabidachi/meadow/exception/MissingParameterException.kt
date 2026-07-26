package moe.tabidachi.meadow.exception

class MissingParameterException(param: String) : IllegalArgumentException("Missing '${param}' parameter")