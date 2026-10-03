import os
import re

JAVA_DIR = "app/src/main/java/com/example"

# Regular expression to find quoted strings
STRING_REGEX = re.compile(r'"([^"\\]*(?:\\.[^"\\]*)*)"')

ignore_prefixes = [
    "http://", "https://", "android.permission", "com.example", "yyyy", "HH:mm",
    "fivelight_", "auth_", "pref_", "btn_", "input_", "tag_", "SELECT ", "UPDATE ", "INSERT ", "DELETE "
]

def scan_files():
    results = {}
    for root, dirs, files in os.walk(JAVA_DIR):
        for f in files:
            if f.endswith(".kt"):
                path = os.path.join(root, f)
                with open(path, "r", encoding="utf-8") as fp:
                    content = fp.read()
                # find string literals
                matches = STRING_REGEX.findall(content)
                rel_path = os.path.relpath(path, JAVA_DIR)
                results[rel_path] = matches
    return results

if __name__ == "__main__":
    res = scan_files()
    for file, strings in sorted(res.items()):
        user_strings = [s for s in strings if len(s) > 1 and not any(s.startswith(p) for p in ignore_prefixes)]
        print(f"=== {file} ({len(user_strings)} strings) ===")
        for s in user_strings[:10]:
            print(f"  {s}")
