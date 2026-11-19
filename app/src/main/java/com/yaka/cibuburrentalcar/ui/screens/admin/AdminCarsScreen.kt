package com.yaka.cibuburrentalcar.ui.screens.admin

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yaka.cibuburrentalcar.data.Car
import com.yaka.cibuburrentalcar.data.CarType
import com.yaka.cibuburrentalcar.data.FuelType
import com.yaka.cibuburrentalcar.data.TransmissionType
import com.yaka.cibuburrentalcar.data.repository.CarRepository
import com.yaka.cibuburrentalcar.data.repository.FirebaseStorageRepository
import com.yaka.cibuburrentalcar.data.repository.CalendarBlockRepository
import com.yaka.cibuburrentalcar.data.repository.NetworkResult
import com.yaka.cibuburrentalcar.ui.components.AppTopBar
import com.yaka.cibuburrentalcar.ui.screens.admin.components.AdminSingleDateCalendarDialog
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCarsScreen(
    onBack: () -> Unit
) {
    val repo = remember { CarRepository() }
    val storage = remember { FirebaseStorageRepository() }
    val blockRepo = remember { CalendarBlockRepository() }
    val scope = rememberCoroutineScope()

    var cars by remember { mutableStateOf<List<Car>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var showDialog by remember { mutableStateOf(false) }
    var editingCar by remember { mutableStateOf<Car?>(null) }

    var showMaintenanceDialog by remember { mutableStateOf(false) }
    var bookingBlockedDates by remember { mutableStateOf<List<String>>(emptyList()) }
    var maintenanceDates by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        isLoading = true
        when (val res = repo.getCars(availableOnly = false)) {
            is NetworkResult.Success -> cars = res.data ?: emptyList()
            is NetworkResult.Error -> error = res.message
            else -> {}
        }
        isLoading = false
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val bookingRepo = remember { com.yaka.cibuburrentalcar.data.repository.BookingRepository() }

    Scaffold(
        topBar = { AppTopBar(title = "Manajemen Mobil", onBackClick = onBack, centerTitle = true) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { editingCar = null; showDialog = true }) {
                Text("+")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
            error?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(cars, key = { it.id }) { car ->
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text(car.name, style = MaterialTheme.typography.titleMedium)
                                Text("${car.brand} • ${car.year} • ${car.type}", style = MaterialTheme.typography.bodySmall)
                                Text("Rp ${car.pricePerDay}/hari", style = MaterialTheme.typography.bodyMedium)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { editingCar = car; showDialog = true }) { Text("Edit") }
                                TextButton(onClick = {
                                    scope.launch {
                                        when (repo.deleteCar(car.id)) {
                                            is NetworkResult.Success -> {
                                                cars = cars.filterNot { it.id == car.id }
                                                snackbarHostState.showSnackbar("Mobil dihapus")
                                            }
                                            is NetworkResult.Error -> { snackbarHostState.showSnackbar("Gagal hapus mobil") }
                                            else -> {}
                                        }
                                    }
                                }) { Text("Hapus") }
                                TextButton(onClick = {
                                    // Open maintenance dialog and preload blocked dates
                                    editingCar = car
                                    scope.launch {
                                        // 1) Booking blocked dates (merah)
                                        when (val bookings = bookingRepo.getBlockedDates(car.id)) {
                                            is NetworkResult.Success -> bookingBlockedDates = bookings.data.orEmpty()
                                            else -> bookingBlockedDates = emptyList()
                                        }
                                        // 2) Maintenance dates (oranye)
                                        when (val res = blockRepo.listBlocks(car.id)) {
                                            is NetworkResult.Success -> {
                                                val blocks = res.data.orEmpty()
                                                val dates = blocks.flatMap { b ->
                                                    val start = LocalDate.parse(b.startDate)
                                                    val end = LocalDate.parse(b.endDate)
                                                    generateSequence(start) { d -> if (d.isBefore(end)) d.plusDays(1) else null }
                                                        .map { it.toString() } + end.toString()
                                                }
                                                maintenanceDates = dates
                                            }
                                            else -> maintenanceDates = emptyList()
                                        }
                                        showMaintenanceDialog = true
                                    }
                                }) { Text("Atur Maintenance") }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        CarEditorDialog(
            initial = editingCar,
            onDismiss = { showDialog = false },
            onSubmit = { draft, imageUri ->
                scope.launch {
                    isLoading = true
                    var finalImageUrl = draft.imageUrl
                    val carId = draft.id.ifBlank { System.currentTimeMillis().toString() }
                    if (imageUri != null) {
                        try { finalImageUrl = storage.uploadCarImage(carId, imageUri) } catch (_: Exception) {}
                    }
                    val carToSave = draft.copy(id = carId, imageUrl = finalImageUrl)
                    val result = if (editingCar == null) repo.addCar(carToSave) else repo.updateCar(carToSave)
                    when (result) {
                        is NetworkResult.Success -> {
                            // refresh list sederhana
                            when (val res2 = repo.getCars(availableOnly = false)) {
                                is NetworkResult.Success -> cars = res2.data ?: emptyList()
                                is NetworkResult.Error -> error = res2.message
                                else -> {}
                            }
                            showDialog = false
                        }
                        is NetworkResult.Error -> { error = result.message }
                        else -> {}
                    }
                    isLoading = false
                }
            }
        )
    }

    // Render maintenance single-date calendar dialog
    if (showMaintenanceDialog && editingCar != null) {
        AdminSingleDateCalendarDialog(
            bookingBlockedDates = bookingBlockedDates, // merah (user bookings)
            maintenanceDates = maintenanceDates,       // oranye (admin blocks)
            onToggleDate = { date ->
                val car = editingCar ?: return@AdminSingleDateCalendarDialog
                val dateStr = date.toString()
                scope.launch {
                    if (maintenanceDates.contains(dateStr)) {
                        // Unblock maintenance for this single date
                        when (val res = blockRepo.listBlocks(car.id)) {
                            is NetworkResult.Success -> {
                                val single = res.data.orEmpty().find { it.startDate == dateStr && it.endDate == dateStr }
                                if (single != null) {
                                    blockRepo.removeBlock(car.id, single.id)
                                    maintenanceDates = maintenanceDates - dateStr
                                    snackbarHostState.showSnackbar("Tanggal $dateStr dibuka")
                                } else {
                                    // If part of a range, we skip destructive split for now
                                    snackbarHostState.showSnackbar("Tanggal ada dalam rentang. Hapus via Admin Calendar.")
                                }
                            }
                            else -> snackbarHostState.showSnackbar("Gagal memuat blocks")
                        }
                    } else if (!bookingBlockedDates.contains(dateStr)) {
                        // Add single-day maintenance block
                        val uid = com.yaka.cibuburrentalcar.data.CurrentUserProvider.currentUser.id
                        val res = blockRepo.addBlock(
                            car.id,
                            com.yaka.cibuburrentalcar.data.CalendarBlock(
                                id = "",
                                startDate = dateStr,
                                endDate = dateStr,
                                reason = "Maintenance",
                                createdBy = uid
                            )
                        )
                        if (res is NetworkResult.Success) {
                            maintenanceDates = maintenanceDates + dateStr
                            snackbarHostState.showSnackbar("Tanggal $dateStr diblok (maintenance)")
                        } else {
                            snackbarHostState.showSnackbar("Gagal memblok tanggal")
                        }
                    } else {
                        snackbarHostState.showSnackbar("Tanggal ini dibooking user (tidak bisa diubah)")
                    }
                }
            },
            onDismiss = { showMaintenanceDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun CarEditorDialog(
    initial: Car?,
    onDismiss: () -> Unit,
    onSubmit: (Car, Uri?) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var brand by remember { mutableStateOf(initial?.brand ?: "") }
    var year by remember { mutableStateOf((initial?.year ?: 2023).toString()) }
    var type by remember { mutableStateOf(initial?.type ?: CarType.MPV) }
    var transmission by remember { mutableStateOf(initial?.transmission ?: TransmissionType.MANUAL) }
    var fuel by remember { mutableStateOf(initial?.fuelType ?: FuelType.GASOLINE) }
    var seats by remember { mutableStateOf((initial?.seats ?: 5).toString()) }
    var price by remember { mutableStateOf((initial?.pricePerDay ?: 0).toString()) }
    var imageUrl by remember { mutableStateOf(initial?.imageUrl ?: "") }
    var image1 by remember { mutableStateOf(initial?.imageUrls?.getOrNull(0) ?: "") }
    var image2 by remember { mutableStateOf(initial?.imageUrls?.getOrNull(1) ?: "") }
    var image3 by remember { mutableStateOf(initial?.imageUrls?.getOrNull(2) ?: "") }
    var image4 by remember { mutableStateOf(initial?.imageUrls?.getOrNull(3) ?: "") }
    var location by remember { mutableStateOf(initial?.location ?: "Jakarta") }
    var available by remember { mutableStateOf(initial?.isAvailable ?: true) }
    var maintenanceUntil by remember { mutableStateOf(initial?.maintenanceUntil?.toString() ?: "") }
    var pickedUri by remember { mutableStateOf<Uri?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Tambah Mobil" else "Edit Mobil") },
        text = {
            val scrollState = rememberScrollState()
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Tahun") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

                // Tipe Mobil
                Text(text = "Tipe Mobil", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(onClick = { type = CarType.MPV }, selected = type == CarType.MPV, label = { Text("MPV") })
                    FilterChip(onClick = { type = CarType.SUV }, selected = type == CarType.SUV, label = { Text("SUV") })
                    FilterChip(onClick = { type = CarType.SEDAN }, selected = type == CarType.SEDAN, label = { Text("Sedan") })
                    FilterChip(onClick = { type = CarType.HATCHBACK }, selected = type == CarType.HATCHBACK, label = { Text("Hatchback") })
                }

                // Transmisi
                Text(text = "Transmisi", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(onClick = { transmission = TransmissionType.MANUAL }, selected = transmission == TransmissionType.MANUAL, label = { Text("Manual") })
                    FilterChip(onClick = { transmission = TransmissionType.AUTOMATIC }, selected = transmission == TransmissionType.AUTOMATIC, label = { Text("Automatic") })
                    FilterChip(onClick = { transmission = TransmissionType.CVT }, selected = transmission == TransmissionType.CVT, label = { Text("CVT") })
                }

                // Bahan Bakar
                Text(text = "Bahan Bakar", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(onClick = { fuel = FuelType.GASOLINE }, selected = fuel == FuelType.GASOLINE, label = { Text("Bensin") })
                    FilterChip(onClick = { fuel = FuelType.DIESEL }, selected = fuel == FuelType.DIESEL, label = { Text("Diesel") })
                    FilterChip(onClick = { fuel = FuelType.ELECTRIC }, selected = fuel == FuelType.ELECTRIC, label = { Text("Listrik") })
                    FilterChip(onClick = { fuel = FuelType.HYBRID }, selected = fuel == FuelType.HYBRID, label = { Text("Hybrid") })
                }

                OutlinedTextField(value = seats, onValueChange = { seats = it }, label = { Text("Kursi") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Harga/Hari") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Lokasi") }, modifier = Modifier.fillMaxWidth())

                // Link gambar (maks 4). Isi yang terpakai saja.
                Text(text = "Link Gambar", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Gambar Utama (opsional)") }, placeholder = { Text("https://...") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = image1, onValueChange = { image1 = it }, label = { Text("Gambar 1 (opsional)") }, placeholder = { Text("https://...") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = image2, onValueChange = { image2 = it }, label = { Text("Gambar 2 (opsional)") }, placeholder = { Text("https://...") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = image3, onValueChange = { image3 = it }, label = { Text("Gambar 3 (opsional)") }, placeholder = { Text("https://...") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = image4, onValueChange = { image4 = it }, label = { Text("Gambar 4 (opsional)") }, placeholder = { Text("https://...") }, modifier = Modifier.fillMaxWidth())

                Spacer(Modifier.height(8.dp))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val images = listOf(image1, image2, image3, image4).filter { it.isNotBlank() }
                val car = Car(
                    id = initial?.id ?: "",
                    name = name,
                    brand = brand,
                    year = year.toIntOrNull() ?: 2023,
                    type = type,
                    transmission = transmission,
                    fuelType = fuel,
                    seats = seats.toIntOrNull() ?: 5,
                    pricePerDay = price.toIntOrNull() ?: 0,
                    imageUrl = imageUrl,
                    imageUrls = images,
                    rating = initial?.rating ?: 0f,
                    reviewCount = initial?.reviewCount ?: 0,
                    features = initial?.features ?: emptyList(),
                    isAvailable = available,
                    location = location,
                    maintenanceUntil = null
                )
                onSubmit(car, pickedUri)
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}


