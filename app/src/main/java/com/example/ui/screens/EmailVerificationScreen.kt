package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.AuthRepository
import com.example.ui.theme.SerifHeaderFont
import com.example.ui.theme.isAppInDarkTheme
import com.example.ui.theme.semanticError
import com.example.ui.theme.semanticSuccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Dedicated Email Verification Screen.
 * Replaces the 4-digit OTP screen.
 */
@Composable
fun EmailVerificationScreen(
    email: String,
    authRepository: AuthRepository,
    onBack: () -> Unit,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var isLoading by remember { mutableStateOf(false) }
    var isResending by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    // Theme Color Tokens matching FiveLight auth Screens
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
        label = "verificationSubmitScale"
    )

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("email_verification_screen"),
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
                    text = "Verify your email",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = eyebrowColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Heading
                Text(
                    text = "Check your inbox",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = SerifHeaderFont,
                        fontWeight = FontWeight.Normal,
                        fontSize = 28.sp,
                        lineHeight = 34.sp
                    ),
                    color = titleColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                Text(
                    text = "We've sent a verification link to your email address. Please open the email and tap the verification link to continue.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.5.sp,
                        lineHeight = 21.sp
                    ),
                    color = eyebrowColor
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Displaying email prominently beneath supporting text
                Text(
                    text = email.ifBlank { "user@example.com" },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.1.sp
                    ),
                    color = titleColor,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Error / Info Banners
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

                if (infoMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.semanticSuccess.copy(alpha = 0.12f))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = infoMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.semanticSuccess,
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
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Primary CTA Button: I've Verified My Email →
                        Button(
                            onClick = {
                                isLoading = true
                                errorMessage = null
                                infoMessage = null
                                coroutineScope.launch {
                                    val reloadResult = authRepository.reloadUser()
                                    isLoading = false
                                    reloadResult.onSuccess { user ->
                                        if (user.isEmailVerified) {
                                            infoMessage = "Email successfully verified!"
                                            onAuthSuccess()
                                        } else {
                                            errorMessage = "Your email is not verified yet. Please check your inbox, click the verification link, and try again."
                                        }
                                    }.onFailure { e ->
                                        errorMessage = e.message ?: "Failed to check verification status. Please try again."
                                    }
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
                                .testTag("verify_email_btn")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = primaryBtnText,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "I've Verified My Email  →",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp,
                                        letterSpacing = 0.2.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Open Email App Option
                        Text(
                            text = "Open Email App",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = titleColor.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .clickable(enabled = !isLoading) {
                                    try {
                                        val intent = Intent(Intent.ACTION_MAIN).apply {
                                            addCategory(Intent.CATEGORY_APP_EMAIL)
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "No email app found.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .testTag("open_email_app_btn")
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Resend Verification Email Link
                        Text(
                            text = if (isResending) "Sending link..." else "Resend Verification Email",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = eyebrowColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .clickable(enabled = !isResending && !isLoading) {
                                    isResending = true
                                    errorMessage = null
                                    infoMessage = null
                                    coroutineScope.launch {
                                        val res = authRepository.sendEmailVerification()
                                        isResending = false
                                        res.onSuccess {
                                            infoMessage = "A fresh verification link has been sent to your email address."
                                        }.onFailure { e ->
                                            errorMessage = e.message ?: "Failed to resend verification email. Please try again later."
                                        }
                                    }
                                }
                                .testTag("resend_email_btn")
                        )
                    }
                }
            }
        }
    }
}
