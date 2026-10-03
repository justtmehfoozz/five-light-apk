import os
import re

def ensure_imports(content):
    if "import androidx.compose.ui.res.stringResource" not in content and "@Composable" in content:
        content = re.sub(r'(package [^\n]+)', r'\1\n\nimport androidx.compose.ui.res.stringResource\nimport com.example.R', content, count=1)
    elif "import com.example.R" not in content:
        content = re.sub(r'(package [^\n]+)', r'\1\n\nimport com.example.R', content, count=1)
    return content

def migrate_prelude_files():
    # PreludeScreen.kt
    p_screen = "app/src/main/java/com/example/ui/prelude/PreludeScreen.kt"
    with open(p_screen, "r", encoding="utf-8") as f:
        c = f.read()
    c = ensure_imports(c)
    c = c.replace('text = "Skip",', 'text = stringResource(R.string.action_skip),')
    c = c.replace('text = "Next",', 'text = stringResource(R.string.action_next),')
    c = c.replace('contentDescription = "Next prelude page",', 'contentDescription = stringResource(R.string.cd_next_prelude_page),')
    with open(p_screen, "w", encoding="utf-8") as f:
        f.write(c)
    print("Migrated PreludeScreen.kt")

    # Page0OpeningScene.kt
    p0 = "app/src/main/java/com/example/ui/prelude/Page0OpeningScene.kt"
    with open(p0, "r", encoding="utf-8") as f:
        c = f.read()
    c = ensure_imports(c)
    c = c.replace('contentDescription = "FiveLight Opening scene"', 'contentDescription = stringResource(R.string.cd_prelude_opening_scene)')
    c = c.replace('text = "FiveLight",', 'text = stringResource(R.string.app_name),')
    c = c.replace('text = "Your day, illuminated.",', 'text = stringResource(R.string.prelude_tagline),')
    with open(p0, "w", encoding="utf-8") as f:
        f.write(c)
    print("Migrated Page0OpeningScene.kt")

    # Page1PrayerScene.kt
    p1 = "app/src/main/java/com/example/ui/prelude/Page1PrayerScene.kt"
    with open(p1, "r", encoding="utf-8") as f:
        c = f.read()
    c = ensure_imports(c)
    c = c.replace('contentDescription = "Prayer timeline scene"', 'contentDescription = stringResource(R.string.cd_prelude_prayer_scene)')
    c = c.replace('text = "Begin with what matters.",', 'text = stringResource(R.string.prelude_prayer_title),')
    c = c.replace('text = "Your prayers, your day, beautifully in rhythm.",', 'text = stringResource(R.string.prelude_prayer_subtitle),')
    with open(p1, "w", encoding="utf-8") as f:
        f.write(c)
    print("Migrated Page1PrayerScene.kt")

    # Page2RemembranceScene.kt
    p2 = "app/src/main/java/com/example/ui/prelude/Page2RemembranceScene.kt"
    with open(p2, "r", encoding="utf-8") as f:
        c = f.read()
    c = ensure_imports(c)
    c = c.replace('contentDescription = "Remembrance scene"', 'contentDescription = stringResource(R.string.cd_prelude_remembrance_scene)')
    c = c.replace('text = "Stay close.",', 'text = stringResource(R.string.prelude_remembrance_title),')
    c = c.replace('text = "Small moments of remembrance, throughout your day.",', 'text = stringResource(R.string.prelude_remembrance_subtitle),')
    c = c.replace('text = "\\"Indeed, in the remembrance of Allah do hearts find rest.\\"",', 'text = stringResource(R.string.prelude_verse_translation),')
    with open(p2, "w", encoding="utf-8") as f:
        f.write(c)
    print("Migrated Page2RemembranceScene.kt")

    # Page3QiblaScene.kt
    p3 = "app/src/main/java/com/example/ui/prelude/Page3QiblaScene.kt"
    with open(p3, "r", encoding="utf-8") as f:
        c = f.read()
    c = ensure_imports(c)
    c = c.replace('contentDescription = "Qibla compass scene"', 'contentDescription = stringResource(R.string.cd_prelude_qibla_scene)')
    c = c.replace('text = "Find your direction.",', 'text = stringResource(R.string.prelude_qibla_title),')
    c = c.replace('text = "Accurate Qibla and prayer guidance, wherever you are.",', 'text = stringResource(R.string.prelude_qibla_subtitle),')
    with open(p3, "w", encoding="utf-8") as f:
        f.write(c)
    print("Migrated Page3QiblaScene.kt")

    # Page4PersonalizationScene.kt
    p4 = "app/src/main/java/com/example/ui/prelude/Page4PersonalizationScene.kt"
    with open(p4, "r", encoding="utf-8") as f:
        c = f.read()
    c = ensure_imports(c)
    c = c.replace('contentDescription = "Personalization scene"', 'contentDescription = stringResource(R.string.cd_prelude_personalization_scene)')
    c = c.replace('text = "Make it yours.",', 'text = stringResource(R.string.prelude_personalization_title),')
    c = c.replace('text = "Your worship, your progress, your FiveLight.",', 'text = stringResource(R.string.prelude_personalization_subtitle),')
    with open(p4, "w", encoding="utf-8") as f:
        f.write(c)
    print("Migrated Page4PersonalizationScene.kt")

    # Page5ClosingScene.kt
    p5 = "app/src/main/java/com/example/ui/prelude/Page5ClosingScene.kt"
    with open(p5, "r", encoding="utf-8") as f:
        c = f.read()
    c = ensure_imports(c)
    c = c.replace('contentDescription = "Five Lights closing scene"', 'contentDescription = stringResource(R.string.cd_prelude_closing_scene)')
    c = c.replace('text = "Made for your journey.",', 'text = stringResource(R.string.prelude_closing_title),')
    c = c.replace('text = "FiveLight",', 'text = stringResource(R.string.app_name),')
    c = c.replace('text = "Begin your journey",', 'text = stringResource(R.string.action_begin_journey),')
    c = c.replace('contentDescription = "Begin journey",', 'contentDescription = stringResource(R.string.cd_begin_journey),')
    with open(p5, "w", encoding="utf-8") as f:
        f.write(c)
    print("Migrated Page5ClosingScene.kt")

if __name__ == "__main__":
    migrate_prelude_files()
