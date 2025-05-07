package com.emrepbu.loginflow.domain.usecase

import com.emrepbu.loginflow.domain.model.User
import com.emrepbu.loginflow.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<User?> {
        return authRepository.currentUser
    }
}