import os

scenes = [
    "app/src/main/java/com/example/ui/prelude/Page0OpeningScene.kt",
    "app/src/main/java/com/example/ui/prelude/Page1PrayerScene.kt",
    "app/src/main/java/com/example/ui/prelude/Page2RemembranceScene.kt",
    "app/src/main/java/com/example/ui/prelude/Page3QiblaScene.kt",
    "app/src/main/java/com/example/ui/prelude/Page4PersonalizationScene.kt",
    "app/src/main/java/com/example/ui/prelude/Page5ClosingScene.kt",
]

for p in scenes:
    with open(p, "r", encoding="utf-8") as f:
        lines = f.readlines()
    seen_scene = False
    new_lines = []
    for line in lines:
        if "val sceneContentDescription = stringResource(" in line:
            if seen_scene:
                continue
            seen_scene = True
        new_lines.append(line)
    with open(p, "w", encoding="utf-8") as f:
        f.writelines(new_lines)
    print(f"Cleaned {p}")
