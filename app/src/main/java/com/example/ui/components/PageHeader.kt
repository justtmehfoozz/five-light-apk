package com.example.ui.components

import com.example.ui.theme.semanticPrimaryAccent
import com.example.ui.theme.semanticSuccess
import com.example.ui.theme.semanticError
import com.example.ui.theme.semanticSurface
import com.example.ui.theme.semanticSurfaceElevated
import com.example.ui.theme.semanticPrimaryText
import com.example.ui.theme.semanticMutedText
import com.example.ui.theme.semanticBorder
import com.example.ui.theme.semanticBackground
import com.example.ui.theme.semanticWarning


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.InstrumentSerifItalic
import com.example.ui.theme.SpaceGrotesk

/**
 * Standardized Page Header Design System Tokens
 * Unified geometry across all primary screens (Home, Explore, Qibla, Tasbeeh, Quran, Calendar)
 */
object PageHeaderDefaults {
    val HeaderTopInset: Dp = 12.dp
    val HeaderBottomSpacing: Dp = 16.dp
    val HorizontalPadding: Dp = 20.dp
    val TitleSubtitleSpacing: Dp = 4.dp
    val TitleFontSize = 42.sp
    val TitleLineHeight = 46.sp
    val SubtitleFontSize = 13.sp
    val SubtitleLineHeight = 18.sp
}

/**
 * Shared Page Header Component used across all primary screens
 */
@Composable
fun PageHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    includeStatusBarPadding: Boolean = true,
    horizontalPadding: Dp = PageHeaderDefaults.HorizontalPadding,
    topPadding: Dp = PageHeaderDefaults.HeaderTopInset,
    bottomPadding: Dp = PageHeaderDefaults.HeaderBottomSpacing,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    actions: @Composable (() -> Unit)? = null,
    subtitleContent: @Composable (() -> Unit)? = null
) {
    val baseModifier = if (includeStatusBarPadding) {
        modifier.statusBarsPadding()
    } else {
        modifier
    }

    Column(
        modifier = baseModifier
            .fillMaxWidth()
            .padding(
                top = topPadding,
                bottom = bottomPadding
            )
            .padding(horizontal = horizontalPadding)
            .testTag("page_header")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontFamily = InstrumentSerifItalic,
                fontStyle = FontStyle.Italic,
                fontSize = PageHeaderDefaults.TitleFontSize,
                lineHeight = PageHeaderDefaults.TitleLineHeight,
                color = titleColor,
                letterSpacing = (-0.5).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .testTag("page_header_title")
            )

            if (actions != null) {
                actions()
            }
        }

        if (subtitleContent != null) {
            Spacer(modifier = Modifier.height(PageHeaderDefaults.TitleSubtitleSpacing))
            subtitleContent()
        } else if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(PageHeaderDefaults.TitleSubtitleSpacing))
            Text(
                text = subtitle,
                fontFamily = SpaceGrotesk,
                fontSize = PageHeaderDefaults.SubtitleFontSize,
                lineHeight = PageHeaderDefaults.SubtitleLineHeight,
                color = subtitleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("page_header_subtitle")
            )
        }
    }
}

