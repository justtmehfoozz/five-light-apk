package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.AuthRepository
import com.example.ui.theme.SerifHeaderFont
import com.example.ui.theme.isAppInDarkTheme
import com.example.ui.theme.semanticError
import com.example.ui.theme.semanticSuccess
import kotlinx.coroutines.launch

/**
 * Dedicated Full-Screen Registration Screen.
 *
 * Implements the FiveLight visual translation of the reference design:
 * - Back button at top
 * - Eyebrow: "Start your spiritual journey with FiveLight"
 * - Title: "Create Your Account"
 * - Large white/elevated form surface card
 * - Fields: Full Name, Email Address, Password
 * - Dynamic Password Requirements Checklist
 * - Terms & Privacy Policy confirmation text
 * - Primary CTA: "Create Your Account  →"
 */
@Composable
fun RegisterScreen(
    authRepository: AuthRepository,
    onBack: () -> Unit,
    onRegistrationSuccess: (registeredEmail: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var emailTouched by remember { mutableStateOf(false) }
    var passwordTouched by remember { mutableStateOf(false) }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }

    // Password requirement rules
    val hasMinLength = password.length >= 8
    val hasLowercase = password.any { it.isLowerCase() }
    val hasUppercase = password.any { it.isUpperCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSymbol = password.any { !it.isLetterOrDigit() && !it.isWhitespace() }
    val isPasswordRequirementsMet = hasMinLength && hasLowercase && hasUppercase && hasDigit && hasSymbol

    val isEmailFormatValid = isValidEmail(email)
    val showEmailError = (emailTouched || hasAttemptedSubmit) && email.isNotEmpty() && !isEmailFormatValid ||
            (hasAttemptedSubmit && email.isEmpty())
    val emailErrorText = if (email.isEmpty()) "Email is required" else "Please enter a valid email address"

    val showPasswordError = (passwordTouched || hasAttemptedSubmit) && !isPasswordRequirementsMet

    // Theme Color Tokens
    val pageBgColor = if (isDark) Color(0xFF121214) else Color(0xFFFAF8F5)
    val eyebrowColor = if (isDark) Color(0xFF9E9EA6) else Color(0xFF71717A)
    val titleColor = if (isDark) Color(0xFFF5F5F7) else Color(0xFF141416)

    // Form Surface Card
    val cardBgColor = if (isDark) Color(0xFF1C1C20) else Color(0xFFFFFFFF)
    val cardBorderColor = if (isDark) Color(0xFF2E2E36) else Color(0xFFEBEAEF)

    // Monochrome Button Style
    val primaryBtnBg = if (isDark) Color(0xFFFFFFFF) else Color(0xFF141416)
    val primaryBtnText = if (isDark) Color(0xFF121214) else Color(0xFFFFFFFF)

    val submitInteractionSource = remember { MutableInteractionSource() }
    val isSubmitPressed by submitInteractionSource.collectIsPressedAsState()
    val submitScale by animateFloatAsState(
        targetValue = if (isSubmitPressed && !isLoading) 0.98f else 1.0f,
        animationSpec = tween(100),
        label = "registerSubmitScale"
    )

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("register_screen"),
        color = pageBgColor
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // Top Bar: Back Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF24242A) else Color(0xFFEFECE6))
                            .clickable(enabled = !isLoading, onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = titleColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Eyebrow
                Text(
                    text = "Start your spiritual journey with FiveLight",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.5.sp,
                        letterSpacing = 0.3.sp
                    ),
                    color = eyebrowColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Heading
                Text(
                    text = "Create Your Account",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = SerifHeaderFont,
                        fontWeight = FontWeight.Normal,
                        fontSize = 28.sp,
                        lineHeight = 34.sp
                    ),
                    color = titleColor
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Error Banner
                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.semanticError.copy(alpha = 0.12f))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.semanticError,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Form Surface Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                    border = BorderStroke(1.dp, cardBorderColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Full Name Field
                        Text(
                            text = "Full Name",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            ),
                            color = eyebrowColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text("Enter your full name") },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            singleLine = true,
                            enabled = !isLoading,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryBtnBg,
                                unfocusedBorderColor = cardBorderColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_name_input")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Email Field
                        Text(
                            text = "Email Address",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            ),
                            color = eyebrowColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                emailTouched = true
                            },
                            placeholder = { Text("Enter your email address") },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Email,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            isError = showEmailError,
                            supportingText = if (showEmailError) {
                                { Text(text = emailErrorText, color = Color.semanticError) }
                            } else null,
                            singleLine = true,
                            enabled = !isLoading,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryBtnBg,
                                unfocusedBorderColor = cardBorderColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_email_input")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Password Field
                        Text(
                            text = "Password",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            ),
                            color = eyebrowColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                passwordTouched = true
                            },
                            placeholder = { Text("Create a password") },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { passwordVisible = !passwordVisible },
                                    modifier = Modifier.testTag("register_password_toggle")
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
                            singleLine = true,
                            enabled = !isLoading,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                            }),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryBtnBg,
                                unfocusedBorderColor = cardBorderColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_password_input")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Live Password Requirements Checklist
                        Text(
                            text = "Password Requirements:",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp
                            ),
                            color = eyebrowColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        PasswordRequirementItem("At least 8 characters", hasMinLength, isDark)
                        PasswordRequirementItem("At least one lowercase letter (a-z)", hasLowercase, isDark)
                        PasswordRequirementItem("At least one uppercase letter (A-Z)", hasUppercase, isDark)
                        PasswordRequirementItem("At least one number (0-9)", hasDigit, isDark)
                        PasswordRequirementItem("At least one symbol with no space", hasSymbol, isDark)

                        Spacer(modifier = Modifier.height(20.dp))

                        // Terms & Privacy Notice
                        Text(
                            text = "By proceeding, I confirm that I have read and accepted the Terms & Conditions and Privacy Policy of FiveLight.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            ),
                            color = eyebrowColor
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Primary CTA: Create Your Account →
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                hasAttemptedSubmit = true
                                if (isEmailFormatValid && isPasswordRequirementsMet && !isLoading) {
                                    isLoading = true
                                    errorMessage = null
                                    coroutineScope.launch {
                                        val result = authRepository.registerWithEmail(
                                            email = email.trim(),
                                            password = password,
                                            displayName = name.ifBlank { null }
                                        )
                                        result.onSuccess {
                                            val verificationResult = authRepository.sendEmailVerification()
                                            isLoading = false
                                            verificationResult.onSuccess {
                                                onRegistrationSuccess(email.trim())
                                            }.onFailure { e ->
                                                errorMessage = "Account created, but we failed to send the verification link: " + (e.message ?: "Unknown error") + ". Please try signing in to resend."
                                            }
                                        }.onFailure { e ->
                                            isLoading = false
                                            errorMessage = e.message ?: "Registration failed. Please try again."
                                        }
                                    }
                                } else if (!isPasswordRequirementsMet) {
                                    errorMessage = "Please satisfy all password requirements before proceeding."
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
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .graphicsLayer {
                                    scaleX = submitScale
                                    scaleY = submitScale
                                }
                                .testTag("register_submit_btn")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = primaryBtnText,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Create Your Account  →",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 15.5.sp,
                                        letterSpacing = 0.2.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PasswordRequirementItem(
    text: String,
    isMet: Boolean,
    isDark: Boolean
) {
    val activeColor = Color.semanticSuccess
    val inactiveColor = if (isDark) Color(0xFF636366) else Color(0xFFA1A1AA)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Icon(
            imageVector = if (isMet) Icons.Filled.Check else Icons.Filled.Close,
            contentDescription = null,
            tint = if (isMet) activeColor else inactiveColor,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = if (isMet) activeColor else inactiveColor
        )
    }
}
