# LoginFlow

A modern Android application demonstrating Google Sign-In using Firebase Authentication in a clean architecture approach.

## Features

- Google Sign-In authentication
- Clean Architecture with MVVM pattern
- Jetpack Compose UI
- State management with StateFlow
- Dependency Injection with Dagger Hilt
- Navigation Compose
- Kotlin Coroutines for asynchronous operations
- Unit testing support
- Multilingual support (English and Turkish)
- Runtime language switching

## Project Structure

The project follows Clean Architecture principles with three main layers:

```
LoginFlow/
├── app/
│   └── src/
│       ├── main/
│       │   ├── java/com/emrepbu/loginflow/
│       │   │   ├── data/                       # Data Layer
│       │   │   │   └── repository/             # Repository implementations
│       │   │   │       └── AuthRepositoryImpl.kt
│       │   │   ├── di/                         # Dependency Injection
│       │   │   │   ├── FirebaseModule.kt       # Firebase services providers
│       │   │   │   ├── LanguageModule.kt       # Language management providers
│       │   │   │   └── RepositoryModule.kt     # Repository bindings
│       │   │   ├── domain/                     # Domain Layer
│       │   │   │   ├── manager/                # Business logic managers
│       │   │   │   │   └── LanguageManager.kt  # Language handling
│       │   │   │   ├── model/                  # Domain models
│       │   │   │   │   ├── AuthResult.kt       # Authentication result wrapper
│       │   │   │   │   ├── Language.kt         # Language enum
│       │   │   │   │   └── User.kt             # User data class
│       │   │   │   ├── repository/             # Repository interfaces
│       │   │   │   │   └── AuthRepository.kt
│       │   │   │   └── usecase/                # Business logic use cases
│       │   │   │       ├── GetCurrentUserUseCase.kt
│       │   │   │       ├── GetUserStateUseCase.kt
│       │   │   │       ├── SignInWithGoogleUseCase.kt
│       │   │   │       └── SignOutUseCase.kt
│       │   │   ├── presentation/               # Presentation Layer
│       │   │   │   ├── auth/                   # Authentication UI
│       │   │   │   │   ├── AuthViewModel.kt    # Authentication ViewModel
│       │   │   │   │   └── LoginScreen.kt      # Login UI
│       │   │   │   ├── common/                 # Shared UI components
│       │   │   │   │   ├── LanguageSelector.kt # Language selection UI
│       │   │   │   │   ├── LocalizedText.kt    # Localized text component
│       │   │   │   │   ├── Navigation.kt       # Navigation controller
│       │   │   │   │   └── UiState.kt          # UI state wrapper
│       │   │   │   └── home/                   # Home screen UI
│       │   │   │       └── HomeScreen.kt       # Home UI
│       │   │   ├── ui/                         # UI resources
│       │   │   │   └── theme/                  # Theme configuration
│       │   │   │       ├── Color.kt
│       │   │   │       ├── Theme.kt
│       │   │   │       └── Type.kt
│       │   │   ├── util/                       # Utilities
│       │   │   │   └── ComposeLanguageUtil.kt  # Compose language utilities
│       │   │   ├── LoginFlowApp.kt             # Application class
│       │   │   └── MainActivity.kt             # Main activity
│       │   ├── res/
│       │   │   ├── values/                     # Default (English) resources
│       │   │   │   └── strings.xml
│       │   │   └── values-tr/                  # Turkish resources
│       │   │       └── strings.xml
│       │   └── AndroidManifest.xml
│       └── test/                               # Unit tests
│           └── java/com/emrepbu/loginflow/
│               └── data/repository/
│                   └── AuthRepositoryImplTest.kt
└── gradle/                                     # Gradle configuration
```

### Presentation Layer
- **Screens**: UI components built with Jetpack Compose (LoginScreen.kt, HomeScreen.kt)
- **ViewModels**: State management and UI logic containers (AuthViewModel.kt)
- **Navigation**: Single-activity architecture with Compose Navigation (Navigation.kt)
- **UI Components**: Reusable Compose UI elements (LanguageSelector.kt, LocalizedText.kt)
- **State Management**: UI state representation with sealed classes (UiState.kt)

### Domain Layer
- **Use Cases**: Single-responsibility business logic components (GetUserStateUseCase.kt, SignInWithGoogleUseCase.kt)
- **Repository Interfaces**: Clean architecture boundaries (AuthRepository.kt)
- **Domain Models**: Core business models independent of data sources (User.kt, AuthResult.kt)
- **Business Logic Managers**: Cross-cutting domain services (LanguageManager.kt)

### Data Layer
- **Repository Implementations**: Data source coordination (AuthRepositoryImpl.kt)
- **Remote Data Sources**: Firebase Authentication integration
- **Local Data Sources**: Shared preferences for language preferences
- **Dependency Injection**: Hilt modules for providing dependencies (FirebaseModule.kt, RepositoryModule.kt, LanguageModule.kt)

## Architecture Diagram

The app follows a clean architecture pattern with unidirectional data flow:

```
┌─────────────────────────────────────────────────────────────────────┐
│                           PRESENTATION LAYER                         │
│                                                                     │
│  ┌─────────────┐      ┌─────────────┐       ┌───────────────────┐   │
│  │             │      │             │       │                   │   │
│  │  LoginScreen│◄────►│ AuthViewModel│◄─────►│ HomeScreen        │   │
│  │             │      │             │       │                   │   │
│  └─────────────┘      └──────┬──────┘       └───────────────────┘   │
│                              │                                      │
└──────────────────────────────┼──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                             DOMAIN LAYER                            │
│                                                                     │
│  ┌────────────────────┐    ┌────────────────────┐                   │
│  │                    │    │                    │                   │
│  │ SignInWithGoogle   │    │ GetCurrentUser     │                   │
│  │ UseCase            │    │ UseCase            │                   │
│  │                    │    │                    │                   │
│  └─────────┬──────────┘    └────────┬───────────┘                   │
│            │                        │                               │
│            │                        │                               │
│            ▼                        ▼                               │
│  ┌────────────────────┐    ┌────────────────────┐                   │
│  │                    │    │                    │                   │
│  │ SignOutUseCase     │    │ GetUserState       │    ┌─────────────┐│
│  │                    │    │ UseCase            │    │LanguageManager││
│  │                    │    │                    │    │             ││
│  └─────────┬──────────┘    └────────┬───────────┘    └─────────────┘│
│            │                        │                               │
│            └────────────┬───────────┘                               │
│                         │                                           │
│                         ▼                                           │
│                  ┌─────────────────┐                                │
│                  │                 │                                │
│                  │  AuthRepository │                                │
│                  │  (Interface)    │                                │
│                  │                 │                                │
│                  └────────┬────────┘                                │
│                           │                                         │
└───────────────────────────┼─────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                             DATA LAYER                              │
│                                                                     │
│                     ┌──────────────────────┐                        │
│                     │                      │                        │
│                     │  AuthRepositoryImpl  │                        │
│                     │                      │                        │
│                     └──────────┬───────────┘                        │
│                                │                                    │
│                                ▼                                    │
│                     ┌──────────────────────┐                        │
│                     │                      │                        │
│                     │  Firebase Auth       │                        │
│                     │                      │                        │
│                     └──────────────────────┘                        │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

## Authentication Flow

1. Users are presented with the login screen
2. Google Sign-In is initiated when the user clicks the sign-in button
3. Firebase Authentication handles the authentication process
4. Upon successful authentication, users are navigated to the home screen
5. User state is maintained to handle automatic login if the user is already authenticated

## Getting Started

### Prerequisites

- Android Studio
- JDK 11 or higher
- An Android device or emulator running API level 24 or higher

### Setup

1. Clone this repository
2. Open the project in Android Studio
3. Create a Firebase project and add your app to it
4. Download the `google-services.json` file and place it in the app directory
5. Configure your Firebase project to enable Google Sign-In authentication
6. Add your Google Web Client ID to `local.properties` file:
   ```
   client-id=YOUR_GOOGLE_WEB_CLIENT_ID
   ```
7. Run the app

## Tech Stack

- Kotlin
- Jetpack Compose
- Coroutines & Flow
- Firebase Authentication
- Navigation Compose
- Dagger Hilt
- Room Database
- DataStore Preferences
- JUnit & Mockito for testing
- Timber for logging
- Coil for image loading
- AppCompat for localization support
- Custom Compose components for localization

## Localization

The app supports multiple languages:
- English (default)
- Turkish

Language features:
- Runtime language switching using on-the-fly Configuration changes
- Seamless persistence of language preferences between app sessions
- Elegant language selector UI accessible from any screen
- All UI text properly localized through string resources
- Locale-aware resource loading with configuration context
- Support for all Android versions with appropriate API handling

### Localization Architecture

The localization system implements several advanced features:

```
┌─────────────────────┐     ┌──────────────────────┐
│                     │     │                      │
│    MainActivity     │     │    LanguageSelector  │
│                     │     │    (Composable)      │
└────────┬────────────┘     └──────────┬───────────┘
         │                             │
         │                             │  Language Selection
         │                             ▼
         │                  ┌──────────────────────┐
         │                  │                      │
         │                  │   LanguageManager    │
         ├─────────────────►│                      │
         │  Apply Settings  └──────────┬───────────┘
         │                             │
         │                             │  Save Preferences
         │                             ▼
         │                  ┌──────────────────────┐
         │                  │                      │
         │                  │ SharedPreferences    │
         │                  │                      │
         │                  └──────────┬───────────┘
         │                             │
         │                             │  Retrieve on App Start
         │                             ▼
┌────────▼────────────┐     ┌──────────────────────┐
│                     │     │                      │
│ attachBaseContext   │◄────┤ Configuration        │
│ (Apply locale)      │     │ (Locale settings)    │
│                     │     │                      │
└────────┬────────────┘     └──────────────────────┘
         │
         │
         ▼
┌──────────────────────┐     ┌──────────────────────┐
│                      │     │                      │
│ LocalizedText        │◄────┤ Resource Loading     │
│ (UI Component)       │     │ with correct locale  │
│                      │     │                      │
└──────────────────────┘     └──────────────────────┘
```

1. **`LocalizedText` Component**: 
   - A custom Compose Text component that recomposes when the locale changes
   - Properly handles configuration changes for immediate text updates
   - Ensures consistent text display across the app

2. **`LanguageManager` Service**:
   - Centralized management of locale preferences
   - Handles saving and loading language preferences
   - Provides locale information to the entire application

3. **`ComposeLanguageUtil` Helper**:
   - Utilities for setting the application locale
   - Configuration handling for different Android versions
   - Locale context management for resource loading

4. **Configuration Context Approach**:
   - Uses `createConfigurationContext` for proper locale-aware resources
   - Implements `attachBaseContext` for application-wide locale support
   - Ensures consistent locale across configuration changes

### Adding a New Language

To add a new language:
1. Create a new values directory (e.g., `values-fr` for French)
2. Add a translated `strings.xml` file in that directory
3. Add the new language to the `Language` enum in `Language.kt`

## Future Enhancements

The app is designed to be easily extended with additional features:

### Authentication Methods
- Email/Password authentication
- Phone number authentication
- Third-party providers (Facebook, Twitter, etc.)

### Localization Improvements
- Support for additional languages (existing architecture makes this simple)
- Right-to-left language support for languages like Arabic and Hebrew
- Language-specific formatting for dates, numbers, and currency
- Automatic detection of device language
- Language preferences based on user account

### Additional Features
- Profile management
- User settings and preferences
- Dark mode / light mode themes
- Push notifications
- Offline support