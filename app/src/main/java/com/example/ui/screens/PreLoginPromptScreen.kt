package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SerifHeaderFont
import com.example.ui.theme.semanticPrimaryAccent

/**
 * Screen 1: Pre-Login Prompt shown ONCE on first app open.
 * Contains a subtle sunrise/crescent atmospheric motif, calm poetic headline,
 * subtext, "Login or Register" primary button, and "Continue as Guest" plain text link.
 */
@Composable
fun PreLoginPromptScreen(
    onLoginOrRegister: () -> Unit,
    onContinueAsGuest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val goldAccent = Color(0xFF8D6B1E)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("pre_login_prompt_screen")
    ) {
        // Atmospheric Sunrise & Oversized Faint Crescent Motif (Canvas)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // 1. Subtle soft horizon gold glow near the top
            val glowBrush = Brush.radialGradient(
                colors = listOf(
                    goldAccent.copy(alpha = 0.14f),
                    goldAccent.copy(alpha = 0.05f),
                    Color.Transparent
                ),
                center = Offset(canvasWidth * 0.5f, canvasHeight * 0.18f),
                radius = canvasWidth * 0.85f
            )
            drawRect(brush = glowBrush)

            // 2. Faint oversized crescent outline (~8-10% opacity) positioned off-center in the background
            val crescentCenterX = canvasWidth * 0.72f
            val crescentCenterY = canvasHeight * 0.22f
            val outerRadius = canvasWidth * 0.32f
            val innerRadius = canvasWidth * 0.28f
            val innerOffsetX = canvasWidth * 0.09f
            val innerOffsetY = -canvasHeight * 0.02f

            val outerPath = Path().apply {
                addOval(
                    androidx.compose.ui.geometry.Rect(
                        center = Offset(crescentCenterX, crescentCenterY),
                        radius = outerRadius
                    )
                )
            }
            val innerPath = Path().apply {
                addOval(
                    androidx.compose.ui.geometry.Rect(
                        center = Offset(crescentCenterX + innerOffsetX, crescentCenterY + innerOffsetY),
                        radius = innerRadius
                    )
                )
            }

            val crescentPath = Path()
            crescentPath.op(outerPath, innerPath, PathOperation.Difference)

            // Draw subtle crescent outline stroke
            drawPath(
                path = crescentPath,
                color = goldAccent.copy(alpha = 0.09f),
                style = Stroke(width = 2.5f)
            )
        }

        // Foreground Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Central poetic copy section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(top = 80.dp)
            ) {
                Text(
                    text = "Keep your journey with you",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = SerifHeaderFont,
                        fontWeight = FontWeight.Normal,
                        fontSize = 28.sp,
                        lineHeight = 36.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("pre_login_headline")
                )

                Text(
                    text = "Sign in to sync your prayers, dhikr, and reflections across your devices.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .testTag("pre_login_subtext")
                )
            }

            // Bottom CTA Buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Button(
                    onClick = onLoginOrRegister,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.semanticPrimaryAccent,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("pre_login_primary_btn")
                ) {
                    Text(
                        text = "Login or Register",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    )
                }

                TextButton(
                    onClick = onContinueAsGuest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("pre_login_guest_btn")
                ) {
                    Text(
                        text = "Continue as Guest",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
