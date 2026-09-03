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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.compose.ui.unit.IntSize
import com.example.data.model.AppearanceMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    var progress by mutableFloatStateOf(1f)
        private set

    var origin by mutableStateOf(Offset.Zero)
        private set

    var effectiveAppearanceMode by mutableStateOf<AppearanceMode?>(null)
        private set

    var needsCapture by mutableStateOf(false)
        private set

    var transitionGeneration by mutableIntStateOf(0)
        private set

    private var pendingTargetMode: AppearanceMode? = null
    private var animationJob: Job? = null
    private var coroutineScope: CoroutineScope? = null

    fun attachScope(scope: CoroutineScope) {
        this.coroutineScope = scope
    }

    fun startTransition(
        targetMode: AppearanceMode,
        tapOrigin: Offset,
        isReducedMotion: Boolean = false,
        isEffectiveThemeChanging: Boolean = true
    ) {
        if (!isEffectiveThemeChanging) {
            effectiveAppearanceMode = targetMode
            return
        }

        transitionGeneration++
        val gen = transitionGeneration
        animationJob?.cancel()

        origin = tapOrigin
        pendingTargetMode = targetMode

        if (isReducedMotion) {
            needsCapture = false
            isTransitioning = false
            progress = 1f
            effectiveAppearanceMode = targetMode
            return
        }

        needsCapture = true
        isTransitioning = false
        progress = 0f

        // Safety fallback: if capture frame doesn't arrive within 120ms, apply target directly
        coroutineScope?.launch {
            delay(120)
            if (needsCapture && transitionGeneration == gen) {
                onCaptureFailed(gen)
            }
        }
    }

    fun onCaptured(generation: Int) {
        if (generation != transitionGeneration) return
        needsCapture = false
        isTransitioning = true
        progress = 0f

        val target = pendingTargetMode ?: return
        effectiveAppearanceMode = target

        val scope = coroutineScope ?: return
        animationJob = scope.launch {
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
                if (transitionGeneration == generation) {
                    isTransitioning = false
                    progress = 1f
                }
            }
        }
    }

    fun onCaptureFailed(generation: Int) {
        if (generation != transitionGeneration) return
        needsCapture = false
        isTransitioning = false
        progress = 1f
        pendingTargetMode?.let { effectiveAppearanceMode = it }
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
    controller: ThemeTransitionController
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
            val gen = controller.transitionGeneration
            val layerObj = layer

            if (controller.needsCapture && layerObj != null) {
                try {
                    layerObj.record(
                        density = this,
                        layoutDirection = layoutDirection,
                        size = IntSize(size.width.roundToInt(), size.height.roundToInt())
                    ) {
                        this@drawWithContent.drawContent()
                    }
                    controller.onCaptured(gen)
                } catch (e: Throwable) {
                    controller.onCaptureFailed(gen)
                }
                drawContent()
                return@drawWithContent
            }

            if (!controller.isTransitioning || layerObj == null) {
                drawContent()
            } else {
                // 1. Draw new theme content underneath
                drawContent()

                // 2. Compute reveal circle from origin
                val origin = controller.origin
                val maxRadius = hypot(
                    maxOf(origin.x, size.width - origin.x),
                    maxOf(origin.y, size.height - origin.y)
                )
                val currentRadius = maxRadius * controller.progress

                circlePath.reset()
                circlePath.addOval(
                    Rect(
                        center = origin,
                        radius = currentRadius
                    )
                )

                // 3. Draw previous theme layer everywhere OUTSIDE the circle
                try {
                    clipPath(path = circlePath, clipOp = ClipOp.Difference) {
                        drawLayer(layerObj)
                    }
                } catch (e: Throwable) {
                    // Safe fallback if hardware clipping fails
                }
            }
        }
    )
}
