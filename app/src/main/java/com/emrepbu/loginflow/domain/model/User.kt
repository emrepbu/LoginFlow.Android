package com.emrepbu.loginflow.domain.model

data class User(
    val id: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?
)