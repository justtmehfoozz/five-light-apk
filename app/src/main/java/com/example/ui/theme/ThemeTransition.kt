package com.example.ui.theme

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.compose.ui.unit.IntSize
import com.example.data.model.AppearanceMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.hypot
import kotlin.math.roundToInt

val RadialRevealEasing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
const val THEME_TRANSITION_DURATION_MS = 400

val LocalThemeTransitionController = compositionLocalOf<ThemeTransitionController> {
    ThemeTransitionController()
}

@Composable
fun isSystemReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        try {
            val durationScale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            durationScale == 0f
        } catch (e: Throwable) {
            false
        }
    }
}

@Stable
class ThemeTransitionController {
    var isTransitioning by mutableStateOf(false)
        private set

    var isCapturing by mutableStateOf(false)
        private set

    var progress by mutableFloatStateOf(1f)
        private set

    var origin by mutableStateOf(Offset.Zero)
        private set

    var effectiveAppearanceMode by mutableStateOf<AppearanceMode?>(null)
        private set

    var transitionGeneration by mutableIntStateOf(0)
        private set

    private var animationJob: Job? = null
    private var coroutineScope: CoroutineScope? = null

    fun attachScope(scope: CoroutineScope) {
        this.coroutineScope = scope
    }

    fun startTransition(
        targetMode: AppearanceMode,
        currentMode: AppearanceMode,
        tapOrigin: Offset,
        isReducedMotion: Boolean = false,
        isEffectiveThemeChanging: Boolean = true,
        onThemeApplied: ((AppearanceMode) -> Unit)? = null
    ) {
        if (!isEffectiveThemeChanging) {
            effectiveAppearanceMode = targetMode
            onThemeApplied?.invoke(targetMode)
            return
        }

        transitionGeneration++
        val gen = transitionGeneration
        animationJob?.cancel()

        origin = tapOrigin

        if (isReducedMotion) {
            isCapturing = false
            isTransitioning = false
            progress = 1f
            effectiveAppearanceMode = targetMode
            onThemeApplied?.invoke(targetMode)
            return
        }

        // Pin current theme mode during capture frame
        effectiveAppearanceMode = currentMode
        isCapturing = true
        isTransitioning = false
        progress = 0f

        val scope = coroutineScope
        if (scope == null) {
            isCapturing = false
            effectiveAppearanceMode = targetMode
            onThemeApplied?.invoke(targetMode)
            return
        }

        animationJob = scope.launch {
            try {
                // Wait for the capture frame to be rendered by Compose
                withTimeoutOrNull(150) {
                    withFrameNanos { }
                }
            } catch (_: Throwable) {}

            if (transitionGeneration != gen) return@launch

            // Capture complete! Now switch theme to target and launch smooth reveal
            isCapturing = false
            effectiveAppearanceMode = targetMode
            onThemeApplied?.invoke(targetMode)
            isTransitioning = true

            val animatable = Animatable(0f)
            try {
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = THEME_TRANSITION_DURATION_MS,
                        easing = RadialRevealEasing
                    )
                ) {
                    progress = this.value
                }
            } finally {
                if (transitionGeneration == gen) {
                    isTransitioning = false
                    progress = 1f
                    effectiveAppearanceMode = null
                }
            }
        }
    }
}

@Composable
fun rememberThemeTransitionController(): ThemeTransitionController {
    val scope = rememberCoroutineScope()
    val controller = remember { ThemeTransitionController() }
    controller.attachScope(scope)
    return controller
}

@Composable
fun Modifier.themeRadialReveal(
    controller: ThemeTransitionController,
    originProvider: (() -> Offset)? = null
): Modifier {
    val graphicsContext = LocalGraphicsContext.current
    val layer = remember(graphicsContext) {
        try {
            graphicsContext.createGraphicsLayer()
        } catch (e: Throwable) {
            null
        }
    }

    DisposableEffect(graphicsContext, layer) {
        onDispose {
            if (layer != null) {
                try {
                    graphicsContext.releaseGraphicsLayer(layer)
                } catch (e: Throwable) {}
            }
        }
    }

    val circlePath = remember { Path() }

    return this.then(
        Modifier.drawWithContent {
            val layerObj = layer

            if (controller.isCapturing && layerObj != null) {
                try {
                    layerObj.record(
                        density = this,
                        layoutDirection = layoutDirection,
                        size = IntSize(
                            size.width.roundToInt().coerceAtLeast(1),
                            size.height.roundToInt().coerceAtLeast(1)
                        )
                    ) {
                        this@drawWithContent.drawContent()
                    }
                } catch (_: Throwable) {}
                drawContent()
                return@drawWithContent
            }

            if (!controller.isTransitioning || layerObj == null) {
                drawContent()
            } else {
                // 1. Draw the captured previous theme across the canvas
                try {
                    drawLayer(layerObj)
                } catch (_: Throwable) {
                    drawContent()
                    return@drawWithContent
                }

                // 2. Compute reveal circle from origin
                val resolvedOrigin = originProvider?.invoke() ?: controller.origin
                val origin = if (resolvedOrigin != Offset.Zero && resolvedOrigin.isSpecified) {
                    resolvedOrigin
                } else {
                    Offset(size.width / 2f, size.height / 2f)
                }

                val maxRadius = hypot(
                    maxOf(origin.x, size.width - origin.x),
                    maxOf(origin.y, size.height - origin.y)
                ).coerceAtLeast(1f)
                val currentRadius = maxRadius * controller.progress

                circlePath.reset()
                circlePath.addOval(
                    Rect(
                        center = origin,
                        radius = currentRadius
                    )
                )

                // 3. Draw new theme content clipped inside the expanding circle
                try {
                    clipPath(path = circlePath) {
                        this@drawWithContent.drawContent()
                    }
                } catch (_: Throwable) {
                    drawContent()
                }
            }
        }
    )
}
