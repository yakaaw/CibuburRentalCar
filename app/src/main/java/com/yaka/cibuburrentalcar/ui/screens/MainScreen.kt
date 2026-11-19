package com.yaka.cibuburrentalcar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yaka.cibuburrentalcar.data.Car
import com.yaka.cibuburrentalcar.data.CurrentUserProvider
import com.yaka.cibuburrentalcar.ui.components.BottomNavigationBar
import com.yaka.cibuburrentalcar.ui.viewmodel.BookingHistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onCarClick: (Car) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        // Pop up to the start destination to avoid building up a large stack
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Home/Beranda - Katalog Mobil
            composable("home") {
                HomeScreen(
                    onCarClick = onCarClick,
                    onProfileClick = { 
                        navController.navigate("profile") {
                            launchSingleTop = true
                        }
                    }
                )
            }
            
            // Riwayat Penyewaan
            composable("history") {
                BookingHistoryScreen(
                    onBookingDetailClick = { booking ->
                        navController.navigate("booking_detail/${booking.id}") {
                            launchSingleTop = true
                        }
                    }
                )
            }

            // Pusat Bantuan
            composable("help") {
                HelpCenterScreen()
            }
            
            // Profile
            composable("profile") {
                ProfileScreen(
                    onLogout = onLogout,
                    onBackClick = {
                        navController.navigate("home") {
                            launchSingleTop = true
                        }
                    },
                    onEditProfile = {
                        navController.navigate("edit_profile") {
                            launchSingleTop = true
                        }
                    }
                )
            }

            // Edit Profile
            composable("edit_profile") {
                val currentUser = CurrentUserProvider.currentUser
                EditProfileScreen(
                    user = currentUser,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onProfileUpdated = { updatedUser ->
                        CurrentUserProvider.setCurrentUser(updatedUser)
                        navController.popBackStack()
                    }
                )
            }

            // Booking Detail
            composable("booking_detail/{bookingId}") { backStackEntry ->
                val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                val bookingHistoryViewModel: BookingHistoryViewModel = viewModel()

                // Find booking from history
                val booking = bookingHistoryViewModel.uiState.value.bookingHistory
                    .find { it.id == bookingId }

                if (booking != null) {
                    val car = bookingHistoryViewModel.uiState.value.carDetails[booking.carId]

                    BookingDetailScreen(
                        booking = booking,
                        car = car,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onEditClick = {
                            navController.navigate("edit_booking/${booking.id}") {
                                launchSingleTop = true
                            }
                        },
                        onCancelClick = {
                            bookingHistoryViewModel.cancelBooking(booking.id)
                            navController.popBackStack()
                        }
                    )
                }
            }

            // Edit Booking
            composable("edit_booking/{bookingId}") { backStackEntry ->
                val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                val bookingHistoryViewModel: BookingHistoryViewModel = viewModel()

                // Find booking from history
                val booking = bookingHistoryViewModel.uiState.value.bookingHistory
                    .find { it.id == bookingId }

                if (booking != null) {
                    EditBookingScreen(
                        booking = booking,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onSaveSuccess = {
                            // Refresh booking history and go back to detail
                            bookingHistoryViewModel.loadBookingHistory()
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
