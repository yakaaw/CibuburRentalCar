package com.yaka.cibuburrentalcar.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yaka.cibuburrentalcar.data.CalendarBlock
import com.yaka.cibuburrentalcar.data.repository.CalendarBlockRepository
import com.yaka.cibuburrentalcar.data.repository.CarRepository
import com.yaka.cibuburrentalcar.data.repository.NetworkResult
import com.yaka.cibuburrentalcar.ui.components.AppTopBar
import com.yaka.cibuburrentalcar.ui.screens.admin.components.RentalFreezeDialog
import kotlinx.coroutines.launch

import com.yaka.cibuburrentalcar.ui.screens.admin.components.AdminSingleDateCalendarDialog
import java.time.LocalDate

private const val FOCUS_CAR_ID = "5c3115d0-ae72-43d7-8528-b4ca713c81e7"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCalendarScreen(
    onBack: () -> Unit
) {
    val carRepo = remember { CarRepository() }
    val calRepo = remember { CalendarBlockRepository() }
    val scope = rememberCoroutineScope()

    var cars by remember { mutableStateOf(emptyList<com.yaka.cibuburrentalcar.data.Car>()) }
    var selectedCarId by remember { mutableStateOf<String?>(null) }
    var blocks by remember { mutableStateOf<List<CalendarBlock>>(emptyList()) }
    var globalBlocks by remember { mutableStateOf<List<CalendarBlock>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        when (val res = carRepo.getCars(availableOnly = false)) {
            is NetworkResult.Success -> {
                val carList = res.data ?: emptyList()
                cars = carList.sortedBy { it.name }
                // Auto-select Sigra jika ada
                val focus = cars.firstOrNull { it.id == FOCUS_CAR_ID }
                if (focus != null) {
                    selectedCarId = focus.id
                    android.util.Log.d("AdminCalendarScreen", "🎯 Auto-select: ${focus.name} (${focus.id})")
                    // refresh akan dipanggil setelah composable selesai init
                    // ditrigger oleh Button/aksi atau bisa pakai LaunchedEffect key

                }
                android.util.Log.d("AdminCalendarScreen", "📋 Loaded ${cars.size} cars: ${cars.map { it.name }}")
            }
            else -> {}
        }
        when (val res = calRepo.listGlobalBlocks()) {
            is NetworkResult.Success -> globalBlocks = res.data ?: emptyList()
            else -> {}
        }
        isLoading = false
    }

    fun refreshBlocks() {
        val id = selectedCarId ?: return
        scope.launch {
            when (val res = calRepo.listBlocks(id)) {
                is NetworkResult.Success -> blocks = res.data ?: emptyList()
                else -> {}
            }
        }
    }

    Scaffold(topBar = { AppTopBar(title = "Maintenance Calendar", onBackClick = onBack, centerTitle = true) }) { padding ->
        var dropdownExpanded by remember { mutableStateOf(false) }
        var showAddBlockDialog by remember { mutableStateOf(false) }
        var showAddGlobalDialog by remember { mutableStateOf(false) }

        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
    // Trigger refresh jika selectedCarId berubah (termasuk auto-select)
    LaunchedEffect(selectedCarId) {
        if (selectedCarId != null) refreshBlocks()
    }


            // Pemilihan Mobil (Dropdown)
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = !dropdownExpanded }
            ) {
                OutlinedTextField(
                    value = cars.find { it.id == selectedCarId }?.name ?: "Pilih mobil",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Mobil") },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier.heightIn(max = 240.dp) // batasi tinggi menu agar tidak 'ketutup'
                ) {
                    cars.forEach { car ->
                        DropdownMenuItem(
                            text = { Text(car.name) },
                            onClick = {
                                dropdownExpanded = false
                                selectedCarId = car.id
                                refreshBlocks()
                            }
                        )
                    }
                }
            }

            // Aksi
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showAddBlockDialog = true },
                    enabled = selectedCarId != null
                ) { Text("Tambah Blok") }
                OutlinedButton(onClick = { showAddGlobalDialog = true }) {
                    Text("Rental Freeze")
                }
            }

            Text("Blok Tanggal Mobil", style = MaterialTheme.typography.titleMedium)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(blocks) { b ->
                    ListItem(
                        headlineContent = { Text("${b.startDate} s/d ${b.endDate}") },
                        supportingContent = { Text(b.reason) },
                        trailingContent = {
                            TextButton(onClick = {
                                val id = selectedCarId ?: return@TextButton
                                scope.launch {
                                    calRepo.removeBlock(id, b.id)
                                    refreshBlocks()
                                }
                            }) { Text("Hapus") }
                        }
                    )
                }
            }

            Text("Global Blocks", style = MaterialTheme.typography.titleMedium)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(globalBlocks) { b ->
                    ListItem(
                        headlineContent = { Text("${b.startDate} s/d ${b.endDate}") },
                        supportingContent = { Text(b.reason) },
                        trailingContent = {
                            TextButton(onClick = {
                                scope.launch {
                                    calRepo.removeGlobalBlock(b.id)
                                    when (val res = calRepo.listGlobalBlocks()) {
                                        is NetworkResult.Success -> globalBlocks = res.data ?: emptyList()
                                        else -> {}
                                    }
                                }
                            }) { Text("Hapus") }
                        }
                    )
                }
            }
        }

        // Dialog Tambah Blok Mobil
        if (showAddBlockDialog) {
            AddBlockDialog(
                title = "Tambah Blok Mobil",
                onDismiss = { showAddBlockDialog = false },
                onConfirm = { start, end, reason ->
                    val id = selectedCarId ?: return@AddBlockDialog
                    scope.launch {
                        when (calRepo.addBlock(id, CalendarBlock(id = "", startDate = start, endDate = end, reason = reason))) {
                            is NetworkResult.Success -> refreshBlocks()
                            else -> {}
                        }
                        showAddBlockDialog = false
                    }
                }
            )
        }

        // Dialog Tambah Global Blok
        if (showAddGlobalDialog) {
            var selected by remember { mutableStateOf<java.time.LocalDate?>(null) }
            AdminSingleDateCalendarDialog(
                bookingBlockedDates = emptyList(),
                maintenanceDates = globalBlocks.flatMap { blk ->
                    val start = LocalDate.parse(blk.startDate)
                    val end = LocalDate.parse(blk.endDate)
                    generateSequence(start) { prev -> if (!prev.isAfter(end)) prev.plusDays(1) else null }
                        .map { it.toString() }
                        .toList()
                },
                onToggleDate = { d -> selected = d },
                onDismiss = { showAddGlobalDialog = false }
            )

            selected?.let { d ->
                LaunchedEffect(d) {
                    scope.launch {
                        when (calRepo.addGlobalBlock(CalendarBlock(id = "", startDate = d.toString(), endDate = d.toString(), reason = "Rental Freeze"))) {
                            is NetworkResult.Success -> {
                                when (val res = calRepo.listGlobalBlocks()) {
                                    is NetworkResult.Success -> globalBlocks = res.data ?: emptyList()
                                    else -> {}
                                }
                            }
                            else -> {}
                        }
                        showAddGlobalDialog = false
                    }
                }
            }
        }
    }
}

@Composable
private fun AddBlockDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (start: String, end: String, reason: String) -> Unit
) {
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("Maintenance") }
    var error by remember { mutableStateOf<String?>(null) }

    fun isValidDate(s: String): Boolean = try {
        java.time.LocalDate.parse(s)
        true
    } catch (_: Exception) { false }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = start, onValueChange = { start = it }, label = { Text("Tanggal Mulai (YYYY-MM-DD)") })
                OutlinedTextField(value = end, onValueChange = { end = it }, label = { Text("Tanggal Selesai (YYYY-MM-DD)") })
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Alasan") })
                if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (!isValidDate(start) || !isValidDate(end)) {
                    error = "Format tanggal harus YYYY-MM-DD"
                    return@TextButton
                }
                val s = java.time.LocalDate.parse(start)
                val e = java.time.LocalDate.parse(end)
                if (e.isBefore(s)) {
                    error = "Tanggal selesai tidak boleh sebelum tanggal mulai"
                    return@TextButton
                }
                onConfirm(start, end, reason)
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
