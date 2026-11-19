package com.yaka.cibuburrentalcar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.yaka.cibuburrentalcar.utils.AuthValidator
import com.yaka.cibuburrentalcar.data.CurrentUserProvider
import com.yaka.cibuburrentalcar.data.repository.NetworkResult
import com.yaka.cibuburrentalcar.data.repository.AuthRepository
import com.yaka.cibuburrentalcar.ui.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    
    var isLoading by remember { mutableStateOf(false) }
    var registerError by remember { mutableStateOf<String?>(null) }
    
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val authRepository = remember { AuthRepository(context) }
    
    // Validation
    fun validateForm(): Boolean {
        nameError = AuthValidator.validateName(name)
        emailError = AuthValidator.validateEmail(email)
        phoneError = AuthValidator.validatePhone(phone)
        passwordError = AuthValidator.validatePassword(password)
        confirmPasswordError = AuthValidator.validateConfirmPassword(password, confirmPassword)
        
        return nameError == null && emailError == null && phoneError == null && 
               passwordError == null && confirmPasswordError == null
    }
    
    // Register function
    fun performRegister() {
        if (!validateForm()) return

        isLoading = true
        registerError = null
        keyboardController?.hide()

        coroutineScope.launch {
            when (val result = authRepository.register(name, email, password, phone)) {
                is NetworkResult.Success -> {
                    // Store user data and token if needed
                    result.data?.let { user ->
                        // Update current user provider
                        CurrentUserProvider.setCurrentUser(user)
                        android.util.Log.d("RegisterScreen", "Registration successful for: ${user.name} (${user.email})")
                        onRegisterSuccess()
                    }
                }
                is NetworkResult.Error -> {
                    registerError = result.message ?: "Registration failed"
                }
                is NetworkResult.Loading -> {
                    // Already handled by isLoading state
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
        com.yaka.cibuburrentalcar.ui.components.AppTopBar(
            title = "Daftar Akun",
            onBackClick = onBackClick,
            centerTitle = true
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Header
            Text(
                text = "Buat Akun Baru",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Bergabunglah dengan Cibubur Rental Car",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Lengkapi data diri Anda untuk mendaftar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))
            
            // Register Form
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Name Field dengan outline icon
                AuthInputField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                        registerError = null
                    },
                    label = "Nama Lengkap",
                    leadingIcon = Icons.Outlined.Person,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    isError = nameError != null,
                    errorMessage = nameError,
                    enabled = !isLoading
                )

                // Email Field dengan outline icon
                AuthInputField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = null
                        registerError = null
                    },
                    label = "Email",
                    leadingIcon = Icons.Outlined.Email,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    isError = emailError != null,
                    errorMessage = emailError,
                    enabled = !isLoading
                )
                
                // Phone Field dengan outline icon
                AuthInputField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        phoneError = null
                        registerError = null
                    },
                    label = "Nomor Telepon",
                    leadingIcon = Icons.Outlined.Phone,
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    isError = phoneError != null,
                    errorMessage = phoneError,
                    enabled = !isLoading
                )

                // Password Field dengan outline icon
                AuthPasswordField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = null
                        confirmPasswordError = null
                        registerError = null
                    },
                    label = "Password",
                    leadingIcon = Icons.Outlined.Lock,
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    isError = passwordError != null,
                    errorMessage = passwordError,
                    enabled = !isLoading
                )

                // Confirm Password Field dengan outline icon
                AuthPasswordField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        confirmPasswordError = null
                        registerError = null
                    },
                    label = "Konfirmasi Password",
                    leadingIcon = Icons.Outlined.Lock,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(
                        onDone = { performRegister() }
                    ),
                    isError = confirmPasswordError != null,
                    errorMessage = confirmPasswordError,
                    enabled = !isLoading
                )
                
                // Register Error
                if (registerError != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = registerError!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                // Register Button dengan modern hover effect
                Button(
                    onClick = { performRegister() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(
                            elevation = if (isLoading) 6.dp else 2.dp, // Dynamic shadow
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = Color.Black.copy(alpha = 0.2f),
                            spotColor = Color.Black.copy(alpha = 0.2f)
                        )
                        .animateContentSize(), // Smooth animation
                    enabled = !isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLoading) Color(0xFF19398A) else MaterialTheme.colorScheme.primary, // Darker blue saat loading
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White.copy(alpha = 0.9f), // Soft white color
                            strokeWidth = 2.dp, // Thinner spinner
                            modifier = Modifier.size(20.dp) // Smaller size untuk lebih centered
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Daftar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Login Footer
            AuthFooter(
                questionText = "Sudah punya akun?",
                actionText = "Masuk Sekarang",
                onActionClick = onNavigateToLogin
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
