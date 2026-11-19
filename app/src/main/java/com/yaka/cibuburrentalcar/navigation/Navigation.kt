package com.yaka.cibuburrentalcar.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yaka.cibuburrentalcar.data.Car
import com.yaka.cibuburrentalcar.data.CurrentUserProvider
// import com.yaka.cibuburrentalcar.data.Rental // Not used anymore
import com.yaka.cibuburrentalcar.data.RentalRequest
import com.yaka.cibuburrentalcar.data.repository.NetworkResult
import com.yaka.cibuburrentalcar.data.repository.CarRepository
// import com.yaka.cibuburrentalcar.data.repository.BookingRepository // Not used anymore
import com.yaka.cibuburrentalcar.ui.screens.BookingScreen
import com.yaka.cibuburrentalcar.ui.screens.BookingHistoryScreen
import com.yaka.cibuburrentalcar.ui.screens.CarDetailScreen
import com.yaka.cibuburrentalcar.ui.screens.EditProfileScreen
import com.yaka.cibuburrentalcar.ui.screens.HelpCenterScreen
import com.yaka.cibuburrentalcar.ui.screens.HomeScreen
import com.yaka.cibuburrentalcar.ui.screens.ImageViewerScreen
import com.yaka.cibuburrentalcar.ui.screens.LoginScreen
import com.yaka.cibuburrentalcar.ui.screens.MainScreen

import com.yaka.cibuburrentalcar.ui.screens.ProfileScreen
import com.yaka.cibuburrentalcar.ui.screens.RegisterScreen
import com.yaka.cibuburrentalcar.ui.screens.SplashScreen
import com.yaka.cibuburrentalcar.ui.screens.AdminDashboardScreen
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object CarDetail : Screen("car_detail/{carId}") {
        fun createRoute(carId: String) = "car_detail/$carId"
    }
    object Booking : Screen("booking/{carId}") {
        fun createRoute(carId: String) = "booking/$carId"
    }


    object ImageViewer : Screen("image_viewer/{carId}/{initialPage}") {
        fun createRoute(carId: String, initialPage: Int = 0) = "image_viewer/$carId/$initialPage"
    }
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object AdminDashboard : Screen("admin_dashboard")
}

@Composable
fun CibuburRentalCarNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    val role = CurrentUserProvider.currentUser.role
                    val target = if (role.equals("admin", ignoreCase = true)) Screen.AdminDashboard.route else Screen.Home.route
                    navController.navigate(target) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Login Screen
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    val role = CurrentUserProvider.currentUser.role
                    val target = if (role.equals("admin", ignoreCase = true)) Screen.AdminDashboard.route else Screen.Home.route
                    navController.navigate(target) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        // Register Screen
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Home.route) {
            MainScreen(
                onCarClick = { car ->
                    navController.navigate(Screen.CarDetail.createRoute(car.id))
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.CarDetail.route) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId") ?: ""

            CarDetailScreen(
                carId = carId,
                onBackClick = {
                    navController.popBackStack()
                },
                onBookClick = { selectedCar ->
                    navController.navigate(Screen.Booking.createRoute(selectedCar.id))
                },
                onImageClick = { carIdForImage, initialPage ->
                    navController.navigate(Screen.ImageViewer.createRoute(carIdForImage, initialPage))
                }
            )
        }

        composable(Screen.Booking.route) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId") ?: ""

            BookingScreenWrapper(
                carId = carId,
                onBackClick = {
                    navController.popBackStack()
                },
                onBookingConfirm = { rentalRequest ->
                    // Original flow: booking success -> back to home
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                }
            )
        }



        composable(Screen.ImageViewer.route) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId") ?: ""
            val initialPage = backStackEntry.arguments?.getString("initialPage")?.toIntOrNull() ?: 0

            ImageViewerScreen(
                carId = carId,
                initialPage = initialPage,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Profile screen now handled by MainScreen's bottom navigation

        composable(Screen.EditProfile.route) {
            val currentUser = CurrentUserProvider.currentUser
            EditProfileScreen(
                user = currentUser,
                onBackClick = {
                    navController.popBackStack()
                },
                onProfileUpdated = { updatedUser ->
                    CurrentUserProvider.setCurrentUser(updatedUser)
                }
            )
        }

        // Admin Dashboard
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onNavigateToCars = { navController.navigate("admin_cars") },
                onNavigateToBookings = { /* TODO */ },
                onNavigateToCalendar = { navController.navigate("admin_calendar") },
                onNavigateToUsers = { /* TODO */ },
                onNavigateToSettings = { navController.navigate("admin_settings") },
                onBack = { }
            )
        }

        // Admin Cars
        composable("admin_cars") {
            com.yaka.cibuburrentalcar.ui.screens.admin.AdminCarsScreen(onBack = { navController.popBackStack() })
        }
        // Admin Calendar
        composable("admin_calendar") {
            com.yaka.cibuburrentalcar.ui.screens.admin.AdminCalendarScreen(onBack = { navController.popBackStack() })
        }
        // Admin Settings
        composable("admin_settings") {
            com.yaka.cibuburrentalcar.ui.screens.admin.AdminSettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.AdminDashboard.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

// Admin Settings (placed outside NavHost by mistake before) - fix: must be inside NavHost's builder
@Composable
private fun AdminSettingsDestination(navController: NavHostController) {
    com.yaka.cibuburrentalcar.ui.screens.admin.AdminSettingsScreen(
        onBack = { navController.popBackStack() },
        onLogout = {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.AdminDashboard.route) { inclusive = true }
            }
        }
    )
}

@Composable
private fun CarDetailScreenWrapper(
    carId: String,
    onBackClick: () -> Unit,
    onBookClick: (Car) -> Unit,
    onImageClick: (String, Int) -> Unit = { _, _ -> }
) {
    var car by remember { mutableStateOf<Car?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val carRepository = remember { CarRepository() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(carId) {
        coroutineScope.launch {
            when (val result = carRepository.getCarById(carId)) {
                is NetworkResult.Success -> {
                    car = result.data
                    errorMessage = null
                }
                is NetworkResult.Error -> {
                    errorMessage = result.message
                }
                is NetworkResult.Loading -> {
                    // Already handled by isLoading
                }
            }
            isLoading = false
        }
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        errorMessage != null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = errorMessage ?: "Error",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    com.yaka.cibuburrentalcar.ui.components.PrimaryButton(
                        text = "Kembali",
                        onClick = onBackClick
                    )
                }
            }
        }
        car != null -> {
            val currentCar = car
            if (currentCar != null) {
                CarDetailScreen(
                    carId = carId,
                    onBackClick = onBackClick,
                    onBookClick = onBookClick,
                    onImageClick = onImageClick
                )
            }
        }
    }
}

@Composable
private fun BookingScreenWrapper(
    carId: String,
    onBackClick: () -> Unit,
    onBookingConfirm: (RentalRequest) -> Unit
) {
    var car by remember { mutableStateOf<Car?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val carRepository = remember { CarRepository() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(carId) {
        coroutineScope.launch {
            when (val result = carRepository.getCarById(carId)) {
                is NetworkResult.Success -> {
                    car = result.data
                    errorMessage = null
                }
                is NetworkResult.Error -> {
                    errorMessage = result.message
                }
                is NetworkResult.Loading -> {
                    // Already handled by isLoading
                }
            }
            isLoading = false
        }
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        errorMessage != null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = errorMessage ?: "Error",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    com.yaka.cibuburrentalcar.ui.components.PrimaryButton(
                        text = "Kembali",
                        onClick = onBackClick
                    )
                }
            }
        }
        car != null -> {
            val currentCar = car
            if (currentCar != null) {
                BookingScreen(
                    car = currentCar,
                    onBackClick = onBackClick,
                    onBookingConfirm = onBookingConfirm
                )
            }
        }
    }
}


