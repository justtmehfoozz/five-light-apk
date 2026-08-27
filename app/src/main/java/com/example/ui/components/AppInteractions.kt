package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Indication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shape-Aware Clickable Modifier:
 * Strictly binds visual press feedback, ripple, and touch overlay to the given [shape].
 * Prevents rectangular bleed/flash when clicking non-rectangular components.
 */
fun Modifier.shapedClickable(
    shape: Shape,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = null,
    onClick: () -> Unit
): Modifier = this
    .clip(shape)
    .clickable(
        interactionSource = interactionSource,
        indication = indication,
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        onClick = onClick
    )

/**
 * Shape-Aware Combined Clickable Modifier:
 * Handles long click, double click, and normal click with exact shape clipping.
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.shapedCombinedClickable(
    shape: Shape,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = null,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onClick: () -> Unit
): Modifier = this
    .clip(shape)
    .combinedClickable(
        interactionSource = interactionSource,
        indication = indication,
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        onLongClickLabel = onLongClickLabel,
        onLongClick = onLongClick,
        onDoubleClick = onDoubleClick,
        onClick = onClick
    )

/**
 * Circular Clickable:
 * Perfect circular press and ripple feedback (CircleShape).
 */
fun Modifier.circleClickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = Role.Button,
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = null,
    onClick: () -> Unit
): Modifier = shapedClickable(
    shape = CircleShape,
    enabled = enabled,
    onClickLabel = onClickLabel,
    role = role,
    interactionSource = interactionSource,
    indication = indication,
    onClick = onClick
)

/**
 * Rounded Rectangle Clickable:
 * Perfect rounded corner press and ripple feedback matching [radius].
 */
fun Modifier.roundedClickable(
    radius: Dp,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = null,
    onClick: () -> Unit
): Modifier = shapedClickable(
    shape = RoundedCornerShape(radius),
    enabled = enabled,
    onClickLabel = onClickLabel,
    role = role,
    interactionSource = interactionSource,
    indication = indication,
    onClick = onClick
)

/**
 * Pill-Shaped Clickable:
 * Perfect pill-shaped press and ripple feedback (CircleShape).
 */
fun Modifier.pillClickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = Role.Button,
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = null,
    onClick: () -> Unit
): Modifier = shapedClickable(
    shape = CircleShape,
    enabled = enabled,
    onClickLabel = onClickLabel,
    role = role,
    interactionSource = interactionSource,
    indication = indication,
    onClick = onClick
)

/**
 * Reusable Shape-Aware Icon Button:
 * Ensures the visual ripple is strictly circular and contained,
 * while respecting the 48.dp minimum interactive accessibility touch target.
 */
@Composable
fun AppIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * Reusable Shape-Aware Pressable Container:
 * Unifies background, border, clipping, and ripple inside the same shape geometry.
 */
@Composable
fun AppPressable(
    onClick: () -> Unit,
    shape: Shape,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = Color.Transparent,
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.Transparent,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (borderWidth > 0.dp && borderColor != Color.Transparent) {
                    Modifier.border(borderWidth, borderColor, shape)
                } else {
                    Modifier
                }
            )
            .background(backgroundColor, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = contentAlignment,
        content = content
    )
}
