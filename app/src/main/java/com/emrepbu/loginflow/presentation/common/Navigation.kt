package com.emrepbu.loginflow.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.emrepbu.loginflow.presentation.auth.AuthViewModel
import com.emrepbu.loginflow.presentation.auth.LoginScreen
import com.emrepbu.loginflow.presentation.home.HomeScreen
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import timber.log.Timber

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Home : Screen("home")
}

@Composable
fun Navigation(
    navController: NavHostController,
    googleSignInClient: GoogleSignInClient,
    startDestination: String = Screen.Login.route
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val isLoggedIn by authViewModel.isUserLoggedIn.collectAsStateWithLifecycle()

    LaunchedEffect(isLoggedIn) {
        Timber.d("Auth: isLoggedIn=$isLoggedIn, screen=${navController.currentDestination?.route}")

        when {
            isLoggedIn && navController.currentDestination?.route == Screen.Login.route -> {
                Timber.d("Nav: login->home")
                navController.navigate(Screen.Home.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }

            !isLoggedIn && navController.currentDestination?.route == Screen.Home.route -> {
                Timber.d("Nav: home->login")
                navController.navigate(Screen.Login.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }

            else -> {
                Timber.d("Nav: no change")
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                googleSignInClient = googleSignInClient,
                onNavigateToHome = {
                    Timber.d("Login complete")
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onLogout = {
                    Timber.d("Logout initiated")
                }
            )
        }
    }
}