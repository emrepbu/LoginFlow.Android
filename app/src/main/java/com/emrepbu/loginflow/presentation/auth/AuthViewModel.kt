package com.emrepbu.loginflow.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emrepbu.loginflow.domain.model.AuthResult
import com.emrepbu.loginflow.domain.usecase.GetCurrentUserUseCase
import com.emrepbu.loginflow.domain.usecase.GetUserStateUseCase
import com.emrepbu.loginflow.domain.usecase.SignInWithGoogleUseCase
import com.emrepbu.loginflow.domain.usecase.SignOutUseCase
import com.emrepbu.loginflow.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    getUserStateUseCase: GetUserStateUseCase
) : ViewModel() {

    private val _signInState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val signInState: StateFlow<UiState<Unit>> = _signInState.asStateFlow()

    // Use Eagerly to prevent stopping/restarting flows which can cause authentication loops
    val isUserLoggedIn = getUserStateUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly, // Keep active for entire ViewModel lifecycle
            initialValue = false
        )

    // Derive user interface state from the current user
    val currentUser = getCurrentUserUseCase()
        .map { user ->
            if (user != null) {
                Timber.d("VM: user=${user.id}")
                UiState.Success(user)
            } else {
                Timber.d("VM: no-user")
                UiState.Error("User not found")
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly, // Keep active for entire ViewModel lifecycle
            initialValue = UiState.Loading
        )

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _signInState.value = UiState.Loading

            when (val result = signInWithGoogleUseCase(idToken)) {
                is AuthResult.Success -> {
                    _signInState.value = UiState.Success(Unit)
                    Timber.d("Auth: signin-ok")
                }

                is AuthResult.Error -> {
                    _signInState.value = UiState.Error(result.message)
                    Timber.e("Auth: signin-err=${result.message}")
                }

                is AuthResult.Unauthorized -> {
                    _signInState.value = UiState.Error(result.message)
                    Timber.e("Auth: signin-unauth=${result.message}")
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _signInState.value = UiState.Loading

            when (val result = signOutUseCase()) {
                is AuthResult.Success -> {
                    _signInState.value = UiState.Idle
                    Timber.d("Auth: signout-ok")
                }

                is AuthResult.Error -> {
                    _signInState.value = UiState.Error(result.message)
                    Timber.e("Auth: signout-err=${result.message}")
                }

                is AuthResult.Unauthorized -> {
                    _signInState.value = UiState.Error(result.message)
                    Timber.e("Auth: signout-unauth=${result.message}")
                }
            }
        }
    }

    fun resetState() {
        _signInState.value = UiState.Idle
    }
}