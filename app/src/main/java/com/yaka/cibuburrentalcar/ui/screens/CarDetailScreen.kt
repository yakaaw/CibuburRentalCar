package com.yaka.cibuburrentalcar.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import coil.compose.AsyncImage
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yaka.cibuburrentalcar.data.Car
import com.yaka.cibuburrentalcar.data.FuelType
import com.yaka.cibuburrentalcar.R
import com.yaka.cibuburrentalcar.ui.components.IconButton
import com.yaka.cibuburrentalcar.ui.components.PrimaryButton
import com.yaka.cibuburrentalcar.ui.theme.EmeraldGreen
import com.yaka.cibuburrentalcar.ui.theme.MediumGray
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarDetailScreen(
    carId: String,
    onBackClick: () -> Unit,
    onBookClick: (Car) -> Unit,
    onImageClick: (String, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    viewModel: com.yaka.cibuburrentalcar.ui.viewmodel.CarDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(carId) {
        viewModel.loadCarDetail(carId)
    }

    when {
        uiState.isLoading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        uiState.error != null -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error: ${uiState.error}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadCarDetail(carId) }) {
                        Text("Retry")
                    }
                }
            }
        }
        uiState.car != null -> {
            CarDetailContent(
                car = uiState.car!!,
                onBackClick = onBackClick,
                onBookClick = onBookClick,
                onImageClick = onImageClick,
                modifier = modifier
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CarDetailContent(
    car: Car,
    onBackClick: () -> Unit,
    onBookClick: (Car) -> Unit,
    onImageClick: (String, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with Image Slider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            // Image Slider: gunakan URL jika ada; jika tidak, fallback ke drawable lokal 20 mobil
            val networkImages = buildList {
                addAll(car.imageUrls.filter { it.isNotBlank() && it.startsWith("http", true) })
                if (car.imageUrl.startsWith("http", true) && !contains(car.imageUrl)) add(car.imageUrl)
            }
            val localImages = if (networkImages.size < 2) when (car.name) {
                "Brio" -> listOf(R.drawable.brio1, R.drawable.brio2, R.drawable.brio3, R.drawable.brio4)
                "BRV" -> listOf(R.drawable.brv1, R.drawable.brv2, R.drawable.brv3, R.drawable.brv4)
                "Calya" -> listOf(R.drawable.calya1, R.drawable.calya2, R.drawable.calya3, R.drawable.calya4)
                "Camry" -> listOf(R.drawable.camry1, R.drawable.camry2, R.drawable.camry3, R.drawable.camry4)
                "City RS" -> listOf(R.drawable.cityrs1, R.drawable.cityrs2, R.drawable.cityrs3, R.drawable.cityrs4)
                "Ertiga" -> listOf(R.drawable.ertiga1, R.drawable.ertiga2, R.drawable.ertiga3, R.drawable.ertiga4)
                "HRV" -> listOf(R.drawable.hrv1, R.drawable.hrv2, R.drawable.hrv3, R.drawable.hrv4)
                "Innova" -> listOf(R.drawable.innovag1, R.drawable.innovag2, R.drawable.innovag3, R.drawable.innovag4)
                "Jazz" -> listOf(R.drawable.jazz1, R.drawable.jazz2, R.drawable.jazz3, R.drawable.jazz4)
                "Mobilio" -> listOf(R.drawable.mobilio1, R.drawable.mobilio2, R.drawable.mobilio3, R.drawable.mobilio4)
                "Innova G" -> listOf(R.drawable.reborn1, R.drawable.reborn2, R.drawable.reborn3, R.drawable.reborn4)
                "Sigra" -> listOf(R.drawable.sigra1, R.drawable.sigra2, R.drawable.sigra3, R.drawable.sigra4)
                "Terios" -> listOf(R.drawable.terios1, R.drawable.terios2, R.drawable.terios3, R.drawable.terios4)
                "Fortuner VRZ" -> listOf(R.drawable.vrz1, R.drawable.vrz2, R.drawable.vrz3, R.drawable.vrz4)
                "Xenia" -> listOf(R.drawable.xenia1, R.drawable.xenia2, R.drawable.xenia3, R.drawable.xenia4)
                "XL7" -> listOf(R.drawable.xl7_1, R.drawable.xl7_2, R.drawable.xl7_3, R.drawable.xl7_4)
                "Xpander Cross" -> listOf(R.drawable.xpandercross1, R.drawable.xpandercross2, R.drawable.xpandercross3, R.drawable.xpandercross4)
                "Yaris" -> listOf(R.drawable.yaris1, R.drawable.yaris2, R.drawable.yaris3, R.drawable.yaris4)
                "Innova Zenix" -> listOf(R.drawable.zenix1, R.drawable.zenix2, R.drawable.zenix3, R.drawable.zenix4)
                "Pajero Sport" -> listOf(R.drawable.jaresopo1, R.drawable.jaresopo2, R.drawable.jaresopo3, R.drawable.jaresopo4)
                else -> emptyList()
            } else emptyList()
            val useLocal = localImages.isNotEmpty()
            val pages = if (useLocal) localImages.size else if (networkImages.isNotEmpty()) networkImages.size else 1
            val pagerState = rememberPagerState(pageCount = { pages })

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            onImageClick(car.id, page)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (networkImages.isNotEmpty()) {
                        AsyncImage(
                            model = networkImages[page],
                            contentDescription = "Car image ${page + 1}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alpha = if (car.isAvailable) 1f else 0.5f
                        )
                    } else if (localImages.isNotEmpty()) {
                        Image(
                            painter = painterResource(id = localImages[page]),
                            contentDescription = "Car image ${page + 1}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alpha = if (car.isAvailable) 1f else 0.5f
                        )
                    } else {
                        // Fallback terakhir jika tidak ada data
                        Image(
                            painter = painterResource(id = R.drawable.xenia1),
                            contentDescription = "placeholder",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alpha = if (car.isAvailable) 1f else 0.5f
                        )
                    }
                }
            }

            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            // Page Indicators
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (pagerState.currentPage == index)
                                    Color.White else Color.White.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                    )
                }
            }
            
            // Back Button
            IconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                onClick = onBackClick,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp)
                    .align(Alignment.TopStart),
                contentDescription = "Back",
                tint = Color.White
            )
            
            // Availability Badge
            if (!car.isAvailable) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(16.dp)
                        .background(
                            Color.Red.copy(alpha = 0.9f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Tidak Tersedia",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        // Content
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .alpha(if (car.isAvailable) 1f else 0.6f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Car Info
            item {
                Column {
                    Text(
                        text = car.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Text(
                        text = car.brand,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MediumGray
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = MediumGray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = car.location,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MediumGray
                        )
                    }
                }
            }
            
            // Price
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Harga Sewa",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = formatPrice(car.pricePerDay),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            text = "per hari",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Specifications
            item {
                Column {
                    Text(
                        text = "Spesifikasi",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SpecificationRow("Tipe", car.type.name.lowercase().replaceFirstChar { 
                                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
                            })
                            SpecificationRow("Transmisi", car.transmission.name.lowercase().replaceFirstChar { 
                                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
                            })
                            SpecificationRow("Bahan Bakar", when (car.fuelType) {
                                FuelType.GASOLINE -> "Bensin"
                                FuelType.DIESEL -> "Diesel"
                                FuelType.ELECTRIC -> "Listrik"
                                FuelType.HYBRID -> "Hybrid"
                            })
                            SpecificationRow("Kapasitas", "${car.seats} orang")
                        }
                    }
                }
            }
            

        }
        
        // Bottom Action
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            PrimaryButton(
                text = if (car.isAvailable) "Sewa Sekarang" else "Tidak Tersedia",
                onClick = { onBookClick(car) },
                enabled = car.isAvailable,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun SpecificationRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MediumGray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun FeatureChip(
    feature: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.secondaryContainer,
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = feature,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

private fun formatPrice(price: Int): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return formatter.format(price).replace("IDR", "Rp")
}
