package com.yaka.cibuburrentalcar.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yaka.cibuburrentalcar.R
import com.yaka.cibuburrentalcar.ui.components.IconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageViewerScreen(
    carId: String,
    initialPage: Int = 0,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    carRepository: com.yaka.cibuburrentalcar.data.repository.CarRepository = com.yaka.cibuburrentalcar.data.repository.CarRepository()
) {
    // Ambil data mobil berdasarkan carId agar fullscreen sesuai dengan slider/URL
    var car by remember { mutableStateOf<com.yaka.cibuburrentalcar.data.Car?>(null) }

    LaunchedEffect(carId) {
        when (val result = carRepository.getCarById(carId)) {
            is com.yaka.cibuburrentalcar.data.repository.NetworkResult.Success -> car = result.data
            else -> car = null
        }
    }

    val networkImages = remember(car) {
        buildList {
            car?.let { c ->
                addAll(c.imageUrls.filter { it.isNotBlank() && it.startsWith("http", true) })
                if (c.imageUrl.startsWith("http", true) && !contains(c.imageUrl)) add(c.imageUrl)
            }
        }
    }

    // Fallback lokal untuk 20 mobil lama jika tidak ada URL
    val localImages = remember(car) {
        if (networkImages.isEmpty()) when (car?.name) {
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
    }

    val useLocal = localImages.isNotEmpty() && networkImages.size < 2
    val totalPages = when {
        useLocal -> localImages.size
        networkImages.isNotEmpty() -> networkImages.size
        else -> 1
    }
    val pagerState = rememberPagerState(
        initialPage = initialPage.coerceIn(0, totalPages - 1),
        pageCount = { totalPages }
    )

    // Ensure initialPage is applied after images are loaded
    LaunchedEffect(totalPages, initialPage) {
        if (totalPages > 0) {
            pagerState.scrollToPage(initialPage.coerceIn(0, totalPages - 1))
        }
    }


    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Image Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when {
                useLocal -> {
                    ZoomableImage(
                        imageRes = localImages[page],
                        contentDescription = "Car image ${page + 1}"
                    )
                }
                networkImages.isNotEmpty() -> {
                    ZoomableImageUrl(
                        imageUrl = networkImages[page],
                        contentDescription = "Car image ${page + 1}"
                    )
                }
                else -> {
                    ZoomableImage(
                        imageRes = R.drawable.xenia1,
                        contentDescription = "placeholder"
                    )
                }
            }
        }

        // Top Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            color = Color.Black.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    onClick = onBackClick,
                    contentDescription = "Back",
                    tint = Color.White
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = car?.name ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    val total = maxOf(networkImages.size, 1)
                    Text(
                        text = "${pagerState.currentPage + 1} dari $total",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Page Indicators
        if (networkImages.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(networkImages.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                color = if (pagerState.currentPage == index)
                                    Color.White else Color.White.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun ZoomableImage(
    imageRes: Int,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)

        val maxX = (scale - 1) * 500f
        val maxY = (scale - 1) * 500f

        offset = Offset(
            x = (offset.x + offsetChange.x).coerceIn(-maxX, maxX),
            y = (offset.y + offsetChange.y).coerceIn(-maxY, maxY)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer(
                translationX = offset.x,
                translationY = offset.y,
                scaleX = scale,
                scaleY = scale
            )
            .transformable(state = transformableState),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun ZoomableImageUrl(
    imageUrl: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)

        val maxX = (scale - 1) * 500f
        val maxY = (scale - 1) * 500f

        offset = Offset(
            x = (offset.x + offsetChange.x).coerceIn(-maxX, maxX),
            y = (offset.y + offsetChange.y).coerceIn(-maxY, maxY)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer(
                translationX = offset.x,
                translationY = offset.y,
                scaleX = scale,
                scaleY = scale
            )
            .transformable(state = transformableState),
        contentAlignment = Alignment.Center
    ) {
        coil.compose.AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }

}
