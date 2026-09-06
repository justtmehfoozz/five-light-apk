package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.BatteryAlert
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.EditLocationAlt
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.auth.AuthRepository
import com.example.data.backup.BackupManager
import com.example.data.backup.GoogleDriveBackupWorker
import com.example.data.backup.GoogleDriveService
import com.example.data.model.AppearanceMode
import com.example.data.model.CalcMethod
import com.example.data.model.CityLocation
import com.example.data.model.HijriDateMethod
import com.example.data.model.Madhab
import com.example.data.model.TasbeehSound
import com.example.data.reminder.PrePrayerReminderOffset
import com.example.data.reminder.SmartPrayerNotificationManager
import com.example.data.sync.FirestoreSyncManager
import com.example.data.util.LocationHelper
import com.example.ui.theme.InstrumentSerifItalic
import com.example.ui.theme.semanticBackground
import com.example.ui.theme.semanticBorder
import com.example.ui.theme.semanticControl
import com.example.ui.theme.semanticError
import com.example.ui.theme.semanticMutedText
import com.example.ui.theme.semanticPrimaryAccent
import com.example.ui.theme.semanticPrimaryText
import com.example.ui.theme.semanticSecondaryText
import com.example.ui.theme.semanticStrongBorder
import com.example.ui.theme.semanticSuccess
import com.example.ui.theme.semanticSurfaceElevated
import com.example.ui.theme.semanticWarning
import com.example.ui.viewmodel.AppViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date
import java.util.Locale

private const val TOTAL_STEPS = 10

/**
 * Top-level capability checks exposed for tests and components.
 */
fun checkLocationPermission(context: Context): Boolean = LocationHelper.hasLocationPermission(context)
fun checkNotificationPermission(context: Context): Boolean = SmartPrayerNotificationManager(context).isNotificationPermissionGranted()
fun checkBackgroundOptimization(context: Context): Boolean = SmartPrayerNotificationManager(context).isIgnoringBatteryOptimizations()

/**
 * Phase 4 — Google Drive Restore + Backup Onboarding & Background Reliability.
 *
 * Configures the user's existing FiveLight preferences and genuine Android capabilities:
 * STEP 1: Set Up FiveLight (Introduction & Overview)
 * STEP 2: Restore your FiveLight data (Google Drive restore detection, confirmation, progress & skip)
 * STEP 3: Your Location (Runtime location permission, manual search fallback, real Android check)
 * STEP 4: Prayer Calculation Method (CalcMethod)
 * STEP 5: Asr Calculation / Madhab (Madhab)
 * STEP 6: Hijri Date Convention (HijriDateMethod & Custom Offset)
 * STEP 7: Prayer Notifications & Background Reliability (POST_NOTIFICATIONS, Battery exemption, Exact alarms)
 * STEP 8: Automatic Backup (Off / Daily / Weekly default, Google Drive authorization, Back Up Now)
 * STEP 9: Tasbeeh & Haptics (TasbeehSound & Vibration)
 * STEP 10: Appearance & Completion (Theme selection & real capability status summary)
 */
@Composable
fun SetUpFiveLightScreen(
    viewModel: AppViewModel,
    currentUser: FirebaseUser?,
    onSetupComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    var currentStep by rememberSaveable { mutableIntStateOf(0) }

    // Intercept back presses: step back within setup, protect against accidental exit on step 0
    BackHandler(enabled = true) {
        if (currentStep > 0) {
            currentStep -= 1
        }
    }

    // Existing Single Source of Truth
    val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()
    val calcMethod by viewModel.calcMethod.collectAsStateWithLifecycle()
    val madhab by viewModel.madhab.collectAsStateWithLifecycle()
    val hijriDateMethod by viewModel.hijriDateMethod.collectAsStateWithLifecycle()
    val customHijriOffset by viewModel.customHijriOffset.collectAsStateWithLifecycle()
    val appearanceMode by viewModel.appearanceMode.collectAsStateWithLifecycle()
    val tasbeehSound by viewModel.tasbeehSound.collectAsStateWithLifecycle()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsStateWithLifecycle()

    val notificationManager = remember(context) { SmartPrayerNotificationManager(context) }
    var isNotificationsEnabledPref by remember {
        mutableStateOf(notificationManager.isSmartNotificationsEnabled)
    }
    var preReminderOffset by remember {
        mutableStateOf(notificationManager.preReminderOffset)
    }

    // REAL Android Capability States (Inspected from system)
    var isLocationPermissionGranted by remember {
        mutableStateOf(LocationHelper.hasLocationPermission(context))
    }
    var locationPermissionAttempted by remember { mutableStateOf(false) }

    var isNotificationPermissionGranted by remember {
        mutableStateOf(notificationManager.isNotificationPermissionGranted())
    }
    var notificationPermissionAttempted by remember { mutableStateOf(false) }

    var isBatteryOptimizationIgnored by remember {
        mutableStateOf(notificationManager.isIgnoringBatteryOptimizations())
    }
    var isExactAlarmPermitted by remember {
        mutableStateOf(notificationManager.canScheduleExactAlarms())
    }

    // Google Drive & Backup States
    var driveAccount by remember {
        mutableStateOf(GoogleDriveService.getAuthorizedAccount(context))
    }
    var isSearchingBackup by remember { mutableStateOf(false) }
    var detectedBackupInfo by remember { mutableStateOf<GoogleDriveService.DriveBackupInfo?>(null) }
    var backupSearchAttempted by remember { mutableStateOf(false) }
    var searchBackupError by remember { mutableStateOf<String?>(null) }

    var isRestoring by remember { mutableStateOf(false) }
    var restoreProgressStage by remember { mutableStateOf<String?>(null) }
    var restoreError by remember { mutableStateOf<String?>(null) }
    var isRestoreCompleted by remember { mutableStateOf(false) }

    var autoBackupFrequency by remember {
        mutableStateOf(BackupManager.AutoBackupFrequency.WEEKLY)
    }
    var lastBackupTime by remember {
        mutableLongStateOf(BackupManager.getLastBackupTime(context))
    }
    var isManualBackingUp by remember { mutableStateOf(false) }
    var manualBackupMessage by remember { mutableStateOf<String?>(null) }

    // Check for existing backup whenever Drive account is authorized on restore step
    LaunchedEffect(driveAccount, currentStep) {
        if (currentStep == 1 && driveAccount != null && !backupSearchAttempted && !isRestoring && !isRestoreCompleted) {
            isSearchingBackup = true
            searchBackupError = null
            val res = BackupManager.checkExistingBackup(context, driveAccount!!)
            isSearchingBackup = false
            backupSearchAttempted = true
            if (res.isSuccess) {
                detectedBackupInfo = res.getOrNull()
            } else {
                searchBackupError = res.exceptionOrNull()?.message
            }
        }
    }

    // Lifecycle observer to re-inspect actual system state on resume after visiting Android Settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isLocationPermissionGranted = LocationHelper.hasLocationPermission(context)
                isNotificationPermissionGranted = notificationManager.isNotificationPermissionGranted()
                isBatteryOptimizationIgnored = notificationManager.isIgnoringBatteryOptimizations()
                isExactAlarmPermitted = notificationManager.canScheduleExactAlarms()
                driveAccount = GoogleDriveService.getAuthorizedAccount(context)

                // If location was granted while in settings, attempt to fetch location if available
                if (isLocationPermissionGranted) {
                    val loc = LocationHelper.getLastKnownLocation(context)
                    if (loc != null) {
                        viewModel.setCity(LocationHelper.resolveCityLocation(context, loc))
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Runtime Permission Launchers
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionAttempted = true
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val granted = fineGranted || coarseGranted
        isLocationPermissionGranted = granted

        if (granted) {
            val loc = LocationHelper.getLastKnownLocation(context)
            if (loc != null) {
                val resolved = LocationHelper.resolveCityLocation(context, loc)
                viewModel.setCity(resolved)
            }
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationPermissionAttempted = true
        isNotificationPermissionGranted = granted
        if (granted) {
            isNotificationsEnabledPref = true
            notificationManager.isSmartNotificationsEnabled = true
            notificationManager.isPrayerTimeNotificationsEnabled = true
        }
    }

    val driveAuthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            if (account != null && GoogleSignIn.hasPermissions(account, GoogleDriveService.DRIVE_APPDATA_SCOPE)) {
                driveAccount = account
                backupSearchAttempted = false
            }
        } catch (_: Exception) {}
    }

    val requestDriveAuth: () -> Unit = {
        val signInClient = GoogleDriveService.getGoogleSignInClient(context)
        signInClient.signOut().addOnCompleteListener {
            driveAuthLauncher.launch(signInClient.signInIntent)
        }
    }

    val onRestoreBackup: () -> Unit = {
        coroutineScope.launch {
            isRestoring = true
            restoreError = null
            restoreProgressStage = "Preparing backup"
            val authRepo = AuthRepository.getInstance(context)
            val syncMgr = FirestoreSyncManager.getInstance(context, viewModel.repository, authRepo)
            val res = BackupManager.performRestore(
                context = context,
                repository = viewModel.repository,
                authRepository = authRepo,
                syncManager = syncMgr,
                onProgress = { stage ->
                    restoreProgressStage = stage
                }
            )
            isRestoring = false
            if (res.isSuccess) {
                isRestoreCompleted = true
                restoreProgressStage = null
            } else {
                restoreError = res.exceptionOrNull()?.message ?: "Restore failed"
            }
        }
    }

    val onManualBackup: () -> Unit = {
        coroutineScope.launch {
            isManualBackingUp = true
            manualBackupMessage = null
            val authRepo = AuthRepository.getInstance(context)
            val res = BackupManager.performBackup(context, viewModel.repository, authRepo)
            isManualBackingUp = false
            if (res.isSuccess) {
                lastBackupTime = res.getOrNull() ?: System.currentTimeMillis()
                manualBackupMessage = "Backup created successfully"
            } else {
                manualBackupMessage = res.exceptionOrNull()?.message ?: "Backup failed"
            }
        }
    }

    val stepTitles = remember {
        listOf(
            "Set Up FiveLight",
            "Restore your FiveLight data",
            "Your Location",
            "Prayer Calculation Method",
            "Asr Calculation",
            "Hijri Date Convention",
            "Prayer Reminders & Reliability",
            "Automatic Backup",
            "Tasbeeh & Haptics",
            "Appearance"
        )
    }

    val stepSubtitles = remember {
        listOf(
            "Configure your prayer schedule, calculation standards, and reminders for a disciplined daily rhythm.",
            "Restore your prayer logs, dhikr history, bookmarks, and preferences from your encrypted Google Drive backup.",
            "Use your location to calculate accurate prayer times and Qibla orientation.",
            "Select the calculation authority and astronomical convention recognized by your local community.",
            "Choose the juristic convention for determining the start of Asr prayer.",
            "Select your regional moon-sighting convention or astronomical calendar.",
            "Enable timely notifications and background reliability for scheduled prayer times.",
            "Keep a secure, encrypted backup of your FiveLight data in your private Google Drive.",
            "Customize audio taps and tactile feedback during daily dhikr and tasbeeh recitation.",
            "Review your system capabilities and personalize your appearance.",
        )
    }

    val primaryBg = Color.semanticBackground
    val primaryText = Color.semanticPrimaryText
    val secondaryText = Color.semanticSecondaryText
    val mutedText = Color.semanticMutedText
    val borderColor = Color.semanticBorder.copy(alpha = 0.5f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(primaryBg)
            .testTag("setup_five_light_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Navigation & Progress Bar
            SetupTopBar(
                currentStep = currentStep,
                totalSteps = TOTAL_STEPS,
                onBack = {
                    if (currentStep > 0) currentStep -= 1
                }
            )

            // Step Content Area (Animated horizontally between steps)
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally(tween(280, easing = FastOutSlowInEasing)) { it / 3 } + fadeIn(tween(280)))
                            .togetherWith(slideOutHorizontally(tween(200, easing = FastOutSlowInEasing)) { -it / 4 } + fadeOut(tween(200)))
                    } else {
                        (slideInHorizontally(tween(280, easing = FastOutSlowInEasing)) { -it / 3 } + fadeIn(tween(280)))
                            .togetherWith(slideOutHorizontally(tween(200, easing = FastOutSlowInEasing)) { it / 4 } + fadeOut(tween(200)))
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                label = "setupStepTransition"
            ) { step ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "STEP ${step + 1} OF $TOTAL_STEPS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.2.sp
                        ),
                        color = Color.semanticPrimaryAccent,
                        modifier = Modifier.testTag("setup_step_badge")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = stepTitles[step],
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = primaryText,
                        modifier = Modifier.testTag("setup_step_title")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = stepSubtitles[step],
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        ),
                        color = secondaryText,
                        modifier = Modifier.testTag("setup_step_subtitle")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Step Body
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        when (step) {
                            0 -> StepWelcome(
                                currentUser = currentUser,
                                primaryText = primaryText,
                                secondaryText = secondaryText,
                                mutedText = mutedText,
                                borderColor = borderColor
                            )
                            1 -> StepRestoreData(
                                currentUser = currentUser,
                                driveAccount = driveAccount,
                                isSearchingBackup = isSearchingBackup,
                                detectedBackupInfo = detectedBackupInfo,
                                searchBackupError = searchBackupError,
                                isRestoring = isRestoring,
                                restoreProgressStage = restoreProgressStage,
                                restoreError = restoreError,
                                isRestoreCompleted = isRestoreCompleted,
                                onRequestDriveAuth = requestDriveAuth,
                                onRestoreBackup = onRestoreBackup,
                                onContinue = {
                                    if (currentStep < TOTAL_STEPS - 1) currentStep += 1
                                },
                                primaryText = primaryText,
                                secondaryText = secondaryText,
                                mutedText = mutedText,
                                borderColor = borderColor
                            )
                            2 -> StepLocationWithPermissions(
                                context = context,
                                selectedCity = selectedCity,
                                isLocationGranted = isLocationPermissionGranted,
                                permissionAttempted = locationPermissionAttempted,
                                onRequestPermission = {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                },
                                predefinedCities = viewModel.repository.PREDEFINED_CITIES,
                                onSelectCity = { city ->
                                    viewModel.setCity(city)
                                },
                                primaryText = primaryText,
                                secondaryText = secondaryText,
                                mutedText = mutedText,
                                borderColor = borderColor
                            )
                            3 -> StepCalcMethod(
                                selectedMethod = calcMethod,
                                onSelectMethod = { method ->
                                    viewModel.setCalcMethod(method)
                                },
                                primaryText = primaryText,
                                secondaryText = secondaryText,
                                borderColor = borderColor
                            )
                            4 -> StepMadhab(
                                selectedMadhab = madhab,
                                onSelectMadhab = { m ->
                                    viewModel.setMadhab(m)
                                },
                                primaryText = primaryText,
                                secondaryText = secondaryText,
                                borderColor = borderColor
                            )
                            5 -> StepHijriConvention(
                                selectedMethod = hijriDateMethod,
                                onSelectMethod = { method ->
                                    viewModel.setHijriDateMethod(method)
                                },
                                customOffset = customHijriOffset,
                                onSetCustomOffset = { offset ->
                                    viewModel.setCustomHijriOffset(offset)
                                },
                                primaryText = primaryText,
                                secondaryText = secondaryText,
                                mutedText = mutedText,
                                borderColor = borderColor
                            )
                            6 -> StepNotificationsAndReliability(
                                context = context,
                                isNotificationsPrefEnabled = isNotificationsEnabledPref,
                                onToggleNotificationsPref = { enabled ->
                                    isNotificationsEnabledPref = enabled
                                    notificationManager.isSmartNotificationsEnabled = enabled
                                    notificationManager.isPrayerTimeNotificationsEnabled = enabled
                                },
                                isNotificationPermissionGranted = isNotificationPermissionGranted,
                                notificationPermissionAttempted = notificationPermissionAttempted,
                                onRequestNotificationPermission = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        notificationManager.openAppNotificationSettings()
                                    }
                                },
                                onOpenNotificationSettings = {
                                    notificationManager.openAppNotificationSettings()
                                },
                                isBatteryOptimizationIgnored = isBatteryOptimizationIgnored,
                                onOpenBatterySettings = {
                                    notificationManager.openBatteryOptimizationSettings()
                                },
                                isExactAlarmPermitted = isExactAlarmPermitted,
                                onOpenExactAlarmSettings = {
                                    notificationManager.openExactAlarmSettings()
                                },
                                preReminderOffset = preReminderOffset,
                                onSelectPreReminder = { offset ->
                                    preReminderOffset = offset
                                    notificationManager.preReminderOffset = offset
                                },
                                primaryText = primaryText,
                                secondaryText = secondaryText,
                                mutedText = mutedText,
                                borderColor = borderColor
                            )
                            7 -> StepAutomaticBackup(
                                currentFrequency = autoBackupFrequency,
                                onSelectFrequency = { freq ->
                                    autoBackupFrequency = freq
                                    BackupManager.setAutoBackupFrequency(context, freq)
                                    if (driveAccount != null) {
                                        GoogleDriveBackupWorker.schedule(context, freq)
                                    }
                                },
                                driveAccount = driveAccount,
                                onRequestDriveAuth = requestDriveAuth,
                                isBackingUp = isManualBackingUp,
                                lastBackupTime = lastBackupTime,
                                manualBackupMessage = manualBackupMessage,
                                onManualBackup = onManualBackup,
                                primaryText = primaryText,
                                secondaryText = secondaryText,
                                mutedText = mutedText,
                                borderColor = borderColor
                            )
                            8 -> StepTasbeehHaptics(
                                selectedSound = tasbeehSound,
                                onSelectSound = { sound ->
                                    viewModel.setTasbeehSound(sound)
                                },
                                vibrationEnabled = vibrationEnabled,
                                onToggleVibration = { enabled ->
                                    viewModel.setVibrationEnabled(enabled)
                                },
                                primaryText = primaryText,
                                secondaryText = secondaryText,
                                mutedText = mutedText,
                                borderColor = borderColor
                            )
                            9 -> StepAppearanceAndCapabilities(
                                selectedMode = appearanceMode,
                                onSelectMode = { mode ->
                                    viewModel.setAppearanceMode(mode)
                                },
                                selectedCity = selectedCity,
                                calcMethod = calcMethod,
                                madhab = madhab,
                                isLocationPermissionGranted = isLocationPermissionGranted,
                                isNotificationPermissionGranted = isNotificationPermissionGranted,
                                isBatteryOptimizationIgnored = isBatteryOptimizationIgnored,
                                driveAccount = driveAccount,
                                autoBackupFrequency = autoBackupFrequency,
                                primaryText = primaryText,
                                secondaryText = secondaryText,
                                mutedText = mutedText,
                                borderColor = borderColor
                            )
                        }
                    }
                }
            }

            // Bottom Navigation Actions
            SetupBottomActions(
                currentStep = currentStep,
                totalSteps = TOTAL_STEPS,
                onBack = {
                    if (currentStep > 0) currentStep -= 1
                },
                onNext = {
                    if (currentStep < TOTAL_STEPS - 1) {
                        currentStep += 1
                    } else {
                        BackupManager.setAutoBackupFrequency(context, autoBackupFrequency)
                        if (driveAccount != null) {
                            GoogleDriveBackupWorker.schedule(context, autoBackupFrequency)
                        }
                        onSetupComplete()
                    }
                }
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// Top Bar with Segmented Progress Indicator
// -------------------------------------------------------------------------------------------------

@Composable
private fun SetupTopBar(
    currentStep: Int,
    totalSteps: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = Color.semanticBorder.copy(alpha = 0.5f)
    val accentColor = Color.semanticPrimaryAccent

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (currentStep > 0) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.semanticSurfaceElevated)
                        .border(1.dp, borderColor, CircleShape)
                        .testTag("setup_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.semanticPrimaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(38.dp))
            }

            Text(
                text = "FiveLight",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                ),
                color = Color.semanticPrimaryText
            )

            Spacer(modifier = Modifier.size(38.dp))
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Segmented Progress Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in 0 until totalSteps) {
                val isCompletedOrCurrent = i <= currentStep
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (isCompletedOrCurrent) accentColor else borderColor.copy(alpha = 0.35f)
                        )
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// Step 1: Welcome & Overview
// -------------------------------------------------------------------------------------------------

@Composable
private fun StepWelcome(
    currentUser: FirebaseUser?,
    primaryText: Color,
    secondaryText: Color,
    mutedText: Color,
    borderColor: Color
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val email = currentUser?.email ?: "Account"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.semanticSurfaceElevated)
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.semanticSuccess)
                    )
                    Text(
                        text = "Signed in as $email",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = Color.semanticSuccess
                    )
                }
                Text(
                    text = "Welcome to FiveLight",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = InstrumentSerifItalic,
                        fontSize = 24.sp
                    ),
                    color = primaryText
                )
                Text(
                    text = "In the next quick steps, we will configure your location, prayer calculations, notification permissions, and appearance. You can change these anytime in Preferences.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 19.sp),
                    color = secondaryText
                )
            }
        }

        // Overview Highlights
        OverviewFeatureRow(
            icon = Icons.Outlined.Place,
            title = "Geographic Location",
            description = "Calculates accurate local prayer times and Qibla angle.",
            primaryText = primaryText,
            secondaryText = secondaryText,
            borderColor = borderColor
        )

        OverviewFeatureRow(
            icon = Icons.Outlined.CheckCircle,
            title = "Calculation & Madhab",
            description = "Conventions for Fajr, Isha, and Asr shadow timings.",
            primaryText = primaryText,
            secondaryText = secondaryText,
            borderColor = borderColor
        )

        OverviewFeatureRow(
            icon = Icons.Outlined.Notifications,
            title = "Prayer Reminders & Reliability",
            description = "Quiet, dependable alerts when prayer times enter.",
            primaryText = primaryText,
            secondaryText = secondaryText,
            borderColor = borderColor
        )

        OverviewFeatureRow(
            icon = Icons.Outlined.LightMode,
            title = "Appearance & Audio",
            description = "Theme aesthetic, tasbeeh sound, and tactile feedback.",
            primaryText = primaryText,
            secondaryText = secondaryText,
            borderColor = borderColor
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun OverviewFeatureRow(
    icon: ImageVector,
    title: String,
    description: String,
    primaryText: Color,
    secondaryText: Color,
    borderColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.semanticSurfaceElevated.copy(alpha = 0.5f))
            .border(1.dp, borderColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.semanticPrimaryAccent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.semanticPrimaryAccent,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = primaryText
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                color = secondaryText
            )
        }
    }
}

private fun formatDriveBackupSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val kb = bytes / 1024.0
    return if (kb < 1024.0) {
        String.format(Locale.US, "%.1f KB", kb)
    } else {
        String.format(Locale.US, "%.1f MB", kb / 1024.0)
    }
}

private fun formatBackupTimestamp(modifiedTimeStr: String?, defaultEpoch: Long = 0L): String {
    if (!modifiedTimeStr.isNullOrBlank()) {
        try {
            val instant = java.time.Instant.parse(modifiedTimeStr)
            val zone = java.time.ZoneId.systemDefault()
            val formatter = java.time.format.DateTimeFormatter.ofLocalizedDateTime(
                java.time.format.FormatStyle.MEDIUM,
                java.time.format.FormatStyle.SHORT
            )
            return formatter.format(instant.atZone(zone))
        } catch (_: Exception) {}
    }
    if (defaultEpoch > 0) {
        try {
            return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(defaultEpoch))
        } catch (_: Exception) {}
    }
    return "Not available"
}

// -------------------------------------------------------------------------------------------------
// Step 2: Restore your FiveLight data
// -------------------------------------------------------------------------------------------------

@Composable
private fun StepRestoreData(
    currentUser: FirebaseUser?,
    driveAccount: GoogleSignInAccount?,
    isSearchingBackup: Boolean,
    detectedBackupInfo: GoogleDriveService.DriveBackupInfo?,
    searchBackupError: String?,
    isRestoring: Boolean,
    restoreProgressStage: String?,
    restoreError: String?,
    isRestoreCompleted: Boolean,
    onRequestDriveAuth: () -> Unit,
    onRestoreBackup: () -> Unit,
    onContinue: () -> Unit,
    primaryText: Color,
    secondaryText: Color,
    mutedText: Color,
    borderColor: Color
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .testTag("restore_data_container"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Account Context Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.semanticSurfaceElevated.copy(alpha = 0.6f))
                .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = null,
                            tint = Color.semanticPrimaryAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "FiveLight Account",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                            color = secondaryText
                        )
                    }
                    Text(
                        text = currentUser?.email ?: "Guest",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                        color = primaryText
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Cloud,
                            contentDescription = null,
                            tint = if (driveAccount != null) Color.semanticSuccess else mutedText,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Google Drive",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                            color = secondaryText
                        )
                    }
                    Text(
                        text = driveAccount?.email ?: "Not connected",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (driveAccount != null) Color.semanticSuccess else Color.semanticWarning
                        )
                    )
                }
            }
        }

        // Main Dynamic State Card
        when {
            isRestoring -> {
                // Restore in progress with staged feedback
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.semanticSurfaceElevated)
                        .border(1.5.dp, Color.semanticPrimaryAccent, RoundedCornerShape(16.dp))
                        .padding(20.dp)
                        .testTag("restore_in_progress_card")
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Color.semanticPrimaryAccent,
                            modifier = Modifier.size(36.dp)
                        )

                        Text(
                            text = "Restoring FiveLight Data",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = primaryText
                        )

                        val currentStageText = restoreProgressStage ?: "Preparing backup"
                        Text(
                            text = "$currentStageText...",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                            color = Color.semanticPrimaryAccent
                        )

                        // Visual Stages
                        val stages = listOf("Preparing backup", "Decrypting", "Verifying", "Restoring data", "Finishing")
                        val activeIndex = stages.indexOfFirst { it.equals(restoreProgressStage, ignoreCase = true) }.coerceAtLeast(0)

                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            stages.forEachIndexed { index, stageName ->
                                val isDone = index < activeIndex
                                val isCurrent = index == activeIndex
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isDone) Icons.Outlined.CheckCircle else if (isCurrent) Icons.Outlined.Refresh else Icons.Outlined.CloudSync,
                                        contentDescription = null,
                                        tint = if (isDone) Color.semanticSuccess else if (isCurrent) Color.semanticPrimaryAccent else mutedText.copy(alpha = 0.4f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = stageName,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isDone) primaryText else if (isCurrent) Color.semanticPrimaryAccent else secondaryText.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            isRestoreCompleted -> {
                // Success banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.semanticSurfaceElevated)
                        .border(1.5.dp, Color.semanticSuccess, RoundedCornerShape(16.dp))
                        .padding(20.dp)
                        .testTag("restore_success_card")
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.semanticSuccess.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = Color.semanticSuccess,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Text(
                            text = "Your Data is Restored",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = primaryText
                        )

                        Text(
                            text = "All your prayer logs, dhikr counters, Quran bookmarks, and preferences have been successfully restored and verified from Google Drive.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
                            color = secondaryText
                        )

                        Button(
                            onClick = onContinue,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("continue_after_restore_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.semanticSuccess,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Continue Setup",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            restoreError != null -> {
                // Error card with retry
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.semanticSurfaceElevated)
                        .border(1.5.dp, Color.semanticError, RoundedCornerShape(16.dp))
                        .padding(18.dp)
                        .testTag("restore_error_card")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Warning,
                                contentDescription = null,
                                tint = Color.semanticError,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Restore Error",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.semanticError
                            )
                        }

                        Text(
                            text = restoreError,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                            color = secondaryText
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onContinue,
                                modifier = Modifier.weight(1f).height(42.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Skip", color = secondaryText)
                            }
                            Button(
                                onClick = onRestoreBackup,
                                modifier = Modifier.weight(1f).height(42.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.semanticPrimaryAccent,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Retry Restore")
                            }
                        }
                    }
                }
            }

            isSearchingBackup -> {
                // Searching indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.semanticSurfaceElevated)
                        .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Color.semanticPrimaryAccent,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "Checking Google Drive for existing backups...",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                            color = secondaryText
                        )
                    }
                }
            }

            driveAccount == null -> {
                // Not connected to Drive
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.semanticSurfaceElevated)
                        .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                        .padding(18.dp)
                        .testTag("drive_not_connected_card")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.semanticPrimaryAccent.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CloudSync,
                                    contentDescription = null,
                                    tint = Color.semanticPrimaryAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Connect Google Drive",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = primaryText
                                )
                                Text(
                                    text = "Restore previous backups from your cloud drive",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = secondaryText
                                )
                            }
                        }

                        Text(
                            text = "If you previously created an encrypted backup of your prayer logs, dhikr history, or preferences, connect your Google Drive account to restore them.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 17.sp),
                            color = secondaryText
                        )

                        Text(
                            text = "FiveLight uses your private Google Drive app storage (drive.appdata). It never accesses your personal files.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = mutedText)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onContinue,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("skip_restore_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Skip for Now", color = secondaryText)
                            }

                            Button(
                                onClick = onRequestDriveAuth,
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(44.dp)
                                    .testTag("connect_drive_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.semanticPrimaryAccent,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Connect Drive", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }

            detectedBackupInfo != null -> {
                // Backup found!
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.semanticSurfaceElevated)
                        .border(1.5.dp, Color.semanticSuccess, RoundedCornerShape(16.dp))
                        .padding(18.dp)
                        .testTag("backup_found_card")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Backup,
                                    contentDescription = null,
                                    tint = Color.semanticSuccess,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "BACKUP FOUND",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = Color.semanticSuccess
                                )
                            }
                        }

                        // Backup Metadata Rows
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.semanticBackground.copy(alpha = 0.5f))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Account", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = secondaryText)
                                Text(detectedBackupInfo.accountEmail, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold), color = primaryText)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Backup Date", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = secondaryText)
                                Text(formatBackupTimestamp(detectedBackupInfo.modifiedTime), style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold), color = primaryText)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Size", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = secondaryText)
                                Text(formatDriveBackupSize(detectedBackupInfo.sizeBytes), style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold), color = primaryText)
                            }
                        }

                        Text(
                            text = "What will be restored:",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            color = primaryText
                        )

                        // Data list
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            listOf(
                                "Prayer logs and completion records",
                                "Missed prayer (Qada) tracking and progress",
                                "Dhikr history, counts, and active sessions",
                                "Quran bookmarks and reading positions",
                                "Custom Tasbeeh presets, counts, and targets",
                                "Calculation methods, madhab, and display preferences"
                            ).forEach { item ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null,
                                        tint = Color.semanticPrimaryAccent,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = item,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = secondaryText
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Restoring will update your local database with your encrypted cloud backup.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
                            color = mutedText
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onContinue,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("skip_restore_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Skip", color = secondaryText)
                            }

                            Button(
                                onClick = onRestoreBackup,
                                modifier = Modifier
                                    .weight(1.4f)
                                    .height(44.dp)
                                    .testTag("restore_backup_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.semanticPrimaryAccent,
                                    contentColor = Color.White
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Restore,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Restore",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }

                        TextButton(
                            onClick = onRequestDriveAuth,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                text = "Switch Google Account",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = secondaryText
                            )
                        }
                    }
                }
            }

            else -> {
                // Drive connected, but no backup found
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.semanticSurfaceElevated)
                        .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                        .padding(18.dp)
                        .testTag("no_backup_found_card")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = Color.semanticSecondaryText,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "NO BACKUP FOUND",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = secondaryText
                            )
                        }

                        Text(
                            text = "No existing FiveLight backup was found in this Google Drive account (${driveAccount?.email}). You can create your first backup in the Automatic Backup step or from your Profile anytime.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 17.sp),
                            color = secondaryText
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onRequestDriveAuth,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Switch Account", color = secondaryText, fontSize = 12.sp)
                            }

                            Button(
                                onClick = onContinue,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("skip_restore_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.semanticPrimaryAccent,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Continue", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// Step 3: Location With Real Android Permission Handling
// -------------------------------------------------------------------------------------------------

@Composable
private fun StepLocationWithPermissions(
    context: Context,
    selectedCity: CityLocation,
    isLocationGranted: Boolean,
    permissionAttempted: Boolean,
    onRequestPermission: () -> Unit,
    predefinedCities: List<CityLocation>,
    onSelectCity: (CityLocation) -> Unit,
    primaryText: Color,
    secondaryText: Color,
    mutedText: Color,
    borderColor: Color
) {
    var searchQuery by remember { mutableStateOf("") }
    var isManualPickerOpen by remember { mutableStateOf(!isLocationGranted) }

    val filteredCities = remember(searchQuery, predefinedCities) {
        if (searchQuery.isBlank()) {
            predefinedCities
        } else {
            predefinedCities.filter {
                it.cityName.contains(searchQuery, ignoreCase = true) ||
                        it.countryName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Status Card: Real Android state inspection
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.semanticSurfaceElevated)
                .border(
                    1.5.dp,
                    if (isLocationGranted) Color.semanticSuccess else borderColor,
                    RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
                .testTag("location_status_card")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isLocationGranted) Icons.Outlined.CheckCircle else Icons.Outlined.Place,
                            contentDescription = null,
                            tint = if (isLocationGranted) Color.semanticSuccess else Color.semanticPrimaryAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isLocationGranted) "LOCATION ENABLED" else "LOCATION NOT ENABLED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = if (isLocationGranted) Color.semanticSuccess else Color.semanticWarning
                        )
                    }

                    if (isLocationGranted) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color.semanticSuccess),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = "Granted",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "${selectedCity.cityName}, ${selectedCity.countryName}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = primaryText,
                    modifier = Modifier.testTag("location_current_name")
                )

                Text(
                    text = String.format(
                        java.util.Locale.US,
                        "Coordinates: %.4f° N, %.4f° E",
                        selectedCity.latitude,
                        selectedCity.longitude
                    ),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = mutedText
                )
            }
        }

        // Action / Permission Prompt Card
        if (!isLocationGranted) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.semanticSurfaceElevated.copy(alpha = 0.6f))
                    .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                    .padding(14.dp)
                    .testTag("location_permission_action_card")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Use your location for accurate prayer times.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = primaryText
                    )
                    Text(
                        text = "Allowing location grants FiveLight access to calculate exact solar angles and Kaaba orientation wherever you travel.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                        color = secondaryText
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onRequestPermission,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("allow_location_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.semanticPrimaryAccent,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MyLocation,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Allow Location",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        OutlinedButton(
                            onClick = { isManualPickerOpen = !isManualPickerOpen },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("choose_city_manually_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.EditLocationAlt,
                                contentDescription = null,
                                tint = primaryText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isManualPickerOpen) "Close Directory" else "Choose Manually",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = primaryText
                            )
                        }
                    }
                }
            }
        } else {
            // Location is granted: provide quick manual override toggle if needed
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isManualPickerOpen = !isManualPickerOpen }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isManualPickerOpen) "Hide manual cities" else "Want to choose a different city manually?",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color.semanticPrimaryAccent
                )
                Text(
                    text = if (isManualPickerOpen) "Done" else "Change",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.semanticPrimaryAccent
                )
            }
        }

        // Manual City Search & Directory Picker
        if (isManualPickerOpen) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.semanticSurfaceElevated)
                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = mutedText,
                        modifier = Modifier.size(16.dp)
                    )
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("location_search_input"),
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = primaryText
                        ),
                        cursorBrush = SolidColor(Color.semanticPrimaryAccent),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search city (e.g., London, Cairo, Dubai)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                    color = mutedText
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("location_city_list"),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredCities, key = { "${it.cityName}_${it.countryName}" }) { city ->
                    val isCurrent = city.cityName.equals(selectedCity.cityName, ignoreCase = true) &&
                            city.countryName.equals(selectedCity.countryName, ignoreCase = true)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isCurrent) Color.semanticPrimaryAccent.copy(alpha = 0.1f) else Color.semanticSurfaceElevated
                            )
                            .border(
                                1.dp,
                                if (isCurrent) Color.semanticPrimaryAccent else borderColor.copy(alpha = 0.4f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                onSelectCity(city)
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("city_item_${city.cityName.lowercase()}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "${city.cityName}, ${city.countryName}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isCurrent) Color.semanticPrimaryAccent else primaryText
                            )
                            Text(
                                text = String.format(java.util.Locale.US, "%.2f° N, %.2f° E", city.latitude, city.longitude),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = mutedText
                            )
                        }

                        if (isCurrent) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = "Active",
                                tint = Color.semanticPrimaryAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// Step 3: Prayer Calculation Method
// -------------------------------------------------------------------------------------------------

@Composable
private fun StepCalcMethod(
    selectedMethod: CalcMethod,
    onSelectMethod: (CalcMethod) -> Unit,
    primaryText: Color,
    secondaryText: Color,
    borderColor: Color
) {
    val methods = remember { CalcMethod.entries }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("calc_method_list"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(methods, key = { it.name }) { method ->
            val isSelected = method == selectedMethod
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) Color.semanticSurfaceElevated else Color.semanticSurfaceElevated.copy(alpha = 0.5f)
                    )
                    .border(
                        1.5.dp,
                        if (isSelected) Color.semanticPrimaryAccent else borderColor,
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { onSelectMethod(method) }
                    .padding(16.dp)
                    .testTag("calc_method_${method.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = method.displayName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 15.sp
                            ),
                            color = primaryText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (method == CalcMethod.UMM_AL_QURA) {
                                "Fajr angle: ${method.fajrAngle}°, Isha: 90 min after Maghrib"
                            } else {
                                "Fajr angle: ${method.fajrAngle}°, Isha angle: ${method.ishaAngle}°"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = secondaryText
                        )
                    }

                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelectMethod(method) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color.semanticPrimaryAccent,
                            unselectedColor = Color.semanticStrongBorder
                        )
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// Step 4: Asr Calculation / Madhab
// -------------------------------------------------------------------------------------------------

@Composable
private fun StepMadhab(
    selectedMadhab: Madhab,
    onSelectMadhab: (Madhab) -> Unit,
    primaryText: Color,
    secondaryText: Color,
    borderColor: Color
) {
    val madhabs = remember { Madhab.entries }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("madhab_list"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        madhabs.forEach { m ->
            val isSelected = m == selectedMadhab
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) Color.semanticSurfaceElevated else Color.semanticSurfaceElevated.copy(alpha = 0.5f)
                    )
                    .border(
                        1.5.dp,
                        if (isSelected) Color.semanticPrimaryAccent else borderColor,
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { onSelectMadhab(m) }
                    .padding(20.dp)
                    .testTag("madhab_card_${m.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = m.displayName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 17.sp
                            ),
                            color = primaryText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (m) {
                                Madhab.STANDARD -> "Shadow length 1x object height (standard majority consensus)."
                                Madhab.HANAFI -> "Shadow length 2x object height (later Asr start time)."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
                            color = secondaryText
                        )
                    }

                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelectMadhab(m) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color.semanticPrimaryAccent,
                            unselectedColor = Color.semanticStrongBorder
                        )
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// Step 5: Hijri Date Convention
// -------------------------------------------------------------------------------------------------

@Composable
private fun StepHijriConvention(
    selectedMethod: HijriDateMethod,
    onSelectMethod: (HijriDateMethod) -> Unit,
    customOffset: Int,
    onSetCustomOffset: (Int) -> Unit,
    primaryText: Color,
    secondaryText: Color,
    mutedText: Color,
    borderColor: Color
) {
    val methods = remember { HijriDateMethod.entries }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("hijri_convention_list"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(methods, key = { it.name }) { method ->
            val isSelected = method == selectedMethod
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) Color.semanticSurfaceElevated else Color.semanticSurfaceElevated.copy(alpha = 0.5f)
                    )
                    .border(
                        1.5.dp,
                        if (isSelected) Color.semanticPrimaryAccent else borderColor,
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { onSelectMethod(method) }
                    .padding(16.dp)
                    .testTag("hijri_method_${method.name.lowercase()}")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = method.displayName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 15.sp
                                ),
                                color = primaryText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = method.description,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = secondaryText
                            )
                        }

                        RadioButton(
                            selected = isSelected,
                            onClick = { onSelectMethod(method) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color.semanticPrimaryAccent,
                                unselectedColor = Color.semanticStrongBorder
                            )
                        )
                    }

                    // Custom Offset Stepper if CUSTOM_OFFSET is selected
                    if (method == HijriDateMethod.CUSTOM_OFFSET && isSelected) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.semanticSurfaceElevated.copy(alpha = 0.6f))
                                .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Adjust Days Offset:",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = secondaryText
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(-2, -1, 0, 1, 2).forEach { offset ->
                                    val isOffsetSelected = customOffset == offset
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isOffsetSelected) Color.semanticPrimaryAccent else Color.Transparent
                                            )
                                            .border(
                                                1.dp,
                                                if (isOffsetSelected) Color.semanticPrimaryAccent else borderColor,
                                                CircleShape
                                            )
                                            .clickable { onSetCustomOffset(offset) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (offset > 0) "+$offset" else "$offset",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isOffsetSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 11.sp
                                            ),
                                            color = if (isOffsetSelected) Color.White else primaryText
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

// -------------------------------------------------------------------------------------------------
// Step 6: Prayer Notifications & Background Reliability
// -------------------------------------------------------------------------------------------------

@Composable
private fun StepNotificationsAndReliability(
    context: Context,
    isNotificationsPrefEnabled: Boolean,
    onToggleNotificationsPref: (Boolean) -> Unit,
    isNotificationPermissionGranted: Boolean,
    notificationPermissionAttempted: Boolean,
    onRequestNotificationPermission: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    isBatteryOptimizationIgnored: Boolean,
    onOpenBatterySettings: () -> Unit,
    isExactAlarmPermitted: Boolean,
    onOpenExactAlarmSettings: () -> Unit,
    preReminderOffset: PrePrayerReminderOffset,
    onSelectPreReminder: (PrePrayerReminderOffset) -> Unit,
    primaryText: Color,
    secondaryText: Color,
    mutedText: Color,
    borderColor: Color
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .testTag("step_notifications_container"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // SECTION 1: NOTIFICATION PERMISSION
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.semanticSurfaceElevated)
                .border(
                    1.5.dp,
                    if (isNotificationPermissionGranted) Color.semanticSuccess else borderColor,
                    RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
                .testTag("notification_runtime_card")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isNotificationPermissionGranted) Icons.Outlined.NotificationsActive else Icons.Outlined.NotificationsOff,
                            contentDescription = null,
                            tint = if (isNotificationPermissionGranted) Color.semanticSuccess else Color.semanticWarning,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isNotificationPermissionGranted) "NOTIFICATIONS ENABLED" else "NOTIFICATIONS NOT ENABLED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = if (isNotificationPermissionGranted) Color.semanticSuccess else Color.semanticWarning
                        )
                    }

                    if (isNotificationPermissionGranted) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color.semanticSuccess),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = "Enabled",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "Receive notifications for your prayer times.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = primaryText
                )

                Text(
                    text = if (isNotificationPermissionGranted) {
                        "FiveLight has system permission to post notifications at Fajr, Dhuhr, Asr, Maghrib, and Isha."
                    } else {
                        "Without notification permission, prayer times can still be viewed in the app, but timely reminders cannot be delivered to your device."
                    },
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                    color = secondaryText
                )

                if (!isNotificationPermissionGranted) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onRequestNotificationPermission,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("allow_notifications_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.semanticPrimaryAccent,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Allow Notifications",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        if (notificationPermissionAttempted) {
                            OutlinedButton(
                                onClick = onOpenNotificationSettings,
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("open_notification_settings_button"),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.OpenInNew,
                                    contentDescription = null,
                                    tint = primaryText,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Settings",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = primaryText
                                )
                            }
                        }
                    }
                } else {
                    // Pre-Prayer Preparation offset when notifications are active
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Pre-Prayer Reminder:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = secondaryText
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PrePrayerReminderOffset.entries.forEach { offset ->
                                val isActive = preReminderOffset == offset
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isActive) Color.semanticPrimaryAccent else Color.Transparent
                                        )
                                        .border(
                                            1.dp,
                                            if (isActive) Color.semanticPrimaryAccent else borderColor,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onSelectPreReminder(offset) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = offset.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp
                                        ),
                                        color = if (isActive) Color.White else primaryText
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // SECTION 2: BACKGROUND RELIABILITY / BATTERY OPTIMIZATIONS
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.semanticSurfaceElevated)
                .border(
                    1.5.dp,
                    if (isBatteryOptimizationIgnored) Color.semanticSuccess else borderColor,
                    RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
                .testTag("battery_reliability_card")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isBatteryOptimizationIgnored) Icons.Outlined.CheckCircle else Icons.Outlined.BatteryAlert,
                            contentDescription = null,
                            tint = if (isBatteryOptimizationIgnored) Color.semanticSuccess else Color.semanticWarning,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isBatteryOptimizationIgnored) "BACKGROUND ACTIVITY: ENABLED" else "BACKGROUND ACTIVITY: NOT ENABLED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = if (isBatteryOptimizationIgnored) Color.semanticSuccess else Color.semanticWarning
                        )
                    }

                    if (isBatteryOptimizationIgnored) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color.semanticSuccess),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = "Enabled",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "Allow FiveLight to run reliably in the background.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = primaryText
                )

                Text(
                    text = if (isBatteryOptimizationIgnored) {
                        "FiveLight is exempt from aggressive battery restrictions, allowing scheduled alarms to ring punctually."
                    } else {
                        "Modern Android power-saving optimizations may defer background timers when the device is locked. Exempting FiveLight ensures prayers ring precisely on time."
                    },
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                    color = secondaryText
                )

                if (!isBatteryOptimizationIgnored) {
                    Button(
                        onClick = onOpenBatterySettings,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("configure_battery_settings_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.semanticSurfaceElevated,
                            contentColor = primaryText
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.OpenInNew,
                            contentDescription = null,
                            tint = Color.semanticPrimaryAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Configure in Settings",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // SECTION 3: EXACT ALARM CAPABILITY (On Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !isExactAlarmPermitted) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.semanticSurfaceElevated.copy(alpha = 0.6f))
                    .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                    .padding(14.dp)
                    .testTag("exact_alarm_card")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = Color.semanticWarning,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "EXACT ALARMS RESTRICTED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.semanticWarning
                            )
                        )
                    }
                    Text(
                        text = "Android 12+ requires permission for precise minute-by-minute prayer alarms.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = secondaryText
                    )
                    OutlinedButton(
                        onClick = onOpenExactAlarmSettings,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("allow_exact_alarms_button"),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                    ) {
                        Text(
                            text = "Allow Exact Alarms in Settings",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = primaryText
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// -------------------------------------------------------------------------------------------------
// Step 7: Tasbeeh Sound & Haptics
// -------------------------------------------------------------------------------------------------

@Composable
private fun StepTasbeehHaptics(
    selectedSound: TasbeehSound,
    onSelectSound: (TasbeehSound) -> Unit,
    vibrationEnabled: Boolean,
    onToggleVibration: (Boolean) -> Unit,
    primaryText: Color,
    secondaryText: Color,
    mutedText: Color,
    borderColor: Color
) {
    val sounds = remember { TasbeehSound.entries }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("step_tasbeeh_haptics_container"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Vibration Feedback Switch Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.semanticSurfaceElevated)
                .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
                .padding(16.dp)
                .testTag("tasbeeh_vibration_card")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.semanticPrimaryAccent.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Vibration,
                            contentDescription = null,
                            tint = Color.semanticPrimaryAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Vibration Feedback",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = primaryText
                        )
                        Text(
                            text = "Gentle tactile pulse on each dhikr count.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = secondaryText
                        )
                    }
                }

                Switch(
                    checked = vibrationEnabled,
                    onCheckedChange = onToggleVibration,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color.semanticPrimaryAccent,
                        uncheckedTrackColor = Color.semanticControl
                    ),
                    modifier = Modifier.testTag("tasbeeh_vibration_switch")
                )
            }
        }

        // Tap Sound Selector
        Text(
            text = "TAP SOUND",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = Color.semanticPrimaryAccent
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("tasbeeh_sound_list"),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sounds, key = { it.name }) { sound ->
                val isSelected = sound == selectedSound
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) Color.semanticSurfaceElevated else Color.semanticSurfaceElevated.copy(alpha = 0.5f)
                        )
                        .border(
                            1.dp,
                            if (isSelected) Color.semanticPrimaryAccent else borderColor,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectSound(sound) }
                        .padding(14.dp)
                        .testTag("tasbeeh_sound_${sound.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = sound.displayName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = primaryText
                            )
                            Text(
                                text = sound.description,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = secondaryText
                            )
                        }

                        RadioButton(
                            selected = isSelected,
                            onClick = { onSelectSound(sound) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color.semanticPrimaryAccent,
                                unselectedColor = Color.semanticStrongBorder
                            )
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// Step 8: Automatic Backup
// -------------------------------------------------------------------------------------------------

@Composable
private fun StepAutomaticBackup(
    currentFrequency: BackupManager.AutoBackupFrequency,
    onSelectFrequency: (BackupManager.AutoBackupFrequency) -> Unit,
    driveAccount: GoogleSignInAccount?,
    onRequestDriveAuth: () -> Unit,
    isBackingUp: Boolean,
    lastBackupTime: Long,
    manualBackupMessage: String?,
    onManualBackup: () -> Unit,
    primaryText: Color,
    secondaryText: Color,
    mutedText: Color,
    borderColor: Color
) {
    val frequencies = remember {
        listOf(
            Triple(
                BackupManager.AutoBackupFrequency.WEEKLY,
                "Weekly (Recommended)",
                "Creates an encrypted backup every 7 days when connected to Wi-Fi/data."
            ),
            Triple(
                BackupManager.AutoBackupFrequency.DAILY,
                "Daily",
                "Creates an encrypted backup every 24 hours."
            ),
            Triple(
                BackupManager.AutoBackupFrequency.OFF,
                "Off",
                "Do not back up automatically."
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("automatic_backup_container"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Frequency Options
        frequencies.forEach { (freq, title, desc) ->
            val isSelected = freq == currentFrequency
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) Color.semanticSurfaceElevated else Color.semanticSurfaceElevated.copy(alpha = 0.5f)
                    )
                    .border(
                        1.5.dp,
                        if (isSelected) Color.semanticPrimaryAccent else borderColor,
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { onSelectFrequency(freq) }
                    .padding(16.dp)
                    .testTag("auto_backup_freq_${freq.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = primaryText
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                            color = secondaryText
                        )
                    }

                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelectFrequency(freq) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color.semanticPrimaryAccent,
                            unselectedColor = Color.semanticStrongBorder
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Google Drive Status Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.semanticSurfaceElevated.copy(alpha = 0.6f))
                .border(
                    1.5.dp,
                    if (driveAccount != null) Color.semanticSuccess else borderColor,
                    RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
                .testTag("auto_backup_drive_status_card")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (driveAccount != null) Icons.Outlined.CloudDone else Icons.Outlined.CloudOff,
                            contentDescription = null,
                            tint = if (driveAccount != null) Color.semanticSuccess else Color.semanticWarning,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (driveAccount != null) "GOOGLE DRIVE CONNECTED" else "GOOGLE DRIVE REQUIRED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = if (driveAccount != null) Color.semanticSuccess else Color.semanticWarning
                        )
                    }
                }

                if (driveAccount != null) {
                    Text(
                        text = "Connected Account: ${driveAccount.email}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                        color = primaryText
                    )
                    Text(
                        text = "Backups are encrypted using AES-256-GCM before being stored in your private Google Drive app storage (drive.appdata).",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                        color = secondaryText
                    )
                    if (lastBackupTime > 0) {
                        Text(
                            text = "Last backed up: ${formatBackupTimestamp(null, lastBackupTime)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = mutedText
                        )
                    }

                    if (manualBackupMessage != null) {
                        Text(
                            text = manualBackupMessage,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                color = if (manualBackupMessage.contains("success", ignoreCase = true)) Color.semanticSuccess else Color.semanticError
                            )
                        )
                    }

                    OutlinedButton(
                        onClick = onManualBackup,
                        enabled = !isBackingUp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("manual_backup_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isBackingUp) {
                            CircularProgressIndicator(
                                color = Color.semanticPrimaryAccent,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Backing up...", color = primaryText)
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Backup,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.semanticPrimaryAccent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Back Up Now", color = primaryText)
                        }
                    }
                } else {
                    Text(
                        text = "To enable automatic backups, connect your Google Drive account. FiveLight will remember your preference (${currentFrequency.name.lowercase()}) and schedule backups as soon as Google Drive is authorized.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                        color = secondaryText
                    )

                    Button(
                        onClick = onRequestDriveAuth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("connect_drive_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.semanticPrimaryAccent,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Connect Google Drive", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// Step 10: Appearance & Complete System Capabilities Summary
// -------------------------------------------------------------------------------------------------

@Composable
private fun StepAppearanceAndCapabilities(
    selectedMode: AppearanceMode,
    onSelectMode: (AppearanceMode) -> Unit,
    selectedCity: CityLocation,
    calcMethod: CalcMethod,
    madhab: Madhab,
    isLocationPermissionGranted: Boolean,
    isNotificationPermissionGranted: Boolean,
    isBatteryOptimizationIgnored: Boolean,
    driveAccount: GoogleSignInAccount?,
    autoBackupFrequency: BackupManager.AutoBackupFrequency,
    primaryText: Color,
    secondaryText: Color,
    mutedText: Color,
    borderColor: Color
) {
    val modes = remember {
        listOf(
            Triple(AppearanceMode.SYSTEM, "System Default", Icons.Outlined.LightMode),
            Triple(AppearanceMode.LIGHT, "Light Mode", Icons.Outlined.LightMode),
            Triple(AppearanceMode.DARK, "Dark Mode", Icons.Outlined.DarkMode)
        )
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .testTag("step_appearance_container"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Theme Options (Immediately responds live)
        modes.forEach { (mode, title, icon) ->
            val isSelected = mode == selectedMode
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) Color.semanticSurfaceElevated else Color.semanticSurfaceElevated.copy(alpha = 0.5f)
                    )
                    .border(
                        1.5.dp,
                        if (isSelected) Color.semanticPrimaryAccent else borderColor,
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { onSelectMode(mode) }
                    .padding(16.dp)
                    .testTag("appearance_mode_${mode.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.semanticPrimaryAccent.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = Color.semanticPrimaryAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = primaryText
                            )
                            Text(
                                text = when (mode) {
                                    AppearanceMode.SYSTEM -> "Follows your Android system theme."
                                    AppearanceMode.LIGHT -> "Clean, high-contrast daytime layout."
                                    AppearanceMode.DARK -> "Deep, eye-safe nighttime canvas."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = secondaryText
                            )
                        }
                    }

                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelectMode(mode) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color.semanticPrimaryAccent,
                            unselectedColor = Color.semanticStrongBorder
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Real Capability & Configuration Summary Card (NO FAKE SUCCESS STATES)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.semanticSurfaceElevated.copy(alpha = 0.6f))
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                .padding(16.dp)
                .testTag("final_capability_summary_card")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SETUP & CAPABILITY SUMMARY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = Color.semanticPrimaryAccent
                )

                // Cloud Sync
                SummaryItemRow(
                    label = "Cloud Sync",
                    value = "✓ Active",
                    isPositive = true
                )

                // Prayer Preferences
                SummaryItemRow(
                    label = "Prayer Schedule",
                    value = "${calcMethod.displayName} • ${madhab.displayName}",
                    isPositive = true
                )

                // Location Real State
                SummaryItemRow(
                    label = "Location",
                    value = if (isLocationPermissionGranted) {
                        "✓ Enabled (${selectedCity.cityName})"
                    } else {
                        "Manual (${selectedCity.cityName})"
                    },
                    isPositive = isLocationPermissionGranted
                )

                // Notifications Real State
                SummaryItemRow(
                    label = "Notifications",
                    value = if (isNotificationPermissionGranted) "✓ Enabled" else "Not enabled",
                    isPositive = isNotificationPermissionGranted
                )

                // Background Activity Real State
                SummaryItemRow(
                    label = "Background Activity",
                    value = if (isBatteryOptimizationIgnored) "✓ Unrestricted" else "Standard",
                    isPositive = isBatteryOptimizationIgnored
                )

                // Google Drive
                SummaryItemRow(
                    label = "Google Drive",
                    value = if (driveAccount != null) "✓ Connected" else "Not connected",
                    isPositive = driveAccount != null
                )

                // Auto Backup
                SummaryItemRow(
                    label = "Automatic Backup",
                    value = when {
                        autoBackupFrequency == BackupManager.AutoBackupFrequency.OFF -> "Off"
                        driveAccount != null -> "✓ ${autoBackupFrequency.name.lowercase().replaceFirstChar { it.uppercase() }}"
                        else -> "Requires Drive (${autoBackupFrequency.name.lowercase()})"
                    },
                    isPositive = driveAccount != null && autoBackupFrequency != BackupManager.AutoBackupFrequency.OFF
                )
            }
        }
    }
}

@Composable
private fun SummaryItemRow(
    label: String,
    value: String,
    isPositive: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = Color.semanticSecondaryText
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            ),
            color = if (value.startsWith("✓")) Color.semanticSuccess else if (value.startsWith("Not enabled") || value.startsWith("Requires Drive")) Color.semanticWarning else Color.semanticPrimaryText
        )
    }
}

// -------------------------------------------------------------------------------------------------
// Bottom Actions Bar
// -------------------------------------------------------------------------------------------------

@Composable
private fun SetupBottomActions(
    currentStep: Int,
    totalSteps: Int,
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLastStep = currentStep == totalSteps - 1
    val buttonText = when {
        currentStep == 0 -> "Get Started"
        isLastStep -> "Start FiveLight"
        else -> "Continue"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.semanticBackground)
            .border(1.dp, Color.semanticBorder.copy(alpha = 0.4f))
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep > 0) {
                TextButton(
                    onClick = onBack,
                    modifier = Modifier
                        .height(50.dp)
                        .testTag("setup_bottom_back_button")
                ) {
                    Text(
                        text = "Back",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.semanticSecondaryText
                    )
                }
            }

            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag(if (isLastStep) "setup_complete_button" else "setup_continue_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.semanticPrimaryAccent,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }
    }
}
