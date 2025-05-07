package com.emrepbu.loginflow.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
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
fun ProfileScreen(
    isEditMode: Boolean = false,
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    
    val currentUserState by viewModel.currentUser.collectAsStateWithLifecycle()
    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    
    // Input states
    var ageText by rememberSaveable { mutableStateOf("") }
    var bioText by rememberSaveable { mutableStateOf("") }
    var ageError by rememberSaveable { mutableStateOf<String?>(null) }
    
    // Initialize input fields with existing data when in edit mode
    LaunchedEffect(currentUserState) {
        if (currentUserState is UiState.Success) {
            val user = (currentUserState as UiState.Success<User>).data
            // Set initial values from user data if available
            user.age?.let { ageText = it.toString() }
            user.bio?.let { bioText = it }
        }
    }
    
    // Handle profile state changes
    LaunchedEffect(profileState) {
        when (profileState) {
            is UiState.Success -> {
                Timber.d("Profile: save-ok")
                val message = if (isEditMode) {
                    context.getString(R.string.profile_update_success)
                } else {
                    context.getString(R.string.profile_save_success)
                }
                snackbarHostState.showSnackbar(message)
                // Navigate to home after successful profile save/update
                onNavigateToHome()
            }
            is UiState.Error -> {
                Timber.d("Profile: save-err=${(profileState as UiState.Error).message}")
                snackbarHostState.showSnackbar(
                    message = (profileState as UiState.Error).message
                        ?: context.getString(R.string.profile_save_error)
                )
                viewModel.resetState()
            }
            else -> { /* no-op */ }
        }
    }
    
    // Function to validate age input
    fun validateAge(): Boolean {
        ageError = when {
            ageText.isBlank() -> context.getString(R.string.profile_error_age_invalid)
            ageText.toIntOrNull() == null -> context.getString(R.string.profile_error_age_invalid)
            ageText.toInt() <= 0 -> context.getString(R.string.profile_error_age_invalid)
            else -> null
        }
        return ageError == null
    }
    
    // Function to save profile
    fun saveProfile() {
        if (validateAge()) {
            viewModel.saveUserProfile(ageText.toInt(), bioText)
        }
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
                    ProfileContent(
                        user = user,
                        isEditMode = isEditMode,
                        ageText = ageText,
                        onAgeChanged = { 
                            ageText = it
                            // Clear error when user types
                            if (ageError != null) {
                                ageError = null
                            }
                        },
                        bioText = bioText,
                        onBioChanged = { bioText = it },
                        ageError = ageError,
                        onSaveProfile = { saveProfile() },
                        isLoading = profileState is UiState.Loading
                    )
                }
                is UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is UiState.Error -> {
                    Text(
                        text = (currentUserState as UiState.Error).message
                            ?: context.getString(R.string.error_unknown),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun ProfileContent(
    user: User,
    isEditMode: Boolean = false,
    ageText: String,
    onAgeChanged: (String) -> Unit,
    bioText: String,
    onBioChanged: (String) -> Unit,
    ageError: String?,
    onSaveProfile: () -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        // Title - different for edit mode vs setup
        LocalizedText(
            resId = if (isEditMode) R.string.profile_edit_title else R.string.profile_setup_title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Subtitle - different for edit mode vs setup
        LocalizedText(
            resId = if (isEditMode) R.string.profile_edit_subtitle else R.string.profile_setup_subtitle,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Profile photo
        user.photoUrl?.let { photoUrl ->
            Box(
                contentAlignment = Alignment.BottomEnd
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                )
                
                // Change photo button (only in edit mode)
                if (isEditMode) {
                    Button(
                        onClick = { /* TODO: Implement photo change functionality */ },
                        modifier = Modifier
                            .size(40.dp),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Text(text = "📷", fontSize = 16.sp)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Name field (read-only, pre-filled)
        OutlinedTextField(
            value = user.displayName ?: "",
            onValueChange = { },
            label = { LocalizedText(resId = R.string.profile_name_label) },
            modifier = Modifier.fillMaxWidth(0.8f),
            readOnly = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Age field (editable)
        OutlinedTextField(
            value = ageText,
            onValueChange = onAgeChanged,
            label = { LocalizedText(resId = R.string.profile_age_label) },
            placeholder = { LocalizedText(resId = R.string.profile_hint_age) },
            modifier = Modifier.fillMaxWidth(0.8f),
            isError = ageError != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            supportingText = ageError?.let { { Text(text = it) } }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Bio field (editable)
        OutlinedTextField(
            value = bioText,
            onValueChange = onBioChanged,
            label = { LocalizedText(resId = R.string.profile_bio_label) },
            placeholder = { LocalizedText(resId = R.string.profile_bio_hint) },
            modifier = Modifier.fillMaxWidth(0.8f),
            minLines = 3,
            maxLines = 5
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Save/Update button
        Button(
            onClick = onSaveProfile,
            modifier = Modifier.fillMaxWidth(0.8f),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                LocalizedText(
                    resId = if (isEditMode) R.string.profile_update_button else R.string.profile_save_button
                )
            }
        }
    }
}