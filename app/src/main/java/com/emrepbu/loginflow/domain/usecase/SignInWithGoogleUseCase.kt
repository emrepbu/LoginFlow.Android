package com.emrepbu.loginflow.domain.usecase

import com.emrepbu.loginflow.domain.model.AuthResult
import com.emrepbu.loginflow.domain.repository.AuthRepository
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): AuthResult {
        return authRepository.signInWithGoogle(idToken)
    }
}