import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color

val Color.Companion.semanticSurface: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF1C1C1E) else Color(0xFFFFFFFF)
