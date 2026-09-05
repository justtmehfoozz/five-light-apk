package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.auth.AuthRepository
import com.example.ui.theme.FiveLightMotion
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.fiveLightPressable
import com.example.ui.theme.rememberIsReducedMotion
import com.example.ui.theme.semanticAccentForeground
import com.example.ui.theme.semanticBorder
import com.example.ui.theme.semanticControl
import com.example.ui.theme.semanticError
import com.example.ui.theme.semanticMutedText
import com.example.ui.theme.semanticPrimaryAccent
import com.example.ui.theme.semanticSurface
import com.example.ui.theme.semanticSurfaceElevated
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Reusable user avatar supporting network image loading via Coil and crisp initials fallback.
 */
@Composable
fun UserAvatar(
    user: FirebaseUser?,
    size: Dp = 48.dp,
    textSize: TextUnit = 18.sp,
    modifier: Modifier = Modifier
) {
    val photoUrl = user?.photoUrl?.toString()
    val displayName = user?.displayName
    val email = user?.email
    val initials = remember(displayName, email) {
        deriveUserInitials(displayName, email)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.semanticPrimaryAccent)
            .border(1.dp, Color.semanticBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else {
            Text(
                text = initials,
                color = Color.semanticAccentForeground,
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = textSize
            )
        }
    }
}

/**
 * Derives 1 or 2 letter initials from displayName or email.
 */
fun deriveUserInitials(displayName: String?, email: String?): String {
    val name = displayName?.trim()
    if (!name.isNullOrBlank()) {
        val parts = name.split("\\s+".toRegex()).filter { it.isNotBlank() }
        return when {
            parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
            parts.isNotEmpty() && parts[0].isNotEmpty() -> parts[0].take(1).uppercase()
            else -> "U"
        }
    }
    val mail = email?.trim()
    if (!mail.isNullOrBlank()) {
        val userPart = mail.substringBefore('@').trim()
        if (userPart.isNotEmpty()) {
            return userPart.take(1).uppercase()
        }
    }
    return "U"
}

/**
 * Returns human-readable sign-in provider name.
 */
fun getAuthProviderName(user: FirebaseUser?): String {
    if (user == null) return "Signed in with Email"
    val isGoogle = user.providerData.any { it.providerId == "google.com" }
    return if (isGoogle) "Google" else "Email"
}

/**
 * Dedicated Profile Sub-Screen inside Preferences Bottom Sheet.
 */
@Composable
fun ProfileSubScreen(
    currentUser: FirebaseUser?,
    authRepository: AuthRepository,
    onBack: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    val isReducedMotion = rememberIsReducedMotion()

    // Entrance Animation States (One-shot)
    val avatarAlpha = remember { Animatable(if (isReducedMotion) 1f else 0f) }
    val avatarOffsetY = remember { Animatable(if (isReducedMotion) 0f else 4f) }

    val identityAlpha = remember { Animatable(if (isReducedMotion) 1f else 0f) }
    val identityOffsetY = remember { Animatable(if (isReducedMotion) 0f else 6f) }

    val accountAlpha = remember { Animatable(if (isReducedMotion) 1f else 0f) }
    val accountOffsetY = remember { Animatable(if (isReducedMotion) 0f else 8f) }

    LaunchedEffect(Unit) {
        if (!isReducedMotion) {
            // Sequence 1: Avatar (0ms)
            launch {
                avatarAlpha.animateTo(1f, tween(180))
            }
            launch {
                avatarOffsetY.animateTo(0f, tween(180))
            }

            // Sequence 2: Identity (slightly delayed ~50ms)
            delay(50)
            launch {
                identityAlpha.animateTo(1f, tween(200))
            }
            launch {
                identityOffsetY.animateTo(0f, tween(200))
            }

            // Sequence 3: Account Section (~100ms)
            delay(50)
            launch {
                accountAlpha.animateTo(1f, tween(220))
            }
            launch {
                accountOffsetY.animateTo(0f, tween(220))
            }
        }
    }

    val providerName = remember(currentUser) { getAuthProviderName(currentUser) }
    val displayName = remember(currentUser) {
        currentUser?.displayName?.takeIf { it.isNotBlank() }
            ?: currentUser?.email?.substringBefore('@')?.takeIf { it.isNotBlank() }
            ?: "Your Profile"
    }

    SubScreenLayout(
        title = "PROFILE",
        onBack = onBack
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header Identity Area
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar Animation Layer
                    Box(
                        modifier = Modifier.graphicsLayer {
                            alpha = avatarAlpha.value
                            translationY = avatarOffsetY.value.dp.toPx()
                        }
                    ) {
                        UserAvatar(
                            user = currentUser,
                            size = 76.dp,
                            textSize = 28.sp,
                            modifier = Modifier.testTag("profile_avatar_large")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Identity Info Animation Layer
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.graphicsLayer {
                            alpha = identityAlpha.value
                            translationY = identityOffsetY.value.dp.toPx()
                        }
                    ) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = SpaceGrotesk,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (!currentUser?.email.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = currentUser?.email.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(percent = 50))
                                .background(Color.semanticControl)
                                .border(1.dp, Color.semanticBorder, RoundedCornerShape(percent = 50))
                                .padding(horizontal = 14.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "Signed in with $providerName",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Account Section
            item {
                Column(
                    modifier = Modifier.graphicsLayer {
                        alpha = accountAlpha.value
                        translationY = accountOffsetY.value.dp.toPx()
                    }
                ) {
                    MenuGroupCard(title = "ACCOUNT") {
                        // Name Row (Editable)
                        AccountItemRow(
                            label = "Name",
                            value = currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "Not set",
                            isEditable = true,
                            onClick = { showEditNameDialog = true },
                            testTag = "profile_name_row"
                        )

                        HorizontalDivider(color = Color.semanticBorder, thickness = 0.6.dp)

                        // Email Row (Informational)
                        AccountItemRow(
                            label = "Email",
                            value = currentUser?.email?.takeIf { it.isNotBlank() } ?: "Not provided",
                            isEditable = false,
                            onClick = null,
                            testTag = "profile_email_row"
                        )

                        HorizontalDivider(color = Color.semanticBorder, thickness = 0.6.dp)

                        // Sign-in Method Row (Informational)
                        AccountItemRow(
                            label = "Sign-in method",
                            value = "Signed in with $providerName",
                            isEditable = false,
                            onClick = null,
                            testTag = "profile_signin_method_row"
                        )
                    }
                }
            }

            // Sign Out Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { showSignOutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .fiveLightPressable(pressedScale = 0.98f) { showSignOutDialog = true }
                        .testTag("profile_sign_out_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.semanticError.copy(alpha = 0.12f),
                        contentColor = Color.semanticError
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = "Sign Out",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Edit Name Dialog
    if (showEditNameDialog) {
        EditNameDialog(
            currentName = currentUser?.displayName.orEmpty(),
            onDismiss = { showEditNameDialog = false },
            onSave = { newName ->
                coroutineScope.launch {
                    val result = authRepository.updateDisplayName(newName)
                    result.onSuccess {
                        showEditNameDialog = false
                        Toast.makeText(context, "Display name updated", Toast.LENGTH_SHORT).show()
                    }.onFailure { err ->
                        Toast.makeText(context, err.message ?: "Failed to update name", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    // Sign Out Confirmation Dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = {
                Text(
                    text = "Sign Out",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Are you sure you want to sign out of your FiveLight account?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        onSignOut()
                    },
                    modifier = Modifier.testTag("confirm_sign_out_btn")
                ) {
                    Text(
                        text = "Sign Out",
                        color = Color.semanticError,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSignOutDialog = false }
                ) {
                    Text("Cancel")
                }
            },
            containerColor = Color.semanticSurfaceElevated,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
private fun AccountItemRow(
    label: String,
    value: String,
    isEditable: Boolean,
    onClick: (() -> Unit)?,
    testTag: String
) {
    val modifier = if (isEditable && onClick != null) {
        Modifier
            .fillMaxWidth()
            .fiveLightPressable(pressedScale = 0.98f) { onClick() }
            .padding(horizontal = 16.dp, vertical = 15.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 15.dp)
    }

    Row(
        modifier = modifier.testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Normal
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = if (isEditable) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isEditable) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Edit $label",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun EditNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(currentName) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val validateAndSave = {
        val trimmed = text.trim()
        when {
            trimmed.isBlank() -> {
                errorMsg = "Name cannot be empty"
            }
            trimmed.length > 50 -> {
                errorMsg = "Name cannot exceed 50 characters"
            }
            else -> {
                errorMsg = null
                isSaving = true
                onSave(trimmed)
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = {
            Text(
                text = "Edit Name",
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        if (errorMsg != null) errorMsg = null
                    },
                    label = { Text("Display Name") },
                    singleLine = true,
                    enabled = !isSaving,
                    isError = errorMsg != null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        validateAndSave()
                    }),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_name_dialog_input")
                )

                if (!errorMsg.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorMsg.orEmpty(),
                        color = Color.semanticError,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = validateAndSave,
                enabled = !isSaving,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.semanticPrimaryAccent,
                    contentColor = Color.semanticAccentForeground
                ),
                modifier = Modifier.testTag("save_edit_name_btn")
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.semanticAccentForeground
                    )
                } else {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text("Cancel")
            }
        },
        containerColor = Color.semanticSurfaceElevated,
        shape = RoundedCornerShape(18.dp)
    )
}
