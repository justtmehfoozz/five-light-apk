import os

directory = 'app/src/main/java/com/example/ui/screens/'

for filename in os.listdir(directory):
    if filename.endswith('.kt'):
        path = os.path.join(directory, filename)
        with open(path, 'r') as f:
            content = f.read()

        # focusedBorderColor = Color.semanticPrimaryAccent
        # unfocusedBorderColor = Color.semanticBorder
        # This is already set in my previous run! Let's verify.
        
        # Check if OutlinedTextFieldDefaults is used
        if "focusedBorderColor = Color.semanticPrimaryAccent," in content:
            # Good
            pass

        with open(path, 'w') as f:
            f.write(content)
