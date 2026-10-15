package com.example.ui.screens

import androidx.compose.ui.res.stringResource
import com.example.R

import com.example.ui.theme.semanticPrimaryAccent
import com.example.ui.theme.semanticAccentForeground
import com.example.ui.theme.semanticSuccess
import com.example.ui.theme.semanticError
import com.example.ui.theme.semanticSurface
import com.example.ui.theme.semanticSurfaceElevated
import com.example.ui.theme.semanticPrimaryText
import com.example.ui.theme.semanticMutedText
import com.example.ui.theme.semanticBorder
import com.example.ui.theme.semanticStrongBorder
import com.example.ui.theme.semanticBackground
import com.example.ui.theme.semanticWarning


import com.example.ui.theme.*

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.PrayerName
import com.example.data.reminder.PrePrayerReminderOffset
import com.example.data.reminder.PrayerAlertMode
import com.example.data.reminder.SmartPrayerNotificationManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartPrayerNotificationsSubScreen(
    onBack: () -> Unit,
    onSettingsChanged: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val manager = remember { SmartPrayerNotificationManager(context) }

    var isSmartEnabled by remember { mutableStateOf(manager.isSmartNotificationsEnabled) }
    var isPrayerTimeEnabled by remember { mutableStateOf(manager.isPrayerTimeNotificationsEnabled) }
    var selectedAlertMode by remember { mutableStateOf(manager.prayerAlertMode) }
    var preReminderOffset by remember { mutableStateOf(manager.preReminderOffset) }
    var isContextualEnabled by remember { mutableStateOf(manager.isContextualRemindersEnabled) }
    var isNaflEnabled by remember { mutableStateOf(manager.isNaflOpportunitiesEnabled) }

    var isTestAzaanPlaying by remember { mutableStateOf(false) }
    var testAzaanPlayer by remember { mutableStateOf<android.media.MediaPlayer?>(null) }

    fun stopTestAzaan() {
        try {
            testAzaanPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.release()
            }
        } catch (_: Exception) {}
        testAzaanPlayer = null
        isTestAzaanPlaying = false
    }

    fun startTestAzaan() {
        if (isTestAzaanPlaying) {
            stopTestAzaan()
            return
        }
        if (!isSmartEnabled) {
            android.widget.Toast.makeText(context, "Master Prayer Notifications switch is disabled", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        try {
            stopTestAzaan()
            val attrs = android.media.AudioAttributes.Builder()
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                .build()
            val mp = android.media.MediaPlayer.create(context, R.raw.azaan, attrs, 0)
            if (mp != null) {
                testAzaanPlayer = mp
                mp.setOnCompletionListener {
                    isTestAzaanPlaying = false
                    try { mp.release() } catch (_: Exception) {}
                    if (testAzaanPlayer === mp) testAzaanPlayer = null
                }
                mp.setOnErrorListener { player, _, _ ->
                    isTestAzaanPlaying = false
                    try { player.release() } catch (_: Exception) {}
                    if (testAzaanPlayer === player) testAzaanPlayer = null
                    android.widget.Toast.makeText(context, "Unable to play Azaan recording", android.widget.Toast.LENGTH_SHORT).show()
                    true
                }
                mp.start()
                isTestAzaanPlaying = true
            } else {
                android.widget.Toast.makeText(context, "Azaan recording file not available", android.widget.Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            isTestAzaanPlaying = false
            android.widget.Toast.makeText(context, "Error starting test Azaan playback", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            stopTestAzaan()
        }
    }

    var isIgnoringBattery by remember { mutableStateOf(manager.isIgnoringBatteryOptimizations()) }
    var isPermissionGranted by remember { mutableStateOf(manager.isNotificationPermissionGranted()) }

    var hasNotificationPermission by remember {
        mutableStateOf(isPermissionGranted)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        isPermissionGranted = isGranted
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                isIgnoringBattery = manager.isIgnoringBatteryOptimizations()
                isPermissionGranted = manager.isNotificationPermissionGranted()
                hasNotificationPermission = isPermissionGranted
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val isDark = isAppInDarkTheme()

    fun updateSettings() {
        manager.isSmartNotificationsEnabled = isSmartEnabled
        manager.isPrayerTimeNotificationsEnabled = isPrayerTimeEnabled
        manager.prayerAlertMode = selectedAlertMode
        manager.preReminderOffset = preReminderOffset
        manager.isContextualRemindersEnabled = isContextualEnabled
        manager.isNaflOpportunitiesEnabled = isNaflEnabled
        if (!isSmartEnabled) {
            stopTestAzaan()
        }
        onSettingsChanged()
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("smart_notif_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    tint = if (isDark) Color(0xFFF2F2EE) else MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.settings_prayer_notifications),
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = SerifHeaderFont),
                color = if (isDark) Color(0xFFF2F2EE) else MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                item {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isDark) Color(0xFF2D1616) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                        ),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFFFF453A).copy(alpha = 0.4f) else MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.notif_permission_required),
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isDark) Color(0xFFFF6B6B) else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.notif_permission_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) Color(0xFFA8A8A2) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFFD32F2F) else MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("grant_notif_permission_button")
                            ) {
                                Text("Enable Notifications", color = if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.onError)
                            }
                        }
                    }
                }
            }

            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (isDark) Color(0xFF242426) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.12f) else MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSmartEnabled) Icons.Outlined.NotificationsActive else Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = if (isDark) (if (isSmartEnabled) Color(0xFFF2F2EE) else Color(0xFFA8A8A2)) else (if (isSmartEnabled) MaterialTheme.colorScheme.primary else LightMutedText),
                            modifier = Modifier.size(28.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.settings_prayer_notifications),
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isDark) Color(0xFFF2F2EE) else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = stringResource(R.string.notif_calm_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) Color(0xFFA8A8A2) else MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Switch(
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = if (isDark) Color(0xFFFFFFFF) else Color.semanticAccentForeground,
                                checkedTrackColor = if (isDark) Color(0xFF4A4556) else Color.semanticPrimaryAccent,
                                checkedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                                uncheckedThumbColor = Color.semanticSecondaryText,
                                uncheckedTrackColor = if (isDark) Color(0xFF2C2C2E) else Color.semanticBorder,
                                uncheckedBorderColor = Color.semanticBorder
                            ),
                            checked = isSmartEnabled,
                            onCheckedChange = { isChecked ->
                                isSmartEnabled = isChecked
                                updateSettings()
                            },
                            modifier = Modifier.testTag("smart_notif_master_switch")
                        )
                    }
                }
            }

            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (isDark) Color(0xFF242426) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                    ),
                    border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.12f) else MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "System Delivery & Battery Reliability",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isDark) Color(0xFFF2F2EE) else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Notifications Permission",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isDark) Color(0xFFF2F2EE) else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isPermissionGranted) "Allowed ✓" else "Disabled in Android Settings",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isPermissionGranted) (if (isDark) Color(0xFF81C784) else MaterialTheme.colorScheme.primary) else (if (isDark) Color(0xFFFF6B6B) else MaterialTheme.colorScheme.error)
                                )
                            }
                            TextButton(
                                onClick = {
                                    manager.openAppNotificationSettings()
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = if (isDark) Color(0xFFA8A8A2) else MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("open_app_notif_settings_button")
                            ) {
                                Text(
                                    text = "App Settings",
                                    color = if (isDark) Color(0xFFA8A8A2) else MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.1f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Background Battery Usage",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isDark) Color(0xFFF2F2EE) else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isIgnoringBattery) "Unrestricted ✓" else "Restricted — notifications may be delayed",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isIgnoringBattery) (if (isDark) Color(0xFF81C784) else MaterialTheme.colorScheme.primary) else (if (isDark) Color(0xFFA8A8A2) else LightMutedText)
                                )
                            }
                            TextButton(
                                onClick = {
                                    manager.openBatteryOptimizationSettings()
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = if (isDark) Color(0xFFA8A8A2) else MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("open_battery_settings_button")
                            ) {
                                Text(
                                    text = "Battery Settings",
                                    color = if (isDark) Color(0xFFA8A8A2) else MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (isDark) Color(0xFF242426) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                    ),
                    border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.12f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Test Notification Delivery",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isDark) Color(0xFFF2F2EE) else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Verify that Android notification channels and alert delivery are working properly on your device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) Color(0xFFA8A8A2) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = {
                                manager.sendTestNotification()
                                android.widget.Toast.makeText(context, "Test notification sent", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isDark) Color(0xFF2C2C2E) else Color.Transparent,
                                contentColor = if (isDark) Color(0xFFF2F2EE) else MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.14f) else MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("send_test_notification_button")
                        ) {
                            Text(
                                text = "Send Test Smart Prayer Notification",
                                color = if (isDark) Color(0xFFF2F2EE) else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            item {
                AnimatedVisibility(
                    visible = isSmartEnabled,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (isDark) Color(0xFF242426) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                            ),
                            border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.12f) else MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Prayer Time Notifications",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isDark) Color(0xFFF2F2EE) else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Notify when Fard prayer times enter",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isDark) Color(0xFFA8A8A2) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    colors = androidx.compose.material3.SwitchDefaults.colors(
                                        checkedThumbColor = if (isDark) Color(0xFFFFFFFF) else Color.semanticAccentForeground,
                                        checkedTrackColor = if (isDark) Color(0xFF4A4556) else Color.semanticPrimaryAccent,
                                        checkedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                                        uncheckedThumbColor = Color.semanticSecondaryText,
                                        uncheckedTrackColor = if (isDark) Color(0xFF2C2C2E) else Color.semanticBorder,
                                        uncheckedBorderColor = Color.semanticBorder
                                    ),
                                    checked = isPrayerTimeEnabled,
                                    onCheckedChange = {
                                        isPrayerTimeEnabled = it
                                        updateSettings()
                                    },
                                    modifier = Modifier.testTag("prayer_time_notif_switch")
                                )
                            }
                        }

                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (isDark) Color(0xFF242426) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                            ),
                            border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.12f) else MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Prayer Alert Mode",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (isDark) Color(0xFFF2F2EE) else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )

                                val modes = PrayerAlertMode.entries
                                modes.forEach { mode ->
                                    val isSelected = selectedAlertMode == mode
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedAlertMode = mode
                                                manager.prayerAlertMode = mode
                                                updateSettings()
                                            }
                                            .testTag(if (mode == PrayerAlertMode.AZAAN_AND_NOTIFICATION) "alert_mode_azaan_and_notification" else "alert_mode_notification_only"),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) {
                                                if (isDark) Color(0xFF323238) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                            } else {
                                                if (isDark) Color(0xFF1C1C1E) else MaterialTheme.colorScheme.surface
                                            }
                                        ),
                                        border = BorderStroke(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) {
                                                if (isDark) Color(0xFF81C784) else MaterialTheme.colorScheme.primary
                                            } else {
                                                if (isDark) Color.White.copy(alpha = 0.08f) else MaterialTheme.colorScheme.outlineVariant
                                            }
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = {
                                                    selectedAlertMode = mode
                                                    manager.prayerAlertMode = mode
                                                    updateSettings()
                                                },
                                                colors = RadioButtonDefaults.colors(
                                                    selectedColor = if (isDark) Color(0xFF81C784) else MaterialTheme.colorScheme.primary,
                                                    unselectedColor = if (isDark) Color(0xFFA8A8A2) else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = mode.title,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (isDark) Color(0xFFF2F2EE) else MaterialTheme.colorScheme.onSurface,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = mode.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (isDark) Color(0xFFA8A8A2) else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (isDark) Color(0xFF242426) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                            ),
                            border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.12f) else MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Test Azaan",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (isDark) Color(0xFFF2F2EE) else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Preview the Azaan recording.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDark) Color(0xFFA8A8A2) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedButton(
                                    onClick = {
                                        if (isTestAzaanPlaying) {
                                            stopTestAzaan()
                                        } else {
                                            startTestAzaan()
                                        }
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isTestAzaanPlaying) {
                                            if (isDark) Color(0xFF422121) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                                        } else {
                                            if (isDark) Color(0xFF2C2C2E) else Color.Transparent
                                        },
                                        contentColor = if (isTestAzaanPlaying) {
                                            if (isDark) Color(0xFFFF8A80) else MaterialTheme.colorScheme.error
                                        } else {
                                            if (isDark) Color(0xFFF2F2EE) else MaterialTheme.colorScheme.primary
                                        }
                                    ),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isTestAzaanPlaying) {
                                            if (isDark) Color(0xFFFF5252) else MaterialTheme.colorScheme.error
                                        } else {
                                            if (isDark) Color.White.copy(alpha = 0.14f) else MaterialTheme.colorScheme.outline
                                        }
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("test_azaan_button")
                                ) {
                                    Icon(
                                        imageVector = if (isTestAzaanPlaying) Icons.Outlined.Stop else Icons.Outlined.VolumeUp,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isTestAzaanPlaying) "Stop Azaan" else "Test Azaan",
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (isDark) Color(0xFF242426) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                            ),
                            border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.12f) else MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "45-Minute Follow-Up Reminder",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isDark) Color(0xFFF2F2EE) else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Sends a gentle follow-up notification (\"Did you get a chance to pray [Prayer]?\") exactly 45 minutes after prayer start time if not yet marked prayed.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDark) Color(0xFFA8A8A2) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (isDark) Color(0xFF242426) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                            ),
                            border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.12f) else MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Contextual Reminders",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isDark) Color(0xFFF2F2EE) else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Calm morning, evening, and night transitions",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isDark) Color(0xFFA8A8A2) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    colors = androidx.compose.material3.SwitchDefaults.colors(
                                        checkedThumbColor = if (isDark) Color(0xFFFFFFFF) else Color.semanticAccentForeground,
                                        checkedTrackColor = if (isDark) Color(0xFF4A4556) else Color.semanticPrimaryAccent,
                                        checkedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                                        uncheckedThumbColor = Color.semanticSecondaryText,
                                        uncheckedTrackColor = if (isDark) Color(0xFF2C2C2E) else Color.semanticBorder,
                                        uncheckedBorderColor = Color.semanticBorder
                                    ),
                                    checked = isContextualEnabled,
                                    onCheckedChange = {
                                        isContextualEnabled = it
                                        updateSettings()
                                    },
                                    modifier = Modifier.testTag("contextual_reminders_switch")
                                )
                            }
                        }

                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (isDark) Color(0xFF242426) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                            ),
                            border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.12f) else MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Nafl Opportunities",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isDark) Color(0xFFF2F2EE) else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Gentle windows for voluntary prayers",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isDark) Color(0xFFA8A8A2) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    colors = androidx.compose.material3.SwitchDefaults.colors(
                                        checkedThumbColor = if (isDark) Color(0xFFFFFFFF) else Color.semanticAccentForeground,
                                        checkedTrackColor = if (isDark) Color(0xFF4A4556) else Color.semanticPrimaryAccent,
                                        checkedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                                        uncheckedThumbColor = Color.semanticSecondaryText,
                                        uncheckedTrackColor = if (isDark) Color(0xFF2C2C2E) else Color.semanticBorder,
                                        uncheckedBorderColor = Color.semanticBorder
                                    ),
                                    checked = isNaflEnabled,
                                    onCheckedChange = {
                                        isNaflEnabled = it
                                        updateSettings()
                                    },
                                    modifier = Modifier.testTag("nafl_opportunities_switch")
                                )
                            }
                        }

                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (isDark) Color(0xFF242426) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                            ),
                            border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.12f) else MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Fard Prayers",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isDark) Color(0xFFF2F2EE) else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )

                                val fardPrayers = listOf(
                                    PrayerName.FAJR,
                                    PrayerName.DHUHR,
                                    PrayerName.ASR,
                                    PrayerName.MAGHRIB,
                                    PrayerName.ISHA
                                )

                                fardPrayers.forEach { prayer ->
                                    var enabled by remember { mutableStateOf(manager.isPrayerEnabled(prayer.id)) }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = prayer.displayName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isDark) Color(0xFFF2F2EE) else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Switch(
                                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                                checkedThumbColor = if (isDark) Color(0xFFFFFFFF) else Color.semanticAccentForeground,
                                                checkedTrackColor = if (isDark) Color(0xFF4A4556) else Color.semanticPrimaryAccent,
                                                checkedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                                                uncheckedThumbColor = Color.semanticSecondaryText,
                                                uncheckedTrackColor = if (isDark) Color(0xFF2C2C2E) else Color.semanticBorder,
                                                uncheckedBorderColor = Color.semanticBorder
                                            ),
                                            checked = enabled,
                                            onCheckedChange = { isChecked ->
                                                enabled = isChecked
                                                manager.setPrayerEnabled(prayer.id, isChecked)
                                                onSettingsChanged()
                                            },
                                            modifier = Modifier.testTag("fard_switch_${prayer.id}")
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
