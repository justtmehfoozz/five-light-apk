import re
import json

# Check how words in corpus look for surah 1 and surah 2:1-5
# Check form_bw, stems, prefixes, suffixes
with open("quranic-corpus-morphology-0.4.txt", "r", encoding="utf-8") as f:
    lines = [l.strip() for l in f if l.startswith("(1:") or l.startswith("(2:1:") or l.startswith("(2:2:")]

for l in lines[:25]:
    print(l)
