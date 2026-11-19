package com.yaka.cibuburrentalcar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import kotlinx.coroutines.launch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import com.yaka.cibuburrentalcar.data.Car
import com.yaka.cibuburrentalcar.data.CarType
import com.yaka.cibuburrentalcar.data.CurrentUserProvider
import com.yaka.cibuburrentalcar.data.repository.NetworkResult
import com.yaka.cibuburrentalcar.data.repository.CarRepository
import com.yaka.cibuburrentalcar.ui.components.CarCard
import com.yaka.cibuburrentalcar.ui.components.FilterRow
import com.yaka.cibuburrentalcar.ui.components.IconButton
import com.yaka.cibuburrentalcar.ui.components.SearchBar
import com.yaka.cibuburrentalcar.ui.theme.MediumGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCarClick: (Car) -> Unit,
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilters by remember { mutableStateOf(setOf<String>()) }
    var cars by remember { mutableStateOf<List<Car>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    var dataSource by remember { mutableStateOf("Unknown") }

    val carTypes = listOf("Semua", "MPV", "Sedan", "Hatchback", "SUV")

    // Calculate car counts per category
    val carCounts = remember(cars) {
        val counts = mutableMapOf<String, Int>()
        counts["Semua"] = cars.size
        carTypes.drop(1).forEach { type: String ->
            counts[type] = cars.count { car ->
                car.type.name.equals(type, ignoreCase = true)
            }
        }
        counts
    }
    val carRepository = remember { CarRepository() }
    val coroutineScope = rememberCoroutineScope()

    // Rotating animation for refresh icon
    val rotationAngle by animateFloatAsState(
        targetValue = if (isRefreshing) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "refresh_rotation"
    )

    // Load cars function
    fun loadCars() {
        isLoading = true
        errorMessage = null

        coroutineScope.launch {
            val selectedType = selectedFilters.firstOrNull()
            when (val result = carRepository.getCars(
                type = selectedType,
                search = searchQuery.takeIf { it.isNotBlank() },
                availableOnly = true
            )) {
                is NetworkResult.Success -> {
                    cars = result.data ?: emptyList()
                    errorMessage = null
                    dataSource = if (cars.size <= 10) "Firebase Firestore" else "Local Fallback"
                    android.util.Log.d("HomeScreen", "Loaded ${cars.size} cars from $dataSource")
                }
                is NetworkResult.Error -> {
                    errorMessage = result.message
                    dataSource = "Error"
                    android.util.Log.e("HomeScreen", "Error loading cars: ${result.message}")

                    // Special handling for network errors
                    when {
                        result.message?.contains("CLEARTEXT communication") == true -> {
                            android.util.Log.e("HomeScreen", "Network Security Policy blocking HTTP - Check AndroidManifest.xml")
                        }
                        result.message?.contains("EPERM") == true || result.message?.contains("Operation not permitted") == true -> {
                            android.util.Log.e("HomeScreen", "Internet permission denied - Check INTERNET permission")
                        }
                    }
                }
                is NetworkResult.Loading -> {
                    // Already handled by isLoading
                }
            }
            isLoading = false
        }
    }

    // Refresh cars function for real-time updates
    fun refreshCars() {
        if (isRefreshing) return // Prevent multiple simultaneous refreshes

        isRefreshing = true
        android.util.Log.d("HomeScreen", "Refreshing cars from database...")

        coroutineScope.launch {
            val selectedType = selectedFilters.firstOrNull()
            when (val result = carRepository.refreshCars(
                type = selectedType,
                search = searchQuery.takeIf { it.isNotBlank() },
                availableOnly = true
            )) {
                is NetworkResult.Success -> {
                    cars = result.data ?: emptyList()
                    errorMessage = null
                    dataSource = if (cars.size <= 10) "Firebase Firestore" else "Local Fallback"
                    android.util.Log.d("HomeScreen", "Refreshed ${cars.size} cars from $dataSource")
                }
                is NetworkResult.Error -> {
                    errorMessage = result.message
                    dataSource = "Error"
                    android.util.Log.e("HomeScreen", "Error refreshing cars: ${result.message}")
                }
                is NetworkResult.Loading -> {
                    // Loading handled by isRefreshing state
                }
            }
            isRefreshing = false
        }
    }

    // Load cars on first composition and when filters change
    LaunchedEffect(selectedFilters, searchQuery) {
        loadCars()
    }

    // Auto-refresh every 30 seconds for real-time updates
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(30000) // 30 seconds
            if (!isLoading && !isRefreshing) {
                refreshCars()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header dengan border radius di bawah
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 4.dp,
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Halo, ${CurrentUserProvider.currentUser.name}!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Medium
                        )

                        // Status indicator (hanya tampilkan saat loading atau error)
                        if (isRefreshing || isLoading || errorMessage != null) {
                            Text(
                                text = when {
                                    isRefreshing -> "🔄 Memperbarui data..."
                                    isLoading -> "⏳ Memuat data..."
                                    errorMessage != null -> "⚠️ Error: ${errorMessage}"
                                    else -> ""
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = when {
                                    isRefreshing || isLoading -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                    errorMessage != null -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                }
                            )
                        }
                    }

                    // Refresh button dengan animasi memutar
                    IconButton(
                        onClick = { refreshCars() },
                        enabled = !isRefreshing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Data",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.rotate(rotationAngle)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { loadCars() },
                    placeholder = "Cari mobil impian Anda..."
                )
            }
        }
        
        // Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Filter Section
            item {
                Column {
                    Text(
                        text = "Kategori",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    FilterRow(
                        filters = carTypes,
                        selectedFilters = selectedFilters,
                        onFilterToggle = { filter ->
                            selectedFilters = if (filter == "Semua") {
                                if (selectedFilters.contains("Semua")) emptySet() else setOf("Semua")
                            } else {
                                val newFilters = selectedFilters.toMutableSet()
                                newFilters.remove("Semua")
                                if (newFilters.contains(filter)) {
                                    newFilters.remove(filter)
                                } else {
                                    newFilters.add(filter)
                                }
                                newFilters
                            }
                        },
                        carCounts = carCounts
                    )
                }
            }
            
            // Cars Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mobil Tersedia",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Text(
                        text = "${cars.size} mobil",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MediumGray
                    )
                }
            }
            
            // Loading State
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Error State
            if (errorMessage != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { loadCars() }
                            ) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                }
            }

            // Car List
            if (!isLoading && errorMessage == null) {
                items(cars) { car ->
                    CarCard(
                        car = car,
                        onClick = { onCarClick(car) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Empty State
                if (cars.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Tidak ada mobil ditemukan",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MediumGray,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Coba ubah kata kunci pencarian atau filter",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MediumGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
