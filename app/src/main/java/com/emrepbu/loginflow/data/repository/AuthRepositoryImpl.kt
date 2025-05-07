package com.emrepbu.loginflow.data.repository

import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.emrepbu.loginflow.domain.model.AuthResult
import com.emrepbu.loginflow.domain.model.User
import com.emrepbu.loginflow.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    // Use a shared flow to avoid duplicate listener registrations
    private val _currentUser = callbackFlow {
        Timber.d("Repo: init-flow")

        // Create a single listener for auth state changes
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            val user = firebaseUser?.let {
                User(
                    id = it.uid,
                    displayName = it.displayName,
                    email = it.email,
                    photoUrl = it.photoUrl?.toString()
                )
            }

            // Only log substantial changes to reduce log spam
            Timber.d("Repo: state-change=${if (user != null) user.id else "none"}")
            trySend(user)
        }

        // Emit initial state immediately
        val initialUser = firebaseAuth.currentUser?.let {
            User(
                id = it.uid,
                displayName = it.displayName,
                email = it.email,
                photoUrl = it.photoUrl?.toString()
            )
        }

        Timber.d("Repo: init-state=${initialUser?.id ?: "none"}")
        trySend(initialUser)

        // Register listener for future updates
        firebaseAuth.addAuthStateListener(listener)

        // Clean up when flow collection ends
        awaitClose {
            Timber.d("Repo: cleanup")
            firebaseAuth.removeAuthStateListener(listener)
        }
    }
        .stateIn(
            scope = ProcessLifecycleOwner.get().lifecycleScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    // Expose flow as a public property
    override val currentUser: Flow<User?> = _currentUser

    override suspend fun signInWithGoogle(idToken: String): AuthResult {
        return try {
            Timber.d("Repo: signin-start")

            if (idToken.isBlank()) {
                Timber.e("Repo: token-blank")
                return AuthResult.Error("Google sign in failed: ID token is blank")
            }

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            Timber.d("Repo: cred-created")

            try {
                val authResult = firebaseAuth.signInWithCredential(credential).await()

                if (authResult.user != null) {
                    val user = authResult.user
                    Timber.d("Repo: signin-ok id=${user?.uid}")
                    Timber.d("Repo: user-new=${authResult.additionalUserInfo?.isNewUser}")
                    AuthResult.Success
                } else {
                    Timber.e("Repo: no-user")
                    AuthResult.Error("Google sign in failed: No user returned")
                }
            } catch (firebaseException: Exception) {
                Timber.e(firebaseException, "Repo: firebase-err")
                AuthResult.Error("Firebase authentication failed: ${firebaseException.message}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Repo: signin-err")
            AuthResult.Error(e.message ?: "Google sign in failed")
        }
    }

    override suspend fun signOut(): AuthResult {
        return try {
            firebaseAuth.signOut()
            AuthResult.Success
        } catch (e: Exception) {
            Timber.e(e, "Repo: signout-err")
            AuthResult.Error(e.message ?: "Sign out failed")
        }
    }

    override fun getUserState(): Flow<Boolean> {
        return currentUser.map { it != null }
    }
}