import os
import re

def ensure_imports(content):
    if "import androidx.compose.ui.res.stringResource" not in content and "@Composable" in content:
        content = re.sub(r'(package [^\n]+)', r'\1\n\nimport androidx.compose.ui.res.stringResource\nimport com.example.R', content, count=1)
    elif "import com.example.R" not in content:
        content = re.sub(r'(package [^\n]+)', r'\1\n\nimport com.example.R', content, count=1)
    return content

def migrate_qibla_screen():
    path = "app/src/main/java/com/example/ui/screens/QiblaScreen.kt"
    with open(path, "r", encoding="utf-8") as f:
        c = f.read()
    c = ensure_imports(c)
    c = c.replace('title = "Qibla Compass"', 'title = stringResource(R.string.qibla_title)')
    c = c.replace('text = "Qibla Compass"', 'text = stringResource(R.string.qibla_title)')
    c = c.replace('text = "You are facing the Kaaba"', 'text = stringResource(R.string.qibla_facing_kaaba)')
    c = c.replace('text = "Low compass accuracy. Calibrate by waving your device in a figure 8."', 'text = stringResource(R.string.qibla_sensor_unreliable)')
    c = c.replace('text = "Compass sensor not available on this device."', 'text = stringResource(R.string.qibla_sensor_unavailable)')
    c = c.replace('text = "Calibrate"', 'text = stringResource(R.string.qibla_calibrate)')
    c = c.replace('contentDescription = "Back"', 'contentDescription = stringResource(R.string.action_back)')
    with open(path, "w", encoding="utf-8") as f:
        f.write(c)
    print("Migrated QiblaScreen.kt")

def migrate_tasbeeh_screen():
    path = "app/src/main/java/com/example/ui/screens/TasbeehScreen.kt"
    with open(path, "r", encoding="utf-8") as f:
        c = f.read()
    c = ensure_imports(c)
    c = c.replace('text = "Tasbeeh"', 'text = stringResource(R.string.tasbeeh_title)')
    c = c.replace('text = "Reset Counter"', 'text = stringResource(R.string.tasbeeh_reset_counter)')
    c = c.replace('text = "Target Reached!"', 'text = stringResource(R.string.tasbeeh_target_reached)')
    c = c.replace('text = "Custom Dhikr"', 'text = stringResource(R.string.tasbeeh_custom_dhikr)')
    c = c.replace('text = "Add Custom Dhikr"', 'text = stringResource(R.string.tasbeeh_add_dhikr)')
    c = c.replace('text = "Edit Dhikr"', 'text = stringResource(R.string.tasbeeh_edit_dhikr)')
    c = c.replace('text = "Delete Dhikr"', 'text = stringResource(R.string.tasbeeh_delete_dhikr)')
    c = c.replace('text = "Restore Default Dhikrs"', 'text = stringResource(R.string.tasbeeh_restore_defaults)')
    c = c.replace('text = "Dhikr History"', 'text = stringResource(R.string.tasbeeh_history)')
    c = c.replace('text = "Vibration Feedback"', 'text = stringResource(R.string.settings_vibration)')
    c = c.replace('text = "Tap Sound"', 'text = stringResource(R.string.settings_tap_sound)')
    c = c.replace('text = "Tap anywhere to count"', 'text = stringResource(R.string.tasbeeh_tap_to_count)')
    c = c.replace('text = "Cancel"', 'text = stringResource(R.string.action_cancel)')
    c = c.replace('text = "Save"', 'text = stringResource(R.string.action_save)')
    c = c.replace('text = "Done"', 'text = stringResource(R.string.action_done)')
    c = c.replace('text = "Delete"', 'text = stringResource(R.string.action_delete)')
    with open(path, "w", encoding="utf-8") as f:
        f.write(c)
    print("Migrated TasbeehScreen.kt")

def migrate_names_screen():
    path = "app/src/main/java/com/example/ui/screens/NamesOfAllahScreen.kt"
    with open(path, "r", encoding="utf-8") as f:
        c = f.read()
    c = ensure_imports(c)
    c = c.replace('title = "Names of Allah"', 'title = stringResource(R.string.explore_names_of_allah)')
    c = c.replace('text = "Names of Allah"', 'text = stringResource(R.string.explore_names_of_allah)')
    c = c.replace('text = "The 99 Beautiful Names (Asma-ul-Husna)"', 'text = stringResource(R.string.explore_names_subtitle)')
    c = c.replace('contentDescription = "Back"', 'contentDescription = stringResource(R.string.action_back)')
    with open(path, "w", encoding="utf-8") as f:
        f.write(c)
    print("Migrated NamesOfAllahScreen.kt")

def migrate_calendar_screen():
    path = "app/src/main/java/com/example/ui/screens/CalendarScreen.kt"
    with open(path, "r", encoding="utf-8") as f:
        c = f.read()
    c = ensure_imports(c)
    c = c.replace('text = "Islamic Calendar"', 'text = stringResource(R.string.explore_islamic_calendar)')
    c = c.replace('text = "Today"', 'text = stringResource(R.string.calendar_today)')
    c = c.replace('contentDescription = "Previous Month"', 'contentDescription = stringResource(R.string.action_back)')
    c = c.replace('contentDescription = "Next Month"', 'contentDescription = stringResource(R.string.action_next)')
    with open(path, "w", encoding="utf-8") as f:
        f.write(c)
    print("Migrated CalendarScreen.kt")

def migrate_profile_subscreen():
    path = "app/src/main/java/com/example/ui/screens/ProfileSubScreen.kt"
    with open(path, "r", encoding="utf-8") as f:
        c = f.read()
    c = ensure_imports(c)
    c = c.replace('title = "Profile"', 'title = stringResource(R.string.profile_title)')
    c = c.replace('text = "Profile"', 'text = stringResource(R.string.profile_title)')
    c = c.replace('text = "Change Password"', 'text = stringResource(R.string.profile_change_password)')
    c = c.replace('text = "Delete Account"', 'text = stringResource(R.string.profile_delete_account)')
    c = c.replace('text = "Google Account"', 'text = stringResource(R.string.profile_google_account)')
    c = c.replace('text = "Email Account"', 'text = stringResource(R.string.profile_email_account)')
    c = c.replace('text = "Cancel"', 'text = stringResource(R.string.action_cancel)')
    c = c.replace('text = "Save"', 'text = stringResource(R.string.action_save)')
    c = c.replace('text = "Sign Out"', 'text = stringResource(R.string.action_sign_out)')
    c = c.replace('contentDescription = "Back"', 'contentDescription = stringResource(R.string.action_back)')
    with open(path, "w", encoding="utf-8") as f:
        f.write(c)
    print("Migrated ProfileSubScreen.kt")

def migrate_bottom_nav():
    path = "app/src/main/java/com/example/ui/components/BottomNavBar.kt"
    with open(path, "r", encoding="utf-8") as f:
        c = f.read()
    c = ensure_imports(c)
    c = c.replace('label = "Home"', 'label = stringResource(R.string.nav_home)')
    c = c.replace('label = "Qibla"', 'label = stringResource(R.string.nav_qibla)')
    c = c.replace('label = "Quran"', 'label = stringResource(R.string.nav_quran)')
    c = c.replace('label = "Tasbeeh"', 'label = stringResource(R.string.nav_tasbeeh)')
    c = c.replace('label = "Explore"', 'label = stringResource(R.string.nav_explore)')
    with open(path, "w", encoding="utf-8") as f:
        f.write(c)
    print("Migrated BottomNavBar.kt")

if __name__ == "__main__":
    migrate_qibla_screen()
    migrate_tasbeeh_screen()
    migrate_names_screen()
    migrate_calendar_screen()
    migrate_profile_subscreen()
    migrate_bottom_nav()
