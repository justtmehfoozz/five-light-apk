import os
import re

def ensure_imports(content):
    if "import androidx.compose.ui.res.stringResource" not in content and "@Composable" in content:
        content = re.sub(r'(package [^\n]+)', r'\1\n\nimport androidx.compose.ui.res.stringResource\nimport com.example.R', content, count=1)
    elif "import com.example.R" not in content:
        content = re.sub(r'(package [^\n]+)', r'\1\n\nimport com.example.R', content, count=1)
    return content

def migrate_prelogin():
    path = "app/src/main/java/com/example/ui/screens/PreLoginPromptScreen.kt"
    with open(path, "r", encoding="utf-8") as f:
        c = f.read()
    c = ensure_imports(c)
    c = c.replace('text = "Keep your journey with you."', 'text = stringResource(R.string.prelogin_title)')
    c = c.replace('text = "Sign in with your account to automatically sync your prayers, Quran bookmarks, and custom Dhikr across all your devices."', 'text = stringResource(R.string.prelogin_subtitle)')
    c = c.replace('text = "Sign In or Register"', 'text = stringResource(R.string.prelogin_sign_in_or_register)')
    c = c.replace('text = "Continue as Guest"', 'text = stringResource(R.string.prelogin_continue_as_guest)')
    c = c.replace('"Cloud Synchronization"', 'stringResource(R.string.prelogin_benefit_cloud_sync)')
    c = c.replace('"Seamlessly access your logs and daily progress on any device."', 'stringResource(R.string.prelogin_benefit_cloud_sync_desc)')
    c = c.replace('"Private & Secure Backup"', 'stringResource(R.string.prelogin_benefit_backup)')
    c = c.replace('"Your spiritual records and streaks are preserved safely in the cloud."', 'stringResource(R.string.prelogin_benefit_backup_desc)')
    c = c.replace('"Cross-Device Continuity"', 'stringResource(R.string.prelogin_benefit_cross_device)')
    c = c.replace('"Pick up right where you left off, from your phone or tablet."', 'stringResource(R.string.prelogin_benefit_cross_device_desc)')
    with open(path, "w", encoding="utf-8") as f:
        f.write(c)
    print("Migrated PreLoginPromptScreen.kt")

def migrate_login_bottom_sheet():
    path = "app/src/main/java/com/example/ui/screens/LoginBottomSheet.kt"
    with open(path, "r", encoding="utf-8") as f:
        c = f.read()
    c = ensure_imports(c)
    c = c.replace('text = "Log In to Continue"', 'text = stringResource(R.string.auth_login_to_continue)')
    c = c.replace('text = "Sign in or create an account to keep everything in sync."', 'text = stringResource(R.string.auth_login_subtitle)')
    c = c.replace('text = "Login with Email"', 'text = stringResource(R.string.auth_login_with_email)')
    c = c.replace('text = "Login with Google"', 'text = stringResource(R.string.auth_login_with_google)')
    c = c.replace('text = "Don\'t have an account?"', 'text = stringResource(R.string.auth_dont_have_account)')
    c = c.replace('text = "Register New Account"', 'text = stringResource(R.string.auth_register_new_account)')
    c = c.replace('text = "Close"', 'text = stringResource(R.string.action_close)')
    c = c.replace('contentDescription = "Email Login"', 'contentDescription = stringResource(R.string.cd_email_login)')
    c = c.replace('contentDescription = "Google Logo"', 'contentDescription = stringResource(R.string.cd_google_logo)')
    with open(path, "w", encoding="utf-8") as f:
        f.write(c)
    print("Migrated LoginBottomSheet.kt")

def migrate_login_screen():
    path = "app/src/main/java/com/example/ui/screens/LoginScreen.kt"
    with open(path, "r", encoding="utf-8") as f:
        c = f.read()
    c = ensure_imports(c)
    c = c.replace('text = "Welcome back"', 'text = stringResource(R.string.auth_welcome_back)')
    c = c.replace('text = "Login to your account"', 'text = stringResource(R.string.auth_login_title)')
    c = c.replace('text = "Email Address"', 'text = stringResource(R.string.auth_email_address)')
    c = c.replace('placeholder = { Text("Enter your email address") }', 'placeholder = { Text(stringResource(R.string.auth_email_placeholder)) }')
    c = c.replace('text = "Password"', 'text = stringResource(R.string.auth_password)')
    c = c.replace('placeholder = { Text("Enter your password") }', 'placeholder = { Text(stringResource(R.string.auth_password_placeholder)) }')
    c = c.replace('text = "Remember me"', 'text = stringResource(R.string.auth_remember_me)')
    c = c.replace('text = "Forgot password?"', 'text = stringResource(R.string.auth_forgot_password)')
    c = c.replace('text = "Login"', 'text = stringResource(R.string.auth_login_button)')
    c = c.replace('text = "Have an Issue in Login?"', 'text = stringResource(R.string.auth_have_issue)')
    c = c.replace('contentDescription = "Show password"', 'contentDescription = stringResource(R.string.cd_show_password)')
    c = c.replace('contentDescription = "Hide password"', 'contentDescription = stringResource(R.string.cd_hide_password)')
    c = c.replace('contentDescription = "Back"', 'contentDescription = stringResource(R.string.action_back)')
    with open(path, "w", encoding="utf-8") as f:
        f.write(c)
    print("Migrated LoginScreen.kt")

def migrate_register_screen():
    path = "app/src/main/java/com/example/ui/screens/RegisterScreen.kt"
    with open(path, "r", encoding="utf-8") as f:
        c = f.read()
    c = ensure_imports(c)
    c = c.replace('text = "Start your spiritual journey with FiveLight"', 'text = stringResource(R.string.auth_create_account_eyebrow)')
    c = c.replace('text = "Create Your Account"', 'text = stringResource(R.string.auth_create_account_title)')
    c = c.replace('text = "Full Name"', 'text = stringResource(R.string.auth_full_name)')
    c = c.replace('placeholder = { Text("Enter your full name") }', 'placeholder = { Text(stringResource(R.string.auth_full_name_placeholder)) }')
    c = c.replace('text = "Email Address"', 'text = stringResource(R.string.auth_email_address)')
    c = c.replace('placeholder = { Text("Enter your email address") }', 'placeholder = { Text(stringResource(R.string.auth_email_placeholder)) }')
    c = c.replace('text = "Password"', 'text = stringResource(R.string.auth_password)')
    c = c.replace('placeholder = { Text("Enter your password") }', 'placeholder = { Text(stringResource(R.string.auth_password_placeholder)) }')
    c = c.replace('text = "Confirm Password"', 'text = stringResource(R.string.auth_confirm_password)')
    c = c.replace('placeholder = { Text("Confirm your password") }', 'placeholder = { Text(stringResource(R.string.auth_confirm_password_placeholder)) }')
    c = c.replace('text = "Already have an account?"', 'text = stringResource(R.string.auth_already_have_account)')
    c = c.replace('contentDescription = "Show password"', 'contentDescription = stringResource(R.string.cd_show_password)')
    c = c.replace('contentDescription = "Hide password"', 'contentDescription = stringResource(R.string.cd_hide_password)')
    c = c.replace('contentDescription = "Back"', 'contentDescription = stringResource(R.string.action_back)')
    with open(path, "w", encoding="utf-8") as f:
        f.write(c)
    print("Migrated RegisterScreen.kt")

def migrate_email_verification():
    path = "app/src/main/java/com/example/ui/screens/EmailVerificationScreen.kt"
    with open(path, "r", encoding="utf-8") as f:
        c = f.read()
    c = ensure_imports(c)
    c = c.replace('text = "Verify your email"', 'text = stringResource(R.string.auth_verification_title)')
    c = c.replace('text = "We\'ve sent a verification link to your email address. Please open the email and tap the verification link to continue."', 'text = stringResource(R.string.auth_verification_subtitle)')
    c = c.replace('text = "Check your inbox"', 'text = stringResource(R.string.auth_check_inbox)')
    c = c.replace('text = "I\'ve Verified My Email"', 'text = stringResource(R.string.auth_verify_button)')
    c = c.replace('text = "Resend Verification Email"', 'text = stringResource(R.string.auth_resend_email)')
    c = c.replace('contentDescription = "Back"', 'contentDescription = stringResource(R.string.action_back)')
    with open(path, "w", encoding="utf-8") as f:
        f.write(c)
    print("Migrated EmailVerificationScreen.kt")

def migrate_setup():
    path = "app/src/main/java/com/example/ui/screens/SetUpFiveLightScreen.kt"
    with open(path, "r", encoding="utf-8") as f:
        c = f.read()
    c = ensure_imports(c)
    c = c.replace('text = "WELCOME"', 'text = stringResource(R.string.setup_welcome_eyebrow)')
    c = c.replace('text = "Make FiveLight yours."', 'text = stringResource(R.string.setup_title)')
    c = c.replace('text = "A few quick choices and FiveLight will be ready for your daily rhythm."', 'text = stringResource(R.string.setup_subtitle)')
    c = c.replace('text = "Where are you located?"', 'text = stringResource(R.string.setup_city_step_title)')
    c = c.replace('text = "Select your city to get exact prayer times and Qibla direction."', 'text = stringResource(R.string.setup_city_step_desc)')
    c = c.replace('text = "Calculation Method"', 'text = stringResource(R.string.setup_calc_step_title)')
    c = c.replace('text = "Choose the prayer calculation standard used in your region."', 'text = stringResource(R.string.setup_calc_step_desc)')
    c = c.replace('text = "Asr Calculation (Madhab)"', 'text = stringResource(R.string.setup_madhab_step_title)')
    c = c.replace('text = "Standard (Shafi\'i, Maliki, Hanbali) or Hanafi timing."', 'text = stringResource(R.string.setup_madhab_step_desc)')
    c = c.replace('text = "Appearance"', 'text = stringResource(R.string.setup_theme_step_title)')
    c = c.replace('text = "Choose your preferred visual theme."', 'text = stringResource(R.string.setup_theme_step_desc)')
    c = c.replace('text = "Get Started"', 'text = stringResource(R.string.setup_complete_button)')
    c = c.replace('text = "Recommended"', 'text = stringResource(R.string.setup_recommended_badge)')
    c = c.replace('text = "Skip"', 'text = stringResource(R.string.action_skip)')
    c = c.replace('text = "Next"', 'text = stringResource(R.string.action_next)')
    with open(path, "w", encoding="utf-8") as f:
        f.write(c)
    print("Migrated SetUpFiveLightScreen.kt")

def migrate_smart_notifications():
    path = "app/src/main/java/com/example/ui/screens/SmartPrayerNotificationsSubScreen.kt"
    with open(path, "r", encoding="utf-8") as f:
        c = f.read()
    c = ensure_imports(c)
    c = c.replace('title = "Smart Prayer Notifications"', 'title = stringResource(R.string.settings_prayer_notifications)')
    c = c.replace('text = "Smart Prayer Notifications"', 'text = stringResource(R.string.settings_prayer_notifications)')
    c = c.replace('text = "Calm, contextual notifications for prayer times, pre-prayer preparation, and voluntary windows."', 'text = stringResource(R.string.notif_calm_desc)')
    c = c.replace('text = "Notification Permission Required"', 'text = stringResource(R.string.notif_permission_required)')
    c = c.replace('text = "To receive prayer time alerts and spiritual reminders, please enable notifications."', 'text = stringResource(R.string.notif_permission_desc)')
    c = c.replace('text = "Enable Notifications"', 'text = stringResource(R.string.action_enable_notifications)')
    c = c.replace('contentDescription = "Back"', 'contentDescription = stringResource(R.string.action_back)')
    with open(path, "w", encoding="utf-8") as f:
        f.write(c)
    print("Migrated SmartPrayerNotificationsSubScreen.kt")

def migrate_app_updates():
    path = "app/src/main/java/com/example/ui/screens/AppUpdatesSubScreen.kt"
    with open(path, "r", encoding="utf-8") as f:
        c = f.read()
    c = ensure_imports(c)
    c = c.replace('title = "App Updates"', 'title = stringResource(R.string.updater_title)')
    c = c.replace('text = "Official Release Build"', 'text = stringResource(R.string.updater_official_build)')
    c = c.replace('text = "Check for Updates"', 'text = stringResource(R.string.updater_check_now)')
    c = c.replace('text = "Check Now"', 'text = stringResource(R.string.updater_check_now)')
    c = c.replace('text = "Checking for updates..."', 'text = stringResource(R.string.updater_checking)')
    c = c.replace('text = "FiveLight is up to date"', 'text = stringResource(R.string.updater_up_to_date)')
    c = c.replace('text = "Update Available"', 'text = stringResource(R.string.updater_update_available)')
    c = c.replace('text = "Update Now"', 'text = stringResource(R.string.updater_update_now)')
    c = c.replace('text = "Install Update"', 'text = stringResource(R.string.updater_install)')
    c = c.replace('text = "Retry"', 'text = stringResource(R.string.action_retry)')
    with open(path, "w", encoding="utf-8") as f:
        f.write(c)
    print("Migrated AppUpdatesSubScreen.kt")

if __name__ == "__main__":
    migrate_prelogin()
    migrate_login_bottom_sheet()
    migrate_login_screen()
    migrate_register_screen()
    migrate_email_verification()
    migrate_setup()
    migrate_smart_notifications()
    migrate_app_updates()
