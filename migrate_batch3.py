import os
import re

def ensure_imports(content):
    if "import androidx.compose.ui.res.stringResource" not in content and "@Composable" in content:
        content = re.sub(r'(package [^\n]+)', r'\1\n\nimport androidx.compose.ui.res.stringResource\nimport com.example.R', content, count=1)
    elif "import com.example.R" not in content:
        content = re.sub(r'(package [^\n]+)', r'\1\n\nimport com.example.R', content, count=1)
    return content

def migrate_splash_screen():
    path = "app/src/main/java/com/example/ui/screens/SplashScreen.kt"
    with open(path, "r", encoding="utf-8") as f:
        c = f.read()
    c = ensure_imports(c)
    c = c.replace('contentDescription = "FiveLight"', 'contentDescription = stringResource(R.string.app_name)')
    c = c.replace('contentDescription = "light appearing from darkness"', 'contentDescription = stringResource(R.string.prelude_tagline)')
    with open(path, "w", encoding="utf-8") as f:
        f.write(c)
    print("Migrated SplashScreen.kt")

def migrate_explore_screen():
    path = "app/src/main/java/com/example/ui/screens/ExploreScreen.kt"
    with open(path, "r", encoding="utf-8") as f:
        c = f.read()
    c = ensure_imports(c)
    c = c.replace('title = "Explore"', 'title = stringResource(R.string.explore_title)')
    c = c.replace('text = "Explore"', 'text = stringResource(R.string.explore_title)')
    c = c.replace('text = "Dua Library"', 'text = stringResource(R.string.explore_dua_library)')
    c = c.replace('text = "Daily Adhkar"', 'text = stringResource(R.string.explore_daily_adhkar)')
    c = c.replace('text = "99 Names of Allah"', 'text = stringResource(R.string.explore_names_of_allah)')
    c = c.replace('text = "Islamic Calendar"', 'text = stringResource(R.string.explore_islamic_calendar)')
    c = c.replace('contentDescription = "Back"', 'contentDescription = stringResource(R.string.action_back)')
    with open(path, "w", encoding="utf-8") as f:
        f.write(c)
    print("Migrated ExploreScreen.kt")

def migrate_dua_library_screen():
    path = "app/src/main/java/com/example/ui/screens/DuaLibraryScreen.kt"
    with open(path, "r", encoding="utf-8") as f:
        c = f.read()
    c = ensure_imports(c)
    c = c.replace('title = "Dua Library"', 'title = stringResource(R.string.explore_dua_library)')
    c = c.replace('text = "Dua Library"', 'text = stringResource(R.string.explore_dua_library)')
    c = c.replace('text = "Categories"', 'text = stringResource(R.string.dua_categories)')
    c = c.replace('text = "Saved Duas"', 'text = stringResource(R.string.dua_bookmarked)')
    c = c.replace('contentDescription = "Back"', 'contentDescription = stringResource(R.string.action_back)')
    c = c.replace('contentDescription = "Search"', 'contentDescription = stringResource(R.string.action_search)')
    c = c.replace('contentDescription = "Clear"', 'contentDescription = stringResource(R.string.action_clear)')
    with open(path, "w", encoding="utf-8") as f:
        f.write(c)
    print("Migrated DuaLibraryScreen.kt")

def migrate_adhkar_screen():
    path = "app/src/main/java/com/example/ui/screens/AdhkarScreen.kt"
    with open(path, "r", encoding="utf-8") as f:
        c = f.read()
    c = ensure_imports(c)
    c = c.replace('title = "Daily Adhkar"', 'title = stringResource(R.string.explore_daily_adhkar)')
    c = c.replace('text = "Daily Adhkar"', 'text = stringResource(R.string.explore_daily_adhkar)')
    c = c.replace('text = "Morning"', 'text = stringResource(R.string.adhkar_morning)')
    c = c.replace('text = "Evening"', 'text = stringResource(R.string.adhkar_evening)')
    c = c.replace('text = "After Prayer"', 'text = stringResource(R.string.adhkar_after_prayer)')
    c = c.replace('text = "Completed"', 'text = stringResource(R.string.home_completed)')
    c = c.replace('contentDescription = "Back"', 'contentDescription = stringResource(R.string.action_back)')
    with open(path, "w", encoding="utf-8") as f:
        f.write(c)
    print("Migrated AdhkarScreen.kt")

def migrate_home_feature_cards():
    path = "app/src/main/java/com/example/ui/components/HomeFeatureCards.kt"
    with open(path, "r", encoding="utf-8") as f:
        c = f.read()
    c = ensure_imports(c)
    c = c.replace('text = "Continue Reading"', 'text = stringResource(R.string.home_feature_continue_reading)')
    c = c.replace('text = "Recently Read"', 'text = stringResource(R.string.home_feature_recently_read)')
    c = c.replace('text = "Reflection of the Day"', 'text = stringResource(R.string.home_feature_reflection)')
    c = c.replace('text = "Voluntary (Nafl) Prayers"', 'text = stringResource(R.string.home_feature_nafl_prayers)')
    c = c.replace('text = "Weekly Prayer Overview"', 'text = stringResource(R.string.home_feature_weekly_overview)')
    c = c.replace('text = "Friday Mubarak"', 'text = stringResource(R.string.home_feature_friday_moment)')
    c = c.replace('text = "Quran Lens"', 'text = stringResource(R.string.home_feature_quran_lens)')
    c = c.replace('text = "Prayer Journey"', 'text = stringResource(R.string.home_feature_prayer_journey)')
    c = c.replace('text = "Qada Tracker"', 'text = stringResource(R.string.home_qada_tracker)')
    with open(path, "w", encoding="utf-8") as f:
        f.write(c)
    print("Migrated HomeFeatureCards.kt")

def migrate_quran_screen():
    path = "app/src/main/java/com/example/ui/screens/QuranScreen.kt"
    with open(path, "r", encoding="utf-8") as f:
        c = f.read()
    c = ensure_imports(c)
    c = c.replace('text = "The Noble Quran"', 'text = stringResource(R.string.quran_title)')
    c = c.replace('text = "All Surahs"', 'text = stringResource(R.string.quran_all_surahs)')
    c = c.replace('text = "Bookmarks"', 'text = stringResource(R.string.quran_bookmarks)')
    c = c.replace('text = "Reader options"', 'text = stringResource(R.string.quran_reader_options)')
    c = c.replace('text = "Translation"', 'text = stringResource(R.string.quran_translation_toggle)')
    c = c.replace('text = "Text size"', 'text = stringResource(R.string.quran_text_size)')
    c = c.replace('contentDescription = "Back"', 'contentDescription = stringResource(R.string.action_back)')
    c = c.replace('contentDescription = "Search"', 'contentDescription = stringResource(R.string.action_search)')
    with open(path, "w", encoding="utf-8") as f:
        f.write(c)
    print("Migrated QuranScreen.kt")

if __name__ == "__main__":
    migrate_splash_screen()
    migrate_explore_screen()
    migrate_dua_library_screen()
    migrate_adhkar_screen()
    migrate_home_feature_cards()
    migrate_quran_screen()
