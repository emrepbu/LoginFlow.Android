package com.emrepbu.loginflow.domain.repository

import com.emrepbu.loginflow.domain.model.AuthResult
import com.emrepbu.loginflow.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>

    suspend fun signInWithGoogle(idToken: String): AuthResult
    suspend fun signOut(): AuthResult

    fun getUserState(): Flow<Boolean>
}