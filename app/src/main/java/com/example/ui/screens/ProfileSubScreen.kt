package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CheckCircle
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
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.ui.theme.semanticSuccess
import com.example.ui.theme.semanticSurfaceElevated
import com.example.ui.theme.semanticWarning
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
    size: Dp = 80.dp,
    textSize: TextUnit = 28.sp,
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
    if (user == null) return "Email"
    val isGoogle = user.providerData.any { it.providerId == "google.com" }
    return if (isGoogle) "Google" else "Email"
}

/**
 * Dedicated Profile Sub-Screen inside Preferences Bottom Sheet (Phase 2B Motion & Micro-interaction Polish).
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
    val easing = FastOutSlowInEasing

    // Staggered One-shot Entrance Animation States (Section 1)
    val avatarAlpha = remember { Animatable(if (isReducedMotion) 1f else 0f) }
    val avatarOffsetY = remember { Animatable(if (isReducedMotion) 0f else 6f) }

    val identityAlpha = remember { Animatable(if (isReducedMotion) 1f else 0f) }
    val identityOffsetY = remember { Animatable(if (isReducedMotion) 0f else 6f) }

    val accountAlpha = remember { Animatable(if (isReducedMotion) 1f else 0f) }
    val accountOffsetY = remember { Animatable(if (isReducedMotion) 0f else 6f) }

    val emailAlpha = remember { Animatable(if (isReducedMotion) 1f else 0f) }
    val emailOffsetY = remember { Animatable(if (isReducedMotion) 0f else 6f) }

    val syncAlpha = remember { Animatable(if (isReducedMotion) 1f else 0f) }
    val syncOffsetY = remember { Animatable(if (isReducedMotion) 0f else 6f) }

    val actionsAlpha = remember { Animatable(if (isReducedMotion) 1f else 0f) }
    val actionsOffsetY = remember { Animatable(if (isReducedMotion) 0f else 6f) }

    LaunchedEffect(Unit) {
        if (!isReducedMotion) {
            // 0–250ms: Avatar
            launch { avatarAlpha.animateTo(1f, tween(250, easing = easing)) }
            launch { avatarOffsetY.animateTo(0f, tween(250, easing = easing)) }

            // 70–450ms: Name/email/provider
            launch {
                delay(70)
                launch { identityAlpha.animateTo(1f, tween(380, easing = easing)) }
                launch { identityOffsetY.animateTo(0f, tween(380, easing = easing)) }
            }

            // 150–500ms: ACCOUNT section
            launch {
                delay(150)
                launch { accountAlpha.animateTo(1f, tween(350, easing = easing)) }
                launch { accountOffsetY.animateTo(0f, tween(350, easing = easing)) }
            }

            // 220–550ms: EMAIL section
            launch {
                delay(220)
                launch { emailAlpha.animateTo(1f, tween(330, easing = easing)) }
                launch { emailOffsetY.animateTo(0f, tween(330, easing = easing)) }
            }

            // 290–600ms: SYNC section
            launch {
                delay(290)
                launch { syncAlpha.animateTo(1f, tween(310, easing = easing)) }
                launch { syncOffsetY.animateTo(0f, tween(310, easing = easing)) }
            }

            // 360–650ms: ACCOUNT ACTIONS section
            launch {
                delay(360)
                launch { actionsAlpha.animateTo(1f, tween(290, easing = easing)) }
                launch { actionsOffsetY.animateTo(0f, tween(290, easing = easing)) }
            }
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
            is com.example.data.sync.SyncState.Offline -> "Offline / Waiting for connection"
            is com.example.data.sync.SyncState.Error -> "Offline / Waiting for connection"
            is com.example.data.sync.SyncState.Idle -> "Synced"
        }
    }

    val syncStatusDotColor = if (currentUser == null) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        when (syncState) {
            is com.example.data.sync.SyncState.Syncing -> Color.semanticPrimaryAccent
            is com.example.data.sync.SyncState.Synced -> Color.semanticSuccess
            is com.example.data.sync.SyncState.Offline -> Color.semanticWarning
            is com.example.data.sync.SyncState.Error -> Color.semanticWarning
            is com.example.data.sync.SyncState.Idle -> Color.semanticSuccess
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

    var lastBackupTime by remember {
        mutableStateOf(com.example.data.backup.BackupManager.getLastBackupTime(context))
    }
    var isBackingUp by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }

    var driveAccount by remember {
        mutableStateOf(com.example.data.backup.GoogleDriveService.getAuthorizedAccount(context))
    }
    var pendingBackupAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val driveAuthLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            if (account != null && com.google.android.gms.auth.api.signin.GoogleSignIn.hasPermissions(account, com.example.data.backup.GoogleDriveService.DRIVE_APPDATA_SCOPE)) {
                driveAccount = account
                Toast.makeText(context, "Google Drive connected: ${account.email}", Toast.LENGTH_SHORT).show()
                pendingBackupAction?.invoke()
                pendingBackupAction = null
            } else {
                Toast.makeText(context, "Google Drive authorization was not granted", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            android.util.Log.e("ProfileSubScreen", "Google Drive authorization failed: ${e.message}")
            Toast.makeText(context, "Google Drive authorization failed", Toast.LENGTH_SHORT).show()
        }
    }

    val requestDriveAuth: (() -> Unit) -> Unit = { onAuthorized ->
        pendingBackupAction = onAuthorized
        val signInClient = com.example.data.backup.GoogleDriveService.getGoogleSignInClient(context)
        driveAuthLauncher.launch(signInClient.signInIntent)
    }

    val formattedLastBackup = remember(lastBackupTime) {
        if (lastBackupTime > 0) {
            try {
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(lastBackupTime))
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Identity Header Area
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 6.dp),
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
                            size = 80.dp,
                            textSize = 30.sp,
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
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (!currentUser?.email.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = currentUser?.email.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (providerName == "Google") "Google account" else "Email account",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
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
                    ProfileSection(title = "ACCOUNT") {
                        // Name Row (Editable)
                        ProfileRow(
                            label = "Name",
                            value = currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "Not set",
                            isAction = true,
                            onClick = { showEditNameDialog = true },
                            testTag = "profile_name_row"
                        )

                        HorizontalDivider(color = Color.semanticBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

                        // Email Row (Informational)
                        ProfileRow(
                            label = "Email",
                            value = currentUser?.email?.takeIf { it.isNotBlank() } ?: "Not provided",
                            isAction = false,
                            onClick = null,
                            testTag = "profile_email_row"
                        )

                        HorizontalDivider(color = Color.semanticBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

                        // Sign-in Method Row
                        ProfileRow(
                            label = "Sign-in method",
                            value = providerName,
                            isAction = false,
                            onClick = null,
                            testTag = "profile_signin_method_row"
                        )

                        if (isEmailUser) {
                            HorizontalDivider(color = Color.semanticBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

                            // Change Password Row (Email users only)
                            ProfileRow(
                                label = "Change Password",
                                value = "",
                                isAction = true,
                                onClick = { showChangePasswordDialog = true },
                                testTag = "profile_change_password_row"
                            )
                        }
                    }
                }
            }

            // EMAIL Section (Email/Password users only)
            if (isEmailUser) {
                item {
                    Column(
                        modifier = Modifier.graphicsLayer {
                            alpha = emailAlpha.value
                            translationY = emailOffsetY.value.dp.toPx()
                        }
                    ) {
                        ProfileSection(title = "EMAIL") {
                            val isVerified = currentUser?.isEmailVerified == true

                            AnimatedContent(
                                targetState = isVerified,
                                transitionSpec = {
                                    (fadeIn(animationSpec = tween(280, easing = FastOutSlowInEasing)) +
                                            slideInVertically(animationSpec = tween(280, easing = FastOutSlowInEasing)) { -10 }) togetherWith
                                            (fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing)) +
                                                    slideOutVertically(animationSpec = tween(200, easing = FastOutSlowInEasing)) { 10 })
                                },
                                label = "emailVerificationStateTransition"
                            ) { verified ->
                                if (verified) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.CheckCircle,
                                            contentDescription = "Verified",
                                            tint = Color.semanticPrimaryAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Email verified",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 14.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text(
                                                text = "!",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color.semanticWarning
                                            )
                                            Column {
                                                Text(
                                                    text = "Email not verified",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Verification required",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
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
                                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                                    disabledContainerColor = Color.semanticControl.copy(alpha = 0.6f),
                                                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                ),
                                                shape = RoundedCornerShape(10.dp),
                                                border = BorderStroke(0.5.dp, Color.semanticBorder),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(38.dp)
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
                                                        text = "Resend verification email",
                                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
                                                    )
                                                }
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Color.semanticControl)
                                                    .border(0.5.dp, Color.semanticBorder, RoundedCornerShape(10.dp))
                                                    .fiveLightPressable(pressedScale = 0.98f) {
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
                                                    }
                                                    .testTag("refresh_verification_status_button"),
                                                contentAlignment = Alignment.Center
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
                    ProfileSection(title = "SYNC") {
                        // Cloud Sync Row
                        ProfileRow(
                            label = "Cloud Sync",
                            value = syncStatusText,
                            statusDotColor = syncStatusDotColor,
                            isSyncing = syncState is com.example.data.sync.SyncState.Syncing,
                            isAction = false,
                            onClick = null,
                            testTag = "profile_cloud_sync_row"
                        )

                        HorizontalDivider(color = Color.semanticBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

                        // Last Synced Row
                        ProfileRow(
                            label = "Last synced",
                            value = formattedLastSynced,
                            isAction = false,
                            onClick = null,
                            testTag = "profile_last_synced_row"
                        )
                    }
                }
            }

            // BACKUP Section
            item {
                Column(
                    modifier = Modifier.graphicsLayer {
                        alpha = syncAlpha.value
                        translationY = syncOffsetY.value.dp.toPx()
                    }
                ) {
                    ProfileSection(title = "BACKUP") {
                        // Drive Backup Status Row
                        ProfileRow(
                            label = "Google Drive",
                            value = if (driveAccount != null) "Connected (${driveAccount?.email ?: ""})" else "Not Authorized",
                            isAction = driveAccount == null,
                            onClick = if (driveAccount == null) { { requestDriveAuth {} } } else null,
                            testTag = "profile_drive_status_row"
                        )

                        HorizontalDivider(color = Color.semanticBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

                        // Last Backup Row
                        ProfileRow(
                            label = "Last backup",
                            value = formattedLastBackup,
                            isAction = false,
                            onClick = null,
                            testTag = "profile_last_backup_row"
                        )

                        HorizontalDivider(color = Color.semanticBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

                        // Back Up Now Action Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fiveLightPressable(pressedScale = 0.98f) {
                                    val runBackup = {
                                        if (!isBackingUp) {
                                            isBackingUp = true
                                            coroutineScope.launch {
                                                try {
                                                    val repo = com.example.data.repository.AppRepository.getInstance(context)
                                                    val res = com.example.data.backup.BackupManager.performBackup(context, repo, authRepository)
                                                    if (res.isSuccess) {
                                                        lastBackupTime = res.getOrNull() ?: System.currentTimeMillis()
                                                        Toast.makeText(context, "Google Drive backup completed successfully", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, "Backup failed: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                                    }
                                                } finally {
                                                    isBackingUp = false
                                                }
                                            }
                                        }
                                    }

                                    if (driveAccount != null) {
                                        runBackup()
                                    } else {
                                        requestDriveAuth { runBackup() }
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                .testTag("profile_backup_now_btn"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isBackingUp) "Backing up..." else "Back Up Now",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.semanticPrimaryAccent,
                                fontWeight = FontWeight.Medium
                            )
                            if (isBackingUp) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.semanticPrimaryAccent
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Back Up Now",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = Color.semanticBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

                        // Restore Action Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fiveLightPressable(pressedScale = 0.98f) {
                                    val runRestore = {
                                        if (!isRestoring) {
                                            isRestoring = true
                                            coroutineScope.launch {
                                                try {
                                                    val repo = com.example.data.repository.AppRepository.getInstance(context)
                                                    val syncManager = com.example.data.sync.FirestoreSyncManager.getInstance(context, repo, authRepository)
                                                    val res = com.example.data.backup.BackupManager.performRestore(context, repo, authRepository, syncManager)
                                                    if (res.isSuccess) {
                                                        Toast.makeText(context, "Data restored successfully from Google Drive", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, "Restore failed: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                                    }
                                                } finally {
                                                    isRestoring = false
                                                }
                                            }
                                        }
                                    }

                                    if (driveAccount != null) {
                                        runRestore()
                                    } else {
                                        requestDriveAuth { runRestore() }
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                .testTag("profile_restore_btn"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isRestoring) "Restoring..." else "Restore from Google Drive",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                            if (isRestoring) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Restore from Google Drive",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ACCOUNT ACTIONS Section
            item {
                Column(
                    modifier = Modifier.graphicsLayer {
                        alpha = actionsAlpha.value
                        translationY = actionsOffsetY.value.dp.toPx()
                    }
                ) {
                    ProfileSection(title = "ACCOUNT ACTIONS") {
                        // Sign Out Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fiveLightPressable(pressedScale = 0.98f) { showSignOutDialog = true }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                .testTag("profile_sign_out_btn"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Sign Out",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Sign Out",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        HorizontalDivider(color = Color.semanticBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

                        // Delete Account Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fiveLightPressable(pressedScale = 0.98f) { showDeleteAccountDialog = true }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                .testTag("profile_delete_account_btn"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Delete Account",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.semanticError,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Delete Account",
                                tint = Color.semanticError.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
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
        SignOutDialog(
            onDismiss = { showSignOutDialog = false },
            onConfirmSignOut = {
                showSignOutDialog = false
                onSignOut()
            }
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
private fun ProfileSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.semanticSurfaceElevated)
                .border(0.5.dp, Color.semanticBorder.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun ProfileRow(
    label: String,
    value: String = "",
    valueColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    statusDotColor: Color? = null,
    isSyncing: Boolean = false,
    isAction: Boolean = false,
    onClick: (() -> Unit)? = null,
    testTag: String = ""
) {
    val isReducedMotion = rememberIsReducedMotion()

    val dotAlpha = if (isSyncing && !isReducedMotion) {
        val infiniteTransition = rememberInfiniteTransition(label = "syncDotPulse")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "syncPulseAlpha"
        )
        alpha
    } else {
        1.0f
    }

    val modifier = if (onClick != null) {
        Modifier
            .fillMaxWidth()
            .fiveLightPressable(pressedScale = 0.98f) { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (statusDotColor != null) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .graphicsLayer { alpha = dotAlpha }
                        .clip(CircleShape)
                        .background(statusDotColor)
                )
            }

            if (value.isNotEmpty()) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = valueColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isAction) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
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

    val isReducedMotion = rememberIsReducedMotion()
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "editNameDialogAlpha"
    )

    val offsetY by animateDpAsState(
        targetValue = if (isVisible || isReducedMotion) 0.dp else 16.dp,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "editNameDialogOffsetY"
    )

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
        modifier = Modifier.graphicsLayer {
            this.alpha = if (isReducedMotion) 1f else alpha
            this.translationY = offsetY.toPx()
        },
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
    val isReducedMotion = rememberIsReducedMotion()
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "changePasswordDialogAlpha"
    )

    val offsetY by animateDpAsState(
        targetValue = if (isVisible || isReducedMotion) 0.dp else 16.dp,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "changePasswordDialogOffsetY"
    )

    val validateAndSave = {
        when {
            currentPassword.isEmpty() -> errorMsg = "Current password is required"
            newPassword.length < 6 -> errorMsg = "New password must be at least 6 characters"
            newPassword != confirmPassword -> errorMsg = "New passwords do not match"
            else -> {
                errorMsg = null
                isSubmitting = true
                onConfirm(currentPassword, newPassword) { err ->
                    isSubmitting = false
                    errorMsg = err
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        modifier = Modifier.graphicsLayer {
            this.alpha = if (isReducedMotion) 1f else alpha
            this.translationY = offsetY.toPx()
        },
        title = {
            Text(
                text = "Change Password",
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
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
                    label = { Text("New Password (min 6 chars)") },
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
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
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
private fun SignOutDialog(
    onDismiss: () -> Unit,
    onConfirmSignOut: () -> Unit
) {
    val isReducedMotion = rememberIsReducedMotion()
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "signOutDialogAlpha"
    )

    val offsetY by animateDpAsState(
        targetValue = if (isVisible || isReducedMotion) 0.dp else 16.dp,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "signOutDialogOffsetY"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.graphicsLayer {
            this.alpha = if (isReducedMotion) 1f else alpha
            this.translationY = offsetY.toPx()
        },
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
                onClick = onConfirmSignOut,
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
            TextButton(onClick = onDismiss) {
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
    val isReducedMotion = rememberIsReducedMotion()
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "deleteAccountDialogAlpha"
    )

    val offsetY by animateDpAsState(
        targetValue = if (isVisible || isReducedMotion) 0.dp else 16.dp,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "deleteAccountDialogOffsetY"
    )

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
        modifier = Modifier.graphicsLayer {
            this.alpha = if (isReducedMotion) 1f else alpha
            this.translationY = offsetY.toPx()
        },
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
