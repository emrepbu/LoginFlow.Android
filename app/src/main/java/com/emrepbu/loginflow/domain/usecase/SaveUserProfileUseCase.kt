package com.emrepbu.loginflow.domain.usecase

import com.emrepbu.loginflow.domain.model.AuthResult
import com.emrepbu.loginflow.domain.model.User
import com.emrepbu.loginflow.domain.repository.AuthRepository
import javax.inject.Inject

class SaveUserProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(user: User, age: Int, bio: String? = null): AuthResult {
        val updatedUser = user.withCompletedProfile(age, bio)

        return authRepository.saveUserProfile(updatedUser)
    }
}