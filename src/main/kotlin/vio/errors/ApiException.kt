package vio.errors

class ApiException(val code: ErrorCode) : Exception(code.name)
