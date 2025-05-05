package com.emrepbu.loginflow.data.repository

import com.emrepbu.loginflow.domain.model.AuthResult
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(MockitoJUnitRunner::class)
class AuthRepositoryImplTest {

    @Mock
    private lateinit var firebaseAuth: FirebaseAuth

    @Mock
    private lateinit var firebaseUser: FirebaseUser

    @Mock
    private lateinit var authTask: Task<AuthResult>

    @Mock
    private lateinit var authResult: AuthResult

    private lateinit var authRepository: AuthRepositoryImpl

    @Before
    fun setup() {
        authRepository = AuthRepositoryImpl(firebaseAuth)
    }

    @Test
    fun `signOut returns success when successful`() = runBlocking {
        // Given - signOut is successful
        
        // When
        val result = authRepository.signOut()
        
        // Then
        verify(firebaseAuth).signOut()
        assert(result is com.emrepbu.loginflow.domain.model.AuthResult.Success)
    }

    @Test
    fun `getUserState returns true when user is logged in`() = runBlocking {
        // Given - User is logged in
        val listener = mutableListOf<FirebaseAuth.AuthStateListener>()
        `when`(firebaseAuth.addAuthStateListener(any())).then {
            listener.add(it.arguments[0] as FirebaseAuth.AuthStateListener)
            Unit
        }
        
        `when`(firebaseAuth.currentUser).thenReturn(firebaseUser)
        
        // When
        val userStateFlow = authRepository.getUserState()
        
        // Trigger the listener
        listener.first().onAuthStateChanged(firebaseAuth)
        
        // Then
        val isLoggedIn = userStateFlow.first()
        assert(isLoggedIn)
    }

    @Test
    fun `getUserState returns false when user is not logged in`() = runBlocking {
        // Given - User is not logged in
        val listener = mutableListOf<FirebaseAuth.AuthStateListener>()
        `when`(firebaseAuth.addAuthStateListener(any())).then {
            listener.add(it.arguments[0] as FirebaseAuth.AuthStateListener)
            Unit
        }
        
        `when`(firebaseAuth.currentUser).thenReturn(null)
        
        // When
        val userStateFlow = authRepository.getUserState()
        
        // Trigger the listener
        listener.first().onAuthStateChanged(firebaseAuth)
        
        // Then
        val isLoggedIn = userStateFlow.first()
        assert(!isLoggedIn)
    }
}