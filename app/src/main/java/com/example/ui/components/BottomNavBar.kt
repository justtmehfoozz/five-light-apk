package com.example.ui.components

import com.example.ui.theme.*

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.Surface
import androidx.compose.ui.zIndex
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.example.data.model.DhikrPreset
import com.example.data.model.NameOfAllah
import com.example.data.model.Surah
import com.example.data.util.DailyDua
import com.example.data.util.QuranData
import com.example.data.util.DuaItem
import com.example.ui.screens.AdhkarItem
import com.example.ui.theme.rememberIsReducedMotion
import com.example.ui.theme.semanticControl
import com.example.ui.theme.semanticDockBackground
import com.example.ui.theme.semanticDockBorder
import com.example.ui.theme.semanticDockIconActive
import com.example.ui.theme.semanticDockIconActiveBg
import com.example.ui.theme.semanticDockIconInactive
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class NavItem(val route: String, val label: String, val iconSelected: ImageVector, val iconUnselected: ImageVector) {
    HOME("home", "Prayer", Icons.Filled.Home, Icons.Outlined.Home),
    QIBLA("qibla", "Qibla", Icons.Filled.Explore, Icons.Outlined.Explore),
    QURAN("quran", "Quran", Icons.Filled.AutoStories, Icons.Outlined.AutoStories),
    TASBEEH("tasbeeh", "Tasbeeh", Icons.Filled.RadioButtonChecked, Icons.Outlined.RadioButtonUnchecked),
    EXPLORE("explore", "Explore", Icons.Filled.Menu, Icons.Outlined.Menu)
}

val DOCK_SPRING_FLOAT = spring<Float>(
    stiffness = 380f,
    dampingRatio = 0.78f
)

val DOCK_DRAG_SPRING = spring<Float>(
    stiffness = 1200f,
    dampingRatio = 0.85f
)

val DOCK_RELEASE_SNAP_SPRING = spring<Float>(
    stiffness = 260f,
    dampingRatio = 0.80f
)

val DOCK_BOUNDARY_BOUNCE_SPRING = spring<Float>(
    stiffness = 320f,
    dampingRatio = 0.65f
)

val DOCK_RESTING_SPRING = spring<Float>(
    stiffness = 380f,
    dampingRatio = 0.78f
)

val DOCK_SPRING_COLOR = spring<Color>(
    stiffness = 400f,
    dampingRatio = 0.8f
)

/**
 * Animated icon morphing between a 3-horizontal-line hamburger menu (progress = 0f)
 * and a magnifying glass search icon (progress = 1f).
 */
@Composable
fun HamburgerToSearchIcon(
    progress: Float,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        val cap = StrokeCap.Round

        val w = size.width
        val h = size.height
        val scaleX = w / 24f
        val scaleY = h / 24f

        fun lerp(start: Float, stop: Float, fraction: Float): Float {
            return start + (stop - start) * fraction
        }

        val t = progress.coerceIn(0f, 1f)

        // Path 1: Top line -> Top arc of magnifying glass
        val p1P0x = lerp(4f, 5.3f, t) * scaleX
        val p1P0y = lerp(7f, 10.5f, t) * scaleY
        val p1P1x = lerp(12f, 10.5f, t) * scaleX
        val p1P1y = lerp(7f, 0.1f, t) * scaleY
        val p1P2x = lerp(20f, 15.7f, t) * scaleX
        val p1P2y = lerp(7f, 10.5f, t) * scaleY

        val path1 = Path().apply {
            moveTo(p1P0x, p1P0y)
            quadraticTo(p1P1x, p1P1y, p1P2x, p1P2y)
        }

        // Path 2: Middle line -> Bottom arc of magnifying glass
        val p2P0x = lerp(4f, 5.3f, t) * scaleX
        val p2P0y = lerp(12f, 10.5f, t) * scaleY
        val p2P1x = lerp(12f, 10.5f, t) * scaleX
        val p2P1y = lerp(12f, 20.9f, t) * scaleY
        val p2P2x = lerp(20f, 15.7f, t) * scaleX
        val p2P2y = lerp(12f, 10.5f, t) * scaleY

        val path2 = Path().apply {
            moveTo(p2P0x, p2P0y)
            quadraticTo(p2P1x, p2P1y, p2P2x, p2P2y)
        }

        // Path 3: Bottom line -> Handle of magnifying glass
        val h0x = lerp(4f, 14.18f, t) * scaleX
        val h0y = lerp(17f, 14.18f, t) * scaleY
        val h1x = lerp(20f, 19.5f, t) * scaleX
        val h1y = lerp(17f, 19.5f, t) * scaleY

        val strokeStyle = Stroke(
            width = strokeWidth,
            cap = cap,
            join = StrokeJoin.Round
        )

        drawPath(path1, color = tint, style = strokeStyle)
        drawPath(path2, color = tint, style = strokeStyle)
        drawLine(
            color = tint,
            start = Offset(h0x, h0y),
            end = Offset(h1x, h1y),
            strokeWidth = strokeWidth,
            cap = cap
        )
    }
}

/**
 * Liquid Glass Morphing Dock Component
 * Behaves as ONE CONTINUOUS MATERIAL SURFACE morphing between:
 *  - DOCK (Navigation dock with floating indicator)
 *  - MUSIC PLAYER (Expanded audio playback bar with scrubbing, controls, and track info)
 *  - SEARCH (Full-width search bar traveling smoothly to top position)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SereneBottomNavBar(
    currentRoute: String,
    onNavigate: (NavItem) -> Unit,
    hazeState: HazeState,
    pagerFraction: Float? = null,
    pagerFractionProvider: (() -> Float)? = null,
    selectorController: DockSelectorStateController? = null,
    isPlaybackMode: Boolean = false,
    playingSurahNumber: Int? = null,
    playingVerseNumber: Int? = null,
    isPlaying: Boolean = false,
    audioProgress: Float = 0f,
    onPlayPause: () -> Unit = {},
    onSkipPrevious: () -> Unit = {},
    onSkipNext: () -> Unit = {},
    onStopAudio: () -> Unit = {},
    onSeekAudio: (Float) -> Unit = {},
    onOpenSearch: () -> Unit = {},
    isSearchActive: Boolean = false,
    onDismissSearch: () -> Unit = {},
    onSelectSurah: (Surah) -> Unit = {},
    onSelectDua: (DailyDua) -> Unit = {},
    onSelectDuaItem: (DuaItem) -> Unit = {},
    onSelectDhikr: (DhikrPreset) -> Unit = {},
    onSelectAdhkarItem: (AdhkarItem) -> Unit = {},
    onSelectNameOfAllah: (NameOfAllah) -> Unit = {},
    searchScope: ExploreSearchScope = ExploreSearchScope.GLOBAL_EXPLORE,
    allDhikrs: List<DhikrPreset> = emptyList(),
    dockFifthSlotMode: String = "more",
    onFifthSlotMoreTap: () -> Unit = {},
    onExpandPlayer: () -> Unit = {},
    isScrolledAwayFromActiveVerse: Boolean = false,
    onJumpToActiveVerse: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentRouteState by rememberUpdatedState(currentRoute)
    val currentOnOpenSearch by rememberUpdatedState(onOpenSearch)
    val currentOnFifthSlotMoreTap by rememberUpdatedState(onFifthSlotMoreTap)
    val currentOnNavigate by rememberUpdatedState(onNavigate)

    val isDark = MaterialTheme.colorScheme.background.run { (red + green + blue) < 1.5f }

    // Active morph state
    val morphState = when {
        isSearchActive -> DockMorphState.SEARCH
        isPlaybackMode -> DockMorphState.MUSIC
        else -> DockMorphState.DOCK
    }

    // Material theme colors
    val targetDockBg = if (isSearchActive) Color.semanticControl else Color.semanticDockBackground
    val dockBg by animateColorAsState(targetValue = targetDockBg, animationSpec = DOCK_SPRING_COLOR, label = "dockBg")

    val lightSearchBorderColor = Color.semanticDockBorder
    val lightSearchIconColor = Color(0xFF4A4A4A)
    val lightSearchClearIconColor = Color(0xFF5A5A5A)

    val targetDockBorderColor = when (morphState) {
        DockMorphState.SEARCH -> if (isDark) Color.semanticPrimaryAccent else lightSearchBorderColor
        DockMorphState.MUSIC -> if (isPlaying) {
            Color.semanticPrimaryAccent.copy(alpha = if (isDark) 0.50f else 0.40f)
        } else {
            Color.semanticPrimaryAccent.copy(alpha = if (isDark) 0.22f else 0.18f)
        }
        DockMorphState.DOCK -> Color.semanticDockBorder
    }
    val dockBorderColor by animateColorAsState(targetValue = targetDockBorderColor, animationSpec = DOCK_SPRING_COLOR, label = "dockBorderColor")

    val activeHighlightBg = Color.dockActiveIndicator
    val activeIconColor = Color.White
    val controlIconColor = if (isDark) activeIconColor else Color.semanticPrimaryText
    val inactiveIconColor = Color.semanticDockIconInactive

    val spotShadowColor = if (isDark) {
        Color.Black.copy(alpha = 0.45f)
    } else {
        com.example.ui.theme.AppBackground.copy(alpha = 0.22f)
    }

    val ambientShadowColor = if (isDark) {
        Color.Black.copy(alpha = 0.20f)
    } else {
        com.example.ui.theme.AppBackground.copy(alpha = 0.09f)
    }

    val view = LocalView.current
    val hapticFeedback = LocalHapticFeedback.current
    val isReducedMotion = rememberIsReducedMotion()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    var searchQuery by remember { mutableStateOf("") }

    // Idle breathing pulse for selector ball outer glow
    val idleTransition = rememberInfiniteTransition(label = "selectorIdlePulse")
    val idlePulseFraction by if (isReducedMotion) {
        remember { mutableFloatStateOf(0.5f) }
    } else {
        idleTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2200, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)),
                repeatMode = RepeatMode.Reverse
            ),
            label = "idlePulse"
        )
    }

    // Active music player breathing pulse for internal audio indicator
    val musicIndicatorTransition = rememberInfiniteTransition(label = "musicIndicatorPulse")
    val musicIndicatorScale by if (isReducedMotion || !isPlaying) {
        remember(isPlaying) { mutableFloatStateOf(1f) }
    } else {
        musicIndicatorTransition.animateFloat(
            initialValue = 0.94f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "musicIndicatorScale"
        )
    }
    val musicIndicatorAlpha by if (isReducedMotion || !isPlaying) {
        remember(isPlaying) { mutableFloatStateOf(0.16f) }
    } else {
        musicIndicatorTransition.animateFloat(
            initialValue = 0.14f,
            targetValue = 0.28f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "musicIndicatorAlpha"
        )
    }

    // Morph animatables for shared surface
    val morphAnimatable = remember { Animatable(if (isSearchActive) 1f else 0f) }
    val movementAnimatable = remember { Animatable(if (isSearchActive) 1f else 0f) }
    val isMorphingActive = morphAnimatable.isRunning || movementAnimatable.isRunning

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            // Phase 1: Morph Expansion
            morphAnimatable.animateTo(
                targetValue = 1f,
                animationSpec = if (isReducedMotion) tween(100) else spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            // Phase 2: Upward Translation
            movementAnimatable.animateTo(
                targetValue = 1f,
                animationSpec = if (isReducedMotion) tween(100) else tween(
                    durationMillis = 450,
                    easing = CubicBezierEasing(0.1f, 0.8f, 0.1f, 1.0f)
                )
            )

            delay(50)
            try {
                focusRequester.requestFocus()
                keyboardController?.show()
            } catch (_: Exception) {}
        } else {
            keyboardController?.hide()

            // Phase 1: Downward Translation
            movementAnimatable.animateTo(
                targetValue = 0f,
                animationSpec = if (isReducedMotion) tween(100) else tween(
                    durationMillis = 400,
                    easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
                )
            )
            // Phase 2: Morph Contraction
            morphAnimatable.animateTo(
                targetValue = 0f,
                animationSpec = if (isReducedMotion) tween(100) else spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )

            searchQuery = ""
        }
    }

    val morphFraction = morphAnimatable.value
    val movementFraction = movementAnimatable.value
    val transformProgress = maxOf(morphFraction, movementFraction)

    BackHandler(enabled = isSearchActive || transformProgress > 0.01f) {
        keyboardController?.hide()
        onDismissSearch()
    }

    val isVibrationEnabled = LocalVibrationEnabled.current

    fun performActivationHaptic() {
        if (!isVibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_TICK)
            } else {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        } catch (_: Exception) {
            try {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            } catch (_: Exception) {}
        }
    }

    fun performItemChangeHaptic() {
        if (!isVibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_TICK)
            } else {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
        } catch (_: Exception) {
            try {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } catch (_: Exception) {}
        }
    }

    val selectedIndex = when (currentRoute) {
        "home" -> 0
        "qibla" -> 1
        "quran" -> 2
        "tasbeeh" -> 3
        "explore", "calendar", "search" -> 4
        else -> 0
    }
    val defaultController = rememberDockSelectorController(initialIndex = selectedIndex)
    val controller = selectorController ?: defaultController

    val targetPosition = when {
        pagerFractionProvider != null -> pagerFractionProvider.invoke()
        pagerFraction != null -> pagerFraction
        else -> selectedIndex.toFloat()
    }

    LaunchedEffect(targetPosition, controller.isDragging.value) {
        if (!controller.isDragging.value) {
            controller.syncWithPager(targetPosition)
        }
    }

    val displayIndicatorFraction = controller.ballPositionAnimatable.value

    // Trailing position for soft drag light trail behind selector ball
    val trailPositionAnimatable = remember { Animatable(displayIndicatorFraction) }
    LaunchedEffect(displayIndicatorFraction) {
        trailPositionAnimatable.animateTo(
            targetValue = displayIndicatorFraction,
            animationSpec = spring(
                dampingRatio = 0.82f,
                stiffness = 200f
            )
        )
    }

    // Base Dimensions for the shared surface
    val targetBaseWidth = when (morphState) {
        DockMorphState.SEARCH -> 340.dp
        DockMorphState.MUSIC -> 330.dp
        DockMorphState.DOCK -> 276.dp
    }
    val targetBaseHeight = when (morphState) {
        DockMorphState.SEARCH -> 60.dp
        DockMorphState.MUSIC -> 76.dp
        DockMorphState.DOCK -> 60.dp
    }
    val targetCornerRadius = targetBaseHeight / 2

    val animatedBaseWidth by animateDpAsState(
        targetValue = targetBaseWidth,
        animationSpec = LIQUID_GLASS_SPRING_DP,
        label = "liquidGlassWidth"
    )
    val animatedBaseHeight by animateDpAsState(
        targetValue = targetBaseHeight,
        animationSpec = LIQUID_GLASS_SPRING_DP,
        label = "liquidGlassHeight"
    )
    val animatedCornerRadius by animateDpAsState(
        targetValue = targetCornerRadius,
        animationSpec = LIQUID_GLASS_SPRING_DP,
        label = "liquidGlassRadius"
    )

    // Navigation Animatable alphas & scales
    val navAlphas = remember { List(5) { Animatable(1f) } }
    val navScales = remember { List(5) { Animatable(1f) } }
    val navYOffsets = remember { List(5) { Animatable(0f) } }

    val playPauseAlpha = remember { Animatable(0f) }
    val playPauseScale = remember { Animatable(0.6f) }
    val playPauseYOffset = remember { Animatable(12f) }

    val flankAlpha = remember { Animatable(0f) }
    val flankScale = remember { Animatable(0.6f) }
    val flankYOffset = remember { Animatable(12f) }

    val closeAlpha = remember { Animatable(0f) }
    val closeScale = remember { Animatable(0.5f) }

    // Playback Mode Morph Transition Logic
    LaunchedEffect(isPlaybackMode) {
        if (isPlaybackMode) {
            launch {
                navAlphas[0].animateTo(0f, animationSpec = tween(130, easing = FastOutLinearInEasing))
                navScales[0].snapTo(0.6f)
            }
            launch {
                navAlphas[4].animateTo(0f, animationSpec = tween(130, easing = FastOutLinearInEasing))
                navScales[4].snapTo(0.6f)
            }
            launch {
                navAlphas[1].animateTo(0f, animationSpec = tween(130, delayMillis = 15, easing = FastOutLinearInEasing))
                navScales[1].snapTo(0.6f)
            }
            launch {
                navAlphas[3].animateTo(0f, animationSpec = tween(130, delayMillis = 15, easing = FastOutLinearInEasing))
                navScales[3].snapTo(0.6f)
            }
            launch {
                navAlphas[2].animateTo(0f, animationSpec = tween(130, delayMillis = 30, easing = FastOutLinearInEasing))
                navScales[2].snapTo(0.6f)
            }

            val enterSpring = spring<Float>(stiffness = 420f, dampingRatio = 0.7f)
            launch {
                delay(70)
                launch { playPauseAlpha.animateTo(1f, tween(160)) }
                launch { playPauseScale.animateTo(1f, enterSpring) }
                launch { playPauseYOffset.animateTo(0f, enterSpring) }
            }
            launch {
                delay(110)
                launch { flankAlpha.animateTo(1f, tween(160)) }
                launch { flankScale.animateTo(1f, enterSpring) }
                launch { flankYOffset.animateTo(0f, enterSpring) }
            }

            val closeSpring = spring<Float>(stiffness = 320f, dampingRatio = 0.8f)
            launch {
                delay(90)
                launch { closeAlpha.animateTo(1f, tween(180)) }
                launch { closeScale.animateTo(1f, closeSpring) }
            }
        } else {
            launch {
                closeAlpha.animateTo(0f, tween(120))
                closeScale.animateTo(0.5f, tween(120))
            }
            launch {
                flankAlpha.animateTo(0f, tween(120))
                flankScale.snapTo(0.6f)
            }
            launch {
                delay(30)
                playPauseAlpha.animateTo(0f, tween(120))
                playPauseScale.snapTo(0.6f)
            }

            val enterSpring = spring<Float>(stiffness = 420f, dampingRatio = 0.7f)
            launch {
                delay(70)
                launch { navAlphas[2].animateTo(1f, tween(160)) }
                launch { navScales[2].animateTo(1f, enterSpring) }
                launch { navYOffsets[2].animateTo(0f, enterSpring) }
            }
            launch {
                delay(105)
                listOf(1, 3).forEach { i ->
                    launch { navAlphas[i].animateTo(1f, tween(160)) }
                    launch { navScales[i].animateTo(1f, enterSpring) }
                    launch { navYOffsets[i].animateTo(0f, enterSpring) }
                }
            }
            launch {
                delay(140)
                listOf(0, 4).forEach { i ->
                    launch { navAlphas[0].animateTo(1f, tween(160)) }
                    launch { navScales[0].animateTo(1f, enterSpring) }
                    launch { navYOffsets[0].animateTo(0f, enterSpring) }
                    launch { navAlphas[4].animateTo(1f, tween(160)) }
                    launch { navScales[4].animateTo(1f, enterSpring) }
                    launch { navYOffsets[4].animateTo(0f, enterSpring) }
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        val searchTargetWidthDp = (maxWidth - 32.dp).coerceAtLeast(280.dp)
        val currentWidthDp = lerp(animatedBaseWidth, searchTargetWidthDp, morphFraction)

        val topSafeArea = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        val topSpacing = 24.dp
        val finalSearchY = maxOf(topSafeArea + topSpacing, 84.dp)

        val bottomRestingOffset = navBarPadding + 24.dp
        val dockHeight = animatedBaseHeight
        val restingTopY = maxHeight - bottomRestingOffset - dockHeight
        val targetTravelY = -(restingTopY - finalSearchY)
        val currentTranslateY = targetTravelY * movementFraction

        val backdropAlpha = transformProgress.coerceIn(0f, 1f) * 0.55f
        val navIconsAlpha = (1f - (morphFraction / 0.5f)).coerceIn(0f, 1f)
        val searchContentAlpha = ((morphFraction - 0.5f) / 0.5f).coerceIn(0f, 1f)
        val resultsAlpha = movementFraction.coerceIn(0f, 1f)

        // Backdrop Dim Overlay
        if (backdropAlpha > 0.001f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = backdropAlpha }
                    .background(Color.Black)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        keyboardController?.hide()
                        onDismissSearch()
                    }
            )
        }

        // Search Results Panel
        if (resultsAlpha > 0.001f) {
            val resultsOffsetY = lerp(16.dp, 0.dp, resultsAlpha)
            val resultsTopPadding = finalSearchY + dockHeight + 12.dp
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.TopCenter)
                    .padding(top = resultsTopPadding, start = 12.dp, end = 12.dp, bottom = 12.dp)
                    .imePadding()
                    .graphicsLayer {
                        alpha = resultsAlpha
                        translationY = resultsOffsetY.toPx()
                    }
            ) {
                DockSearchResultsPanel(
                    searchQuery = searchQuery,
                    scope = searchScope,
                    allDhikrs = allDhikrs,
                    onSelectSurah = { surah ->
                        keyboardController?.hide()
                        onSelectSurah(surah)
                        onDismissSearch()
                    },
                    onSelectDua = { dua ->
                        keyboardController?.hide()
                        onSelectDua(dua)
                        onDismissSearch()
                    },
                    onSelectDuaItem = { duaItem ->
                        keyboardController?.hide()
                        onSelectDuaItem(duaItem)
                        onDismissSearch()
                    },
                    onSelectDhikr = { dhikr ->
                        keyboardController?.hide()
                        onSelectDhikr(dhikr)
                        onDismissSearch()
                    },
                    onSelectAdhkarItem = { adhkarItem ->
                        keyboardController?.hide()
                        onSelectAdhkarItem(adhkarItem)
                        onDismissSearch()
                    },
                    onSelectNameOfAllah = { name ->
                        keyboardController?.hide()
                        onSelectNameOfAllah(name)
                        onDismissSearch()
                    },
                    isDark = isDark
                )
            }
        }

        // SINGLE CONTINUOUS LIQUID GLASS MORPHING CONTAINER
        LiquidGlassSurface(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .navigationBarsPadding()
                .offset(y = currentTranslateY)
                .width(currentWidthDp)
                .height(dockHeight),
            hazeState = hazeState,
            backgroundColor = dockBg,
            borderColor = dockBorderColor,
            cornerRadius = animatedCornerRadius,
            borderWidth = 1.5.dp,
            elevation = 16.dp,
            spotShadowColor = spotShadowColor,
            ambientShadowColor = ambientShadowColor,
            isMorphing = isMorphingActive,
            morphProgress = transformProgress,
            isDark = isDark
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                // STATE 1 & STATE 2: DOCK & MUSIC PLAYER CONTENT
                if (navIconsAlpha > 0.001f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        if (!isPlaybackMode) {
                            // DOCK NAVIGATION ITEMS (5 SLOTS)
                            Box(
                                modifier = Modifier
                                    .width(256.dp)
                                    .fillMaxHeight()
                                    .align(Alignment.Center)
                                    .pointerInput(isSearchActive, controller) {
                                        if (isSearchActive) return@pointerInput
                                        coroutineScope {
                                            awaitPointerEventScope {
                                                while (true) {
                                                    val down = awaitFirstDown(requireUnconsumed = false)
                                                    val startX = down.position.x
                                                    val startY = down.position.y
                                                    val containerHeightPx = size.height.toFloat()
                                                    val slotPitchPx = 52.dp.toPx()
                                                    val startOffsetPx = 26.dp.toPx()

                                                    controller.onGestureStart(
                                                        startX = startX,
                                                        slotPitchPx = slotPitchPx,
                                                        startOffsetPx = startOffsetPx,
                                                        currentRoute = currentRouteState,
                                                        onFifthSlotAction = {
                                                            if (currentRouteState == "explore") {
                                                                currentOnOpenSearch()
                                                            } else {
                                                                currentOnNavigate(NavItem.EXPLORE)
                                                                currentOnFifthSlotMoreTap()
                                                            }
                                                        }
                                                    )

                                                    var isCanceled = false
                                                    try {
                                                        do {
                                                            val event = awaitPointerEvent()
                                                            val pointer = event.changes.firstOrNull { it.id == down.id } ?: break

                                                            if (pointer.pressed) {
                                                                val movedOk = controller.onGestureMove(
                                                                    currentX = pointer.position.x,
                                                                    currentY = pointer.position.y,
                                                                    startX = startX,
                                                                    startY = startY,
                                                                    containerHeightPx = containerHeightPx,
                                                                    slotPitchPx = slotPitchPx,
                                                                    startOffsetPx = startOffsetPx,
                                                                    onNavigate = currentOnNavigate
                                                                )
                                                                if (!movedOk) {
                                                                    isCanceled = true
                                                                    break
                                                                }
                                                            } else {
                                                                break
                                                            }
                                                        } while (event.changes.any { it.pressed })
                                                    } finally {
                                                        if (isCanceled) {
                                                            controller.cancelGesture(targetPosition)
                                                        }
                                                    }

                                                    if (!isCanceled) {
                                                        controller.onGestureRelease(
                                                            currentRoute = currentRouteState,
                                                            onNavigate = currentOnNavigate,
                                                            onFifthSlotAction = {
                                                                if (currentRouteState == "explore") {
                                                                    currentOnOpenSearch()
                                                                } else {
                                                                    currentOnNavigate(NavItem.EXPLORE)
                                                                    currentOnFifthSlotMoreTap()
                                                                }
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.CenterStart
                            ) {
                                // Soft trailing light trail behind the moving selector ball
                                val trailFraction = trailPositionAnimatable.value
                                val trailDelta = displayIndicatorFraction - trailFraction
                                val trailDistance = kotlin.math.abs(trailDelta)

                                if (trailDistance > 0.012f) {
                                    Canvas(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer { alpha = navIconsAlpha }
                                    ) {
                                        val currentCenterPx = (displayIndicatorFraction * 52.dp.toPx()) + 24.dp.toPx()
                                        val trailCenterPx = (trailFraction * 52.dp.toPx()) + 24.dp.toPx()
                                        val centerYPx = size.height / 2f

                                        val minXPx = minOf(currentCenterPx, trailCenterPx) - 16.dp.toPx()
                                        val maxXPx = maxOf(currentCenterPx, trailCenterPx) + 16.dp.toPx()
                                        val trailHeightPx = 36.dp.toPx()
                                        val topPx = centerYPx - (trailHeightPx / 2f)

                                        val trailAlpha = if (isDark) {
                                            (trailDistance * 1.5f).coerceIn(0.12f, 0.46f)
                                        } else {
                                            (trailDistance * 1.1f).coerceIn(0.08f, 0.32f)
                                        }

                                        val trailColor = activeHighlightBg.copy(alpha = trailAlpha)

                                        // Horizontal gradient from tail to head
                                        drawRoundRect(
                                            brush = Brush.horizontalGradient(
                                                colors = if (currentCenterPx >= trailCenterPx) {
                                                    listOf(
                                                        Color.Transparent,
                                                        trailColor.copy(alpha = trailAlpha * 0.45f),
                                                        trailColor
                                                    )
                                                } else {
                                                    listOf(
                                                        trailColor,
                                                        trailColor.copy(alpha = trailAlpha * 0.45f),
                                                        Color.Transparent
                                                    )
                                                },
                                                startX = minXPx,
                                                endX = maxXPx
                                            ),
                                            topLeft = Offset(minXPx, topPx),
                                            size = Size(maxXPx - minXPx, trailHeightPx),
                                            cornerRadius = CornerRadius(trailHeightPx / 2f, trailHeightPx / 2f)
                                        )
                                    }
                                }

                                // Active Highlight Pill with outer glow shadow
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .graphicsLayer {
                                            translationX = (displayIndicatorFraction * 52.dp.toPx()) + 2.dp.toPx()
                                            scaleX = controller.ballScale.value
                                            scaleY = controller.ballScale.value
                                            alpha = navIconsAlpha
                                        }
                                        .size(44.dp)
                                        .drawBehind {
                                            val scaleVal = controller.ballScale.value
                                            val pressProgress = ((scaleVal - 1f) / 0.10f).coerceIn(0f, 1f)

                                            // Gentle breathing pulse when idle, smoothly blending into press-and-hold
                                            val effectiveIdlePulse = idlePulseFraction * (1f - pressProgress)
                                            val restingAlpha = if (isDark) {
                                                androidx.compose.ui.util.lerp(0.15f, 0.32f, effectiveIdlePulse)
                                            } else {
                                                androidx.compose.ui.util.lerp(0.10f, 0.25f, effectiveIdlePulse)
                                            }
                                            val maxPressedAlpha = if (isDark) 0.65f else 0.48f
                                            val glowAlpha = androidx.compose.ui.util.lerp(restingAlpha, maxPressedAlpha, pressProgress)

                                            val restingExpandPx = androidx.compose.ui.util.lerp(3.dp.toPx(), 6.5.dp.toPx(), effectiveIdlePulse)
                                            val maxPressedExpandPx = 10.dp.toPx()
                                            val maxGlowExpandPx = androidx.compose.ui.util.lerp(restingExpandPx, maxPressedExpandPx, pressProgress)

                                            val glowColor = activeHighlightBg.copy(alpha = glowAlpha)
                                            val centerPx = Offset(size.width / 2f, size.height / 2f)
                                            val baseRadiusPx = size.width / 2f

                                            // Faint outer glow shadow with idle breathing pulse and press confirmation
                                            drawCircle(
                                                brush = Brush.radialGradient(
                                                    colorStops = arrayOf(
                                                        0.0f to glowColor,
                                                        0.65f to glowColor.copy(alpha = glowAlpha * 0.45f),
                                                        1.0f to glowColor.copy(alpha = 0f)
                                                    ),
                                                    center = centerPx,
                                                    radius = baseRadiusPx + maxGlowExpandPx
                                                ),
                                                center = centerPx,
                                                radius = baseRadiusPx + maxGlowExpandPx
                                            )

                                            // Very brief, minimal ring-ripple animation upon initial press
                                            val rippleAlpha = controller.pressRippleAlpha.value
                                            val rippleProgress = controller.pressRippleProgress.value
                                            if (rippleAlpha > 0.001f) {
                                                val rippleRadius = baseRadiusPx + androidx.compose.ui.util.lerp(1.dp.toPx(), 14.dp.toPx(), rippleProgress)
                                                val ringStrokeWidth = androidx.compose.ui.util.lerp(2.dp.toPx(), 0.75.dp.toPx(), rippleProgress)
                                                val ringColor = if (isDark) {
                                                    activeHighlightBg.copy(alpha = rippleAlpha * 0.80f)
                                                } else {
                                                    activeHighlightBg.copy(alpha = rippleAlpha * 0.60f)
                                                }
                                                drawCircle(
                                                    color = ringColor,
                                                    radius = rippleRadius,
                                                    center = centerPx,
                                                    style = Stroke(width = ringStrokeWidth)
                                                )
                                            }
                                        }
                                        .clip(CircleShape)
                                        .background(activeHighlightBg)
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    NavItem.entries.forEachIndexed { index, item ->
                                        val active = kotlin.math.round(displayIndicatorFraction).toInt().coerceIn(0, 4) == index

                                        val animatedIconColor by animateColorAsState(
                                            targetValue = if (active) activeIconColor else inactiveIconColor,
                                            animationSpec = DOCK_SPRING_COLOR,
                                            label = "dockIconColor"
                                        )

                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .testTag("nav_${item.route}")
                                                .graphicsLayer {
                                                    if (index == 4) {
                                                        val naturalX = (currentWidthDp.toPx() - 256.dp.toPx()) / 2f + 232.dp.toPx()
                                                        val actualX = lerp(242.dp, 26.dp, morphFraction).toPx()
                                                        translationX = actualX - naturalX

                                                        val targetScale = 20f / 22f
                                                        scaleX = androidx.compose.ui.util.lerp(1f, targetScale, morphFraction) * navScales[index].value
                                                        scaleY = scaleX
                                                        alpha = navAlphas[index].value
                                                    } else {
                                                        alpha = navAlphas[index].value * navIconsAlpha
                                                        scaleX = navScales[index].value * (1f - transformProgress * 0.35f).coerceIn(0.65f, 1f)
                                                        scaleY = navScales[index].value * (1f - transformProgress * 0.35f).coerceIn(0.65f, 1f)
                                                        translationX = -12.dp.toPx() * transformProgress
                                                        translationY = navYOffsets[index].value.dp.toPx()
                                                    }
                                                }
                                                .clip(CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (index == 4) {
                                                val isExploreActive = currentRoute == "explore"
                                                val exploreMorphProgress by animateFloatAsState(
                                                    targetValue = if (isExploreActive) 1f else 0f,
                                                    animationSpec = tween(
                                                        durationMillis = 200,
                                                        easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
                                                    ),
                                                    label = "exploreIconMorphProgress"
                                                )

                                                val startColor = animatedIconColor
                                                val endColor = if (isDark) Color(0xFFE5E2DC) else lightSearchIconColor
                                                val effectiveIconColor = androidx.compose.ui.graphics.lerp(startColor, endColor, morphFraction)

                                                val finalIconMorphProgress = (exploreMorphProgress + morphFraction).coerceIn(0f, 1f)

                                                HamburgerToSearchIcon(
                                                    progress = finalIconMorphProgress,
                                                    tint = effectiveIconColor,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = if (active) item.iconSelected else item.iconUnselected,
                                                    contentDescription = item.label,
                                                    tint = animatedIconColor,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // MUSIC PLAYER / AUDIO PLAYBACK CONTROLS
                            val playingSurah = remember(playingSurahNumber) {
                                playingSurahNumber?.let { num ->
                                    QuranData.SURAHS_DIRECTORY.find { it.number == num }
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .pointerInput(Unit) {
                                        detectVerticalDragGestures { _, dragAmount ->
                                            if (dragAmount < -18f) {
                                                onExpandPlayer()
                                            }
                                        }
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left artwork/track identity badge (tap to expand full player, subtle internal pulse when playing)
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .graphicsLayer {
                                            alpha = flankAlpha.value
                                            scaleX = flankScale.value * musicIndicatorScale
                                            scaleY = flankScale.value * musicIndicatorScale
                                        }
                                        .clip(CircleShape)
                                        .background(Color.semanticPrimaryAccent.copy(alpha = musicIndicatorAlpha))
                                        .clickable { onExpandPlayer() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.GraphicEq,
                                        contentDescription = "Expand Full Player",
                                        tint = Color.semanticPrimaryAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Center Column: Title & Interactive Seeker
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    // Title & Controls Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Track label (tap to expand full player)
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(end = 4.dp)
                                                .clickable { onExpandPlayer() },
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = playingSurah?.nameEnglish ?: "Quran Recitation",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = controlIconColor,
                                                    fontSize = 12.sp
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = if (playingVerseNumber != null) "Verse $playingVerseNumber • Tap for details" else (playingSurah?.nameArabic ?: "Surah Recitation"),
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = inactiveIconColor,
                                                    fontSize = 10.sp
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Playback buttons: Previous, Play/Pause, Next
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Skip Previous
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .graphicsLayer {
                                                        alpha = flankAlpha.value
                                                        scaleX = flankScale.value
                                                        scaleY = flankScale.value
                                                        translationY = flankYOffset.value.dp.toPx()
                                                    }
                                                    .clip(CircleShape)
                                                    .clickable { onSkipPrevious() },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.SkipPrevious,
                                                    contentDescription = "Previous Verse",
                                                    tint = controlIconColor,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            // Play / Pause Focal Button
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .graphicsLayer {
                                                        alpha = playPauseAlpha.value
                                                        scaleX = playPauseScale.value
                                                        scaleY = playPauseScale.value
                                                        translationY = playPauseYOffset.value.dp.toPx()
                                                    }
                                                    .clip(CircleShape)
                                                    .background(Color.semanticPrimaryAccent)
                                                    .clickable { onPlayPause() },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                                    tint = Color.semanticAccentForeground,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            // Skip Next
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .graphicsLayer {
                                                        alpha = flankAlpha.value
                                                        scaleX = flankScale.value
                                                        scaleY = flankScale.value
                                                        translationY = flankYOffset.value.dp.toPx()
                                                    }
                                                    .clip(CircleShape)
                                                    .clickable { onSkipNext() },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.SkipNext,
                                                    contentDescription = "Next Verse",
                                                    tint = controlIconColor,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Interactive Scrubbing Progress Bar
                                    val progressAccent = Color.semanticPrimaryAccent
                                    val inactiveTrackColor = if (isDark) Color.White.copy(alpha = 0.20f) else Color.Black.copy(alpha = 0.14f)
                                    var isDraggingProgress by remember { mutableStateOf(false) }
                                    var dragProgressFraction by remember { mutableFloatStateOf(0f) }
                                    val currentDisplayProg = if (isDraggingProgress) dragProgressFraction else audioProgress.coerceIn(0f, 1f)

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(10.dp)
                                            .pointerInput(Unit) {
                                                detectTapGestures(
                                                    onPress = { offset ->
                                                        val fraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                                        dragProgressFraction = fraction
                                                        isDraggingProgress = true
                                                        onSeekAudio(fraction)
                                                        tryAwaitRelease()
                                                        isDraggingProgress = false
                                                    }
                                                )
                                            }
                                            .pointerInput(Unit) {
                                                detectHorizontalDragGestures(
                                                    onDragStart = { offset ->
                                                        isDraggingProgress = true
                                                        dragProgressFraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                                        onSeekAudio(dragProgressFraction)
                                                    },
                                                    onDragEnd = {
                                                        isDraggingProgress = false
                                                        onSeekAudio(dragProgressFraction)
                                                    },
                                                    onDragCancel = {
                                                        isDraggingProgress = false
                                                    },
                                                    onHorizontalDrag = { change, _ ->
                                                        change.consume()
                                                        dragProgressFraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                                                        onSeekAudio(dragProgressFraction)
                                                    }
                                                )
                                            },
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        // Inactive Track
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(2.dp)
                                                .clip(CircleShape)
                                                .background(inactiveTrackColor)
                                        )

                                        // Active Track
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(fraction = currentDisplayProg)
                                                .height(2.dp)
                                                .clip(CircleShape)
                                                .background(progressAccent)
                                        )

                                        // Progress Thumb
                                        if (currentDisplayProg > 0f) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(fraction = currentDisplayProg)
                                                    .height(6.dp),
                                                contentAlignment = Alignment.CenterEnd
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(width = 8.dp, height = 4.dp)
                                                        .clip(CircleShape)
                                                        .background(progressAccent)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Subtle Divider & Close Button
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(24.dp)
                                            .background(dockBorderColor.copy(alpha = 0.35f))
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .graphicsLayer {
                                                alpha = closeAlpha.value
                                                scaleX = closeScale.value
                                                scaleY = closeScale.value
                                            }
                                            .clip(CircleShape)
                                            .clickable { onStopAudio() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Dismiss audio bar",
                                            tint = controlIconColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // STATE 3: SEARCH BAR CONTENT
                if (isSearchActive || transformProgress > 0.001f) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = searchContentAlpha }
                            .padding(start = 16.dp, end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Magnifying glass traveling smoothly into place
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = if (isDark) dockBorderColor else lightSearchIconColor,
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .size(20.dp)
                        )

                        // Search Text Input
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    try {
                                        focusRequester.requestFocus()
                                        keyboardController?.show()
                                    } catch (_: Exception) {}
                                }
                        ) {
                            if (searchQuery.isEmpty()) {
                                val placeholderText = when (searchScope) {
                                    ExploreSearchScope.GLOBAL_EXPLORE -> "Search the Qur’an, duas, adhkar & more"
                                    ExploreSearchScope.DUA_LIBRARY -> "Search duas or categories..."
                                    ExploreSearchScope.NAMES_OF_ALLAH -> "Search by name, meaning or number..."
                                    ExploreSearchScope.DAILY_ADHKAR -> "Search adhkar..."
                                }
                                Text(
                                    text = placeholderText,
                                    color = if (isDark) Color(0xFF8E8E93) else Color(0xFF7A7771),
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                enabled = isSearchActive,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester)
                                    .testTag("search_text_input"),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = if (isDark) activeIconColor else Color.semanticDockIconActiveBg,
                                    fontSize = 15.sp
                                ),
                                cursorBrush = SolidColor(Color.semanticPrimaryAccent),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = {
                                    keyboardController?.hide()
                                })
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Single Context-Aware 'X' Button:
                        // - If text exists: clears the query, keeps search mode open for immediate typing
                        // - If text is empty: exits search mode, dismisses keyboard, morphs back to dock
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(dockBorderColor.copy(alpha = 0.25f))
                                .clickable {
                                    if (searchQuery.isNotEmpty()) {
                                        searchQuery = ""
                                        try {
                                            focusRequester.requestFocus()
                                        } catch (_: Exception) {}
                                    } else {
                                        keyboardController?.hide()
                                        onDismissSearch()
                                    }
                                }
                                .testTag("close_search_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = if (searchQuery.isNotEmpty()) "Clear search query" else "Close search",
                                tint = if (isDark) activeIconColor else lightSearchClearIconColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // INTEGRATED JUMP-TO-CURRENT-VERSE MORPH CONTROL
        JumpToCurrentVerseMorphControl(
            visible = isScrolledAwayFromActiveVerse && isPlaybackMode && !isSearchActive && (playingVerseNumber != null),
            playingVerseNumber = playingVerseNumber,
            isDark = isDark,
            isReducedMotion = isReducedMotion,
            dockTranslateY = currentTranslateY,
            dockHeight = dockHeight,
            dockBg = dockBg,
            dockBorderColor = dockBorderColor,
            onClick = onJumpToActiveVerse,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
}

/**
 * Integrated Jump-to-Current-Verse control that visually separates from the audio player
 * through a fluid, organic liquid separation morph and absorbs back seamlessly.
 *
 * Sequence:
 * 1. Droplet emerges from the audio player top boundary under fluid surface tension.
 * 2. Surface stretches upward with a temporary liquid neck connecting during physical detachment.
 * 3. Neck thins and cleanly detaches; two independent rounded surfaces form.
 * 4. Droplet expands horizontally into a refined pill container, revealing text cleanly.
 * 5. Pill rests above the audio player with an intentional clean air gap (NO permanent line or stem).
 * 6. Reverse animation cleanly contracts text, shrinks into droplet, and absorbs back into the player.
 */
@Composable
fun JumpToCurrentVerseMorphControl(
    visible: Boolean,
    playingVerseNumber: Int?,
    isDark: Boolean,
    isReducedMotion: Boolean,
    dockTranslateY: Dp,
    dockHeight: Dp,
    dockBg: Color,
    dockBorderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = updateTransition(targetState = visible, label = "jumpToVerseFluidMorph")

    // Custom fluid easing for organic physical mass and cohesive liquid movement
    val fluidSeparationEasing = remember { CubicBezierEasing(0.22f, 0.0f, 0.12f, 1.0f) }
    val fluidAbsorptionEasing = remember { CubicBezierEasing(0.32f, 0.0f, 0.18f, 1.0f) }

    val morphProgress by transition.animateFloat(
        transitionSpec = {
            if (isReducedMotion) {
                snap()
            } else if (targetState) {
                // Emerging & liquid separation: deliberate, organic 560ms motion
                tween(durationMillis = 560, easing = fluidSeparationEasing)
            } else {
                // Reverse absorption: fluid 480ms contraction back into player
                tween(durationMillis = 480, easing = fluidAbsorptionEasing)
            }
        },
        label = "morphProgress"
    ) { isVis ->
        if (isVis) 1f else 0f
    }

    if (morphProgress > 0.001f || visible) {
        val accentColor = Color.semanticPrimaryAccent
        val verseText = if (playingVerseNumber == 0) "Jump to Bismillah" else "Jump to Verse ${playingVerseNumber ?: 1}"

        // Multi-stage fluid parameter interpolation:
        // Phase 1 (0.00 - 0.36): Emergence & vertical tension stretch
        // Phase 2 (0.36 - 0.45): Detachment & droplet spherical relaxation
        // Phase 3 (0.45 - 0.88): Horizontal pill expansion & text reveal
        // Phase 4 (0.88 - 1.00): Soft settle at resting position
        val separationFraction = (morphProgress / 0.38f).coerceIn(0f, 1f)
        val expandFraction = ((morphProgress - 0.40f) / 0.50f).coerceIn(0f, 1f)
        val textFraction = ((morphProgress - 0.52f) / 0.40f).coerceIn(0f, 1f)

        val targetPillWidth = 168.dp
        val circleSize = 34.dp

        // Dynamic width during liquid morph
        val currentWidth = if (isReducedMotion) {
            if (visible) targetPillWidth else circleSize
        } else {
            if (morphProgress < 0.40f) {
                // Slight narrowing under vertical stretch tension
                lerp(circleSize, 29.dp, (morphProgress / 0.25f).coerceIn(0f, 1f))
            } else {
                lerp(34.dp, targetPillWidth, expandFraction)
            }
        }

        // Dynamic height during liquid morph (elongates under tension, then relaxes into standard pill)
        val currentHeight = if (isReducedMotion) {
            34.dp
        } else {
            if (morphProgress < 0.28f) {
                // Vertical stretch
                lerp(24.dp, 38.dp, (morphProgress / 0.28f).coerceIn(0f, 1f))
            } else if (morphProgress < 0.45f) {
                // Relaxation back to 34dp after detachment
                lerp(38.dp, 34.dp, ((morphProgress - 0.28f) / 0.17f).coerceIn(0f, 1f))
            } else {
                34.dp
            }
        }

        val currentCornerRadius = currentHeight / 2

        // Vertical travel offset: moves from right against player edge up to clean resting gap
        val verticalTravelOffset = if (isReducedMotion) {
            0.dp
        } else {
            lerp(26.dp, 0.dp, separationFraction)
        }

        val controlAlpha = if (isReducedMotion) {
            if (visible) 1f else 0f
        } else {
            (morphProgress / 0.18f).coerceIn(0f, 1f)
        }

        val textAlpha = if (isReducedMotion) {
            if (visible) 1f else 0f
        } else {
            textFraction
        }

        // Resting position: sits directly above the audio player dock with an 8.dp clean air gap
        val bottomPadding = 24.dp + dockHeight + 8.dp

        Box(
            modifier = modifier
                .padding(bottom = bottomPadding)
                .offset(y = dockTranslateY + verticalTravelOffset)
                .graphicsLayer {
                    alpha = controlAlpha
                }
                .zIndex(30f), // Highest stacking order: always above the audio player & reader text
            contentAlignment = Alignment.BottomCenter
        ) {
            // TEMPORARY FLUID LIQUID NECK / MENISCUS (Visible ONLY during physical separation / absorption)
            // Completely disappears once separated (morphProgress > 0.38f) leaving a pure clean gap!
            if (!isReducedMotion && morphProgress in 0.05f..0.38f) {
                val neckProgress = ((morphProgress - 0.05f) / 0.33f).coerceIn(0f, 1f)
                val neckAlpha = (1f - neckProgress).coerceIn(0f, 1f)

                Canvas(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 22.dp)
                        .width(36.dp)
                        .height(24.dp)
                        .graphicsLayer { alpha = neckAlpha }
                ) {
                    val w = size.width
                    val h = size.height
                    val topNeckHalfWidth = lerp(10.dp, 3.dp, neckProgress).toPx()
                    val bottomBaseHalfWidth = lerp(16.dp, 6.dp, neckProgress).toPx()
                    val centerX = w / 2f

                    val fluidPath = Path().apply {
                        moveTo(centerX - topNeckHalfWidth, 0f)
                        // Organic curved meniscus pulling apart
                        cubicTo(
                            centerX - (topNeckHalfWidth * 0.5f), h * 0.4f,
                            centerX - bottomBaseHalfWidth, h * 0.8f,
                            centerX - bottomBaseHalfWidth, h
                        )
                        lineTo(centerX + bottomBaseHalfWidth, h)
                        cubicTo(
                            centerX + bottomBaseHalfWidth, h * 0.8f,
                            centerX + (topNeckHalfWidth * 0.5f), h * 0.4f,
                            centerX + topNeckHalfWidth, 0f
                        )
                        close()
                    }

                    // Fill fluid neck matching dock background
                    drawPath(path = fluidPath, color = dockBg)

                    // Draw organic boundary stroke
                    drawPath(
                        path = fluidPath,
                        color = dockBorderColor.copy(alpha = if (isDark) 0.50f * neckAlpha else 0.35f * neckAlpha),
                        style = Stroke(width = 1.2.dp.toPx())
                    )
                }
            }

            // MORPHING LIQUID SURFACE (Droplet -> Pill)
            Surface(
                onClick = onClick,
                shape = RoundedCornerShape(currentCornerRadius),
                color = dockBg,
                border = BorderStroke(
                    1.2.dp,
                    dockBorderColor.copy(alpha = if (isDark) 0.55f else 0.40f)
                ),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .width(currentWidth)
                    .height(currentHeight)
                    .testTag("jump_to_current_verse_pill")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Jump to Verse",
                        tint = accentColor,
                        modifier = Modifier.size(15.dp)
                    )

                    if (textAlpha > 0.01f) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = verseText,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = accentColor,
                                fontSize = 12.5.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.graphicsLayer {
                                alpha = textAlpha
                            }
                        )
                    }
                }
            }
        }
    }
}
