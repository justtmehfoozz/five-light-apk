import os

for root, _, files in os.walk('app/src/main/java/com/example/ui/screens'):
    for file in files:
        if file.endswith('.kt'):
            path = os.path.join(root, file)
            with open(path, 'r') as f:
                content = f.read()
            
            # fix collapsed imports
            content = content.replace("screensimport", "screens\nimport")
            content = content.replace("themeimport", "theme\nimport")
            content = content.replace("Successimport", "Success\nimport")
            content = content.replace("Errorimport", "Error\nimport")
            content = content.replace("Surfaceimport", "Surface\nimport")
            content = content.replace("Elevatedimport", "Elevated\nimport")
            content = content.replace("Textimport", "Text\nimport")
            content = content.replace("Backgroundimport", "Background\nimport")
            content = content.replace("Warningimport", "Warning\nimport")

            with open(path, 'w') as f:
                f.write(content)
