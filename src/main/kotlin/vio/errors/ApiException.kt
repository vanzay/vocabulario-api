package vio.errors

class ApiException(val code: String) : Exception(code)
