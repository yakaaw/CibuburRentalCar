package com.yaka.cibuburrentalcar.ui.screens.admin.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentalFreezeDialog(
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, String) -> Unit,
    bookingBlockedDates: List<String> = emptyList(),
    maintenanceDates: List<String> = emptyList()
) {
    var selected by remember { mutableStateOf(LocalDate.now()) }
    var note by remember { mutableStateOf("Rental Freeze") }

    AdminSingleDateCalendarDialog(
        bookingBlockedDates = bookingBlockedDates,
        maintenanceDates = maintenanceDates,
        onToggleDate = { selected = it },
        onDismiss = onDismiss
    )

    // Bagian input catatan + tombol simpan (menggunakan AlertDialog sendiri akan menggandakan dialog)
    // Jadi, kita cukup memodifikasi AdminSingleDateCalendarDialog jika perlu menambahkan footer.
    // Untuk sekarang, konfirmasi dilakukan via callback eksternal dari layar induk.

    // Panggil onConfirm saat diperlukan dari luar, atau bisa ditambahkan tombol di dialog calendar admin.
}

