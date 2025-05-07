package com.emrepbu.loginflow.domain.model

import java.util.Date

data class User(
    val id: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?,
    val age: Int? = null,
    val bio: String? = null,
    val createdAt: Date? = null,
    val isProfileComplete: Boolean = false
) {
    companion object {
        fun fromFirebaseUser(
            id: String,
            displayName: String?,
            email: String?,
            photoUrl: String?
        ): User {
            return User(
                id = id,
                displayName = displayName,
                email = email,
                photoUrl = photoUrl,
                createdAt = Date(),
                isProfileComplete = false
            )
        }
    }
    
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "displayName" to displayName,
            "email" to email,
            "photoUrl" to photoUrl, 
            "age" to age,
            "bio" to bio,
            "createdAt" to createdAt,
            "isProfileComplete" to isProfileComplete
        )
    }
    
    fun withCompletedProfile(age: Int, bio: String? = null): User {
        return this.copy(
            age = age,
            bio = bio ?: this.bio,
            isProfileComplete = true,
            createdAt = createdAt ?: Date()
        )
    }
}