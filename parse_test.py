import re
import json

with open("quranic-corpus-morphology-0.4.txt", "r", encoding="utf-8") as f:
    text = f.read()

lines = [l for l in text.splitlines() if l and not l.startswith("#")]
print("Lines count:", len(lines))

# Sample first 20 lines
for l in lines[:15]:
    print(l)
