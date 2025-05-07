package com.emrepbu.loginflow.data.repository

import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.emrepbu.loginflow.domain.model.AuthResult
import com.emrepbu.loginflow.domain.model.User
import com.emrepbu.loginflow.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    // Use a shared flow to avoid duplicate listener registrations
    private val _currentUser = callbackFlow {
        Timber.d("Repo: init-flow")

        // Create a single listener for auth state changes
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            val user = firebaseUser?.let {
                // First create basic user from Firebase Auth
                val basicUser = User.fromFirebaseUser(
                    id = it.uid,
                    displayName = it.displayName,
                    email = it.email,
                    photoUrl = it.photoUrl?.toString()
                )
                
                // Then fetch additional data from Firestore
                firestore.collection("users").document(it.uid).get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            // Update user with Firestore data
                            val isProfileComplete = document.getBoolean("isProfileComplete") ?: false
                            val age = document.getLong("age")?.toInt()
                            
                            // Update profile completion status
                            val updatedUser = if (isProfileComplete && age != null) {
                                basicUser.copy(isProfileComplete = true, age = age)
                            } else {
                                basicUser
                            }
                            
                            // Send updated user
                            _isProfileComplete.value = updatedUser.isProfileComplete
                            trySend(updatedUser)
                        } else {
                            // No Firestore data yet, use basic user
                            _isProfileComplete.value = false
                            trySend(basicUser)
                        }
                    }
                    .addOnFailureListener { e ->
                        Timber.e(e, "Failed to fetch user data from Firestore")
                        _isProfileComplete.value = false
                        trySend(basicUser)
                    }
                
                // Return basic user initially, will be updated when Firestore data arrives
                basicUser
            }

            // Only log substantial changes to reduce log spam
            Timber.d("Repo: state-change=${if (user != null) user.id else "none"}")
            trySend(user)
        }

        // Emit initial state immediately
        val initialUser = firebaseAuth.currentUser?.let {
            User.fromFirebaseUser(
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

    // Profile completion status flow
    private val _isProfileComplete = MutableStateFlow(false)
    override val isProfileComplete: Flow<Boolean> = _isProfileComplete.asStateFlow()
    
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
    
    override suspend fun saveUserProfile(user: User): AuthResult {
        return try {
            Timber.d("Repo: save-profile id=${user.id}")
            
            // Get reference to the user document
            val userDocRef = firestore.collection("users").document(user.id)
            
            // Save user data to Firestore
            userDocRef.set(user.toMap()).await()
            
            // Update profile completion status
            _isProfileComplete.value = user.isProfileComplete
            
            Timber.d("Repo: profile-saved")
            AuthResult.Success
        } catch (e: Exception) {
            Timber.e(e, "Repo: profile-save-err")
            AuthResult.Error(e.message ?: "Failed to save profile")
        }
    }
}