import os

def replace_in_file(filepath, replacements):
    with open(filepath, 'r') as f:
        content = f.read()

    for old, new in replacements:
        content = content.replace(old, new)

    with open(filepath, 'w') as f:
        f.write(content)

directory = "app/src/main/java/com/example/ui/"
for root, dirs, files in os.walk(directory):
    for file in files:
        if file.endswith(".kt"):
            replace_in_file(os.path.join(root, file), [
                ("LightBorderVariant", "LightBorder"),
                ("LightBorder.copy(alpha=0.35f)Variant", "LightBorder.copy(alpha=0.35f)") # Just in case
            ])
