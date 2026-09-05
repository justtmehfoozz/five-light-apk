package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.auth.AuthRepository
import com.example.data.auth.GoogleAuthException
import com.example.ui.theme.SerifHeaderFont
import com.example.ui.theme.isAppInDarkTheme
import com.example.ui.theme.semanticError
import com.example.ui.theme.semanticSuccess
import kotlinx.coroutines.launch

private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

fun isValidEmail(email: String): Boolean {
    return email.isNotBlank() && EMAIL_REGEX.matches(email.trim())
}

private enum class LoginSheetStep {
    OPTIONS,
    EMAIL_LOGIN,
    REGISTER,
    FORGOT_PASSWORD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginBottomSheet(
    sheetState: SheetState,
    authRepository: AuthRepository,
    onDismiss: () -> Unit,
    onAuthSuccess: () -> Unit
) {
    val isDark = isAppInDarkTheme()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentStep by remember { mutableStateOf(LoginSheetStep.OPTIONS) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    val sheetBgColor = if (isDark) Color(0xFF161619) else Color(0xFFFCFCFD)
    val scrimBgColor = Color.Black.copy(alpha = if (isDark) 0.65f else 0.45f)
    val handleColor = if (isDark) Color(0xFF38383E) else Color(0xFFDCDCE0)

    ModalBottomSheet(
        onDismissRequest = {
            if (!isLoading) onDismiss()
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = sheetBgColor,
        scrimColor = scrimBgColor,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .background(color = handleColor, shape = CircleShape)
            )
        },
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .testTag("login_bottom_sheet")
        ) {
            // Error / Info banners
            if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.semanticError.copy(alpha = 0.12f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.semanticError,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (infoMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.semanticSuccess.copy(alpha = 0.12f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = infoMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.semanticSuccess,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState == LoginSheetStep.OPTIONS) {
                        slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    }
                },
                label = "loginSheetSteps"
            ) { step ->
                when (step) {
                    LoginSheetStep.OPTIONS -> {
                        MainLoginOptions(
                            isLoading = isLoading,
                            isDark = isDark,
                            onLoginWithEmail = {
                                if (!isLoading) {
                                    errorMessage = null
                                    infoMessage = null
                                    currentStep = LoginSheetStep.EMAIL_LOGIN
                                }
                            },
                            onLoginWithGoogle = {
                                if (!isLoading) {
                                    errorMessage = null
                                    infoMessage = null
                                    isLoading = true
                                    coroutineScope.launch {
                                        val result = authRepository.signInWithGoogle(context)
                                        isLoading = false
                                        result.onSuccess {
                                            onAuthSuccess()
                                        }.onFailure { e ->
                                            if (e is GoogleAuthException.Cancelled || e.message == "Sign-in cancelled") {
                                                // User cancelled the Google account chooser; return cleanly to login sheet
                                                errorMessage = null
                                            } else {
                                                errorMessage = e.message ?: "Google Sign-In failed"
                                            }
                                        }
                                    }
                                }
                            },
                            onRegisterNewAccount = {
                                if (!isLoading) {
                                    errorMessage = null
                                    infoMessage = null
                                    currentStep = LoginSheetStep.REGISTER
                                }
                            },
                            onClose = {
                                if (!isLoading) onDismiss()
                            }
                        )
                    }

                    LoginSheetStep.EMAIL_LOGIN -> {
                        EmailLoginForm(
                            isLoading = isLoading,
                            isDark = isDark,
                            onBack = {
                                if (!isLoading) {
                                    errorMessage = null
                                    infoMessage = null
                                    currentStep = LoginSheetStep.OPTIONS
                                }
                            },
                            onForgotPassword = {
                                if (!isLoading) {
                                    errorMessage = null
                                    infoMessage = null
                                    currentStep = LoginSheetStep.FORGOT_PASSWORD
                                }
                            },
                            onLogin = { email, password ->
                                if (!isLoading) {
                                    errorMessage = null
                                    infoMessage = null
                                    isLoading = true
                                    coroutineScope.launch {
                                        val result = authRepository.signInWithEmail(email, password)
                                        isLoading = false
                                        result.onSuccess {
                                            onAuthSuccess()
                                        }.onFailure { e ->
                                            errorMessage = e.message ?: "Login failed. Please check your credentials."
                                        }
                                    }
                                }
                            }
                        )
                    }

                    LoginSheetStep.REGISTER -> {
                        RegisterForm(
                            isLoading = isLoading,
                            isDark = isDark,
                            onBack = {
                                if (!isLoading) {
                                    errorMessage = null
                                    infoMessage = null
                                    currentStep = LoginSheetStep.OPTIONS
                                }
                            },
                            onRegister = { name, email, password ->
                                if (!isLoading) {
                                    errorMessage = null
                                    infoMessage = null
                                    isLoading = true
                                    coroutineScope.launch {
                                        val result = authRepository.registerWithEmail(
                                            email = email,
                                            password = password,
                                            displayName = name.ifBlank { null }
                                        )
                                        isLoading = false
                                        result.onSuccess {
                                            onAuthSuccess()
                                        }.onFailure { e ->
                                            errorMessage = e.message ?: "Registration failed. Please check your details."
                                        }
                                    }
                                }
                            }
                        )
                    }

                    LoginSheetStep.FORGOT_PASSWORD -> {
                        ForgotPasswordForm(
                            isLoading = isLoading,
                            isDark = isDark,
                            onBack = {
                                if (!isLoading) {
                                    errorMessage = null
                                    infoMessage = null
                                    currentStep = LoginSheetStep.EMAIL_LOGIN
                                }
                            },
                            onSendReset = { email ->
                                if (!isLoading) {
                                    errorMessage = null
                                    infoMessage = null
                                    isLoading = true
                                    coroutineScope.launch {
                                        val result = authRepository.sendPasswordReset(email)
                                        isLoading = false
                                        result.onSuccess {
                                            infoMessage = "Password reset link sent to $email."
                                            currentStep = LoginSheetStep.EMAIL_LOGIN
                                        }.onFailure { e ->
                                            errorMessage = e.message ?: "Could not send reset email."
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Screen 2 Main View:
 * 1. Title: "Log In to Continue"
 * 2. Subtitle: "Sign in or create an account to keep everything in sync."
 * 3. Two side-by-side subtle cards: "Login with Email" & "Login with Google"
 * 4. Thin subtle divider: "Don't have an account?"
 * 5. Refined monochrome pill button: "Register New Account"
 * 6. Quiet text action: "Close"
 */
@Composable
private fun MainLoginOptions(
    isLoading: Boolean,
    isDark: Boolean,
    onLoginWithEmail: () -> Unit,
    onLoginWithGoogle: () -> Unit,
    onRegisterNewAccount: () -> Unit,
    onClose: () -> Unit
) {
    val titleColor = if (isDark) Color(0xFFEDEDF0) else Color(0xFF141416)
    val subtitleColor = if (isDark) Color(0xFFA1A1AA) else Color(0xFF71717A)
    val cardBgColor = if (isDark) Color(0xFF1F1F24) else Color(0xFFF7F7F8)
    val cardBorderColor = if (isDark) Color(0xFF2E2E36) else Color(0xFFE6E6EB)
    val cardContentColor = if (isDark) Color(0xFFEDEDF0) else Color(0xFF18181B)
    val dividerColor = if (isDark) Color(0xFF2E2E36) else Color(0xFFE8E8ED)
    val dividerTextColor = if (isDark) Color(0xFF8E8E93) else Color(0xFF8E8E93)

    val registerBtnBg = if (isDark) Color(0xFFFFFFFF) else Color(0xFF141416)
    val registerBtnText = if (isDark) Color(0xFF121214) else Color(0xFFFFFFFF)
    val closeTextColor = if (isDark) Color(0xFF9E9EA6) else Color(0xFF71717A)

    val emailInteractionSource = remember { MutableInteractionSource() }
    val isEmailPressed by emailInteractionSource.collectIsPressedAsState()
    val emailScale by animateFloatAsState(
        targetValue = if (isEmailPressed && !isLoading) 0.98f else 1.0f,
        animationSpec = tween(100),
        label = "emailScale"
    )

    val googleInteractionSource = remember { MutableInteractionSource() }
    val isGooglePressed by googleInteractionSource.collectIsPressedAsState()
    val googleScale by animateFloatAsState(
        targetValue = if (isGooglePressed && !isLoading) 0.98f else 1.0f,
        animationSpec = tween(100),
        label = "googleScale"
    )

    val registerInteractionSource = remember { MutableInteractionSource() }
    val isRegisterPressed by registerInteractionSource.collectIsPressedAsState()
    val registerScale by animateFloatAsState(
        targetValue = if (isRegisterPressed && !isLoading) 0.985f else 1.0f,
        animationSpec = tween(100),
        label = "registerScale"
    )

    val closeInteractionSource = remember { MutableInteractionSource() }
    val isClosePressed by closeInteractionSource.collectIsPressedAsState()
    val closeAlpha by animateFloatAsState(
        targetValue = if (isClosePressed && !isLoading) 0.60f else 1.0f,
        animationSpec = tween(100),
        label = "closeAlpha"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // 1. Title
        Text(
            text = "Log In to Continue",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = SerifHeaderFont,
                fontWeight = FontWeight.Normal,
                fontSize = 23.sp,
                letterSpacing = (-0.2).sp
            ),
            color = titleColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("login_sheet_title")
        )

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Subtitle
        Text(
            text = "Sign in or create an account to keep everything in sync.",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.5.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Normal
            ),
            color = subtitleColor,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .testTag("login_sheet_subtitle")
        )

        Spacer(modifier = Modifier.height(26.dp))

        // 3. Two refined side-by-side tiles: Login with Email & Login with Google
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Email Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(76.dp)
                    .graphicsLayer {
                        scaleX = emailScale
                        scaleY = emailScale
                    }
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = emailInteractionSource,
                        indication = null,
                        enabled = !isLoading,
                        onClick = onLoginWithEmail
                    )
                    .testTag("login_with_email_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                border = BorderStroke(1.dp, cardBorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "Email Login",
                        tint = cardContentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Login with Email",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.5.sp
                        ),
                        color = cardContentColor,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Google Card (With Official Google "G" Logo)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(76.dp)
                    .graphicsLayer {
                        scaleX = googleScale
                        scaleY = googleScale
                    }
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = googleInteractionSource,
                        indication = null,
                        enabled = !isLoading,
                        onClick = onLoginWithGoogle
                    )
                    .testTag("login_with_google_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                border = BorderStroke(1.dp, cardBorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = cardContentColor
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google_logo),
                            contentDescription = "Google Logo",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Login with Google",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.5.sp
                        ),
                        color = cardContentColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        // 4. Divider with centered text
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 0.8.dp,
                color = dividerColor
            )
            Text(
                text = "Don't have an account?",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = dividerTextColor,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 0.8.dp,
                color = dividerColor
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Full-width monochrome pill button: Register New Account
        Button(
            onClick = onRegisterNewAccount,
            enabled = !isLoading,
            interactionSource = registerInteractionSource,
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = registerBtnBg,
                contentColor = registerBtnText,
                disabledContainerColor = registerBtnBg.copy(alpha = 0.4f),
                disabledContentColor = registerBtnText.copy(alpha = 0.6f)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                focusedElevation = 0.dp,
                hoveredElevation = 0.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .graphicsLayer {
                    scaleX = registerScale
                    scaleY = registerScale
                }
                .testTag("register_new_account_btn")
        ) {
            Text(
                text = "Register New Account",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    letterSpacing = 0.2.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 6. Plain text link: Close
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clickable(
                    interactionSource = closeInteractionSource,
                    indication = null,
                    enabled = !isLoading,
                    onClick = onClose
                )
                .graphicsLayer {
                    alpha = closeAlpha
                }
                .testTag("login_sheet_close_btn"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Close",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.5.sp
                ),
                color = closeTextColor
            )
        }
    }
}

@Composable
private fun EmailLoginForm(
    isLoading: Boolean,
    isDark: Boolean,
    onBack: () -> Unit,
    onForgotPassword: () -> Unit,
    onLogin: (email: String, pass: String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var emailTouched by remember { mutableStateOf(false) }
    var passwordTouched by remember { mutableStateOf(false) }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    val isEmailFormatValid = isValidEmail(email)
    val showEmailError = (emailTouched || hasAttemptedSubmit) && email.isNotEmpty() && !isEmailFormatValid ||
            (hasAttemptedSubmit && email.isEmpty())
    val emailErrorText = if (email.isEmpty()) "Email is required" else "Please enter a valid email address"

    val isPasswordFilled = password.isNotBlank()
    val showPasswordError = hasAttemptedSubmit && !isPasswordFilled

    val primaryBtnBg = if (isDark) Color(0xFFFFFFFF) else Color(0xFF141416)
    val primaryBtnText = if (isDark) Color(0xFF121214) else Color(0xFFFFFFFF)
    val forgotPwdColor = if (isDark) Color(0xFFD4D4D8) else Color(0xFF3F3F46)

    val submitInteractionSource = remember { MutableInteractionSource() }
    val isSubmitPressed by submitInteractionSource.collectIsPressedAsState()
    val submitScale by animateFloatAsState(
        targetValue = if (isSubmitPressed && !isLoading) 0.985f else 1.0f,
        animationSpec = tween(100),
        label = "loginSubmitScale"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBack,
                enabled = !isLoading,
                modifier = Modifier.testTag("email_login_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "Log In with Email",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = SerifHeaderFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailTouched = true
            },
            label = { Text("Email Address") },
            leadingIcon = {
                Icon(Icons.Outlined.Email, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            isError = showEmailError,
            supportingText = if (showEmailError) {
                { Text(text = emailErrorText, color = Color.semanticError, style = MaterialTheme.typography.bodySmall) }
            } else null,
            singleLine = true,
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("email_input_field")
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordTouched = true
            },
            label = { Text("Password") },
            leadingIcon = {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            trailingIcon = {
                IconButton(
                    onClick = { passwordVisible = !passwordVisible },
                    modifier = Modifier.testTag("login_password_visibility_toggle")
                ) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = showPasswordError,
            supportingText = if (showPasswordError) {
                { Text(text = "Password is required", color = Color.semanticError, style = MaterialTheme.typography.bodySmall) }
            } else null,
            singleLine = true,
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                hasAttemptedSubmit = true
                if (isEmailFormatValid && password.isNotBlank() && !isLoading) {
                    onLogin(email.trim(), password)
                }
            }),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("password_input_field")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onForgotPassword,
                enabled = !isLoading,
                modifier = Modifier.testTag("forgot_password_btn")
            ) {
                Text(
                    text = "Forgot password?",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    ),
                    color = forgotPwdColor
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                hasAttemptedSubmit = true
                if (isEmailFormatValid && password.isNotBlank() && !isLoading) {
                    onLogin(email.trim(), password)
                }
            },
            enabled = !isLoading,
            interactionSource = submitInteractionSource,
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryBtnBg,
                contentColor = primaryBtnText,
                disabledContainerColor = primaryBtnBg.copy(alpha = 0.45f),
                disabledContentColor = primaryBtnText.copy(alpha = 0.7f)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                focusedElevation = 0.dp,
                hoveredElevation = 0.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .graphicsLayer {
                    scaleX = submitScale
                    scaleY = submitScale
                }
                .testTag("submit_email_login_btn")
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = primaryBtnText,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Log In",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        letterSpacing = 0.2.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun RegisterForm(
    isLoading: Boolean,
    isDark: Boolean,
    onBack: () -> Unit,
    onRegister: (name: String, email: String, pass: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var emailTouched by remember { mutableStateOf(false) }
    var passwordTouched by remember { mutableStateOf(false) }
    var confirmPasswordTouched by remember { mutableStateOf(false) }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    val isEmailFormatValid = isValidEmail(email)
    val showEmailError = (emailTouched || hasAttemptedSubmit) && email.isNotEmpty() && !isEmailFormatValid ||
            (hasAttemptedSubmit && email.isEmpty())
    val emailErrorText = if (email.isEmpty()) "Email is required" else "Please enter a valid email address"

    val isPasswordLengthValid = password.length >= 6
    val showPasswordError = (passwordTouched || hasAttemptedSubmit) && password.isNotEmpty() && !isPasswordLengthValid ||
            (hasAttemptedSubmit && password.isEmpty())
    val passwordErrorText = if (password.isEmpty()) "Password is required" else "Password must be at least 6 characters"

    val isConfirmPasswordMatching = confirmPassword == password
    val showConfirmPasswordError = (confirmPasswordTouched || hasAttemptedSubmit) && confirmPassword.isNotEmpty() && !isConfirmPasswordMatching ||
            (hasAttemptedSubmit && confirmPassword.isEmpty())
    val confirmPasswordErrorText = if (confirmPassword.isEmpty()) "Please confirm your password" else "Passwords do not match"

    val primaryBtnBg = if (isDark) Color(0xFFFFFFFF) else Color(0xFF141416)
    val primaryBtnText = if (isDark) Color(0xFF121214) else Color(0xFFFFFFFF)

    val submitInteractionSource = remember { MutableInteractionSource() }
    val isSubmitPressed by submitInteractionSource.collectIsPressedAsState()
    val submitScale by animateFloatAsState(
        targetValue = if (isSubmitPressed && !isLoading) 0.985f else 1.0f,
        animationSpec = tween(100),
        label = "registerSubmitScale"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBack,
                enabled = !isLoading,
                modifier = Modifier.testTag("register_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = SerifHeaderFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name (Optional)") },
            leadingIcon = {
                Icon(Icons.Outlined.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            singleLine = true,
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_name_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailTouched = true
            },
            label = { Text("Email Address") },
            leadingIcon = {
                Icon(Icons.Outlined.Email, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            isError = showEmailError,
            supportingText = if (showEmailError) {
                { Text(text = emailErrorText, color = Color.semanticError, style = MaterialTheme.typography.bodySmall) }
            } else null,
            singleLine = true,
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_email_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordTouched = true
            },
            label = { Text("Password (min 6 characters)") },
            leadingIcon = {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            trailingIcon = {
                IconButton(
                    onClick = { passwordVisible = !passwordVisible },
                    modifier = Modifier.testTag("register_password_visibility_toggle")
                ) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = showPasswordError,
            supportingText = if (showPasswordError) {
                { Text(text = passwordErrorText, color = Color.semanticError, style = MaterialTheme.typography.bodySmall) }
            } else null,
            singleLine = true,
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_password_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                confirmPasswordTouched = true
            },
            label = { Text("Confirm Password") },
            leadingIcon = {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            trailingIcon = {
                IconButton(
                    onClick = { confirmPasswordVisible = !confirmPasswordVisible },
                    modifier = Modifier.testTag("register_confirm_password_visibility_toggle")
                ) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = showConfirmPasswordError,
            supportingText = if (showConfirmPasswordError) {
                { Text(text = confirmPasswordErrorText, color = Color.semanticError, style = MaterialTheme.typography.bodySmall) }
            } else null,
            singleLine = true,
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                hasAttemptedSubmit = true
                if (isEmailFormatValid && isPasswordLengthValid && isConfirmPasswordMatching && !isLoading) {
                    onRegister(name.trim(), email.trim(), password)
                }
            }),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_confirm_password_input")
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                hasAttemptedSubmit = true
                if (isEmailFormatValid && isPasswordLengthValid && isConfirmPasswordMatching && !isLoading) {
                    onRegister(name.trim(), email.trim(), password)
                }
            },
            enabled = !isLoading,
            interactionSource = submitInteractionSource,
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryBtnBg,
                contentColor = primaryBtnText,
                disabledContainerColor = primaryBtnBg.copy(alpha = 0.45f),
                disabledContentColor = primaryBtnText.copy(alpha = 0.7f)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                focusedElevation = 0.dp,
                hoveredElevation = 0.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .graphicsLayer {
                    scaleX = submitScale
                    scaleY = submitScale
                }
                .testTag("submit_register_btn")
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = primaryBtnText,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        letterSpacing = 0.2.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun ForgotPasswordForm(
    isLoading: Boolean,
    isDark: Boolean,
    onBack: () -> Unit,
    onSendReset: (email: String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var emailTouched by remember { mutableStateOf(false) }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val isEmailFormatValid = isValidEmail(email)
    val showEmailError = (emailTouched || hasAttemptedSubmit) && email.isNotEmpty() && !isEmailFormatValid ||
            (hasAttemptedSubmit && email.isEmpty())
    val emailErrorText = if (email.isEmpty()) "Email is required" else "Please enter a valid email address"

    val primaryBtnBg = if (isDark) Color(0xFFFFFFFF) else Color(0xFF141416)
    val primaryBtnText = if (isDark) Color(0xFF121214) else Color(0xFFFFFFFF)

    val submitInteractionSource = remember { MutableInteractionSource() }
    val isSubmitPressed by submitInteractionSource.collectIsPressedAsState()
    val submitScale by animateFloatAsState(
        targetValue = if (isSubmitPressed && !isLoading) 0.985f else 1.0f,
        animationSpec = tween(100),
        label = "resetSubmitScale"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBack,
                enabled = !isLoading,
                modifier = Modifier.testTag("forgot_password_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "Reset Password",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = SerifHeaderFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Enter your registered email address and we will send you a link to reset your password.",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.5.sp,
                lineHeight = 20.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailTouched = true
            },
            label = { Text("Email Address") },
            leadingIcon = {
                Icon(Icons.Outlined.Email, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            isError = showEmailError,
            supportingText = if (showEmailError) {
                { Text(text = emailErrorText, color = Color.semanticError, style = MaterialTheme.typography.bodySmall) }
            } else null,
            singleLine = true,
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                hasAttemptedSubmit = true
                if (isEmailFormatValid && !isLoading) onSendReset(email.trim())
            }),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("forgot_password_email_input")
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                hasAttemptedSubmit = true
                if (isEmailFormatValid && !isLoading) onSendReset(email.trim())
            },
            enabled = !isLoading,
            interactionSource = submitInteractionSource,
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryBtnBg,
                contentColor = primaryBtnText,
                disabledContainerColor = primaryBtnBg.copy(alpha = 0.45f),
                disabledContentColor = primaryBtnText.copy(alpha = 0.7f)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                focusedElevation = 0.dp,
                hoveredElevation = 0.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .graphicsLayer {
                    scaleX = submitScale
                    scaleY = submitScale
                }
                .testTag("submit_forgot_password_btn")
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = primaryBtnText,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Send Reset Link",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        letterSpacing = 0.2.sp
                    )
                )
            }
        }
    }
}
