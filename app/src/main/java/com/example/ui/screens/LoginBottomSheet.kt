package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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

/**
 * Screen 2: FiveLight Authentication Choice Modal Bottom Sheet.
 *
 * Appears directly as a modal sheet over Screen 1 (PreLoginPromptScreen)
 * with a dimmed background scrim.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginBottomSheet(
    sheetState: SheetState,
    authRepository: AuthRepository,
    onNavigateToLoginEmail: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onDismiss: () -> Unit,
    onAuthSuccess: () -> Unit
) {
    val isDark = isAppInDarkTheme()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
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

            MainLoginOptions(
                isLoading = isLoading,
                isDark = isDark,
                onLoginWithEmail = {
                    if (!isLoading) {
                        errorMessage = null
                        infoMessage = null
                        onNavigateToLoginEmail()
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
                        onNavigateToRegister()
                    }
                },
                onClose = {
                    if (!isLoading) onDismiss()
                }
            )
        }
    }
}

/**
 * Screen 2 Main Options View:
 * 1. Title: "Log In to Continue"
 * 2. Subtitle: "Sign in or create an account to keep everything in sync."
 * 3. Two side-by-side cards: "Login with Email" & "Login with Google"
 * 4. Thin divider: "Don't have an account?"
 * 5. Full-width monochrome button: "Register New Account"
 * 6. Action: "Close"
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

        // 3. Two side-by-side tiles: Login with Email & Login with Google
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

            // Google Card
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
