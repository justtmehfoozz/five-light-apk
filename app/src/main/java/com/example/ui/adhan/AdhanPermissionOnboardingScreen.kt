package com.example.ui.adhan

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.reminder.AdhanPermissionHelper
import com.example.data.reminder.PrayerNotificationLogger

/**
 * Dedicated permission onboarding screen for Adhan Alerts & Full-Screen Overlay.
 * Displays real-time permission status, handles system settings routing,
 * and records completion/dismissal only when explicitly acted upon by the user.
 */
@Composable
fun AdhanPermissionOnboardingScreen(
    onDismiss: () -> Unit,
    isOpenedFromSettings: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Live permission states
    var notifsGranted by remember { mutableStateOf(AdhanPermissionHelper.areNotificationsEnabled(context)) }
    var exactAlarmGranted by remember { mutableStateOf(AdhanPermissionHelper.canScheduleExactAlarms(context)) }
    var overlayGranted by remember { mutableStateOf(AdhanPermissionHelper.canDrawOverlays(context)) }
    var fullScreenGranted by remember { mutableStateOf(AdhanPermissionHelper.canUseFullScreenIntent(context)) }
    var batteryOptimizationExempt by remember { mutableStateOf(AdhanPermissionHelper.isIgnoringBatteryOptimizations(context)) }

    // Recheck permissions whenever the app returns from system settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notifsGranted = AdhanPermissionHelper.areNotificationsEnabled(context)
                exactAlarmGranted = AdhanPermissionHelper.canScheduleExactAlarms(context)
                overlayGranted = AdhanPermissionHelper.canDrawOverlays(context)
                fullScreenGranted = AdhanPermissionHelper.canUseFullScreenIntent(context)
                batteryOptimizationExempt = AdhanPermissionHelper.isIgnoringBatteryOptimizations(context)

                PrayerNotificationLogger.logValidationSuccess(
                    "adhan_permission_onboarding",
                    "ALL",
                    "live_check",
                    "Permissions rechecked: notif=$notifsGranted, exact=$exactAlarmGranted, overlay=$overlayGranted, fullScreen=$fullScreenGranted, battery=$batteryOptimizationExempt"
                )
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Permission launcher for Android 13+ POST_NOTIFICATIONS
    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notifsGranted = granted
    }

    val allRequiredGranted = notifsGranted && exactAlarmGranted && overlayGranted && fullScreenGranted

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("adhan_permission_onboarding_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isOpenedFromSettings) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("adhan_perms_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                if (!isOpenedFromSettings) {
                    TextButton(
                        onClick = {
                            AdhanPermissionHelper.setCompletedOrDismissedOnboarding(context, true)
                            PrayerNotificationLogger.logValidationSuccess(
                                "adhan_permission_onboarding",
                                "ALL",
                                "dismiss",
                                "User dismissed Adhan permission onboarding via Maybe Later."
                            )
                            onDismiss()
                        },
                        modifier = Modifier.testTag("adhan_perms_skip_button")
                    ) {
                        Text(
                            text = "Maybe Later",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Adhan Alerts & Permissions",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Enable system permissions for full-screen calls to prayer, background audio playback, and timely alerts.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }

                // 1. Notifications
                item {
                    PermissionCard(
                        title = "Notifications",
                        description = "Required to deliver prayer reminders and sound alerts.",
                        icon = Icons.Filled.Notifications,
                        isGranted = notifsGranted,
                        actionLabel = "Enable",
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                AdhanPermissionHelper.openNotificationSettings(context)
                            }
                        },
                        testTag = "perm_row_notifications"
                    )
                }

                // 2. Exact Alarms
                item {
                    PermissionCard(
                        title = "Exact Alarms",
                        description = "Delivers prayer calls at the exact minute without system delay.",
                        icon = Icons.Filled.Alarm,
                        isGranted = exactAlarmGranted,
                        actionLabel = "Allow",
                        onClick = {
                            AdhanPermissionHelper.openExactAlarmSettings(context)
                        },
                        testTag = "perm_row_exact_alarms"
                    )
                }

                // 3. Display Over Other Apps (MANDATORY)
                item {
                    PermissionCard(
                        title = "Display Over Other Apps",
                        description = "Mandatory for the full-screen Adhan overlay to appear over locked screens or while using other apps.",
                        icon = Icons.Filled.Layers,
                        isGranted = overlayGranted,
                        actionLabel = "Configure",
                        onClick = {
                            AdhanPermissionHelper.openOverlaySettings(context)
                        },
                        testTag = "perm_row_overlay"
                    )
                }

                // 4. Full-Screen Notifications
                item {
                    PermissionCard(
                        title = "Full-Screen Alerts",
                        description = "Wakes the screen and presents the full-screen call to prayer.",
                        icon = Icons.Filled.Fullscreen,
                        isGranted = fullScreenGranted,
                        actionLabel = "Configure",
                        onClick = {
                            AdhanPermissionHelper.openFullScreenIntentSettings(context)
                        },
                        testTag = "perm_row_fullscreen"
                    )
                }

                // 5. Battery Optimization Exemption
                item {
                    PermissionCard(
                        title = "Unrestricted Battery",
                        description = "Exempts FiveLight from aggressive battery savers that suspend background alarms.",
                        icon = Icons.Filled.BatteryAlert,
                        isGranted = batteryOptimizationExempt,
                        actionLabel = "Optimize",
                        onClick = {
                            AdhanPermissionHelper.openBatteryOptimizationSettings(context)
                        },
                        testTag = "perm_row_battery"
                    )
                }

                // 6. Manufacturer Autostart Guidance (if applicable)
                if (AdhanPermissionHelper.hasManufacturerSpecificGuidance()) {
                    item {
                        val manufacturerName = AdhanPermissionHelper.getDetectedManufacturer().replaceFirstChar { it.uppercase() }
                        PermissionCard(
                            title = "$manufacturerName Autostart",
                            description = "Allow background autostart and background activity launch in $manufacturerName system settings.",
                            icon = Icons.Filled.PhoneAndroid,
                            isGranted = false,
                            actionLabel = "Open Settings",
                            onClick = {
                                AdhanPermissionHelper.openManufacturerSettings(context)
                            },
                            testTag = "perm_row_manufacturer"
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Bottom Confirmation / Done Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Button(
                    onClick = {
                        AdhanPermissionHelper.setCompletedOrDismissedOnboarding(context, true)
                        PrayerNotificationLogger.logValidationSuccess(
                            "adhan_permission_onboarding",
                            "ALL",
                            "completed",
                            "User completed Adhan permission onboarding (allRequiredGranted=$allRequiredGranted)."
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("adhan_perms_done_button"),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (allRequiredGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(
                        text = if (allRequiredGranted) "All Permissions Set • Continue" else "Continue to FiveLight",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (allRequiredGranted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.surface
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    actionLabel: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = if (isGranted) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = !isGranted, onClick = onClick)
            .testTag(testTag),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isGranted) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            if (isGranted) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Granted",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onClick,
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = actionLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
