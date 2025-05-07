package com.emrepbu.loginflow.domain.model

sealed class AuthResult {
    data object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
    data class Unauthorized(val message: String = "User is not logged in") : AuthResult()
}