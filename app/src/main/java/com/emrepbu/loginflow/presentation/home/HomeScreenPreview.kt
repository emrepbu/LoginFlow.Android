package com.emrepbu.loginflow.presentation.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.emrepbu.loginflow.domain.model.User
import java.util.Date

@Preview(showBackground = true)
@Composable
fun HomeContentPreview() {
    val sampleUser = User(
        id = "123",
        displayName = "John Doe",
        email = "john.doe@example.com",
        photoUrl = null,
        age = 30,
        bio = "I'm a software developer interested in Android and Kotlin. Love to build apps and explore new technologies.",
        createdAt = Date(),
        isProfileComplete = true
    )
    
    MaterialTheme {
        HomeContent(
            user = sampleUser,
            onSignOut = {},
            onNavigateToProfile = {}
        )
    }
}