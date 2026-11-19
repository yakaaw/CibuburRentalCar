// BACKUP IMPLEMENTASI CALENDAR BLOCKING
// Implementasi yang sudah benar tapi ada compilation error

// 1. STATE
var blockedDates by remember { mutableStateOf<List<String>>(emptyList()) }

// 2. LOAD BLOCKED DATES
LaunchedEffect(car.id) {
    android.util.Log.d("BookingScreen", "🗓️ Loading blocked dates for car: ${car.id}")
    
    try {
        when (val result = bookingRepository.getBlockedDates(car.id)) {
            is NetworkResult.Success -> {
                blockedDates = result.data
                android.util.Log.d("BookingScreen", "✅ Blocked dates loaded: ${blockedDates.size} dates")
                android.util.Log.d("BookingScreen", "📅 Blocked dates: $blockedDates")
            }
            is NetworkResult.Error -> {
                android.util.Log.e("BookingScreen", "❌ Failed to load blocked dates: ${result.message}")
                blockedDates = emptyList()
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("BookingScreen", "❌ Exception loading blocked dates: ${e.message}")
        blockedDates = emptyList()
    }
}

// 3. VALIDATION
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

// Check if any selected date is blocked
val blockedSelectedDates = selectedDates.filter { blockedDates.contains(it) }

if (blockedSelectedDates.isNotEmpty()) {
    android.util.Log.w("BookingScreen", "❌ Cannot book blocked dates: $blockedSelectedDates")
    Toast.makeText(
        context,
        "Tanggal ${blockedSelectedDates.joinToString(", ")} sudah dipesan. Silakan pilih tanggal lain.",
        Toast.LENGTH_LONG
    ).show()
    isLoading = false
    return@launch
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

// 4. CALENDAR FUNCTIONS
DatePickerField(
    label = "Tanggal Mulai",
    date = startDate,
    onDateSelected = { startDate = it },
    blockedDates = blockedDates,
    modifier = Modifier.weight(1f)
)

DatePickerField(
    label = "Tanggal Selesai", 
    date = endDate,
    onDateSelected = { endDate = it },
    blockedDates = blockedDates,
    modifier = Modifier.weight(1f)
)

// 5. RELOAD AFTER BOOKING
// Reload blocked dates untuk update calendar
try {
    when (val blockedResult = bookingRepository.getBlockedDates(car.id)) {
        is NetworkResult.Success -> {
            blockedDates = blockedResult.data
            android.util.Log.d("BookingScreen", "✅ Blocked dates reloaded: ${blockedDates.size} dates")
        }
        is NetworkResult.Error -> {
            android.util.Log.e("BookingScreen", "❌ Failed to reload blocked dates")
        }
    }
} catch (e: Exception) {
    android.util.Log.e("BookingScreen", "❌ Exception reloading blocked dates: ${e.message}")
}
