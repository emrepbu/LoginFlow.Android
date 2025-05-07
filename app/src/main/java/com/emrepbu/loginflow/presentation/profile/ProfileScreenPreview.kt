package com.emrepbu.loginflow.presentation.profile

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.emrepbu.loginflow.domain.model.User
import java.util.Date

@Preview(showBackground = true, name = "Profile Setup Screen")
@Composable
fun ProfileContentSetupPreview() {
    val sampleUser = User(
        id = "123",
        displayName = "John Doe",
        email = "john.doe@example.com",
        photoUrl = null,
        createdAt = Date(),
        isProfileComplete = false
    )
    
    MaterialTheme {
        ProfileContent(
            user = sampleUser,
            isEditMode = false,
            ageText = "",
            onAgeChanged = {},
            bioText = "",
            onBioChanged = {},
            ageError = null,
            onSaveProfile = {},
            isLoading = false
        )
    }
}

@Preview(showBackground = true, name = "Profile Edit Screen")
@Composable
fun ProfileContentEditPreview() {
    val sampleUser = User(
        id = "123",
        displayName = "John Doe",
        email = "john.doe@example.com",
        photoUrl = null,
        age = 30,
        bio = "I'm a software developer interested in Android and Kotlin.",
        createdAt = Date(),
        isProfileComplete = true
    )
    
    MaterialTheme {
        ProfileContent(
            user = sampleUser,
            isEditMode = true,
            ageText = "30",
            onAgeChanged = {},
            bioText = "I'm a software developer interested in Android and Kotlin.",
            onBioChanged = {},
            ageError = null,
            onSaveProfile = {},
            isLoading = false
        )
    }
}

@Preview(showBackground = true, name = "Profile With Validation Error")
@Composable
fun ProfileContentErrorPreview() {
    val sampleUser = User(
        id = "123",
        displayName = "John Doe",
        email = "john.doe@example.com",
        photoUrl = null,
        createdAt = Date(),
        isProfileComplete = false
    )
    
    MaterialTheme {
        ProfileContent(
            user = sampleUser,
            isEditMode = false,
            ageText = "-5",
            onAgeChanged = {},
            bioText = "",
            onBioChanged = {},
            ageError = "Please enter a valid age",
            onSaveProfile = {},
            isLoading = false
        )
    }
}