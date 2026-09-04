package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
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
import com.example.ui.theme.SerifHeaderFont
import com.example.ui.theme.semanticBorder
import com.example.ui.theme.semanticError
import com.example.ui.theme.semanticPrimaryAccent
import com.example.ui.theme.semanticSuccess
import com.example.ui.theme.semanticSurfaceElevated
import kotlinx.coroutines.launch

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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentStep by remember { mutableStateOf(LoginSheetStep.OPTIONS) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface
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
                            onLoginWithEmail = {
                                errorMessage = null
                                infoMessage = null
                                currentStep = LoginSheetStep.EMAIL_LOGIN
                            },
                            onLoginWithGoogle = {
                                errorMessage = null
                                infoMessage = null
                                isLoading = true
                                coroutineScope.launch {
                                    val result = authRepository.signInWithGoogle(context)
                                    isLoading = false
                                    result.onSuccess {
                                        onAuthSuccess()
                                    }.onFailure { e ->
                                        errorMessage = e.message ?: "Google Sign-In failed"
                                    }
                                }
                            },
                            onRegisterNewAccount = {
                                errorMessage = null
                                infoMessage = null
                                currentStep = LoginSheetStep.REGISTER
                            },
                            onClose = onDismiss
                        )
                    }

                    LoginSheetStep.EMAIL_LOGIN -> {
                        EmailLoginForm(
                            isLoading = isLoading,
                            onBack = {
                                errorMessage = null
                                infoMessage = null
                                currentStep = LoginSheetStep.OPTIONS
                            },
                            onForgotPassword = {
                                errorMessage = null
                                infoMessage = null
                                currentStep = LoginSheetStep.FORGOT_PASSWORD
                            },
                            onLogin = { email, password ->
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
                        )
                    }

                    LoginSheetStep.REGISTER -> {
                        RegisterForm(
                            isLoading = isLoading,
                            onBack = {
                                errorMessage = null
                                infoMessage = null
                                currentStep = LoginSheetStep.OPTIONS
                            },
                            onRegister = { email, password ->
                                errorMessage = null
                                infoMessage = null
                                isLoading = true
                                coroutineScope.launch {
                                    val result = authRepository.registerWithEmail(email, password)
                                    isLoading = false
                                    result.onSuccess {
                                        onAuthSuccess()
                                    }.onFailure { e ->
                                        errorMessage = e.message ?: "Registration failed. Please check your details."
                                    }
                                }
                            }
                        )
                    }

                    LoginSheetStep.FORGOT_PASSWORD -> {
                        ForgotPasswordForm(
                            isLoading = isLoading,
                            onBack = {
                                errorMessage = null
                                infoMessage = null
                                currentStep = LoginSheetStep.EMAIL_LOGIN
                            },
                            onSendReset = { email ->
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
 * 3. Two side-by-side cards: "Login with Email" (lock icon) and "Login with Google" (Google "G" icon)
 * 4. Divider with centered text: "Don't have an account?"
 * 5. Full-width button: "Register New Account"
 * 6. Text link: "Close"
 */
@Composable
private fun MainLoginOptions(
    isLoading: Boolean,
    onLoginWithEmail: () -> Unit,
    onLoginWithGoogle: () -> Unit,
    onRegisterNewAccount: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // 1. Title
        Text(
            text = "Log In to Continue",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = SerifHeaderFont,
                fontWeight = FontWeight.Normal,
                fontSize = 24.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("login_sheet_title")
        )

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Subtitle
        Text(
            text = "Sign in or create an account to keep everything in sync.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .testTag("login_sheet_subtitle")
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Two side-by-side cards: Login with Email & Login with Google
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Email Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(96.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(enabled = !isLoading, onClick = onLoginWithEmail)
                    .testTag("login_with_email_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.semanticSurfaceElevated
                ),
                border = BorderStroke(1.dp, Color.semanticBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "Email Login",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Login with Email",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Google Card (With Official Google "G" Logo)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(96.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(enabled = !isLoading, onClick = onLoginWithGoogle)
                    .testTag("login_with_google_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.semanticSurfaceElevated
                ),
                border = BorderStroke(1.dp, Color.semanticBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google_logo),
                            contentDescription = "Google Logo",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Login with Google",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Divider with centered text
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Text(
                text = "Don't have an account?",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Full-width button: Register New Account
        OutlinedButton(
            onClick = onRegisterNewAccount,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("register_new_account_btn")
        ) {
            Text(
                text = "Register New Account",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 6. Plain text link: Close
        TextButton(
            onClick = onClose,
            modifier = Modifier.testTag("login_sheet_close_btn")
        ) {
            Text(
                text = "Close",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmailLoginForm(
    isLoading: Boolean,
    onBack: () -> Unit,
    onForgotPassword: () -> Unit,
    onLogin: (email: String, pass: String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("email_login_back_btn")) {
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
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            leadingIcon = {
                Icon(Icons.Outlined.Email, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("email_input_field")
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = "Toggle password visibility"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                if (email.isNotBlank() && password.isNotBlank()) {
                    onLogin(email, password)
                }
            }),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("password_input_field")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onForgotPassword, modifier = Modifier.testTag("forgot_password_btn")) {
                Text(
                    text = "Forgot password?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                onLogin(email, password)
            },
            enabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.semanticPrimaryAccent,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("submit_email_login_btn")
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Log In",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
private fun RegisterForm(
    isLoading: Boolean,
    onBack: () -> Unit,
    onRegister: (email: String, pass: String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("register_back_btn")) {
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
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (validationError != null) {
            Text(
                text = validationError ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = Color.semanticError,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                validationError = null
            },
            label = { Text("Email Address") },
            leadingIcon = {
                Icon(Icons.Outlined.Email, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_email_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                validationError = null
            },
            label = { Text("Password (min 6 characters)") },
            leadingIcon = {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = "Toggle password visibility"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_password_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                validationError = null
            },
            label = { Text("Confirm Password") },
            leadingIcon = {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                if (password != confirmPassword) {
                    validationError = "Passwords do not match"
                } else if (password.length < 6) {
                    validationError = "Password must be at least 6 characters"
                } else if (email.isNotBlank()) {
                    onRegister(email, password)
                }
            }),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_confirm_password_input")
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                if (password != confirmPassword) {
                    validationError = "Passwords do not match"
                } else if (password.length < 6) {
                    validationError = "Password must be at least 6 characters"
                } else {
                    onRegister(email, password)
                }
            },
            enabled = email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank() && !isLoading,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.semanticPrimaryAccent,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("submit_register_btn")
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
private fun ForgotPasswordForm(
    isLoading: Boolean,
    onBack: () -> Unit,
    onSendReset: (email: String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
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
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Enter your registered email address and we will send you a password reset link.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            leadingIcon = {
                Icon(Icons.Outlined.Email, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                if (email.isNotBlank()) onSendReset(email)
            }),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("forgot_password_email_input")
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                onSendReset(email)
            },
            enabled = email.isNotBlank() && !isLoading,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.semanticPrimaryAccent,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("submit_forgot_password_btn")
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Send Reset Link",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}
