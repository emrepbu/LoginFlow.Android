package com.emrepbu.loginflow.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.emrepbu.loginflow.R
import com.emrepbu.loginflow.domain.model.User
import com.emrepbu.loginflow.presentation.auth.AuthViewModel
import com.emrepbu.loginflow.presentation.common.LocalizedText
import com.emrepbu.loginflow.presentation.common.UiState
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val currentUserState by viewModel.currentUser.collectAsStateWithLifecycle()
    val signInState by viewModel.signInState.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isUserLoggedIn.collectAsStateWithLifecycle()

    // Auth nav handled by Navigation.kt
    LaunchedEffect(isLoggedIn) {
        Timber.d("Home: auth=$isLoggedIn")
    }

    // Handle sign-in state changes
    LaunchedEffect(signInState) {
        when (signInState) {
            is UiState.Error -> {
                Timber.d("Home: error=${(signInState as UiState.Error).message}")
                snackbarHostState.showSnackbar(
                    (signInState as UiState.Error).message
                )
                viewModel.resetState()
            }

            is UiState.Success -> {
                Timber.d("Home: success")
            }

            is UiState.Loading -> {
                Timber.d("Home: loading")
            }

            else -> {
                /* no-op */
            }
        }
    }

    // Track user state
    LaunchedEffect(currentUserState) {
        Timber.d("Home: user=${currentUserState::class.simpleName}")
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when (currentUserState) {
                is UiState.Success -> {
                    val user = (currentUserState as UiState.Success<User>).data
                    HomeContent(
                        user = user,
                        onSignOut = {
                            Timber.d("Home: signout")
                            viewModel.signOut()
                        },
                        onNavigateToProfile = onNavigateToProfile
                    )
                }

                is UiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is UiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LocalizedText(
                            resId = R.string.unable_to_load_profile,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                Timber.d("Home: error-signout")
                                viewModel.signOut()
                            }
                        ) {
                            LocalizedText(resId = R.string.return_to_login)
                        }
                    }
                }

                else -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun HomeContent(
    user: User,
    onSignOut: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Profile pic
        user.photoUrl?.let { photoUrl ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Name
        Text(
            text = user.displayName ?: "User",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Email
        user.email?.let { email ->
            Text(
                text = email,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }

        // Bio
        user.bio?.let { bio ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = bio,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(0.8f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Welcome
        LocalizedText(
            resId = R.string.welcome_message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Profile button
        Button(
            onClick = onNavigateToProfile,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            LocalizedText(resId = R.string.profile_edit_button)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Logout
        Button(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            LocalizedText(resId = R.string.sign_out)
        }
    }
}