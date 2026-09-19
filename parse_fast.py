import re
import json

with open("app/src/main/assets/quran_complete.json", "r", encoding="utf-8") as f:
    quran = json.load(f)

# Load max word per (s, a) from corpus
verse_word_max = {}
with open("quranic-corpus-morphology-0.4.txt", "r", encoding="utf-8") as f:
    for line in f:
        if not line or line[0] != '(':
            continue
        # Extract location up to tab
        idx = line.find('\t')
        loc = line[:idx] # (s:a:w:seg)
        # Quick parse
        inner = loc[1:-1]
        parts = inner.split(':')
        s, a, w = int(parts[0]), int(parts[1]), int(parts[2])
        key = (s, a)
        if key not in verse_word_max or w > verse_word_max[key]:
            verse_word_max[key] = w

print("Loaded verse word counts from corpus:", len(verse_word_max))

mismatches = 0
for s_str, verses in quran.items():
    s = int(s_str)
    for v in verses:
        a = v["verseNumber"]
        text = v["textArabic"].strip().replace("\ufeff", "")
        q_words = text.split()
        c_count = verse_word_max.get((s, a), 0)
        if len(q_words) != c_count:
            mismatches += 1
            if mismatches <= 5:
                print(f"Mismatch at {s}:{a} -> quran words={len(q_words)}, corpus words={c_count}")

print(f"Total checked. Mismatches: {mismatches}")
