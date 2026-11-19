package com.yaka.cibuburrentalcar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yaka.cibuburrentalcar.data.User
import com.yaka.cibuburrentalcar.data.repository.AuthRepository
import com.yaka.cibuburrentalcar.data.repository.NetworkResult
import com.yaka.cibuburrentalcar.ui.components.PrimaryButton
import com.yaka.cibuburrentalcar.ui.components.SecondaryButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    user: User,
    onBackClick: () -> Unit,
    onProfileUpdated: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authRepository = remember { AuthRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    var name by remember { mutableStateOf(user.name) }
    var phone by remember { mutableStateOf(user.phone) }
    var address by remember { mutableStateOf(user.address) }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    // Validation states
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    
    fun validateForm(): Boolean {
        var isValid = true
        
        // Validate name
        if (name.isBlank()) {
            nameError = "Nama tidak boleh kosong"
            isValid = false
        } else if (name.length < 2) {
            nameError = "Nama minimal 2 karakter"
            isValid = false
        } else {
            nameError = null
        }
        
        // Validate phone
        if (phone.isBlank()) {
            phoneError = "Nomor telepon tidak boleh kosong"
            isValid = false
        } else if (!phone.matches(Regex("^[0-9+\\-\\s()]{10,15}$"))) {
            phoneError = "Format nomor telepon tidak valid"
            isValid = false
        } else {
            phoneError = null
        }
        
        return isValid
    }
    
    fun updateProfile() {
        if (!validateForm()) return
        
        isLoading = true
        errorMessage = null
        successMessage = null
        
        coroutineScope.launch {
            when (val result = authRepository.updateProfile(name.trim(), phone.trim(), address.trim())) {
                is NetworkResult.Success -> {
                    successMessage = "Profil berhasil diperbarui!"
                    result.data?.let { updatedUser ->
                        onProfileUpdated(updatedUser)
                    }
                    // Auto navigate back after success
                    kotlinx.coroutines.delay(1500)
                    onBackClick()
                }
                is NetworkResult.Error -> {
                    errorMessage = result.message ?: "Gagal memperbarui profil"
                }
                is NetworkResult.Loading -> {
                    // Already handled by isLoading
                }
            }
            isLoading = false
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        // Custom AppTopBar dengan offset tambahan untuk Edit Profil
        androidx.compose.material3.Surface(
            color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
            tonalElevation = 0.dp,
            shadowElevation = 4.dp,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
        ) {
            androidx.compose.material3.TopAppBar(
                modifier = androidx.compose.ui.Modifier.statusBarsPadding(),
                title = {
                    androidx.compose.foundation.layout.Box(
                        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        androidx.compose.material3.Text(
                            "Edit Profil",
                            modifier = Modifier.offset(x = (-10).dp) // Geser sedikit lagi ke kiri khusus Edit Profil
                        )
                    }
                },
                navigationIcon = {
                    if (!isLoading && onBackClick != null) {
                        androidx.compose.material3.IconButton(onClick = onBackClick) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Kembali",
                                tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    titleContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                )
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.firstOrNull()?.toString()?.uppercase() ?: "U",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Form Fields
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Name Field
                    EditProfileTextField(
                        value = name,
                        onValueChange = { 
                            name = it
                            nameError = null
                        },
                        label = "Nama Lengkap",
                        icon = Icons.Default.Person,
                        error = nameError,
                        enabled = !isLoading
                    )
                    
                    // Phone Field
                    EditProfileTextField(
                        value = phone,
                        onValueChange = { 
                            phone = it
                            phoneError = null
                        },
                        label = "Nomor Telepon",
                        icon = Icons.Default.Phone,
                        error = phoneError,
                        keyboardType = KeyboardType.Phone,
                        enabled = !isLoading
                    )
                    
                    // Address Field
                    EditProfileTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = "Alamat",
                        icon = Icons.Default.LocationOn,
                        singleLine = false,
                        maxLines = 3,
                        enabled = !isLoading
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Messages
            errorMessage?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            successMessage?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SecondaryButton(
                    text = "Batal",
                    onClick = onBackClick,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f)
                )
                
                PrimaryButton(
                    text = if (isLoading) "Menyimpan..." else "Simpan",
                    onClick = ::updateProfile,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    enabled: Boolean = true
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (error != null) MaterialTheme.colorScheme.error 
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            isError = error != null,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = singleLine,
            maxLines = maxLines,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}
