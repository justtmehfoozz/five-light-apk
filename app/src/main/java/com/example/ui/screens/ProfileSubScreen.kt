package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.MarkEmailUnread
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.auth.AuthRepository
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.fiveLightPressable
import com.example.ui.theme.rememberIsReducedMotion
import com.example.ui.theme.semanticAccentForeground
import com.example.ui.theme.semanticBorder
import com.example.ui.theme.semanticControl
import com.example.ui.theme.semanticError
import com.example.ui.theme.semanticPrimaryAccent
import com.example.ui.theme.semanticSurfaceElevated
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

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
 * Dedicated Profile Sub-Screen inside Preferences Bottom Sheet (Phase 2).
 */
@Composable
fun ProfileSubScreen(
    currentUser: FirebaseUser?,
    authRepository: AuthRepository,
    syncState: com.example.data.sync.SyncState = com.example.data.sync.SyncState.Idle,
    lastSyncedTime: Long? = null,
    onChangePassword: suspend (currentPassword: String, newPassword: String) -> Result<Unit> = { _, _ -> Result.failure(Exception("Not implemented")) },
    onDeleteAccount: suspend (passwordForReauth: String?) -> Result<Unit> = { _ -> Result.failure(Exception("Not implemented")) },
    onBack: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    var isResendingEmail by remember { mutableStateOf(false) }
    var isReloadingUser by remember { mutableStateOf(false) }

    val isReducedMotion = rememberIsReducedMotion()

    // Entrance Animation States (One-shot)
    val avatarAlpha = remember { Animatable(if (isReducedMotion) 1f else 0f) }
    val avatarOffsetY = remember { Animatable(if (isReducedMotion) 0f else 4f) }

    val identityAlpha = remember { Animatable(if (isReducedMotion) 1f else 0f) }
    val identityOffsetY = remember { Animatable(if (isReducedMotion) 0f else 6f) }

    val accountAlpha = remember { Animatable(if (isReducedMotion) 1f else 0f) }
    val accountOffsetY = remember { Animatable(if (isReducedMotion) 0f else 8f) }

    val syncAlpha = remember { Animatable(if (isReducedMotion) 1f else 0f) }
    val syncOffsetY = remember { Animatable(if (isReducedMotion) 0f else 10f) }

    val actionsAlpha = remember { Animatable(if (isReducedMotion) 1f else 0f) }
    val actionsOffsetY = remember { Animatable(if (isReducedMotion) 0f else 12f) }

    LaunchedEffect(Unit) {
        if (!isReducedMotion) {
            launch { avatarAlpha.animateTo(1f, tween(180)) }
            launch { avatarOffsetY.animateTo(0f, tween(180)) }

            delay(40)
            launch { identityAlpha.animateTo(1f, tween(180)) }
            launch { identityOffsetY.animateTo(0f, tween(180)) }

            delay(40)
            launch { accountAlpha.animateTo(1f, tween(200)) }
            launch { accountOffsetY.animateTo(0f, tween(200)) }

            delay(40)
            launch { syncAlpha.animateTo(1f, tween(200)) }
            launch { syncOffsetY.animateTo(0f, tween(200)) }

            delay(40)
            launch { actionsAlpha.animateTo(1f, tween(220)) }
            launch { actionsOffsetY.animateTo(0f, tween(220)) }
        }
    }

    val providerName = remember(currentUser) { getAuthProviderName(currentUser) }
    val isEmailUser = remember(currentUser) {
        currentUser != null &&
                currentUser.providerData.none { it.providerId == "google.com" } &&
                !currentUser.email.isNullOrBlank()
    }

    LaunchedEffect(currentUser?.uid) {
        if (isEmailUser && currentUser != null && !currentUser.isEmailVerified) {
            authRepository.reloadUser()
        }
    }
    val displayName = remember(currentUser) {
        currentUser?.displayName?.takeIf { it.isNotBlank() }
            ?: currentUser?.email?.substringBefore('@')?.takeIf { it.isNotBlank() }
            ?: "Your Profile"
    }

    val syncStatusText = remember(currentUser, syncState) {
        if (currentUser == null) "Not signed in"
        else when (syncState) {
            is com.example.data.sync.SyncState.Syncing -> "Syncing..."
            is com.example.data.sync.SyncState.Synced -> "Synced"
            is com.example.data.sync.SyncState.Error -> "Offline / Waiting for connection"
            is com.example.data.sync.SyncState.Idle -> "Synced"
        }
    }

    val formattedLastSynced = remember(lastSyncedTime) {
        if (lastSyncedTime != null && lastSyncedTime > 0) {
            try {
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(lastSyncedTime))
            } catch (_: Exception) {
                "Not available"
            }
        } else {
            "Not available"
        }
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

            // ACCOUNT Section
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

                        if (isEmailUser) {
                            HorizontalDivider(color = Color.semanticBorder, thickness = 0.6.dp)

                            val isVerified = currentUser?.isEmailVerified == true
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Email Verification",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    if (isVerified) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(percent = 50))
                                                .background(Color.semanticControl)
                                                .border(1.dp, Color.semanticBorder, RoundedCornerShape(percent = 50))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.CheckCircle,
                                                contentDescription = "Verified",
                                                tint = Color.semanticPrimaryAccent,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "Verified",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color.semanticPrimaryAccent
                                            )
                                        }
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(percent = 50))
                                                .background(Color.semanticControl)
                                                .border(1.dp, Color.semanticBorder, RoundedCornerShape(percent = 50))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.MarkEmailUnread,
                                                contentDescription = "Unverified",
                                                tint = Color.semanticError,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "Not Verified",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color.semanticError
                                            )
                                        }
                                    }
                                }

                                if (!isVerified) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = {
                                                if (!isResendingEmail) {
                                                    coroutineScope.launch {
                                                        isResendingEmail = true
                                                        val res = authRepository.sendEmailVerification()
                                                        isResendingEmail = false
                                                        if (res.isSuccess) {
                                                            Toast.makeText(context, "Verification email sent to ${currentUser?.email}", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            Toast.makeText(context, res.exceptionOrNull()?.message ?: "Failed to send verification email", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            },
                                            enabled = !isResendingEmail,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.semanticControl,
                                                contentColor = MaterialTheme.colorScheme.onSurface
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, Color.semanticBorder),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(36.dp)
                                                .testTag("resend_verification_email_button")
                                        ) {
                                            if (isResendingEmail) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(14.dp),
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    strokeWidth = 2.dp
                                                )
                                            } else {
                                                Text(
                                                    text = "Resend Verification Email",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = {
                                                if (!isReloadingUser) {
                                                    coroutineScope.launch {
                                                        isReloadingUser = true
                                                        val res = authRepository.reloadUser()
                                                        isReloadingUser = false
                                                        if (res.isSuccess) {
                                                            val u = res.getOrNull()
                                                            if (u?.isEmailVerified == true) {
                                                                Toast.makeText(context, "Email is verified!", Toast.LENGTH_SHORT).show()
                                                            } else {
                                                                Toast.makeText(context, "Email is not verified yet.", Toast.LENGTH_SHORT).show()
                                                            }
                                                        } else {
                                                            Toast.makeText(context, res.exceptionOrNull()?.message ?: "Failed to refresh verification status", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.semanticControl)
                                                .border(1.dp, Color.semanticBorder, RoundedCornerShape(8.dp))
                                                .testTag("refresh_verification_status_button")
                                        ) {
                                            if (isReloadingUser) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(14.dp),
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    strokeWidth = 2.dp
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Outlined.Refresh,
                                                    contentDescription = "Refresh Verification Status",
                                                    tint = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = Color.semanticBorder, thickness = 0.6.dp)

                        // Sign-in Method Row (Informational)
                        AccountItemRow(
                            label = "Sign-in method",
                            value = "Signed in with $providerName",
                            isEditable = false,
                            onClick = null,
                            testTag = "profile_signin_method_row"
                        )

                        if (isEmailUser) {
                            HorizontalDivider(color = Color.semanticBorder, thickness = 0.6.dp)

                            // Change Password Row (Email users only)
                            AccountItemRow(
                                label = "Change Password",
                                value = "",
                                isEditable = true,
                                onClick = { showChangePasswordDialog = true },
                                testTag = "profile_change_password_row"
                            )
                        }
                    }
                }
            }

            // SYNC Section
            item {
                Column(
                    modifier = Modifier.graphicsLayer {
                        alpha = syncAlpha.value
                        translationY = syncOffsetY.value.dp.toPx()
                    }
                ) {
                    MenuGroupCard(title = "SYNC") {
                        // Cloud Sync Row
                        AccountItemRow(
                            label = "Cloud Sync",
                            value = syncStatusText,
                            isEditable = false,
                            onClick = null,
                            testTag = "profile_cloud_sync_row"
                        )

                        HorizontalDivider(color = Color.semanticBorder, thickness = 0.6.dp)

                        // Last Synced Row
                        AccountItemRow(
                            label = "Last synced",
                            value = formattedLastSynced,
                            isEditable = false,
                            onClick = null,
                            testTag = "profile_last_synced_row"
                        )
                    }
                }
            }

            // ACCOUNT ACTIONS Section
            item {
                Column(
                    modifier = Modifier.graphicsLayer {
                        alpha = actionsAlpha.value
                        translationY = actionsOffsetY.value.dp.toPx()
                    },
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "ACCOUNT ACTIONS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                    )

                    // Sign Out Button
                    Button(
                        onClick = { showSignOutDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .fiveLightPressable(pressedScale = 0.98f) { showSignOutDialog = true }
                            .testTag("profile_sign_out_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.semanticControl,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        border = BorderStroke(1.dp, Color.semanticBorder)
                    ) {
                        Text(
                            text = "Sign Out",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = SpaceGrotesk,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    // Delete Account Button
                    Button(
                        onClick = { showDeleteAccountDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .fiveLightPressable(pressedScale = 0.98f) { showDeleteAccountDialog = true }
                            .testTag("profile_delete_account_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.semanticError.copy(alpha = 0.12f),
                            contentColor = Color.semanticError
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = "Delete Account",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = SpaceGrotesk,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Dialog 1: Edit Name
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

    // Dialog 2: Change Password
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onConfirm = { currPass, newPass, onError ->
                coroutineScope.launch {
                    val result = onChangePassword(currPass, newPass)
                    result.onSuccess {
                        showChangePasswordDialog = false
                        Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show()
                    }.onFailure { err ->
                        val msg = err.message ?: "Failed to change password"
                        onError(msg)
                    }
                }
            }
        )
    }

    // Dialog 3: Sign Out Confirmation
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

    // Dialog 4: Delete Account Confirmation
    if (showDeleteAccountDialog) {
        DeleteAccountDialog(
            isEmailUser = isEmailUser,
            onDismiss = { showDeleteAccountDialog = false },
            onConfirmDelete = { passwordForReauth, onError ->
                coroutineScope.launch {
                    val result = onDeleteAccount(passwordForReauth)
                    result.onSuccess {
                        showDeleteAccountDialog = false
                        Toast.makeText(context, "Account deleted", Toast.LENGTH_SHORT).show()
                        onBack()
                    }.onFailure { err ->
                        val msg = when {
                            err.message == "REAUTH_REQUIRED_PASSWORD" -> "Password required for re-authentication"
                            err.message?.contains("REAUTH_REQUIRED") == true -> "Recent sign-in required. Please sign out and sign in again before deleting."
                            else -> err.message ?: "Failed to delete account"
                        }
                        onError(msg)
                    }
                }
            }
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
            if (value.isNotEmpty()) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = if (isEditable) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

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

@Composable
private fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (currentPass: String, newPass: String, onError: (String) -> Unit) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var currentPassVisible by remember { mutableStateOf(false) }
    var newPassVisible by remember { mutableStateOf(false) }
    var confirmPassVisible by remember { mutableStateOf(false) }

    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    val validateAndSave = {
        val curr = currentPassword.trim()
        val newP = newPassword.trim()
        val conf = confirmPassword.trim()

        when {
            curr.isEmpty() -> {
                errorMsg = "Current password is required"
            }
            newP.length < 6 -> {
                errorMsg = "New password must be at least 6 characters"
            }
            conf != newP -> {
                errorMsg = "Passwords do not match"
            }
            else -> {
                errorMsg = null
                isSubmitting = true
                onConfirm(curr, newP) { err ->
                    isSubmitting = false
                    errorMsg = err
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = {
            Text(
                text = "Change Password",
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = {
                        currentPassword = it
                        if (errorMsg != null) errorMsg = null
                    },
                    label = { Text("Current Password") },
                    singleLine = true,
                    enabled = !isSubmitting,
                    visualTransformation = if (currentPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { currentPassVisible = !currentPassVisible }) {
                            Icon(
                                imageVector = if (currentPassVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = if (currentPassVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("change_password_current_input")
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        if (errorMsg != null) errorMsg = null
                    },
                    label = { Text("New Password") },
                    singleLine = true,
                    enabled = !isSubmitting,
                    visualTransformation = if (newPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { newPassVisible = !newPassVisible }) {
                            Icon(
                                imageVector = if (newPassVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = if (newPassVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("change_password_new_input")
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        if (errorMsg != null) errorMsg = null
                    },
                    label = { Text("Confirm New Password") },
                    singleLine = true,
                    enabled = !isSubmitting,
                    visualTransformation = if (confirmPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPassVisible = !confirmPassVisible }) {
                            Icon(
                                imageVector = if (confirmPassVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = if (confirmPassVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        validateAndSave()
                    }),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("change_password_confirm_input")
                )

                if (!errorMsg.isNullOrBlank()) {
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
                enabled = !isSubmitting,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.semanticPrimaryAccent,
                    contentColor = Color.semanticAccentForeground
                ),
                modifier = Modifier.testTag("save_change_password_btn")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.semanticAccentForeground
                    )
                } else {
                    Text("Update Password", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting
            ) {
                Text("Cancel")
            }
        },
        containerColor = Color.semanticSurfaceElevated,
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
private fun DeleteAccountDialog(
    isEmailUser: Boolean,
    onDismiss: () -> Unit,
    onConfirmDelete: (passwordForReauth: String?, onError: (String) -> Unit) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isDeleting by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    val handleDelete = {
        if (isEmailUser && password.trim().isEmpty()) {
            errorMsg = "Password is required to confirm deletion"
        } else {
            errorMsg = null
            isDeleting = true
            onConfirmDelete(password.takeIf { isEmailUser }?.trim()) { err ->
                isDeleting = false
                errorMsg = err
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        title = {
            Text(
                text = "Delete your account?",
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "This permanently deletes your FiveLight account and cloud-synced data. This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isEmailUser) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (errorMsg != null) errorMsg = null
                        },
                        label = { Text("Confirm Current Password") },
                        singleLine = true,
                        enabled = !isDeleting,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            handleDelete()
                        }),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("delete_account_password_input")
                    )
                }

                if (!errorMsg.isNullOrBlank()) {
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
                onClick = handleDelete,
                enabled = !isDeleting,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.semanticError,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("confirm_delete_account_btn")
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text("Delete Account", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDeleting
            ) {
                Text("Cancel")
            }
        },
        containerColor = Color.semanticSurfaceElevated,
        shape = RoundedCornerShape(18.dp)
    )
}
