import os

directory = './app/src/main/java/com/example/ui'

for root, dirs, files in os.walk(directory):
    for file in files:
        if file.endswith('.kt') and file != 'Color.kt':
            path = os.path.join(root, file)
            with open(path, 'r') as f:
                content = f.read()
            
            original_content = content
            
            content = content.replace('com.example.ui.theme.PrimaryAccentLight', 'Color.semanticPrimaryAccent')
            content = content.replace('com.example.ui.theme.PrimaryAccentDark', 'Color.semanticPrimaryAccent')
            content = content.replace('com.example.ui.theme.SuccessLight', 'Color.semanticSuccess')
            content = content.replace('com.example.ui.theme.SuccessDark', 'Color.semanticSuccess')
            content = content.replace('com.example.ui.theme.ErrorLight', 'Color.semanticError')
            content = content.replace('com.example.ui.theme.ErrorDark', 'Color.semanticError')
            
            if content != original_content:
                with open(path, 'w') as f:
                    f.write(content)
                print(f"Updated {file}")
