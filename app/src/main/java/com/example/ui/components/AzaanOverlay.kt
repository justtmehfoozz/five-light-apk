package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.PrayerName
import com.example.data.reminder.ActiveAzaanState
import com.example.data.reminder.AzaanAudioPlayer
import com.example.data.reminder.SmartPrayerNotificationManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class OverlayStage {
    ADHAN_PLAYING,
    DUA_AFTER_ADHAN,
    CLOSING
}

@Composable
fun AzaanOverlay(
    activeState: ActiveAzaanState?,
    onDismiss: () -> Unit,
    onSnooze: (PrayerName) -> Unit
) {
    if (activeState == null) return

    val context = LocalContext.current
    val prayerName = activeState.prayerName
    val isDark = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    // Check system reduced motion setting
    val isReducedMotion = remember(context) {
        try {
            val scale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            scale == 0f
        } catch (_: Exception) {
            false
        }
    }

    var stage by remember { mutableStateOf(OverlayStage.ADHAN_PLAYING) }
    var isOverlayVisible by remember { mutableStateOf(true) }

    // Live Date and Time
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val formattedDateTime = remember(currentTimeMillis) {
        val sdf = SimpleDateFormat("h:mm a • EEEE, MMM d", Locale.getDefault())
        sdf.format(Date(currentTimeMillis))
    }

    // Stop audio and transition to Dua
    val triggerStopAndShowDua = remember {
        {
            if (stage == OverlayStage.ADHAN_PLAYING) {
                AzaanAudioPlayer.stopAzaan()
                stage = OverlayStage.DUA_AFTER_ADHAN
            }
        }
    }

    // Register audio completion listener
    LaunchedEffect(activeState) {
        if (!AzaanAudioPlayer.isPlaying()) {
            AzaanAudioPlayer.playAzaan(context) {
                triggerStopAndShowDua()
            }
        }
    }

    // Colors - Strict Monochrome
    val bgColor = if (isDark) Color(0xFF000000) else Color(0xFFFFFFFF)
    val primaryTextColor = if (isDark) Color(0xFFFFFFFF) else Color(0xFF000000)
    val secondaryTextColor = if (isDark) Color(1f, 1f, 1f, 0.60f) else Color(0x3C / 255f, 0x3C / 255f, 0x43 / 255f, 0.60f)
    val tertiaryTextColor = if (isDark) Color(1f, 1f, 1f, 0.30f) else Color(0x3C / 255f, 0x3C / 255f, 0x43 / 255f, 0.30f)

    val primaryBtnBg = if (isDark) Color(0xFFFFFFFF) else Color(0xFF000000)
    val primaryBtnText = if (isDark) Color(0xFF000000) else Color(0xFFFFFFFF)

    // Radial Glow Pulse
    val infiniteTransition = rememberInfiniteTransition(label = "glow_pulse")
    val glowPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 0.90f,
        animationSpec = infiniteRepeatable(
            animation = tween(1750, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    val glowPulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    // Overlay Exit Animation
    val overlayAlpha = remember { Animatable(1f) }

    fun dismissOverlaySafely() {
        scope.launch {
            stage = OverlayStage.CLOSING
            AzaanAudioPlayer.stopAzaan()
            overlayAlpha.animateTo(0f, animationSpec = tween(700, easing = FastOutSlowInEasing))
            isOverlayVisible = false
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = { dismissOverlaySafely() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        if (isOverlayVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = overlayAlpha.value }
                    .background(bgColor)
                    .testTag("full_screen_adhan_overlay")
            ) {
                // Background Pattern: 8-Point Islamic Geometric Pattern
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val patternColor = if (isDark) Color.White.copy(alpha = 0.035f) else Color.Black.copy(alpha = 0.035f)
                    val step = 80.dp.toPx()
                    var x = 0f
                    while (x < size.width + step) {
                        var y = 0f
                        while (y < size.height + step) {
                            val r = 18.dp.toPx()
                            // Draw subtle 8-point star
                            val path = Path()
                            for (i in 0 until 8) {
                                val angle = i * Math.PI / 4
                                val outerX = (x + r * Math.cos(angle)).toFloat()
                                val outerY = (y + r * Math.sin(angle)).toFloat()
                                val innerAngle = angle + Math.PI / 8
                                val innerX = (x + (r * 0.5f) * Math.cos(innerAngle)).toFloat()
                                val innerY = (y + (r * 0.5f) * Math.sin(innerAngle)).toFloat()
                                if (i == 0) path.moveTo(outerX, outerY) else path.lineTo(outerX, outerY)
                                path.lineTo(innerX, innerY)
                            }
                            path.close()
                            drawPath(path, patternColor, style = Stroke(width = 1.dp.toPx()))
                            y += step
                        }
                        x += step
                    }
                }

                // Crescent & Mosque Silhouette at Bottom Edge
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    val silhouetteAlpha = if (isDark) 0.06f else 0.05f
                    val sColor = if (isDark) Color.White.copy(alpha = silhouetteAlpha) else Color.Black.copy(alpha = silhouetteAlpha)
                    val path = Path()
                    val w = size.width
                    val h = size.height

                    // Mosque Silhouette outline
                    path.moveTo(0f, h)
                    path.lineTo(0f, h * 0.6f)
                    path.cubicTo(w * 0.15f, h * 0.6f, w * 0.25f, h * 0.35f, w * 0.35f, h * 0.35f)
                    path.cubicTo(w * 0.45f, h * 0.35f, w * 0.48f, h * 0.15f, w * 0.5f, h * 0.15f) // Main Central Dome
                    path.cubicTo(w * 0.52f, h * 0.15f, w * 0.55f, h * 0.35f, w * 0.65f, h * 0.35f)
                    path.cubicTo(w * 0.75f, h * 0.35f, w * 0.85f, h * 0.6f, w, h * 0.6f)
                    path.lineTo(w, h)
                    path.close()
                    drawPath(path, sColor)
                }

                // Screen Safe Container
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Time & Date Header
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = formattedDateTime,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = secondaryTextColor,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.weight(0.15f))

                    // Content Crossfade State Switch
                    Crossfade(
                        targetState = stage,
                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                        label = "adhanContentCrossfade",
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) { currentStage ->
                        when (currentStage) {
                            OverlayStage.ADHAN_PLAYING -> {
                                AdhanPlayingContent(
                                    prayerName = prayerName,
                                    primaryTextColor = primaryTextColor,
                                    secondaryTextColor = secondaryTextColor,
                                    glowAlpha = if (isReducedMotion) 0.6f else glowPulseAlpha,
                                    glowScale = if (isReducedMotion) 1.0f else glowPulseScale,
                                    isDark = isDark,
                                    isReducedMotion = isReducedMotion
                                )
                            }

                            OverlayStage.DUA_AFTER_ADHAN, OverlayStage.CLOSING -> {
                                DuaAfterAdhanContent(
                                    primaryTextColor = primaryTextColor,
                                    secondaryTextColor = secondaryTextColor,
                                    tertiaryTextColor = tertiaryTextColor,
                                    isDark = isDark,
                                    onDone = {
                                        performSoftSuccessHaptic(context)
                                        dismissOverlaySafely()
                                    }
                                )
                            }
                        }
                    }

                    // Bottom Controls (Visible only during ADHAN_PLAYING)
                    if (stage == OverlayStage.ADHAN_PLAYING) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 34.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Primary: Stop Adhan Button
                            AdhanCapsuleButton(
                                label = "Stop Adhan",
                                containerColor = primaryBtnBg,
                                contentColor = primaryBtnText,
                                heightDp = 56,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("stop_adhan_button"),
                                onClick = {
                                    try { haptic.performHapticFeedback(HapticFeedbackType.LongPress) } catch (_: Exception) {}
                                    triggerStopAndShowDua()
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Secondary: Snooze 5 min Button
                            AdhanCapsuleButton(
                                label = "Snooze 5 min",
                                containerColor = Color.Transparent,
                                contentColor = secondaryTextColor,
                                heightDp = 48,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("snooze_adhan_button"),
                                onClick = {
                                    try { haptic.performHapticFeedback(HapticFeedbackType.LongPress) } catch (_: Exception) {}
                                    AzaanAudioPlayer.stopAzaan()
                                    SmartPrayerNotificationManager(context).scheduleSnooze(prayerName)
                                    onSnooze(prayerName)
                                    onDismiss()
                                }
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AdhanPlayingContent(
    prayerName: PrayerName,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    glowAlpha: Float,
    glowScale: Float,
    isDark: Boolean,
    isReducedMotion: Boolean
) {
    val romanUrduSentence = remember(prayerName) {
        when (prayerName) {
            PrayerName.FAJR -> "Fajr ka waqt ho gaya hai"
            PrayerName.DHUHR -> "Dhuhr ka waqt ho gaya hai"
            PrayerName.ASR -> "Asr ka waqt ho gaya hai"
            PrayerName.MAGHRIB -> "Maghrib ka waqt ho gaya hai"
            PrayerName.ISHA -> "Isha ka waqt ho gaya hai"
            PrayerName.SUNRISE -> "Sunrise ka waqt ho gaya hai"
        }
    }

    val isFriday = remember { com.example.data.util.PrayerDisplayUtils.isFriday() }
    val displayPrayerName = remember(prayerName, isFriday) {
        com.example.data.util.PrayerDisplayUtils.getPrayerDisplayName(prayerName, isFriday).uppercase()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Arabic Call to Prayer with Monochrome Soft Glow
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            // Soft Monochrome Radial Glow
            Canvas(
                modifier = Modifier
                    .size(240.dp)
                    .scale(glowScale)
                    .graphicsLayer { alpha = glowAlpha }
            ) {
                val glowColor = if (isDark) Color.White.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.10f)
                val brush = Brush.radialGradient(
                    colors = listOf(glowColor, Color.Transparent),
                    center = Offset(this.size.width / 2f, this.size.height / 2f),
                    radius = this.size.width / 2f
                )
                drawCircle(brush = brush)
            }

            Text(
                text = "اللّٰهُ أَكْبَرُ",
                fontSize = 68.sp,
                fontWeight = FontWeight.Normal,
                color = primaryTextColor,
                textAlign = TextAlign.Center,
                lineHeight = 100.sp,
                modifier = Modifier.testTag("adhan_arabic_text")
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Prayer Name with Staggered Letter Reveal or Clean Fade
        Text(
            text = displayPrayerName,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = primaryTextColor,
            letterSpacing = 6.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("adhan_prayer_name")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Roman Urdu Sentence
        Text(
            text = romanUrduSentence,
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal,
            color = secondaryTextColor,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun DuaAfterAdhanContent(
    primaryTextColor: Color,
    secondaryTextColor: Color,
    tertiaryTextColor: Color,
    isDark: Boolean,
    onDone: () -> Unit
) {
    val cardBg = if (isDark) Color(1f, 1f, 1f, 0.08f) else Color(0xF2 / 255f, 0xF2 / 255f, 0xF7 / 255f, 0.85f)
    val cardBorder = if (isDark) Color(1f, 1f, 1f, 0.12f) else Color(0x3C / 255f, 0x3C / 255f, 0x43 / 255f, 0.12f)
    val dividerColor = if (isDark) Color(1f, 1f, 1f, 0.10f) else Color(0x3C / 255f, 0x3C / 255f, 0x43 / 255f, 0.10f)

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Category Header
        Text(
            text = "DUA AFTER ADHAN",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = secondaryTextColor,
            letterSpacing = 1.5.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Frosted / Translucent Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .clip(RoundedCornerShape(28.dp))
                .border(1.dp, cardBorder, RoundedCornerShape(28.dp))
                .testTag("dua_after_adhan_card"),
            color = cardBg,
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Arabic Dua
                Text(
                    text = "اللَّهُمَّ رَبَّ هَذِهِ الدَّعْوَةِ التَّامَّةِ وَالصَّلَاةِ الْقَائِمَةِ آتِ مُحَمَّدًا الْوَسِيلَةَ وَالْفَضِيلَةَ وَابْعَثْهُ مَقَامًا مَحْمُودًا الَّذِي وَعَدْتَهُ",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Normal,
                    color = primaryTextColor,
                    textAlign = TextAlign.Center,
                    lineHeight = 44.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // 2. Transliteration
                Text(
                    text = "“Allahumma rabba hadhihid-da'watit-tammah, was-salatil-qa'imah, ati Muhammadanil-wasilata wal-fadilah, wab'ath-hu maqaman mahmudanil-ladhi wa'adtah”",
                    fontSize = 15.sp,
                    fontStyle = FontStyle.Italic,
                    color = secondaryTextColor,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // 3. English Meaning
                Text(
                    text = "O Allah, Lord of this perfect call and the prayer about to be established, grant Muhammad ﷺ the Wasilah and the Fadilah, and raise him to the praised station that You have promised him.",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = primaryTextColor,
                    textAlign = TextAlign.Start,
                    lineHeight = 22.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Roman Urdu Meaning
                Text(
                    text = "Ae Allah, is mukammal pukar aur qaim hone wali namaz ke Rab, Muhammad ﷺ ko Wasilah aur Fazilat ata farma, aur unhe woh Maqam-e-Mahmood ata farma jiska tune unse wada kiya hai.",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = secondaryTextColor,
                    textAlign = TextAlign.Start,
                    lineHeight = 22.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // 5. Short Reflection
                Text(
                    text = "Adhan sunne ke baad yeh dua padhna Sunnah hai. Hadith (Sahih al-Bukhari 614) ke mutabiq jo yeh dua padhe, uske liye Qayamat ke din Nabi ﷺ ki shafa'at wajib ho jaati hai.",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = secondaryTextColor,
                    textAlign = TextAlign.Start,
                    lineHeight = 18.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Done Button
        AdhanCapsuleButton(
            label = "Done",
            containerColor = if (isDark) Color(0xFFFFFFFF) else Color(0xFF000000),
            contentColor = if (isDark) Color(0xFF000000) else Color(0xFFFFFFFF),
            heightDp = 56,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dua_done_button"),
            onClick = onDone
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun AdhanCapsuleButton(
    label: String,
    containerColor: Color,
    contentColor: Color,
    heightDp: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scaleAnim by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 400f),
        label = "btnScale"
    )

    Surface(
        modifier = modifier
            .height(heightDp.dp)
            .scale(scaleAnim)
            .clip(CircleShape)
            .clickable {
                isPressed = true
                onClick()
            },
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}

private fun performSoftSuccessHaptic(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            val vibrator = vm?.defaultVibrator
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(50)
            }
        }
    } catch (_: Exception) {}
}
