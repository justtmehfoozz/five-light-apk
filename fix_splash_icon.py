import re

with open("app/src/main/java/com/example/ui/screens/SplashScreen.kt", "r") as f:
    content = f.read()

imports = """
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import com.example.R
"""

content = content.replace("import androidx.compose.foundation.layout.*", "import androidx.compose.foundation.layout.*\n" + imports)


logo_code = """
            // ----------------------------------------------------
            // 0. APP LOGO WITH FADE IN
            // ----------------------------------------------------
            val logoAlpha = if (isReducedMotion) {
                1.0f
            } else {
                val raw = ((animationTimeMs - 50f) / 300f).coerceIn(0f, 1f)
                cubicEaseOut.transform(raw)
            }
            val iconRes = if (isDark) R.drawable.fivelight_icon_dark else R.drawable.fivelight_icon_light
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = "FiveLight Logo",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .graphicsLayer {
                        alpha = logoAlpha
                        translationY = with(density) { ((1f - logoAlpha) * 10.dp.toPx()) }
                    }
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            // ----------------------------------------------------
            // 1. CENTERED WORDMARK
"""

content = content.replace(
    "            // ----------------------------------------------------\n            // 1. CENTERED WORDMARK (\"FiveLight\") WITH STAGGER REVEAL & SHIMMER",
    logo_code
)

with open("app/src/main/java/com/example/ui/screens/SplashScreen.kt", "w") as f:
    f.write(content)

