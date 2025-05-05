package com.emrepbu.loginflow.presentation.auth

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.emrepbu.loginflow.presentation.common.LocalizedText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emrepbu.loginflow.R
import com.emrepbu.loginflow.presentation.common.UiState
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@SuppressLint("LocalContextConfigurationRead")
@Composable
fun LoginScreen(
    googleSignInClient: GoogleSignInClient,
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val isLoggedIn by viewModel.isUserLoggedIn.collectAsStateWithLifecycle()
    val signInState by viewModel.signInState.collectAsStateWithLifecycle()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { token ->
                    viewModel.signInWithGoogle(token)
                } ?: run {
                    scope.launch {
                        snackbarHostState.showSnackbar(context.getString(R.string.error_google_sign_in))
                    }
                }
            } catch (e: ApiException) {
                Timber.e(e, "Google sign in failed")
                val errorMessageResId = when (e.statusCode) {
                    GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> R.string.error_sign_in_cancelled
                    GoogleSignInStatusCodes.SIGN_IN_FAILED -> R.string.error_sign_in_failed
                    GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS -> R.string.error_sign_in_progress
                    GoogleSignInStatusCodes.NETWORK_ERROR -> R.string.error_network
                    else -> R.string.error_unknown
                }

                val configuration = context.resources.configuration
                val errorMessage =
                    context.createConfigurationContext(configuration).getString(errorMessageResId)

                Timber.e(e, "Google sign in failed: $errorMessage (Status Code: ${e.statusCode})")
                scope.launch {
                    snackbarHostState.showSnackbar(errorMessage)
                }
            }
        }
    }

    LaunchedEffect(isLoggedIn) {
        Timber.d("Auth: isLoggedIn=$isLoggedIn")
    }

    LaunchedEffect(signInState) {
        when (signInState) {
            is UiState.Error -> {
                Timber.d("Sign-in error: ${(signInState as UiState.Error).message}")
                snackbarHostState.showSnackbar(
                    (signInState as UiState.Error).message
                )
                viewModel.resetState()
            }

            is UiState.Success -> {
                Timber.d("Sign-in successful")
                viewModel.resetState()
            }

            else -> { /* no-op */
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Login Flow",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LocalizedText(
                resId = R.string.sign_in_to_continue,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(64.dp))

            OutlinedButton(
                onClick = {
                    val signInIntent = googleSignInClient.signInIntent
                    launcher.launch(signInIntent)
                },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                LocalizedText(resId = R.string.sign_in_with_google)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (signInState is UiState.Loading) {
                CircularProgressIndicator()
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}