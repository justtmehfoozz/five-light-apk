import os
import re

def fix_prelude_scenes():
    scenes = [
        ("app/src/main/java/com/example/ui/prelude/Page0OpeningScene.kt", "R.string.cd_prelude_opening_scene"),
        ("app/src/main/java/com/example/ui/prelude/Page1PrayerScene.kt", "R.string.cd_prelude_prayer_scene"),
        ("app/src/main/java/com/example/ui/prelude/Page2RemembranceScene.kt", "R.string.cd_prelude_remembrance_scene"),
        ("app/src/main/java/com/example/ui/prelude/Page3QiblaScene.kt", "R.string.cd_prelude_qibla_scene"),
        ("app/src/main/java/com/example/ui/prelude/Page4PersonalizationScene.kt", "R.string.cd_prelude_personalization_scene"),
        ("app/src/main/java/com/example/ui/prelude/Page5ClosingScene.kt", "R.string.cd_prelude_closing_scene"),
    ]
    for path, res_id in scenes:
        with open(path, "r", encoding="utf-8") as f:
            c = f.read()
        # remove stringResource from inside semantics block
        c = c.replace(f'contentDescription = stringResource({res_id})', 'contentDescription = sceneContentDescription')
        # add val sceneContentDescription = stringResource(...) at start of function
        c = re.sub(r'(val isDark = [^\n]+)', f'val sceneContentDescription = stringResource({res_id})\n    \\1', c, count=1)
        with open(path, "w", encoding="utf-8") as f:
            f.write(c)
        print(f"Fixed {path}")

def fix_all_duplicate_imports():
    for root, dirs, files in os.walk("app/src/main/java/com/example"):
        for file in files:
            if file.endswith(".kt"):
                p = os.path.join(root, file)
                with open(p, "r", encoding="utf-8") as f:
                    lines = f.readlines()
                seen = set()
                new_lines = []
                for line in lines:
                    if line.startswith("import "):
                        if line in seen:
                            continue
                        seen.add(line)
                    new_lines.append(line)
                with open(p, "w", encoding="utf-8") as f:
                    f.writelines(new_lines)

if __name__ == "__main__":
    fix_prelude_scenes()
    fix_all_duplicate_imports()
