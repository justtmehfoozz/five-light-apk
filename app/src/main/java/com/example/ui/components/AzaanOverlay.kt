package com.example.ui.components

import com.example.ui.theme.semanticPrimaryAccent
import com.example.ui.theme.semanticAccentForeground
import com.example.ui.theme.semanticSuccess
import com.example.ui.theme.semanticError
import com.example.ui.theme.semanticSurface
import com.example.ui.theme.semanticSurfaceElevated
import com.example.ui.theme.semanticPrimaryText
import com.example.ui.theme.semanticMutedText
import com.example.ui.theme.semanticBorder
import com.example.ui.theme.semanticBackground
import com.example.ui.theme.semanticWarning


import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Snooze
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.model.PrayerName
import com.example.data.reminder.ActiveAzaanState
import com.example.data.reminder.PrayerReminderManager
import com.example.ui.theme.InstrumentSerif
import com.example.ui.theme.SpaceGrotesk
import kotlinx.coroutines.delay

@Composable
fun AzaanOverlay(
    activeState: ActiveAzaanState?,
    onDismiss: () -> Unit,
    onSnooze: (PrayerName) -> Unit
) {
    if (activeState == null) return

    val context = LocalContext.current
    val prayerName = activeState.prayerName

    // Setup Azaan MediaPlayer on loop with 3-minute max limit
    val mediaPlayer = remember {
        try {
            val attrs = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()
            MediaPlayer.create(context, R.raw.azaan, attrs, 0)?.apply {
                isLooping = true
                setOnErrorListener { mp, _, _ ->
                    try { mp.reset(); mp.release() } catch (_: Exception) {}
                    true
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    LaunchedEffect(activeState) {
        mediaPlayer?.start()
        // Auto-stop after 3 minutes (180 seconds)
        delay(180_000L)
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    val isDark = MaterialTheme.colorScheme.background.run { (red + green + blue) < 1.5f }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Dialog(
        onDismissRequest = {
            mediaPlayer?.stop()
            onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(com.example.ui.theme.AppBackground.copy(alpha = 0.94f))
                .padding(24.dp)
                .testTag("azaan_overlay")
        ) {
            // Close X button top right
            IconButton(
                onClick = {
                    mediaPlayer?.stop()
                    onDismiss()
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Dismiss",
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Animated Glowing Pulse Ring
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background((Color.semanticPrimaryAccent).copy(alpha = 0.13f))
                        .border(1.5.dp, Color.semanticPrimaryAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsActive,
                        contentDescription = "Prayer Alert",
                        tint = Color.semanticPrimaryAccent,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Time for Prayer",
                    fontFamily = SpaceGrotesk,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.semanticPrimaryAccent,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                val isFriday = remember { com.example.data.util.PrayerDisplayUtils.isFriday() }
                val displayName = remember(prayerName, isFriday) {
                    com.example.data.util.PrayerDisplayUtils.getPrayerDisplayName(prayerName, isFriday)
                }

                Text(
                    text = displayName,
                    fontFamily = InstrumentSerif,
                    fontSize = 48.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(12.dp))

                val subtext = remember(prayerName, isFriday) {
                    com.example.data.util.PrayerDisplayUtils.getPrayerPoeticSubtext(prayerName, isFriday)
                }

                Text(
                    text = "“$subtext”",
                    fontFamily = SpaceGrotesk,
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Action Buttons: Snooze & Dismiss
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            mediaPlayer?.stop()
                            onSnooze(prayerName)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("azaan_snooze_button"),
                        shape = RoundedCornerShape(26.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.semanticAccentForeground
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Snooze,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Snooze 5 min",
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = {
                            mediaPlayer?.stop()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("azaan_dismiss_button"),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.semanticPrimaryAccent,
                            contentColor = Color.semanticAccentForeground
                        )
                    ) {
                        Text(
                            text = "Dismiss",
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
