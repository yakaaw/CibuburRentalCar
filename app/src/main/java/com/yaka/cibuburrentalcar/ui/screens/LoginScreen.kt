package com.yaka.cibuburrentalcar.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.yaka.cibuburrentalcar.utils.AuthValidator
import com.yaka.cibuburrentalcar.data.CurrentUserProvider
import com.yaka.cibuburrentalcar.data.repository.NetworkResult
import com.yaka.cibuburrentalcar.data.repository.AuthRepository
import com.yaka.cibuburrentalcar.ui.components.*
import com.yaka.cibuburrentalcar.R
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }
    
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val authRepository = remember { AuthRepository(context) }
    
    // Validation
    fun validateForm(): Boolean {
        emailError = AuthValidator.validateEmail(email)
        passwordError = AuthValidator.validatePassword(password)
        return emailError == null && passwordError == null
    }
    
    // Login function
    fun performLogin() {
        if (!validateForm()) return

        isLoading = true
        loginError = null
        keyboardController?.hide()

        coroutineScope.launch {
            when (val result = authRepository.login(email, password)) {
                is NetworkResult.Success -> {
                    // Store user data and token if needed
                    result.data?.let { user ->
                        // Update current user provider
                        CurrentUserProvider.setCurrentUser(user)
                        android.util.Log.d("LoginScreen", "Login successful for: ${user.name} (${user.email})")
                        onLoginSuccess()
                    }
                }
                is NetworkResult.Error -> {
                    loginError = result.message ?: "Login failed"
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
            .background(Color(0xFFFAFAFA)) // Light gray background untuk kontras yang baik
            .padding(horizontal = 24.dp, vertical = 20.dp), // Padding optimal
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween // Distribusi ruang secara merata
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp)) // Spacing optimal untuk header

            // App Logo dengan shadow lembut - ukuran dikurangi sedikit untuk fit screen
            Card(
                modifier = Modifier
                    .size(140.dp) // Kurangi dari 180dp ke 140dp untuk fit screen
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(70.dp), // Menyesuaikan radius
                        ambientColor = Color.Black.copy(alpha = 0.1f),
                        spotColor = Color.Black.copy(alpha = 0.1f)
                    ),
                shape = RoundedCornerShape(70.dp), // Menyesuaikan radius
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.cibuburrentcar),
                    contentDescription = "Cibubur Rental Car Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp) // Tetap tipis
                )
            }

            Spacer(modifier = Modifier.height(20.dp)) // Spacing antara logo dan header text

            // Header Text - kompak
            Text(
                text = "Selamat Datang",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(6.dp)) // Spacing antara title dan subtitle

            Text(
                text = "Sewa Mobil Mudah & Cepat",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp)) // Spacing antara subtitle dan description

            Text(
                text = "Masuk ke akun Anda untuk melanjutkan",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp)) // Spacing antara header dan form

            // Login Form dengan spacing optimal
            Column(
                verticalArrangement = Arrangement.spacedBy(18.dp) // Spacing optimal antar field
            ) {
            // Email Field dengan outline icon
            AuthInputField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                    loginError = null
                },
                label = "Email",
                leadingIcon = Icons.Outlined.Email,
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                isError = emailError != null,
                errorMessage = emailError,
                enabled = !isLoading
            )

            // Password Field dengan outline icon
            AuthPasswordField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                    loginError = null
                },
                label = "Password",
                leadingIcon = Icons.Outlined.Lock,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(
                    onDone = { performLogin() }
                ),
                isError = passwordError != null,
                errorMessage = passwordError,
                enabled = !isLoading
            )
            
            // Login Error
            if (loginError != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = loginError!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
                Spacer(modifier = Modifier.height(20.dp)) // Spacing antara form dan button

                // Login Button dengan modern hover effect
                Button(
                    onClick = { performLogin() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
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
                            text = "Masuk",
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
        }

        // Spacer untuk memberikan ruang yang cukup sebelum footer
        Spacer(modifier = Modifier.height(16.dp))

        // Register Footer
        AuthFooter(
            questionText = "Belum punya akun?",
            actionText = "Daftar Sekarang",
            onActionClick = onNavigateToRegister
        )
    }
}
