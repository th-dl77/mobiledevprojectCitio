package edu.ap.citioios.ui.navigation

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.ap.citioios.models.City
import edu.ap.citioios.ui.screens.AddCityScreen
import edu.ap.citioios.ui.screens.CityScreen
import edu.ap.citioios.ui.screens.HomeScreen
import edu.ap.citioios.ui.screens.LoginScreen
import edu.ap.citioios.ui.screens.RegisterScreen
import edu.ap.citioios.ui.screens.StartScreen
import edu.ap.citioios.ui.viewmodels.AuthViewModel
import edu.ap.citioios.ui.viewmodels.CityViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val authUiState by authViewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedCity by remember { mutableStateOf<City?>(null) }

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
                    }
                )
            } ?: run {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }
    }
}
