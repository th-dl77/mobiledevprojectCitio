package edu.ap.citioios.ui.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import edu.ap.citioios.models.City
import edu.ap.citioios.models.Location
import edu.ap.citioios.ui.screens.AddCityScreen
import edu.ap.citioios.ui.screens.AddLocationScreen
import edu.ap.citioios.ui.screens.ChatScreen
import edu.ap.citioios.ui.screens.CityScreen
import edu.ap.citioios.ui.screens.ConversationsListScreen
import edu.ap.citioios.ui.screens.HomeScreen
import edu.ap.citioios.ui.screens.LocationDetailScreen
import edu.ap.citioios.ui.screens.LoginScreen
import edu.ap.citioios.ui.screens.ProfileScreen
import edu.ap.citioios.ui.screens.RegisterScreen
import edu.ap.citioios.ui.screens.StartScreen
import edu.ap.citioios.ui.viewmodels.AuthViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val authUiState by authViewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedCity by remember { mutableStateOf<City?>(null) }
    var selectedLocation by remember { mutableStateOf<Location?>(null) }
    var selectedConversationId by remember { mutableStateOf<String?>(null) }
    var selectedOtherUserId by remember { mutableStateOf<String?>(null) }
    var selectedOtherUserEmail by remember { mutableStateOf<String?>(null) }

    // Navigate to HOME when user successfully logs in
    LaunchedEffect(authUiState.isLoggedIn) {
        if (authUiState.isLoggedIn) {
            navController.navigate(AuthScreen.HOME.name) {
                popUpTo(AuthScreen.START.name) { inclusive = false }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (authUiState.isLoggedIn) AuthScreen.HOME.name else AuthScreen.START.name,
        modifier = modifier
    ) {
        composable(route = AuthScreen.START.name) {
            StartScreen(
                onLoginClick = { 
                    navController.navigate(AuthScreen.LOGIN.name)
                },
                onRegisterClick = { 
                    navController.navigate(AuthScreen.REGISTER.name)
                }
            )
        }

        composable(route = AuthScreen.LOGIN.name) {
            LoginScreen(
                isLoading = authUiState.isLoading,
                errorMessage = authUiState.errorMessage,
                onLoginClick = { email, password ->
                    authViewModel.loginUser(email, password)
                },
                onBackClick = { 
                    navController.popBackStack(AuthScreen.START.name, inclusive = false)
                },
                onRegisterClick = { 
                    navController.navigate(AuthScreen.REGISTER.name)
                },
                onErrorDismissed = {
                    authViewModel.clearError()
                }
            )

            LaunchedEffect(authUiState.isLoggedIn) {
                if (authUiState.isLoggedIn) {
                    Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        composable(route = AuthScreen.REGISTER.name) {
            RegisterScreen(
                isLoading = authUiState.isLoading,
                errorMessage = authUiState.errorMessage,
                onRegisterClick = { displayName, email, password ->
                    authViewModel.registerUser(displayName, email, password)
                },
                onBackClick = { 
                    navController.popBackStack(AuthScreen.START.name, inclusive = false)
                },
                onLoginClick = { 
                    navController.navigate(AuthScreen.LOGIN.name)
                },
                onErrorDismissed = {
                    authViewModel.clearError()
                }
            )

            LaunchedEffect(authUiState.isLoggedIn) {
                if (authUiState.isLoggedIn) {
                    Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        composable(route = AuthScreen.HOME.name) {
            HomeScreen(
                userEmail = authUiState.userEmail,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(AuthScreen.START.name) {
                        popUpTo(AuthScreen.START.name) { inclusive = false }
                    }
                },
                onCityClick = { city ->
                    selectedCity = city
                    navController.navigate(AuthScreen.CITY.name)
                },
                onAddCityClick = {
                    navController.navigate(AuthScreen.ADD_CITY.name)
                },
                onProfileClick = {
                    navController.navigate(AuthScreen.PROFILE.name)
                }
            )
        }

        composable(route = AuthScreen.PROFILE.name) {
            ProfileScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(AuthScreen.START.name) {
                        popUpTo(AuthScreen.START.name) { inclusive = false }
                    }
                },
                onMessagesClick = {
                    navController.navigate(AuthScreen.CONVERSATIONS.name)
                }
            )
        }

        composable(route = AuthScreen.ADD_CITY.name) {
            AddCityScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onCityAdded = {
                    navController.popBackStack()
                    Toast.makeText(context, "Stad toegevoegd!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        composable(route = AuthScreen.CITY.name) {
            selectedCity?.let { city ->
                CityScreen(
                    city = city,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onAddLocationClick = {
                        navController.navigate(AuthScreen.ADD_LOCATION.name)
                    },
                    onDetailScreenClick = { location ->
                        selectedLocation = location
                        navController.navigate(AuthScreen.LOCATION_DETAIL.name)
                    }
                )
            } ?: run {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }

        composable(route = AuthScreen.ADD_LOCATION.name) {
            selectedCity?.let { city ->
                AddLocationScreen(
                    city = city,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onLocationAdded = {
                        navController.popBackStack()
                        Toast.makeText(context, "Locatie toegevoegd!", Toast.LENGTH_SHORT).show()
                    }
                )
            } ?: run {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }
        composable(route = AuthScreen.LOCATION_DETAIL.name) {
            selectedLocation?.let { location ->
                LocationDetailScreen(
                    location = location,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onMessageReviewer = { userId, userEmail ->
                        val currentUser = edu.ap.citioios.repository.FirebaseRepository.getCurrentUser()
                        if (currentUser != null && userId != currentUser.uid) {
                            edu.ap.citioios.repository.FirebaseRepository.getOrCreateConversation(
                                currentUserId = currentUser.uid,
                                currentUserEmail = currentUser.email,
                                otherUserId = userId,
                                otherUserEmail = userEmail,
                                onSuccess = { conversationId ->
                                    selectedConversationId = conversationId
                                    selectedOtherUserId = userId
                                    selectedOtherUserEmail = userEmail
                                    navController.navigate(AuthScreen.CHAT.name)
                                },
                                onError = { exception ->
                                    Toast.makeText(
                                        context,
                                        "Fout bij maken gesprek: ${exception.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        } else if (userId == currentUser?.uid) {
                            Toast.makeText(
                                context,
                                "Je kunt geen bericht naar jezelf sturen",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            } ?: run {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }

        composable(route = AuthScreen.CONVERSATIONS.name) {
            ConversationsListScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onConversationClick = { conversationId, otherUserId, otherUserEmail ->
                    selectedConversationId = conversationId
                    selectedOtherUserId = otherUserId
                    selectedOtherUserEmail = otherUserEmail
                    navController.navigate(AuthScreen.CHAT.name)
                }
            )
        }

        composable(route = AuthScreen.CHAT.name) {
            if (selectedConversationId != null && selectedOtherUserId != null && selectedOtherUserEmail != null) {
                ChatScreen(
                    conversationId = selectedConversationId!!,
                    otherUserId = selectedOtherUserId!!,
                    otherUserEmail = selectedOtherUserEmail!!,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }
    }
}
