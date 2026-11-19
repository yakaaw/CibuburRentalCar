package com.yaka.cibuburrentalcar.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yaka.cibuburrentalcar.R
import com.yaka.cibuburrentalcar.data.Car
import com.yaka.cibuburrentalcar.data.CurrentUserProvider
import com.yaka.cibuburrentalcar.data.RentalRequest
import com.yaka.cibuburrentalcar.data.repository.NetworkResult
import com.yaka.cibuburrentalcar.data.repository.BookingRepository
import com.yaka.cibuburrentalcar.data.repository.CarRepository
import com.yaka.cibuburrentalcar.ui.components.IconButton
import com.yaka.cibuburrentalcar.ui.components.PrimaryButton
import com.yaka.cibuburrentalcar.ui.theme.MediumGray
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.Instant
import java.time.ZoneId
import java.util.*
// Using RentalRequest from data package

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    car: Car,
    onBackClick: () -> Unit,
    onBookingConfirm: (RentalRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }

    var notes by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var hasActiveBooking by remember { mutableStateOf(false) }
    var activeBookingMessage by remember { mutableStateOf<String?>(null) }
    // Calendar blocking state - ACTIVATED with force recomposition
    var blockedDates by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingBlockedDates by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val bookingRepository = remember { BookingRepository() }
    val carRepository = remember { CarRepository() }

    // Auto load user data when screen opens
    LaunchedEffect(Unit) {
        val currentUser = CurrentUserProvider.currentUser
        customerName = currentUser.name
        customerPhone = currentUser.phone
        android.util.Log.d("BookingScreen", "✅ Auto loaded user data - Name: ${currentUser.name}, Phone: ${currentUser.phone}")
    }

    val totalDays = remember(startDate, endDate) {
        if (startDate != null && endDate != null) {
            (endDate!!.toEpochDay() - startDate!!.toEpochDay()).toInt()
        } else 0
    }

    val totalPrice = remember(totalDays) {
        totalDays * car.pricePerDay
    }



    // Load blocked dates dari database saat screen loads - ACTIVATED
    LaunchedEffect(car.id, car.name) {
        android.util.Log.d("BookingScreen", "🗓️ Loading blocked dates for car: ${car.id} (${car.name})")
        android.util.Log.d("BookingScreen", "🔥 Using Firebase backend")

        isLoadingBlockedDates = true

        try {
            when (val result = bookingRepository.getBlockedDates(car.id)) {
                is NetworkResult.Success -> {
                    val newBlockedDates = result.data ?: emptyList()
                    android.util.Log.d("BookingScreen", "✅ Blocked dates loaded: ${newBlockedDates.size} dates")
                    android.util.Log.d("BookingScreen", "📅 Blocked dates: $newBlockedDates")

                    // Force state update with explicit assignment and delay for recomposition
                    blockedDates = newBlockedDates.toMutableList()

                    android.util.Log.d("BookingScreen", "🔄 State updated - blockedDates size: ${blockedDates.size}")
                    android.util.Log.d("BookingScreen", "🔄 Current blockedDates state: $blockedDates")

                    if (blockedDates.isNotEmpty()) {
                        android.util.Log.d("BookingScreen", "🔴 CALENDAR BLOCKING ACTIVE - Found ${blockedDates.size} blocked dates")
                        blockedDates.forEach { date ->
                            android.util.Log.d("BookingScreen", "🚫 Blocked date: $date")
                        }
                    } else {
                        android.util.Log.d("BookingScreen", "✅ No blocked dates found - calendar is clear")
                    }
                }
                is NetworkResult.Error<*> -> {
                    android.util.Log.e("BookingScreen", "❌ Failed to load blocked dates: ${result.message}")
                    blockedDates = emptyList()
                }
                is NetworkResult.Loading<*> -> {
                    android.util.Log.d("BookingScreen", "⏳ Loading blocked dates...")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("BookingScreen", "❌ Exception loading blocked dates: ${e.message}")
            android.util.Log.e("BookingScreen", "❌ Exception stack trace:", e)
            blockedDates = emptyList()
        } finally {
            isLoadingBlockedDates = false
        }
    }
    // Check for active booking when screen loads (using current user data)
    LaunchedEffect(Unit) {
        val userIdentifier = "${CurrentUserProvider.currentUser.name}_${CurrentUserProvider.currentUser.phone}"
        android.util.Log.d("BookingScreen", "Checking for active booking for user: $userIdentifier")
        when (val result = bookingRepository.hasActiveBooking(userIdentifier)) {
            is NetworkResult.Success -> {
                hasActiveBooking = result.data ?: false
                if (hasActiveBooking) {
                    // Get active booking details
                    when (val bookingResult = bookingRepository.getActiveBooking(userIdentifier)) {
                        is NetworkResult.Success -> {
                            val booking = bookingResult.data
                            activeBookingMessage = "Anda sudah memiliki pesanan aktif untuk Car ID: ${booking?.toString()?.take(50) ?: "Unknown"}"
                            android.util.Log.d("BookingScreen", "Active booking found: ${booking?.toString()}")
                        }
                        else -> {
                            activeBookingMessage = "Anda sudah memiliki pesanan aktif"
                        }
                    }
                }
            }
            is NetworkResult.Error<*> -> {
                android.util.Log.e("BookingScreen", "Error checking active booking: ${result.message}")
            }
            else -> {}
        }
    }

    // Function to generate WhatsApp message
    fun generateWhatsAppMessage(): String {
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return """
            Halo Admin, saya ingin memesan mobil dengan detail berikut:

            - Nama: $customerName
            - Nomor HP: $customerPhone
            - Mobil: ${car.name}
            - Tanggal Sewa: ${startDate?.format(dateFormatter)} - ${endDate?.format(dateFormatter)}
            - Durasi Sewa: $totalDays hari

            - Total Harga: ${formatPrice(totalPrice)}
            ${if (notes.isNotBlank()) "- Catatan: $notes" else ""}

            Mohon konfirmasi ketersediaannya. Terima kasih!
        """.trimIndent()
    }

    // Function to open WhatsApp
    fun openWhatsApp() {
        val adminPhoneNumber = "6287859947782" // Nomor admin WhatsApp
        val message = generateWhatsAppMessage()
        val encodedMessage = Uri.encode(message)

        val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://wa.me/$adminPhoneNumber?text=$encodedMessage")
        }

        try {
            context.startActivity(whatsappIntent)
        } catch (e: Exception) {
            // Fallback jika WhatsApp tidak terinstall
            val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://web.whatsapp.com/send?phone=$adminPhoneNumber&text=$encodedMessage")
            }
            context.startActivity(browserIntent)
        }
    }
    
    val isFormValid = customerName.isNotBlank() && customerPhone.isNotBlank() &&
            startDate != null && endDate != null && totalDays > 0
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        com.yaka.cibuburrentalcar.ui.components.AppTopBar(
            title = "Booking Mobil",
            onBackClick = onBackClick,
            centerTitle = true
        )
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Active Booking Warning
            if (hasActiveBooking) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pesanan Aktif Ditemukan",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = activeBookingMessage ?: "Anda sudah memiliki pesanan aktif",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    // Cancel active booking for demo
                                    coroutineScope.launch {
                                        val userIdentifier = "${CurrentUserProvider.currentUser.name}_${CurrentUserProvider.currentUser.phone}"
                                        when (val result = bookingRepository.cancelBooking(userIdentifier)) {
                                            is NetworkResult.Success -> {
                                                hasActiveBooking = false
                                                activeBookingMessage = null
                                                android.util.Log.d("BookingScreen", "Active booking cancelled")
                                            }
                                            is NetworkResult.Error<*> -> {
                                                android.util.Log.e("BookingScreen", "Failed to cancel booking: ${result.message}")
                                            }
                                            else -> {}
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Batalkan Pesanan Aktif")
                            }
                        }
                    }
                }
            }

            // Car Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Car Image
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            // Use dynamic drawable loading like in CarCard
                            val context = LocalContext.current
                            val drawableId = context.resources.getIdentifier(
                                car.imageUrl,
                                "drawable",
                                context.packageName
                            )

                            if (drawableId != 0) {
                                Image(
                                    painter = painterResource(id = drawableId),
                                    contentDescription = "${car.brand} ${car.name}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // Fallback mapping for 20 mobil yang benar
                                Image(
                                    painter = painterResource(
                                        id = when (car.name) {
                                            // 20 mobil yang benar sesuai koreksi
                                            "Brio" -> R.drawable.brio1
                                            "BRV" -> R.drawable.brv1
                                            "Calya" -> R.drawable.calya1
                                            "Camry" -> R.drawable.camry1
                                            "City RS" -> R.drawable.cityrs1
                                            "Ertiga" -> R.drawable.ertiga1
                                            "HRV" -> R.drawable.hrv1
                                            "Innova" -> R.drawable.innovag1
                                            "Jazz" -> R.drawable.jazz1
                                            "Mobilio" -> R.drawable.mobilio1
                                            "Innova G" -> R.drawable.reborn1
                                            "Sigra" -> R.drawable.sigra1
                                            "Terios" -> R.drawable.terios1
                                            "Fortuner VRZ" -> R.drawable.vrz1
                                            "Xenia" -> R.drawable.xenia1
                                            "XL7" -> R.drawable.xl7_1
                                            "Xpander Cross" -> R.drawable.xpandercross1
                                            "Yaris" -> R.drawable.yaris1
                                            "Innova Zenix" -> R.drawable.zenix1
                                            "Pajero Sport" -> R.drawable.jaresopo1
                                            else -> R.drawable.xenia1
                                        }
                                    ),
                                    contentDescription = "${car.brand} ${car.name}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = car.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = car.brand,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MediumGray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatPrice(car.pricePerDay) + "/hari",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Customer Information
            item {
                Column {
                    Text(
                        text = "Informasi Pemesan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    // Helper text untuk auto-filled data
                    if (customerName.isNotBlank() && customerPhone.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Data otomatis terisi dari akun Anda",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Nama Lengkap") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Name",
                                tint = if (customerName.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = if (customerName.isNotBlank()) {
                            {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Auto filled",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (customerName.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            focusedLabelColor = if (customerName.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("No HP (WhatsApp)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Phone",
                                tint = if (customerPhone.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = if (customerPhone.isNotBlank()) {
                            {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Auto filled",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (customerPhone.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            focusedLabelColor = if (customerPhone.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            // Date Selection
            item {
                Column {
                    Text(
                        text = "Tanggal Sewa",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Start Date
                        DatePickerField(
                            label = "Tanggal Mulai",
                            date = startDate,
                            onDateSelected = { startDate = it },
                            blockedDates = blockedDates, // ACTIVATED
                            modifier = Modifier.weight(1f)
                        )

                        // End Date
                        DatePickerField(
                            label = "Tanggal Selesai",
                            date = endDate,
                            onDateSelected = { endDate = it },
                            blockedDates = blockedDates, // ACTIVATED
                            modifier = Modifier.weight(1f)
                        )
                    }


                    
                    if (totalDays > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Durasi: $totalDays hari",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            

            
            // Notes
            item {
                Column {
                    Text(
                        text = "Catatan (Opsional)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Tambahkan catatan...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        maxLines = 3
                    )
                }
            }
            
            // Price Summary
            if (totalDays > 0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Ringkasan Biaya",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Harga per hari",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = formatPrice(car.pricePerDay),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Durasi",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "$totalDays hari",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = formatPrice(totalPrice),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
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
            Button(
                onClick = {
                    if (isFormValid) {
                        isLoading = true

                        // Log booking attempt
                        android.util.Log.d("BookingScreen", "🚀 Attempting to book car ${car.id} from ${startDate} to ${endDate}")

                        // Calculate booking details
                        val totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate).toInt()
                        val pricePerDay = car.pricePerDay
                        val totalPrice = totalDays * pricePerDay

                        val request = RentalRequest(
                            userId = CurrentUserProvider.currentUser.id,
                            carId = car.id,
                            startDate = startDate!!,
                            endDate = endDate!!,
                            pickupLocation = "Cibubur",
                            dropoffLocation = "Cibubur",
                            totalDays = totalDays,
                            pricePerDay = pricePerDay,
                            totalPrice = totalPrice,
                            notes = notes,
                            status = "PENDING",
                            customerName = customerName,
                            customerPhone = customerPhone
                        )

                        // Real-time availability check and booking
                        coroutineScope.launch {
                            // Calendar blocking validation - ACTIVATED
                            // Check blocked dates first
                            android.util.Log.d("BookingScreen", "🔍 Checking blocked dates for car ${car.id}")

                            // Check if selected dates are blocked
                            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            val selectedDates = mutableListOf<String>()

                            // Generate all dates between start and end
                            var currentDate = startDate!!
                            while (!currentDate.isAfter(endDate!!)) {
                                selectedDates.add(currentDate.format(dateFormatter))
                                currentDate = currentDate.plusDays(1)
                            }

                            // Check if any selected date is blocked - ENHANCED VALIDATION
                            android.util.Log.d("BookingScreen", "🔍 BOOKING VALIDATION START")
                            android.util.Log.d("BookingScreen", "🔍 Car: ${car.name} (ID: ${car.id})")
                            android.util.Log.d("BookingScreen", "🔍 Selected dates: $selectedDates")
                            android.util.Log.d("BookingScreen", "🔍 Current blocked dates: $blockedDates")
                            android.util.Log.d("BookingScreen", "🔍 Blocked dates count: ${blockedDates.size}")

                            // Double-check by reloading blocked dates before booking
                            android.util.Log.d("BookingScreen", "🔄 Reloading blocked dates for final validation...")
                            try {
                                when (val reloadResult = bookingRepository.getBlockedDates(car.id)) {
                                    is NetworkResult.Success -> {
                                        val latestBlockedDates = reloadResult.data ?: emptyList()
                                        android.util.Log.d("BookingScreen", "🔄 Latest blocked dates: $latestBlockedDates")

                                        val blockedSelectedDates = selectedDates.filter { latestBlockedDates.contains(it) }
                                        android.util.Log.d("BookingScreen", "🔍 Final blocked selected dates: $blockedSelectedDates")

                                        if (blockedSelectedDates.isNotEmpty()) {
                                            android.util.Log.w("BookingScreen", "❌ BOOKING BLOCKED: Cannot book blocked dates: $blockedSelectedDates")
                                            Toast.makeText(
                                                context,
                                                "❌ Tanggal ${blockedSelectedDates.joinToString(", ")} sudah dipesan oleh orang lain. Silakan pilih tanggal lain.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            isLoading = false
                                            return@launch
                                        }
                                    }
                                    is NetworkResult.Error<*> -> {
                                        android.util.Log.w("BookingScreen", "⚠️ Could not reload blocked dates, using cached data")
                                        val blockedSelectedDates = selectedDates.filter { blockedDates.contains(it) }
                                        if (blockedSelectedDates.isNotEmpty()) {
                                            android.util.Log.w("BookingScreen", "❌ BOOKING BLOCKED (cached): Cannot book blocked dates: $blockedSelectedDates")
                                            Toast.makeText(
                                                context,
                                                "❌ Tanggal ${blockedSelectedDates.joinToString(", ")} sudah dipesan. Silakan pilih tanggal lain.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            isLoading = false
                                            return@launch
                                        }
                                    }
                                    is NetworkResult.Loading<*> -> {
                                        android.util.Log.d("BookingScreen", "⏳ Still loading blocked dates...")
                                    }
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("BookingScreen", "❌ Error reloading blocked dates: ${e.message}")
                                // Fallback to cached blocked dates
                                val blockedSelectedDates = selectedDates.filter { blockedDates.contains(it) }
                                if (blockedSelectedDates.isNotEmpty()) {
                                    Toast.makeText(
                                        context,
                                        "❌ Tanggal ${blockedSelectedDates.joinToString(", ")} sudah dipesan. Silakan pilih tanggal lain.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    isLoading = false
                                    return@launch
                                }
                            }

                            // Check if any selected date is in the past
                            val today = LocalDate.now()
                            val pastDates = selectedDates.filter { dateStr ->
                                try {
                                    LocalDate.parse(dateStr, dateFormatter).isBefore(today)
                                } catch (e: Exception) {
                                    false
                                }
                            }

                            if (pastDates.isNotEmpty()) {
                                android.util.Log.w("BookingScreen", "❌ Cannot book past dates: $pastDates")
                                Toast.makeText(
                                    context,
                                    "Tidak dapat memesan tanggal yang sudah lewat.",
                                    Toast.LENGTH_LONG
                                ).show()
                                isLoading = false
                                return@launch
                            }

                            android.util.Log.d("BookingScreen", "✅ All selected dates are valid, proceeding with booking")

                            // Car is available, proceed with booking
                            android.util.Log.d("BookingScreen", "Car ${car.id} is available, creating booking")

                            // Extract values to avoid scope issues
                            val bookingCarId = request.carId
                            val bookingStartDate = request.startDate
                            val bookingEndDate = request.endDate
                            val bookingPickupLocation = request.pickupLocation
                            val bookingDropoffLocation = request.dropoffLocation
                            val bookingTotalDays = request.totalDays
                            val bookingPricePerDay = request.pricePerDay
                            val bookingTotalPrice = request.totalPrice
                            val bookingNotes = request.notes

                            when (val result = bookingRepository.createBooking(
                                carId = bookingCarId,
                                userId = CurrentUserProvider.currentUser.id,
                                startDate = bookingStartDate,
                                endDate = bookingEndDate,
                                pickupLocation = bookingPickupLocation,
                                dropoffLocation = bookingDropoffLocation,
                                totalDays = bookingTotalDays,
                                pricePerDay = bookingPricePerDay,
                                totalPrice = bookingTotalPrice,
                                notes = bookingNotes
                            )) {
                                is NetworkResult.Success -> {
                                    // Booking berhasil, reload blocked dates dan buka WhatsApp
                                    android.util.Log.d("BookingScreen", "Booking successful for car ${car.id}")

                                    // Reload blocked dates untuk update calendar - ACTIVATED
                                    try {
                                        when (val blockedResult = bookingRepository.getBlockedDates(car.id)) {
                                            is NetworkResult.Success -> {
                                                blockedDates = blockedResult.data ?: emptyList()
                                                android.util.Log.d("BookingScreen", "✅ Blocked dates reloaded: ${blockedDates.size} dates")
                                            }
                                            is NetworkResult.Error<*> -> {
                                                android.util.Log.e("BookingScreen", "❌ Failed to reload blocked dates")
                                            }
                                            is NetworkResult.Loading<*> -> {
                                                android.util.Log.d("BookingScreen", "⏳ Reloading blocked dates...")
                                            }
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("BookingScreen", "❌ Exception reloading blocked dates: ${e.message}")
                                    }

                                    openWhatsApp()
                                    onBookingConfirm(request)
                                }
                                is NetworkResult.Error<*> -> {
                                    // Booking gagal, tetap buka WhatsApp tapi tampilkan error
                                    android.util.Log.e("BookingScreen", "Booking failed: ${result.message}")
                                    openWhatsApp()
                                    onBookingConfirm(request)
                                }
                                is NetworkResult.Loading<*> -> {
                                    // Loading handled by isLoading state
                                }
                            }
                            isLoading = false
                        }
                    }
                },
                enabled = isFormValid && !isLoading && !hasActiveBooking,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF25D366) // WhatsApp green color
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        // WhatsApp icon placeholder (you can add actual WhatsApp icon here)
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "WhatsApp",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isLoading) "Memproses..." else "Kirim ke WhatsApp",
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    label: String,
    date: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    blockedDates: List<String> = emptyList(), // ACTIVATED
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    // Elegant Card Design for Date Field
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
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
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = date?.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", java.util.Locale("id", "ID")))
                        ?: "Pilih tanggal",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (date != null) MaterialTheme.colorScheme.onSurface
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (date != null) FontWeight.Medium else FontWeight.Normal
                )
            }

            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "Pilih Tanggal",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }

    // Custom Elegant Calendar Dialog
    if (showDatePicker) {
        // Force recomposition with key
        key(blockedDates.size, blockedDates.hashCode()) {
            CustomCalendarDialog(
                selectedDate = date,
                blockedDates = blockedDates, // ACTIVATED
                onDateSelected = { selectedDate ->
                    onDateSelected(selectedDate)
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }
    }
}

@Composable
private fun CustomCalendarDialog(
    selectedDate: LocalDate?,
    blockedDates: List<String> = emptyList(), // ACTIVATED
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var currentMonth by remember { mutableStateOf(selectedDate ?: LocalDate.now()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header
                Text(
                    text = "Pilih Tanggal",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Month Navigation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { currentMonth = currentMonth.minusMonths(1) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous Month",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale("id", "ID"))),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(
                        onClick = { currentMonth = currentMonth.plusMonths(1) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next Month",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Calendar Grid with forced recomposition
                key(currentMonth, blockedDates.size, blockedDates.hashCode()) {
                    CustomCalendarGrid(
                        currentMonth = currentMonth,
                        selectedDate = selectedDate,
                        blockedDates = blockedDates, // ACTIVATED
                        onDateSelected = onDateSelected
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("Batal")
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomCalendarGrid(
    currentMonth: LocalDate,
    selectedDate: LocalDate?,
    blockedDates: List<String> = emptyList(), // ACTIVATED
    onDateSelected: (LocalDate) -> Unit
) {
    // Debug log untuk memastikan parameter sampai
    android.util.Log.d("CustomCalendarGrid", "🗓️ Rendering calendar for month: $currentMonth")
    android.util.Log.d("CustomCalendarGrid", "📋 Received blocked dates: $blockedDates")
    val firstDayOfMonth = currentMonth.withDayOfMonth(1)
    val lastDayOfMonth = currentMonth.withDayOfMonth(currentMonth.lengthOfMonth())
    // Convert Java DayOfWeek to Sunday=0 format
    // Java: MONDAY=1, TUESDAY=2, ..., SUNDAY=7
    // We want: SUNDAY=0, MONDAY=1, ..., SATURDAY=6
    val firstDayOfWeek = when (firstDayOfMonth.dayOfWeek.value) {
        7 -> 0 // Sunday
        else -> firstDayOfMonth.dayOfWeek.value // Monday=1, Tuesday=2, etc.
    }

    // Day headers (S M T W T F S)
    val dayHeaders = listOf("S", "M", "T", "W", "T", "F", "S") // Sunday to Saturday

    Column {
        // Day headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            dayHeaders.forEach { day ->
                Box(
                    modifier = Modifier
                        .size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar days - create a proper grid
        val daysInMonth = lastDayOfMonth.dayOfMonth
        val totalWeeksNeeded = kotlin.math.ceil((daysInMonth + firstDayOfWeek) / 7.0).toInt()

        // Create only the needed rows (not always 6)
        for (week in 0 until totalWeeksNeeded) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (dayIndex in 0 until 7) {
                    val cellIndex = week * 7 + dayIndex
                    val dayNumber = cellIndex - firstDayOfWeek + 1

                    val date = if (dayNumber in 1..daysInMonth) {
                        firstDayOfMonth.withDayOfMonth(dayNumber)
                    } else {
                        null
                    }

                    CalendarDayCell(
                        date = date,
                        isSelected = date == selectedDate,
                        isToday = date == LocalDate.now(),
                        blockedDates = blockedDates, // ACTIVATED
                        onDateSelected = onDateSelected
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate?,
    isSelected: Boolean,
    isToday: Boolean,
    blockedDates: List<String> = emptyList(), // ACTIVATED
    onDateSelected: (LocalDate) -> Unit
) {
    Box(
        modifier = Modifier.size(40.dp),
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            val dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val isBlocked = blockedDates.contains(dateString) // ACTIVATED
            val isPastDate = date.isBefore(LocalDate.now())
            val isDisabled = isBlocked || isPastDate

            // Debug log untuk semua tanggal
            android.util.Log.d("CalendarDayCell", "📅 Checking date: $dateString")
            android.util.Log.d("CalendarDayCell", "📋 Available blocked dates: $blockedDates")
            android.util.Log.d("CalendarDayCell", "🔍 Is blocked: $isBlocked")

            // Debug log untuk tanggal yang blocked
            if (isBlocked) {
                android.util.Log.d("CalendarDayCell", "🔴 Date $dateString is BLOCKED - should show primary color")
            }

            val blockedColor = Color(0xFFE53935).copy(alpha = 0.35f)
            val todayColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            val isBlockedVisible = isBlocked && !isPastDate

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isBlockedVisible -> blockedColor
                            isPastDate -> Color.Transparent
                            isSelected -> MaterialTheme.colorScheme.primary
                            isToday -> todayColor
                            else -> Color.Transparent
                        }
                    )
                    .clickable(enabled = !isDisabled) {
                        if (!isDisabled) {
                            onDateSelected(date)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isBlockedVisible -> Color.White
                        isPastDate -> Color.Gray.copy(alpha = 0.35f)
                        isSelected -> MaterialTheme.colorScheme.onPrimary
                        isToday -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
        // If date is null, show nothing (empty cell)
    }
}

private fun formatPrice(price: Int): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return formatter.format(price).replace("IDR", "Rp")
}
