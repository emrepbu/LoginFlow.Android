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
import com.emrepbu.loginflow.presentation.profile.ProfileScreen
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import timber.log.Timber

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Profile : Screen("profile")
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
    val isProfileComplete by authViewModel.isProfileComplete.collectAsStateWithLifecycle()

    LaunchedEffect(isLoggedIn, isProfileComplete) {
        val currentRoute = navController.currentDestination?.route
        Timber.d("Auth: isLoggedIn=$isLoggedIn, isProfileComplete=$isProfileComplete, screen=$currentRoute")

        when {
            // Not logged in but on a protected screen -> go to login
            !isLoggedIn && currentRoute != Screen.Login.route -> {
                Timber.d("Nav: protected->login")
                navController.navigate(Screen.Login.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }

            isLoggedIn && currentRoute == Screen.Login.route -> {
                if (isProfileComplete) {
                    Timber.d("Nav: login->home")
                    navController.navigate(Screen.Home.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                } else {
                    Timber.d("Nav: login->profile")
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            }

            isLoggedIn && !isProfileComplete && currentRoute == Screen.Home.route -> {
                Timber.d("Nav: home->profile (required)")
                navController.navigate(Screen.Profile.route) {
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

        composable(Screen.Profile.route) {
            // Pass boolean for edit mode
            val isEditMode = isProfileComplete
            ProfileScreen(
                isEditMode = isEditMode,
                onNavigateToHome = {
                    Timber.d("Profile complete")
                    navController.navigate(Screen.Home.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onLogout = {
                    Timber.d("Logout initiated")
                },
                onNavigateToProfile = {
                    Timber.d("Home->Profile (user requested)")
                    navController.navigate(Screen.Profile.route)
                }
            )
        }
    }
}