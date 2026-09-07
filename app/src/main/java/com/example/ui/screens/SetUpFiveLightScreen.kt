package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.BatteryAlert
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import com.example.data.backup.GoogleDriveService
import com.example.data.model.AppearanceMode
import com.example.data.model.CalcMethod
import com.example.data.model.CityLocation
import com.example.data.model.HijriDateMethod
import com.example.data.model.Madhab
import com.example.data.model.TasbeehSound
import com.example.data.reminder.PrePrayerReminderOffset
import com.example.data.reminder.SmartPrayerNotificationManager
import com.example.data.util.LocationHelper
import com.example.ui.theme.InstrumentSerifItalic
import com.example.ui.theme.isAppInDarkTheme
import com.example.ui.theme.semanticAccentForeground
import com.example.ui.theme.semanticBackground
import com.example.ui.theme.semanticBorder
import com.example.ui.theme.semanticError
import com.example.ui.theme.semanticMutedText
import com.example.ui.theme.semanticPrimaryAccent
import com.example.ui.theme.semanticPrimaryText
import com.example.ui.theme.semanticSecondaryText
import com.example.ui.theme.semanticSuccess
import com.example.ui.theme.semanticSurfaceElevated
import com.example.ui.theme.semanticWarning
import com.example.ui.viewmodel.AppViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

/**
 * Top-level capability checks exposed for tests and components.
 */
fun checkLocationPermission(context: Context): Boolean = LocationHelper.hasLocationPermission(context)
fun checkNotificationPermission(context: Context): Boolean = SmartPrayerNotificationManager(context).isNotificationPermissionGranted()
fun checkBackgroundOptimization(context: Context): Boolean = SmartPrayerNotificationManager(context).isIgnoringBatteryOptimizations()

/**
 * Global Reusable "Recommended" Chip Component for FiveLight.
 *
 * Requirements:
 * - Single source of truth across all onboarding screens and bottom sheets.
 * - Always renders on ONE horizontal line (no vertical letter wrapping, softWrap = false, maxLines = 1).
 * - Intrinsic content-based sizing with stable horizontal padding (8.dp) and vertical padding (3.dp).
 * - Chip background uses the FiveLight accent color (dark-mode accent in dark theme, primary accent in light theme).
 * - Chip text is ALWAYS high-contrast pure WHITE (Color.White, #FFFFFF) with semi-bold typography.
 * - Text never inherits secondary, muted, or disabled colors and never becomes grey-on-dark.
 * - The full word "Recommended" is guaranteed to be visible without clipping or truncation.
 */
@Composable
fun RecommendedChip(
    modifier: Modifier = Modifier,
    isOnAccentBackground: Boolean = false
) {
    val isDark = isAppInDarkTheme()
    val bgColor = when {
        isOnAccentBackground -> Color.White.copy(alpha = 0.22f)
        isDark -> Color(0xFF494556) // FiveLight dark-mode accent color
        else -> Color.semanticPrimaryAccent // FiveLight light-mode accent color
    }
    val textColor = Color(0xFFFFFFFF) // ALWAYS crisp, pure WHITE text

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Recommended",
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.3.sp,
                color = textColor
            ),
            maxLines = 1,
            softWrap = false
        )
    }
}

/**
 * Backward compatibility alias for RecommendedChip.
 */
@Composable
fun FiveLightRecommendedBadge(
    modifier: Modifier = Modifier,
    isOnAccentBackground: Boolean = false
) {
    RecommendedChip(
        modifier = modifier,
        isOnAccentBackground = isOnAccentBackground
    )
}

/**
 * Global Reusable Selectable Option Row for FiveLight Onboarding & Sheets.
 *
 * Requirements:
 * - Selected background spans full row within rounded 12.dp bounds without clipping or edge bleeding.
 * - Entire row is tappable with proper touch target.
 * - Title and Recommended chip layout is responsive: title wraps gracefully if long, chip is never clipped.
 * - Consistent dark mode surface, border, and accent colors.
 */
@Composable
fun SelectableOptionRow(
    title: String,
    subtitle: String? = null,
    isRecommended: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()
    val animatedBgColor by animateColorAsState(
        targetValue = if (isSelected) {
            if (isDark) Color(0xFF494556).copy(alpha = 0.22f) else Color.semanticPrimaryAccent.copy(alpha = 0.08f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(220),
        label = "SelectableRowBg"
    )
    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) Color.semanticPrimaryAccent else Color.semanticBorder.copy(alpha = 0.6f),
        animationSpec = tween(220),
        label = "SelectableRowBorder"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(animatedBgColor)
            .border(1.dp, animatedBorderColor, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = androidx.compose.material3.ripple(),
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 13.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = Color.semanticPrimaryText,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isRecommended) {
                        RecommendedChip()
                    }
                }
                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color.semanticSecondaryText,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            FiveLightAnimatedRadio(
                selected = isSelected,
                onClick = onClick
            )
        }
    }
}

/**
 * Custom Animated Radio / Selection Indicator for FiveLight.
 *
 * Provides a tactile, calm animated transition between unselected ring (○) and selected dot (◉).
 */
@Composable
fun FiveLightAnimatedRadio(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val innerDotScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "RadioDotScale"
    )
    val ringColor by animateColorAsState(
        targetValue = if (selected) Color.semanticPrimaryAccent else Color.semanticMutedText.copy(alpha = 0.6f),
        animationSpec = tween(220),
        label = "RadioRingColor"
    )

    Box(
        modifier = modifier
            .size(22.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .border(2.dp, ringColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (innerDotScale > 0f) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .scale(innerDotScale)
                    .clip(CircleShape)
                    .background(Color.semanticPrimaryAccent)
            )
        }
    }
}

/**
 * Adaptive Onboarding Step definitions for FiveLight.
 */
enum class AdaptiveOnboardingStep(
    val sectionLabel: String,
    val title: String,
    val subtitle: String
) {
    WELCOME(
        sectionLabel = "WELCOME",
        title = "Make FiveLight yours.",
        subtitle = "A few quick choices and FiveLight will be ready for your daily rhythm."
    ),
    DATA_RESTORE(
        sectionLabel = "YOUR DATA",
        title = "Welcome back",
        subtitle = "We found a FiveLight backup associated with your account."
    ),
    WHERE_ARE_YOU(
        sectionLabel = "LOCATION",
        title = "Where are you?",
        subtitle = "FiveLight uses your location to calculate accurate prayer times and Qibla direction."
    ),
    PRAYER_TIMES(
        sectionLabel = "PRAYER",
        title = "Prayer Times",
        subtitle = "Configured automatically for your region."
    ),
    STAY_ON_TIME(
        sectionLabel = "REMINDERS",
        title = "Stay on time",
        subtitle = "Allow FiveLight to notify you when prayer times arrive."
    ),
    KEEP_RELIABLE(
        sectionLabel = "RELIABILITY",
        title = "Keep reminders reliable",
        subtitle = "FiveLight needs to run reliably in the background so scheduled prayer reminders can arrive on time."
    ),
    AUTOMATIC_BACKUP(
        sectionLabel = "BACKUP",
        title = "Automatic Backup",
        subtitle = "Keep an encrypted backup of your FiveLight data in your private Google Drive."
    ),
    TASBEEH_HAPTICS(
        sectionLabel = "DHIKR & HAPTICS",
        title = "Dhikr & Haptics",
        subtitle = "Customize audio taps and tactile feedback during daily dhikr recitation."
    ),
    APPEARANCE_AND_READY(
        sectionLabel = "READY",
        title = "You're ready.",
        subtitle = "FiveLight is set up for you."
    )
}

@Composable
fun SetUpFiveLightScreen(
    viewModel: AppViewModel,
    currentUser: FirebaseUser? = null,
    onSetupComplete: () -> Unit = {},
    modifier: Modifier = Modifier,
    authRepository: AuthRepository = AuthRepository.getInstance(LocalContext.current),
    onSetupFinished: () -> Unit = onSetupComplete
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val notificationManager = remember { SmartPrayerNotificationManager(context) }

    val activeUser by viewModel.currentUser.collectAsStateWithLifecycle(initialValue = currentUser)
    val effectiveUser = currentUser ?: activeUser
    val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()
    val calcMethod by viewModel.calcMethod.collectAsStateWithLifecycle()
    val madhab by viewModel.madhab.collectAsStateWithLifecycle()
    val appearanceMode by viewModel.appearanceMode.collectAsStateWithLifecycle()
    val hijriDateMethod by viewModel.hijriDateMethod.collectAsStateWithLifecycle()
    val tasbeehSound by viewModel.tasbeehSound.collectAsStateWithLifecycle()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsStateWithLifecycle()

    // Dynamic Permission & Capability States
    var hasLocationPermission by remember { mutableStateOf(checkLocationPermission(context)) }
    var hasNotificationPermission by remember { mutableStateOf(checkNotificationPermission(context)) }
    var isBackgroundExempt by remember { mutableStateOf(checkBackgroundOptimization(context)) }

    // Adaptive Backup Detection: Only show Welcome Back / Data Restore if a backup genuinely exists
    var hasDetectedBackup by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(effectiveUser) {
        if (effectiveUser != null) {
            val lastLocalTime = BackupManager.getLastBackupTime(context)
            if (lastLocalTime > 0) {
                hasDetectedBackup = true
            } else {
                try {
                    val remoteRes = viewModel.checkRemoteBackup()
                    if (remoteRes.isSuccess && remoteRes.getOrNull() != null) {
                        hasDetectedBackup = true
                    }
                } catch (_: Exception) {}
            }
        }
    }

    // Track restore outcome for summary on final screen
    var restoreCompletedSuccessfully by rememberSaveable { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasLocationPermission = checkLocationPermission(context)
                hasNotificationPermission = checkNotificationPermission(context)
                isBackgroundExempt = checkBackgroundOptimization(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Compute Active Onboarding Sequence Adaptively
    val activeSteps = remember(effectiveUser, hasDetectedBackup) {
        buildList {
            add(AdaptiveOnboardingStep.WELCOME)
            if (effectiveUser != null && hasDetectedBackup) {
                add(AdaptiveOnboardingStep.DATA_RESTORE)
            }
            add(AdaptiveOnboardingStep.WHERE_ARE_YOU)
            add(AdaptiveOnboardingStep.PRAYER_TIMES)
            add(AdaptiveOnboardingStep.STAY_ON_TIME)
            add(AdaptiveOnboardingStep.KEEP_RELIABLE)
            if (effectiveUser != null) {
                add(AdaptiveOnboardingStep.AUTOMATIC_BACKUP)
            }
            add(AdaptiveOnboardingStep.TASBEEH_HAPTICS)
            add(AdaptiveOnboardingStep.APPEARANCE_AND_READY)
        }
    }

    var stepIndex by rememberSaveable { mutableIntStateOf(0) }
    val currentStep = activeSteps.getOrElse(stepIndex) { AdaptiveOnboardingStep.WELCOME }

    // Navigation Handlers
    val canGoBack = stepIndex > 0
    val goBack: () -> Unit = {
        if (canGoBack) {
            stepIndex--
        }
    }

    val goNext: () -> Unit = {
        if (stepIndex < activeSteps.lastIndex) {
            stepIndex++
        } else {
            val uid = effectiveUser?.uid ?: "anonymous_user"
            authRepository.setSetupCompleted(uid, true)
            viewModel.syncManager.notifyPreferencesChanged()
            onSetupFinished()
        }
    }

    BackHandler(enabled = canGoBack) {
        goBack()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.semanticBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("setup_fivelight_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            OnboardingHeaderBar(
                currentStep = currentStep,
                stepIndex = stepIndex,
                totalSteps = activeSteps.size,
                canGoBack = canGoBack,
                onBackClicked = goBack
            )

            // Animated Screen Container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        val targetIdx = activeSteps.indexOf(targetState)
                        val initialIdx = activeSteps.indexOf(initialState)
                        val forward = targetIdx >= initialIdx
                        val offsetFraction = 0.12f

                        if (forward) {
                            (slideInHorizontally(
                                animationSpec = tween(300, easing = FastOutSlowInEasing),
                                initialOffsetX = { fullWidth -> (fullWidth * offsetFraction).toInt() }
                            ) + fadeIn(animationSpec = tween(280, easing = FastOutSlowInEasing))) togetherWith (
                                slideOutHorizontally(
                                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                                    targetOffsetX = { fullWidth -> -(fullWidth * offsetFraction).toInt() }
                                ) + fadeOut(animationSpec = tween(220, easing = FastOutSlowInEasing))
                            )
                        } else {
                            (slideInHorizontally(
                                animationSpec = tween(300, easing = FastOutSlowInEasing),
                                initialOffsetX = { fullWidth -> -(fullWidth * offsetFraction).toInt() }
                            ) + fadeIn(animationSpec = tween(280, easing = FastOutSlowInEasing))) togetherWith (
                                slideOutHorizontally(
                                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                                    targetOffsetX = { fullWidth -> (fullWidth * offsetFraction).toInt() }
                                ) + fadeOut(animationSpec = tween(220, easing = FastOutSlowInEasing))
                            )
                        }
                    },
                    label = "OnboardingStepTransition"
                ) { step ->
                    when (step) {
                        AdaptiveOnboardingStep.WELCOME -> {
                            WelcomeStep(
                                currentUser = effectiveUser,
                                onContinue = goNext
                            )
                        }

                        AdaptiveOnboardingStep.DATA_RESTORE -> {
                            WelcomeBackRestoreStep(
                                viewModel = viewModel,
                                currentUser = effectiveUser,
                                onRestoreSuccess = { restoreCompletedSuccessfully = true },
                                onContinue = goNext
                            )
                        }

                        AdaptiveOnboardingStep.WHERE_ARE_YOU -> {
                            WhereAreYouStep(
                                viewModel = viewModel,
                                selectedCity = selectedCity,
                                hasLocationPermission = hasLocationPermission,
                                onPermissionUpdated = { hasLocationPermission = it },
                                onContinue = goNext
                            )
                        }

                        AdaptiveOnboardingStep.PRAYER_TIMES -> {
                            CombinedPrayerScreen(
                                viewModel = viewModel,
                                selectedCity = selectedCity,
                                calcMethod = calcMethod,
                                madhab = madhab,
                                hijriDateMethod = hijriDateMethod,
                                onContinue = goNext
                            )
                        }

                        AdaptiveOnboardingStep.STAY_ON_TIME -> {
                            StayOnTimeStep(
                                notificationManager = notificationManager,
                                hasNotificationPermission = hasNotificationPermission,
                                onPermissionUpdated = { hasNotificationPermission = it },
                                onContinue = goNext
                            )
                        }

                        AdaptiveOnboardingStep.KEEP_RELIABLE -> {
                            KeepReliableStep(
                                isBackgroundExempt = isBackgroundExempt,
                                onStateUpdated = { isBackgroundExempt = it },
                                onContinue = goNext
                            )
                        }

                        AdaptiveOnboardingStep.AUTOMATIC_BACKUP -> {
                            AutomaticBackupStep(
                                viewModel = viewModel,
                                currentUser = effectiveUser,
                                onContinue = goNext
                            )
                        }

                        AdaptiveOnboardingStep.TASBEEH_HAPTICS -> {
                            TasbeehHapticsStep(
                                viewModel = viewModel,
                                tasbeehSound = tasbeehSound,
                                vibrationEnabled = vibrationEnabled,
                                onContinue = goNext
                            )
                        }

                        AdaptiveOnboardingStep.APPEARANCE_AND_READY -> {
                            AppearanceAndReadyStep(
                                viewModel = viewModel,
                                appearanceMode = appearanceMode,
                                selectedCity = selectedCity,
                                calcMethod = calcMethod,
                                hasLocationPermission = hasLocationPermission,
                                hasNotificationPermission = hasNotificationPermission,
                                isBackgroundExempt = isBackgroundExempt,
                                isRestoreRestored = restoreCompletedSuccessfully,
                                effectiveUser = effectiveUser,
                                onFinish = {
                                    val uid = effectiveUser?.uid.orEmpty().ifBlank { "anonymous_user" }
                                    authRepository.setSetupCompleted(uid, true)
                                    viewModel.setSetupCompleted(uid, true)
                                    viewModel.syncManager.notifyPreferencesChanged()
                                    onSetupFinished()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Top bar with gentle progress indicator and back navigation.
 * Uses a clean contextual section title and smooth subtle progress line
 * without technical numeric ratios (e.g. 1/9).
 */
@Composable
private fun OnboardingHeaderBar(
    currentStep: AdaptiveOnboardingStep,
    stepIndex: Int,
    totalSteps: Int,
    canGoBack: Boolean,
    onBackClicked: () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = ((stepIndex + 1).toFloat() / totalSteps.toFloat()).coerceIn(0f, 1f),
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "ProgressAnimation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (canGoBack) {
                IconButton(
                    onClick = onBackClicked,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("onboarding_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.semanticPrimaryText
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(40.dp))
            }

            Text(
                text = currentStep.sectionLabel,
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = Color.semanticMutedText
                )
            )

            // Balance spacer for symmetric center alignment
            Spacer(modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Subtle progress line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .clip(CircleShape)
                .background(Color.semanticBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(2.dp)
                    .clip(CircleShape)
                    .background(Color.semanticPrimaryAccent)
            )
        }
    }
}

/**
 * Standard Primary CTA Button with calm subtle press animation and high-contrast text.
 */
@Composable
private fun PrimaryOnboardingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String = ""
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        animationSpec = tween(120),
        label = "ButtonPressScale"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .scale(scale)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.semanticPrimaryAccent,
            contentColor = Color.semanticAccentForeground
        )
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.semanticAccentForeground
        )
    }
}

/**
 * Step 1: Welcome Step.
 */
@Composable
private fun WelcomeStep(
    currentUser: FirebaseUser?,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "FiveLight",
                fontFamily = InstrumentSerifItalic,
                fontSize = 38.sp,
                color = Color.semanticPrimaryText,
                lineHeight = 42.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "A calm, privacy-first companion designed around the five daily prayers.",
                fontSize = 15.sp,
                color = Color.semanticSecondaryText,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Account status badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.semanticSurfaceElevated)
                    .border(1.dp, Color.semanticBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.semanticPrimaryAccent.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (currentUser != null) Icons.Outlined.CloudDone else Icons.Outlined.CloudOff,
                            contentDescription = null,
                            tint = Color.semanticPrimaryAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = if (currentUser != null) "Signed in" else "Local mode",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.semanticPrimaryText
                        )
                        Text(
                            text = currentUser?.email ?: "Zero trackers · strictly private",
                            fontSize = 13.sp,
                            color = Color.semanticMutedText
                        )
                    }
                }
            }
        }

        PrimaryOnboardingButton(
            text = "Begin Setup",
            onClick = onContinue,
            testTag = "welcome_get_started_button"
        )
    }
}

/**
 * Step 2: Welcome Back & Data Restore Step.
 */
@Composable
private fun WelcomeBackRestoreStep(
    viewModel: AppViewModel,
    currentUser: FirebaseUser?,
    onRestoreSuccess: () -> Unit,
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var driveAccount by remember { mutableStateOf(GoogleDriveService.getAuthorizedAccount(context)) }
    var isCheckingBackup by remember { mutableStateOf(true) }
    var backupFound by remember { mutableStateOf(false) }
    var formattedBackupDate by remember { mutableStateOf("") }

    var restoreState by remember { mutableStateOf("IDLE") } // IDLE, RESTORING, SUCCESS, FAILURE
    var restoreErrorMessage by remember { mutableStateOf<String?>(null) }
    var currentRestoreStage by remember { mutableStateOf("Preparing") }

    val userEmail = currentUser?.email ?: ""
    val driveEmail = driveAccount?.email ?: ""
    val isAccountMismatch = remember(userEmail, driveEmail, driveAccount) {
        driveAccount != null && userEmail.isNotBlank() && driveEmail.isNotBlank() && !driveEmail.equals(userEmail, ignoreCase = true)
    }

    val driveAuthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null && GoogleSignIn.hasPermissions(account, GoogleDriveService.DRIVE_APPDATA_SCOPE)) {
                driveAccount = account
                Toast.makeText(context, "Google Drive connected: ${account.email}", Toast.LENGTH_SHORT).show()
                isCheckingBackup = true
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Google Drive authorization was not granted", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(driveAccount, isCheckingBackup) {
        if (isCheckingBackup) {
            val lastLocalTime = BackupManager.getLastBackupTime(context)
            if (lastLocalTime > 0) {
                backupFound = true
                formattedBackupDate = try {
                    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(lastLocalTime))
                } catch (_: Exception) { "Recent" }
            }

            try {
                val remoteRes = viewModel.checkRemoteBackup()
                val remoteInfo = remoteRes.getOrNull()
                if (remoteInfo != null) {
                    backupFound = true
                    if (formattedBackupDate.isBlank()) {
                        formattedBackupDate = try {
                            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                            val parsed = sdf.parse(remoteInfo.modifiedTime.substringBefore("."))
                            if (parsed != null) DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(parsed)
                            else remoteInfo.modifiedTime
                        } catch (_: Exception) {
                            "Recent"
                        }
                    }
                }
            } catch (_: Exception) {
                // Ignore silent network check errors
            } finally {
                isCheckingBackup = false
            }
        }
    }

    fun startRestore() {
        restoreState = "RESTORING"
        restoreErrorMessage = null
        scope.launch {
            val result = viewModel.performDriveRestore(
                onProgress = { stage ->
                    currentRestoreStage = stage
                }
            )
            if (result.isSuccess) {
                restoreState = "SUCCESS"
                onRestoreSuccess()
            } else {
                restoreState = "FAILURE"
                val rawMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                restoreErrorMessage = when {
                    rawMsg.contains("permission", ignoreCase = true) || rawMsg.contains("drive.appdata", ignoreCase = true) ->
                        "Drive permission is required to access your backup. Please check your Google account connection."
                    rawMsg.contains("Decryption", ignoreCase = true) || rawMsg.contains("different user", ignoreCase = true) ->
                        "The backup is encrypted with a different account key. Please ensure you are signed into the correct FiveLight account."
                    rawMsg.contains("No FiveLight backup", ignoreCase = true) ->
                        "No backup file was found in Google Drive."
                    else -> "We were unable to restore your backup due to a network interruption. Please try again."
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            if (isAccountMismatch) {
                Text(
                    text = "Account Mismatch",
                    fontFamily = InstrumentSerifItalic,
                    fontSize = 30.sp,
                    color = Color.semanticPrimaryText,
                    lineHeight = 34.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "The connected Google Drive account differs from your FiveLight account.",
                    fontSize = 15.sp,
                    color = Color.semanticSecondaryText,
                    lineHeight = 22.sp
                )
            } else if (restoreState == "RESTORING") {
                Text(
                    text = "Welcome back",
                    fontFamily = InstrumentSerifItalic,
                    fontSize = 30.sp,
                    color = Color.semanticPrimaryText,
                    lineHeight = 34.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Restoring your FiveLight data…",
                    fontSize = 15.sp,
                    color = Color.semanticSecondaryText
                )
            } else if (restoreState == "SUCCESS") {
                Text(
                    text = "Welcome back",
                    fontFamily = InstrumentSerifItalic,
                    fontSize = 30.sp,
                    color = Color.semanticPrimaryText,
                    lineHeight = 34.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your FiveLight data is ready.",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.semanticSuccess
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your previous experience has been restored.",
                    fontSize = 14.sp,
                    color = Color.semanticSecondaryText
                )
            } else if (restoreState == "FAILURE") {
                Text(
                    text = "Restore Interrupted",
                    fontFamily = InstrumentSerifItalic,
                    fontSize = 30.sp,
                    color = Color.semanticPrimaryText,
                    lineHeight = 34.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "We couldn't restore your backup.",
                    fontSize = 15.sp,
                    color = Color.semanticSecondaryText
                )
            } else if (backupFound) {
                Text(
                    text = "Welcome back",
                    fontFamily = InstrumentSerifItalic,
                    fontSize = 30.sp,
                    color = Color.semanticPrimaryText,
                    lineHeight = 34.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "We found a FiveLight backup associated with your account.",
                    fontSize = 15.sp,
                    color = Color.semanticSecondaryText,
                    lineHeight = 22.sp
                )
            } else {
                Text(
                    text = "Your Data",
                    fontFamily = InstrumentSerifItalic,
                    fontSize = 30.sp,
                    color = Color.semanticPrimaryText,
                    lineHeight = 34.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No previous FiveLight backup was found for this account.",
                    fontSize = 15.sp,
                    color = Color.semanticSecondaryText,
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Body Card based on state
            if (isAccountMismatch) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.semanticSurfaceElevated)
                        .border(1.dp, Color.semanticWarning.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Warning,
                                contentDescription = null,
                                tint = Color.semanticWarning,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Accounts do not match",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.semanticPrimaryText
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "FiveLight account: $userEmail",
                                fontSize = 13.sp,
                                color = Color.semanticSecondaryText
                            )
                            Text(
                                text = "Google Drive: $driveEmail",
                                fontSize = 13.sp,
                                color = Color.semanticSecondaryText
                            )
                        }

                        Text(
                            text = "To protect your privacy and ensure your data matches your identity, connect the matching Google Drive account or start fresh.",
                            fontSize = 12.sp,
                            color = Color.semanticMutedText,
                            lineHeight = 16.sp
                        )
                    }
                }
            } else if (restoreState == "RESTORING" || restoreState == "SUCCESS") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.semanticSurfaceElevated)
                        .border(
                            1.dp,
                            if (restoreState == "SUCCESS") Color.semanticSuccess.copy(alpha = 0.4f) else Color.semanticBorder,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        RestoreProgressItem(
                            label = "Prayer history",
                            isComplete = restoreState == "SUCCESS" || currentRestoreStage == "Restoring data" || currentRestoreStage == "Finishing",
                            isInProgress = restoreState == "RESTORING" && currentRestoreStage != "Restoring data" && currentRestoreStage != "Finishing"
                        )
                        RestoreProgressItem(
                            label = "Dhikr",
                            isComplete = restoreState == "SUCCESS" || currentRestoreStage == "Restoring data" || currentRestoreStage == "Finishing",
                            isInProgress = false
                        )
                        RestoreProgressItem(
                            label = "Quran",
                            isComplete = restoreState == "SUCCESS" || currentRestoreStage == "Finishing",
                            isInProgress = false
                        )
                        RestoreProgressItem(
                            label = "Preferences",
                            isComplete = restoreState == "SUCCESS",
                            isInProgress = false
                        )
                    }
                }
            } else if (restoreState == "FAILURE") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.semanticSurfaceElevated)
                        .border(1.dp, Color.semanticError.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = Color.semanticError,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Restore failed",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.semanticPrimaryText
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = restoreErrorMessage ?: "We couldn't restore your data. You can try again or start fresh.",
                            fontSize = 13.sp,
                            color = Color.semanticSecondaryText,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else if (backupFound) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.semanticSurfaceElevated)
                        .border(1.dp, Color.semanticBorder, RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "FIVELIGHT ACCOUNT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = Color.semanticMutedText
                                )
                                Text(
                                    text = userEmail.ifBlank { "Signed In" },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.semanticPrimaryText
                                )
                            }

                            Icon(
                                imageVector = Icons.Outlined.AccountCircle,
                                contentDescription = null,
                                tint = Color.semanticPrimaryAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        HorizontalRowDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "GOOGLE DRIVE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = Color.semanticMutedText
                                )
                                Text(
                                    text = driveEmail.ifBlank { userEmail },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.semanticPrimaryText
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.semanticSuccess,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Connected",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.semanticSuccess
                                )
                            }
                        }

                        HorizontalRowDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "LAST BACKUP",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = Color.semanticMutedText
                                )
                                Text(
                                    text = formattedBackupDate.ifBlank { "Available" },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.semanticPrimaryText
                                )
                            }

                            Icon(
                                imageVector = Icons.Outlined.Restore,
                                contentDescription = null,
                                tint = Color.semanticPrimaryAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        HorizontalRowDivider()

                        Column {
                            Text(
                                text = "YOUR BACKUP CONTAINS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Color.semanticMutedText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Prayer history · Dhikr · Quran · Preferences",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.semanticSecondaryText
                            )
                        }

                        Text(
                            text = "Your backup is encrypted before it is stored in Google Drive.",
                            fontSize = 12.sp,
                            color = Color.semanticMutedText,
                            lineHeight = 16.sp
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.semanticSurfaceElevated)
                        .border(1.dp, Color.semanticBorder, RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.CloudDone,
                                contentDescription = null,
                                tint = Color.semanticPrimaryAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Fresh Setup",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.semanticPrimaryText
                            )
                        }
                        Text(
                            text = "No previous FiveLight backup was found for this account. Continue with a fresh setup.",
                            fontSize = 13.sp,
                            color = Color.semanticSecondaryText,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Actions Bottom Section
        Column {
            if (isAccountMismatch) {
                PrimaryOnboardingButton(
                    text = "Connect Matching Drive Account",
                    onClick = {
                        val signInClient = GoogleDriveService.getGoogleSignInClient(context)
                        signInClient.signOut().addOnCompleteListener {
                            driveAuthLauncher.launch(signInClient.signInIntent)
                        }
                    },
                    testTag = "switch_google_drive_account_button"
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("start_fresh_mismatch_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = SolidColor(Color.semanticBorder)
                    )
                ) {
                    Text(
                        text = "Skip for now",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.semanticPrimaryText
                    )
                }
            } else if (restoreState == "SUCCESS") {
                PrimaryOnboardingButton(
                    text = "Continue",
                    onClick = onContinue,
                    testTag = "continue_after_restore_success_button"
                )
            } else if (restoreState == "FAILURE") {
                PrimaryOnboardingButton(
                    text = "Try again",
                    onClick = { startRestore() },
                    testTag = "retry_restore_button"
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("start_fresh_after_fail_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = SolidColor(Color.semanticBorder)
                    )
                ) {
                    Text(
                        text = "Skip for now",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.semanticPrimaryText
                    )
                }
            } else if (restoreState == "RESTORING") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.semanticPrimaryAccent,
                        strokeWidth = 2.5.dp
                    )
                }
            } else if (backupFound) {
                PrimaryOnboardingButton(
                    text = "Restore",
                    onClick = { startRestore() },
                    testTag = "restore_my_data_button"
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("start_fresh_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = SolidColor(Color.semanticBorder)
                    )
                ) {
                    Text(
                        text = "Skip for now",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.semanticPrimaryText
                    )
                }
            } else {
                PrimaryOnboardingButton(
                    text = "Continue",
                    onClick = onContinue,
                    testTag = "continue_no_backup_button"
                )
            }
        }
    }
}

@Composable
private fun RestoreProgressItem(
    label: String,
    isComplete: Boolean,
    isInProgress: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.semanticPrimaryText
        )

        if (isComplete) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "$label completed",
                tint = Color.semanticSuccess,
                modifier = Modifier.size(18.dp)
            )
        } else if (isInProgress) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = Color.semanticPrimaryAccent,
                strokeWidth = 2.dp
            )
        } else {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color.semanticBorder)
            )
        }
    }
}

/**
 * Step 3: Location Moment "WHERE ARE YOU?"
 * Contextual auto-advance upon verified location resolution.
 */
@Composable
private fun WhereAreYouStep(
    viewModel: AppViewModel,
    selectedCity: CityLocation,
    hasLocationPermission: Boolean,
    onPermissionUpdated: (Boolean) -> Unit,
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isDetectingLocation by remember { mutableStateOf(false) }
    var locationState by remember { mutableStateOf(if (hasLocationPermission) "RESOLVED" else "INITIAL") }
    var showCityPickerSheet by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun resolveCurrentGPS(triggerAutoAdvance: Boolean = false) {
        isDetectingLocation = true
        errorMessage = null
        try {
            val location = LocationHelper.getLastKnownLocation(context)
            if (location != null) {
                val resolved = LocationHelper.resolveCityLocation(context, location)
                viewModel.autoConfigureFromLocation(resolved)
                locationState = "RESOLVED"
            } else {
                locationState = "RESOLVED"
            }

            if (triggerAutoAdvance) {
                scope.launch {
                    delay(350)
                    onContinue()
                }
            }
        } catch (_: Exception) {
            locationState = "UNAVAILABLE"
            errorMessage = "Location unavailable. Please choose your city manually."
        } finally {
            isDetectingLocation = false
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val granted = fineGranted || coarseGranted

        onPermissionUpdated(granted)
        if (granted) {
            resolveCurrentGPS(triggerAutoAdvance = true)
        } else {
            locationState = "DENIED"
        }
    }

    var hasAutoRequestedPermission by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(hasLocationPermission) {
        if (!hasLocationPermission && !hasAutoRequestedPermission) {
            hasAutoRequestedPermission = true
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else if (hasLocationPermission && locationState == "INITIAL") {
            resolveCurrentGPS(triggerAutoAdvance = false)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Where are you?",
                fontFamily = InstrumentSerifItalic,
                fontSize = 30.sp,
                color = Color.semanticPrimaryText,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "FiveLight uses your location to calculate accurate prayer times and Qibla direction.",
                fontSize = 15.sp,
                color = Color.semanticSecondaryText,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (locationState) {
                "RESOLVED" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.semanticSurfaceElevated)
                            .border(1.dp, Color.semanticSuccess.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "YOUR LOCATION",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = Color.semanticMutedText
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = Color.semanticSuccess,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Location found",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.semanticSuccess
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "${selectedCity.cityName}, ${selectedCity.countryName}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.semanticPrimaryText
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Prayer methods, Hijri convention, and solar angles auto-configured for your coordinates.",
                                fontSize = 13.sp,
                                color = Color.semanticMutedText
                            )
                        }
                    }
                }

                "DENIED" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.semanticSurfaceElevated)
                            .border(1.dp, Color.semanticWarning.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Warning,
                                    contentDescription = null,
                                    tint = Color.semanticWarning,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Location access needed",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.semanticPrimaryText
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Location permission is required for automatic solar calculations. You can retry or choose your city manually.",
                                fontSize = 13.sp,
                                color = Color.semanticSecondaryText,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                "UNAVAILABLE" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.semanticSurfaceElevated)
                            .border(1.dp, Color.semanticError.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = Color.semanticError,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Location unavailable",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.semanticPrimaryText
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = errorMessage ?: "Unable to acquire location. Please choose your city manually.",
                                fontSize = 13.sp,
                                color = Color.semanticSecondaryText
                            )
                        }
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.semanticSurfaceElevated)
                            .border(1.dp, Color.semanticBorder, RoundedCornerShape(16.dp))
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color.semanticPrimaryAccent.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.MyLocation,
                                    contentDescription = null,
                                    tint = Color.semanticPrimaryAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Automatic & Private",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.semanticPrimaryText
                                )
                                Text(
                                    text = "Stored exclusively on device",
                                    fontSize = 13.sp,
                                    color = Color.semanticMutedText
                                )
                            }
                        }
                    }
                }
            }
        }

        // Actions
        Column {
            if (locationState == "RESOLVED") {
                PrimaryOnboardingButton(
                    text = "Continue to Prayer Times",
                    onClick = onContinue,
                    testTag = "continue_to_prayer_times_button"
                )

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(
                    onClick = { showCityPickerSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("change_city_button")
                ) {
                    Text(
                        text = "Change city manually",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.semanticPrimaryAccent
                    )
                }
            } else {
                Button(
                    onClick = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("use_my_location_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.semanticPrimaryAccent,
                        contentColor = Color.semanticAccentForeground
                    )
                ) {
                    if (isDetectingLocation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.semanticAccentForeground,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.MyLocation,
                                contentDescription = null,
                                tint = Color.semanticAccentForeground,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (locationState == "DENIED") "Try Again" else "Use my location",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.semanticAccentForeground
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { showCityPickerSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("choose_city_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = SolidColor(Color.semanticBorder)
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Place,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.semanticSecondaryText
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Choose a city",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.semanticPrimaryText
                        )
                    }
                }

                if (locationState == "DENIED") {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.parse("package:${context.packageName}")
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Open System Settings",
                            fontSize = 13.sp,
                            color = Color.semanticMutedText
                        )
                    }
                }
            }
        }
    }

    if (showCityPickerSheet) {
        CityPickerBottomSheet(
            viewModel = viewModel,
            selectedCity = selectedCity,
            onCitySelected = { chosenCity ->
                viewModel.autoConfigureFromLocation(chosenCity)
                locationState = "RESOLVED"
                showCityPickerSheet = false
            },
            onDismiss = { showCityPickerSheet = false }
        )
    }
}

/**
 * Step 4: Combined Prayer Times Screen with clean reusable recommended badges.
 */
@Composable
private fun CombinedPrayerScreen(
    viewModel: AppViewModel,
    selectedCity: CityLocation,
    calcMethod: CalcMethod,
    madhab: Madhab,
    hijriDateMethod: HijriDateMethod,
    onContinue: () -> Unit
) {
    var showCitySheet by remember { mutableStateOf(false) }
    var showCalcSheet by remember { mutableStateOf(false) }
    var showMadhabSheet by remember { mutableStateOf(false) }
    var showHijriSheet by remember { mutableStateOf(false) }

    val regionalDefaults = remember(selectedCity) {
        LocationHelper.determineRegionalPrayerSettings(selectedCity)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Prayer Times",
                fontFamily = InstrumentSerifItalic,
                fontSize = 30.sp,
                color = Color.semanticPrimaryText,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Configured for ${selectedCity.cityName}, ${selectedCity.countryName}",
                fontSize = 15.sp,
                color = Color.semanticSecondaryText
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.semanticSurfaceElevated)
                    .border(1.dp, Color.semanticBorder, RoundedCornerShape(16.dp))
            ) {
                Column {
                    SelectablePrayerRow(
                        title = "Location",
                        value = "${selectedCity.cityName}, ${selectedCity.countryName}",
                        subtitle = "Solar coordinates and timezone",
                        testTag = "row_location",
                        onClick = { showCitySheet = true }
                    )

                    HorizontalRowDivider()

                    SelectablePrayerRow(
                        title = "Calculation method",
                        value = calcMethod.displayName,
                        subtitle = regionalDefaults.calcMethodRecommendation,
                        isRecommended = calcMethod == regionalDefaults.calcMethod,
                        testTag = "row_calc_method",
                        onClick = { showCalcSheet = true }
                    )

                    HorizontalRowDivider()

                    SelectablePrayerRow(
                        title = "Asr calculation",
                        value = madhab.displayName,
                        subtitle = regionalDefaults.madhabRecommendation,
                        isRecommended = madhab == regionalDefaults.madhab,
                        testTag = "row_madhab",
                        onClick = { showMadhabSheet = true }
                    )

                    HorizontalRowDivider()

                    SelectablePrayerRow(
                        title = "Hijri calendar",
                        value = hijriDateMethod.displayName,
                        subtitle = regionalDefaults.hijriMethodRecommendation,
                        isRecommended = hijriDateMethod == regionalDefaults.hijriDateMethod,
                        testTag = "row_hijri_method",
                        onClick = { showHijriSheet = true }
                    )
                }
            }
        }

        PrimaryOnboardingButton(
            text = "Continue",
            onClick = onContinue,
            testTag = "continue_to_notifications_button"
        )
    }

    if (showCitySheet) {
        CityPickerBottomSheet(
            viewModel = viewModel,
            selectedCity = selectedCity,
            onCitySelected = { city ->
                viewModel.autoConfigureFromLocation(city)
                showCitySheet = false
            },
            onDismiss = { showCitySheet = false }
        )
    }

    if (showCalcSheet) {
        CalcMethodBottomSheet(
            selectedMethod = calcMethod,
            recommendedMethod = regionalDefaults.calcMethod,
            onMethodSelected = {
                viewModel.setCalcMethod(it)
                showCalcSheet = false
            },
            onDismiss = { showCalcSheet = false }
        )
    }

    if (showMadhabSheet) {
        MadhabBottomSheet(
            selectedMadhab = madhab,
            recommendedMadhab = regionalDefaults.madhab,
            onMadhabSelected = {
                viewModel.setMadhab(it)
                showMadhabSheet = false
            },
            onDismiss = { showMadhabSheet = false }
        )
    }

    if (showHijriSheet) {
        HijriMethodBottomSheet(
            selectedMethod = hijriDateMethod,
            recommendedMethod = regionalDefaults.hijriDateMethod,
            onMethodSelected = {
                viewModel.setHijriDateMethod(it)
                showHijriSheet = false
            },
            onDismiss = { showHijriSheet = false }
        )
    }
}

@Composable
private fun SelectablePrayerRow(
    title: String,
    value: String,
    subtitle: String,
    isRecommended: Boolean = false,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = androidx.compose.material3.ripple(),
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag(testTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.semanticPrimaryText,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (isRecommended) {
                    RecommendedChip()
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.semanticSecondaryText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color.semanticMutedText
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = "Edit",
            tint = Color.semanticMutedText,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun HorizontalRowDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.semanticBorder)
    )
}

/**
 * Step 5: Notifications Screen "STAY ON TIME"
 * Contextual auto-advance when permission is granted.
 */
@Composable
private fun StayOnTimeStep(
    notificationManager: SmartPrayerNotificationManager,
    hasNotificationPermission: Boolean,
    onPermissionUpdated: (Boolean) -> Unit,
    onContinue: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var preReminder by remember { mutableStateOf(notificationManager.preReminderOffset) }
    var permissionDeniedState by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onPermissionUpdated(isGranted)
        if (isGranted) {
            notificationManager.isSmartNotificationsEnabled = true
            notificationManager.isPrayerTimeNotificationsEnabled = true
            permissionDeniedState = false
            scope.launch {
                delay(350)
                onContinue()
            }
        } else {
            permissionDeniedState = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Stay on time",
                fontFamily = InstrumentSerifItalic,
                fontSize = 30.sp,
                color = Color.semanticPrimaryText,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Allow FiveLight to notify you when prayer times arrive.",
                fontSize = 15.sp,
                color = Color.semanticSecondaryText,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.semanticSurfaceElevated)
                    .border(
                        1.dp,
                        if (hasNotificationPermission) Color.semanticSuccess.copy(alpha = 0.4f) else Color.semanticBorder,
                        RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (hasNotificationPermission) Color.semanticSuccess.copy(alpha = 0.12f)
                                else Color.semanticPrimaryAccent.copy(alpha = 0.12f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (hasNotificationPermission) Icons.Outlined.NotificationsActive else Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = if (hasNotificationPermission) Color.semanticSuccess else Color.semanticPrimaryAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = if (hasNotificationPermission) "Prayer reminders" else "Notifications required",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.semanticPrimaryText
                        )
                        Text(
                            text = if (hasNotificationPermission) "✓ Notifications enabled" else "Not enabled yet",
                            fontSize = 13.sp,
                            fontWeight = if (hasNotificationPermission) FontWeight.Medium else FontWeight.Normal,
                            color = if (hasNotificationPermission) Color.semanticSuccess else Color.semanticMutedText
                        )
                    }
                }
            }

            if (permissionDeniedState && !hasNotificationPermission) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.semanticSurfaceElevated)
                        .border(1.dp, Color.semanticWarning.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Notifications are off",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.semanticPrimaryText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Prayer reminders won't appear until notifications are enabled.",
                            fontSize = 12.sp,
                            color = Color.semanticSecondaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "PRE-PRAYER REMINDER",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color.semanticMutedText
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PrePrayerReminderOffset.entries.forEach { offset ->
                    val isSelected = preReminder == offset
                    val animatedBg by animateColorAsState(
                        targetValue = if (isSelected) Color.semanticPrimaryAccent else Color.semanticSurfaceElevated,
                        animationSpec = tween(220),
                        label = "OffsetBg"
                    )
                    val animatedBorder by animateColorAsState(
                        targetValue = if (isSelected) Color.semanticPrimaryAccent else Color.semanticBorder,
                        animationSpec = tween(220),
                        label = "OffsetBorder"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(animatedBg)
                            .border(1.dp, animatedBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                preReminder = offset
                                notificationManager.preReminderOffset = offset
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = offset.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.semanticAccentForeground else Color.semanticPrimaryText
                        )
                    }
                }
            }
        }

        // Action Buttons
        Column {
            if (!hasNotificationPermission) {
                PrimaryOnboardingButton(
                    text = if (permissionDeniedState) "Try again" else "Allow notifications",
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            notificationManager.isSmartNotificationsEnabled = true
                            notificationManager.isPrayerTimeNotificationsEnabled = true
                            onPermissionUpdated(true)
                            onContinue()
                        }
                    },
                    testTag = "allow_notifications_button"
                )

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("continue_without_notifications_button")
                ) {
                    Text(
                        text = "Continue for now",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.semanticSecondaryText
                    )
                }
            } else {
                PrimaryOnboardingButton(
                    text = "Continue",
                    onClick = onContinue,
                    testTag = "continue_notifications_button"
                )
            }
        }
    }
}

/**
 * Step 6: Background Delivery Screen "KEEP REMINDERS RELIABLE"
 * Refined editorial typography to prevent awkward line breaks on narrow devices.
 */
@Composable
private fun KeepReliableStep(
    isBackgroundExempt: Boolean,
    onStateUpdated: (Boolean) -> Unit,
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    var currentExempt by remember { mutableStateOf(isBackgroundExempt) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val status = checkBackgroundOptimization(context)
                currentExempt = status
                onStateUpdated(status)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Keep reminders reliable",
                fontFamily = InstrumentSerifItalic,
                fontSize = 28.sp,
                color = Color.semanticPrimaryText,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "FiveLight needs reliable background delivery so scheduled prayer reminders can arrive on time.",
                fontSize = 15.sp,
                color = Color.semanticSecondaryText,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.semanticSurfaceElevated)
                    .border(
                        1.dp,
                        if (currentExempt) Color.semanticSuccess.copy(alpha = 0.4f) else Color.semanticBorder,
                        RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (currentExempt) Color.semanticSuccess.copy(alpha = 0.12f)
                                else Color.semanticPrimaryAccent.copy(alpha = 0.12f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (currentExempt) Icons.Outlined.CheckCircle else Icons.Outlined.BatteryAlert,
                            contentDescription = null,
                            tint = if (currentExempt) Color.semanticSuccess else Color.semanticPrimaryAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Background delivery",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.semanticPrimaryText
                        )
                        Text(
                            text = if (currentExempt) "✓ Background delivery enabled" else "Currently restricted by system battery optimization",
                            fontSize = 13.sp,
                            fontWeight = if (currentExempt) FontWeight.Medium else FontWeight.Normal,
                            color = if (currentExempt) Color.semanticSuccess else Color.semanticMutedText
                        )
                    }
                }
            }

            if (!currentExempt) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.semanticSurfaceElevated)
                        .border(1.dp, Color.semanticBorder, RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Android battery savers may defer prayer reminders unless FiveLight is allowed to deliver in the background.",
                        fontSize = 13.sp,
                        color = Color.semanticSecondaryText,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Column {
            if (!currentExempt) {
                PrimaryOnboardingButton(
                    text = "Allow Background Delivery",
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            try {
                                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                try {
                                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                        }
                    },
                    testTag = "enable_background_button"
                )

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("continue_background_button")
                ) {
                    Text(
                        text = "Continue for now",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.semanticSecondaryText
                    )
                }
            } else {
                PrimaryOnboardingButton(
                    text = "Continue",
                    onClick = onContinue,
                    testTag = "continue_background_enabled_button"
                )
            }
        }
    }
}

data class AutoBackupOption(
    val freq: BackupManager.AutoBackupFrequency,
    val title: String,
    val description: String,
    val isRecommended: Boolean = false
)

/**
 * Step 7: Automatic Backup Step (Animated Selection Group).
 *
 * Requirements:
 * - Unselected options start in a COMPACT/COLLAPSED state (~48dp height).
 * - Tapped option becomes SELECTED and smoothly expands via animateContentSize() to reveal description.
 * - Single-line FiveLightRecommendedBadge with crisp white text in Dark mode.
 * - Smooth 200–300ms animated transition for card size, background, and radio indicator.
 */
@Composable
private fun AutomaticBackupStep(
    viewModel: AppViewModel,
    currentUser: FirebaseUser?,
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var driveAccount by remember { mutableStateOf(GoogleDriveService.getAuthorizedAccount(context)) }
    var selectedFrequency by remember { mutableStateOf(BackupManager.getAutoBackupFrequency(context)) }
    var lastBackupTime by remember { mutableLongStateOf(BackupManager.getLastBackupTime(context)) }
    var isBackingUpNow by remember { mutableStateOf(false) }

    var hasAutoTriggeredDrive by rememberSaveable { mutableStateOf(false) }

    val driveAuthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null && GoogleSignIn.hasPermissions(account, GoogleDriveService.DRIVE_APPDATA_SCOPE)) {
                driveAccount = account
                Toast.makeText(context, "Google Drive connected: ${account.email}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Google Drive connection cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    // Automatically detect and initialize existing Google Drive authorization or trigger connect flow
    LaunchedEffect(Unit) {
        val account = GoogleDriveService.getAuthorizedAccount(context)
        if (account != null && GoogleSignIn.hasPermissions(account, GoogleDriveService.DRIVE_APPDATA_SCOPE)) {
            driveAccount = account
        } else if (!hasAutoTriggeredDrive) {
            hasAutoTriggeredDrive = true
            try {
                val signInClient = GoogleDriveService.getGoogleSignInClient(context)
                driveAuthLauncher.launch(signInClient.signInIntent)
            } catch (_: Exception) {}
        }
    }

    val formattedLastBackup = remember(lastBackupTime) {
        if (lastBackupTime > 0) {
            try {
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(lastBackupTime))
            } catch (_: Exception) {
                "Not backed up yet"
            }
        } else {
            "Not backed up yet"
        }
    }

    val backupOptions = remember {
        listOf(
            AutoBackupOption(
                freq = BackupManager.AutoBackupFrequency.WEEKLY,
                title = "Weekly",
                description = "Back up once every 7 days in the background",
                isRecommended = true
            ),
            AutoBackupOption(
                freq = BackupManager.AutoBackupFrequency.DAILY,
                title = "Daily",
                description = "Back up once every 24 hours in the background",
                isRecommended = false
            ),
            AutoBackupOption(
                freq = BackupManager.AutoBackupFrequency.OFF,
                title = "Off",
                description = "Manual backups only",
                isRecommended = false
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Automatic Backup",
                fontFamily = InstrumentSerifItalic,
                fontSize = 30.sp,
                color = Color.semanticPrimaryText,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Keep an encrypted backup of your FiveLight data in your private Google Drive.",
                fontSize = 15.sp,
                color = Color.semanticSecondaryText
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.semanticSurfaceElevated)
                    .border(1.dp, Color.semanticBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Google Drive",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.semanticPrimaryText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (driveAccount != null) "Connected (${driveAccount?.email ?: ""})"
                                else "Your backup will be stored securely in your Google Drive.",
                                fontSize = 13.sp,
                                color = if (driveAccount != null) Color.semanticSuccess else Color.semanticMutedText
                            )
                        }

                        if (driveAccount == null) {
                            Button(
                                onClick = {
                                    val client = GoogleDriveService.getGoogleSignInClient(context)
                                    client.signOut().addOnCompleteListener {
                                        driveAuthLauncher.launch(client.signInIntent)
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.semanticPrimaryAccent,
                                    contentColor = Color.semanticAccentForeground
                                ),
                                modifier = Modifier.testTag("connect_google_drive_button")
                            ) {
                                Text(
                                    text = "Connect",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.semanticAccentForeground
                                )
                            }
                        }
                    }

                    HorizontalRowDivider()

                    // Animated Selection Group: compact unselected items, smooth accordion expansion when selected
                    Column {
                        Text(
                            text = "BACKUP FREQUENCY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color.semanticMutedText
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            backupOptions.forEach { option ->
                                val isSelected = selectedFrequency == option.freq
                                AnimatedBackupOptionRow(
                                    option = option,
                                    isSelected = isSelected,
                                    onClick = {
                                        selectedFrequency = option.freq
                                        BackupManager.setAutoBackupFrequency(context, option.freq)
                                    }
                                )
                            }
                        }
                    }

                    HorizontalRowDivider()

                    // Manual Backup and Last Backup
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LAST BACKUP",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Color.semanticMutedText
                            )
                            Text(
                                text = formattedLastBackup,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.semanticPrimaryText
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                if (!isBackingUpNow) {
                                    isBackingUpNow = true
                                    scope.launch {
                                        val res = viewModel.performDriveBackup()
                                        isBackingUpNow = false
                                        if (res.isSuccess) {
                                            lastBackupTime = res.getOrNull() ?: System.currentTimeMillis()
                                            Toast.makeText(context, "Backup completed successfully", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Backup failed: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            },
                            enabled = !isBackingUpNow && driveAccount != null,
                            shape = RoundedCornerShape(10.dp),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = driveAccount != null).copy(
                                brush = SolidColor(Color.semanticBorder)
                            ),
                            modifier = Modifier.testTag("backup_now_button")
                        ) {
                            if (isBackingUpNow) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.semanticPrimaryAccent
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.CloudUpload,
                                        contentDescription = null,
                                        tint = if (driveAccount != null) Color.semanticPrimaryText else Color.semanticMutedText,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Back Up Now",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (driveAccount != null) Color.semanticPrimaryText else Color.semanticMutedText
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        PrimaryOnboardingButton(
            text = "Continue",
            onClick = onContinue,
            testTag = "continue_backup_step_button"
        )
    }
}

/**
 * Animated Expandable Option Card for Backup Frequency Selection.
 */
@Composable
private fun AnimatedBackupOptionRow(
    option: AutoBackupOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedBgColor by animateColorAsState(
        targetValue = if (isSelected) Color.semanticPrimaryAccent.copy(alpha = 0.08f) else Color.semanticBackground,
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "BackupOptionBg"
    )
    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) Color.semanticPrimaryAccent else Color.semanticBorder,
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "BackupOptionBorder"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(animatedBgColor)
            .border(1.dp, animatedBorderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .animateContentSize(animationSpec = tween(250, easing = FastOutSlowInEasing))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = option.title,
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = Color.semanticPrimaryText
                    )
                    if (option.isRecommended) {
                        RecommendedChip()
                    }
                }

                // Expandable Description: only rendered and visible when selected!
                if (isSelected) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = option.description,
                        fontSize = 12.sp,
                        color = Color.semanticSecondaryText,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            FiveLightAnimatedRadio(
                selected = isSelected,
                onClick = onClick
            )
        }
    }
}

/**
 * Step 8: Tasbeeh & Haptics Feedback.
 */
@Composable
private fun TasbeehHapticsStep(
    viewModel: AppViewModel,
    tasbeehSound: TasbeehSound,
    vibrationEnabled: Boolean,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Dhikr & Haptics",
                fontFamily = InstrumentSerifItalic,
                fontSize = 30.sp,
                color = Color.semanticPrimaryText,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Customize audio taps and tactile feedback during daily dhikr recitation.",
                fontSize = 15.sp,
                color = Color.semanticSecondaryText
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.semanticSurfaceElevated)
                    .border(1.dp, Color.semanticBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Tactile Vibration",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.semanticPrimaryText
                            )
                            Text(
                                text = "Subtle haptic tick upon each count",
                                fontSize = 13.sp,
                                color = Color.semanticMutedText
                            )
                        }

                        Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = { viewModel.setVibrationEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color.semanticPrimaryAccent,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.semanticBorder
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "AUDIO FEEDBACK",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color.semanticMutedText
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TasbeehSound.entries.forEach { sound ->
                            val isSelected = tasbeehSound == sound
                            SelectableOptionRow(
                                title = sound.displayName,
                                subtitle = sound.description,
                                isSelected = isSelected,
                                onClick = { viewModel.setTasbeehSound(sound) }
                            )
                        }
                    }
                }
            }
        }

        PrimaryOnboardingButton(
            text = "Continue",
            onClick = onContinue,
            testTag = "continue_haptics_button"
        )
    }
}

/**
 * Step 9: Final Screen "YOU'RE READY".
 * Shows only verified confirmations based on actual system state.
 */
@Composable
private fun AppearanceAndReadyStep(
    viewModel: AppViewModel,
    appearanceMode: AppearanceMode,
    selectedCity: CityLocation,
    calcMethod: CalcMethod,
    hasLocationPermission: Boolean,
    hasNotificationPermission: Boolean,
    isBackgroundExempt: Boolean,
    isRestoreRestored: Boolean,
    effectiveUser: FirebaseUser?,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val autoBackupFreq = remember { BackupManager.getAutoBackupFrequency(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "You're ready.",
                fontFamily = InstrumentSerifItalic,
                fontSize = 32.sp,
                color = Color.semanticPrimaryText,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "FiveLight is set up for you.",
                fontSize = 15.sp,
                color = Color.semanticSecondaryText
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Appearance Selector with High-Contrast Text
            Text(
                text = "APPEARANCE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color.semanticMutedText
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    AppearanceMode.SYSTEM to "System",
                    AppearanceMode.LIGHT to "Light",
                    AppearanceMode.DARK to "Dark"
                ).forEach { (mode, label) ->
                    val isSelected = appearanceMode == mode
                    val animatedBg by animateColorAsState(
                        targetValue = if (isSelected) Color.semanticPrimaryAccent else Color.semanticSurfaceElevated,
                        animationSpec = tween(220),
                        label = "AppearanceBg"
                    )
                    val animatedBorder by animateColorAsState(
                        targetValue = if (isSelected) Color.semanticPrimaryAccent else Color.semanticBorder,
                        animationSpec = tween(220),
                        label = "AppearanceBorder"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(animatedBg)
                            .border(1.dp, animatedBorder, RoundedCornerShape(12.dp))
                            .clickable { viewModel.setAppearanceMode(mode) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.semanticAccentForeground else Color.semanticPrimaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Verified Confirmation Checklist
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.semanticSurfaceElevated)
                    .border(1.dp, Color.semanticBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    VerifiedReadyItem(
                        text = "Prayer times for ${selectedCity.cityName}, ${selectedCity.countryName}"
                    )

                    VerifiedReadyItem(
                        text = "${calcMethod.displayName} method configured"
                    )

                    if (hasNotificationPermission) {
                        VerifiedReadyItem(
                            text = "Prayer reminders enabled"
                        )
                    }

                    if (isBackgroundExempt) {
                        VerifiedReadyItem(
                            text = "Background delivery configured"
                        )
                    }

                    if (isRestoreRestored) {
                        VerifiedReadyItem(
                            text = "Your previous data restored"
                        )
                    }

                    if (effectiveUser != null && autoBackupFreq != BackupManager.AutoBackupFrequency.OFF) {
                        VerifiedReadyItem(
                            text = "${autoBackupFreq.label} backup enabled"
                        )
                    }
                }
            }
        }

        PrimaryOnboardingButton(
            text = "Begin your journey →",
            onClick = onFinish,
            testTag = "complete_setup_button"
        )
    }
}

@Composable
private fun VerifiedReadyItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = Color.semanticSuccess,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.semanticPrimaryText
        )
    }
}

// -------------------------------------------------------------------------------------------------
// Modal Bottom Sheets with Reusable SelectableOptionRow, Recommended Badges & Animated Radios
// -------------------------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CityPickerBottomSheet(
    viewModel: AppViewModel,
    selectedCity: CityLocation? = null,
    onCitySelected: (CityLocation) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }
    val allCities = viewModel.repository.PREDEFINED_CITIES

    val filteredCities = remember(searchQuery) {
        if (searchQuery.isBlank()) allCities
        else allCities.filter {
            it.cityName.contains(searchQuery, ignoreCase = true) ||
                    it.countryName.contains(searchQuery, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.semanticSurfaceElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Select City",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.semanticPrimaryText
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.semanticBackground)
                    .border(1.dp, Color.semanticBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = Color.semanticMutedText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = Color.semanticPrimaryText
                        ),
                        cursorBrush = SolidColor(Color.semanticPrimaryAccent),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search city or country...",
                                    fontSize = 14.sp,
                                    color = Color.semanticMutedText
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredCities) { city ->
                    val isSelected = selectedCity != null &&
                            city.cityName.equals(selectedCity.cityName, ignoreCase = true) &&
                            city.countryName.equals(selectedCity.countryName, ignoreCase = true)

                    SelectableOptionRow(
                        title = city.cityName,
                        subtitle = city.countryName,
                        isSelected = isSelected,
                        onClick = { onCitySelected(city) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalcMethodBottomSheet(
    selectedMethod: CalcMethod,
    recommendedMethod: CalcMethod,
    onMethodSelected: (CalcMethod) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.semanticSurfaceElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Prayer Calculation Method",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.semanticPrimaryText
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CalcMethod.entries.forEach { method ->
                    val isSelected = selectedMethod == method
                    val isRec = recommendedMethod == method

                    SelectableOptionRow(
                        title = method.displayName,
                        subtitle = "Fajr: ${method.fajrAngle}° · Isha: ${method.ishaAngle}°",
                        isRecommended = isRec,
                        isSelected = isSelected,
                        onClick = { onMethodSelected(method) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MadhabBottomSheet(
    selectedMadhab: Madhab,
    recommendedMadhab: Madhab = Madhab.STANDARD,
    onMadhabSelected: (Madhab) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.semanticSurfaceElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Asr Calculation (Madhab)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.semanticPrimaryText
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Madhab.entries.forEach { m ->
                    val isSelected = selectedMadhab == m
                    SelectableOptionRow(
                        title = m.displayName,
                        subtitle = if (m == Madhab.HANAFI) "Asr enters when shadow is 2x object length"
                        else "Asr enters when shadow is 1x object length (Standard)",
                        isRecommended = m == recommendedMadhab,
                        isSelected = isSelected,
                        onClick = { onMadhabSelected(m) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HijriMethodBottomSheet(
    selectedMethod: HijriDateMethod,
    recommendedMethod: HijriDateMethod,
    onMethodSelected: (HijriDateMethod) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.semanticSurfaceElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Hijri Date Convention",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.semanticPrimaryText
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HijriDateMethod.entries.forEach { method ->
                    val isSelected = selectedMethod == method
                    val isRec = recommendedMethod == method

                    SelectableOptionRow(
                        title = method.displayName,
                        subtitle = method.description,
                        isRecommended = isRec,
                        isSelected = isSelected,
                        onClick = { onMethodSelected(method) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
