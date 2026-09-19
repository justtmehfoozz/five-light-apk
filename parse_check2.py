import json
import unicodedata

with open("app/src/main/assets/quran_complete.json", "r", encoding="utf-8") as f:
    quran = json.load(f)

# Load max word per (s, a) from corpus
verse_word_max = {}
with open("quranic-corpus-morphology-0.4.txt", "r", encoding="utf-8") as f:
    for line in f:
        if not line or line[0] != '(':
            continue
        idx = line.find('\t')
        loc = line[:idx]
        inner = loc[1:-1]
        parts = inner.split(':')
        s, a, w = int(parts[0]), int(parts[1]), int(parts[2])
        key = (s, a)
        if key not in verse_word_max or w > verse_word_max[key]:
            verse_word_max[key] = w

mismatches = 0
for s_str, verses in quran.items():
    s = int(s_str)
    for v in verses:
        a = v["verseNumber"]
        text = v["textArabic"].strip().replace("\ufeff", "")
        # Filter tokens that have at least one letter (non-mark)
        tokens = [t for t in text.split() if any(unicodedata.category(c).startswith('L') for c in t)]
        c_count = verse_word_max.get((s, a), 0)
        if len(tokens) != c_count:
            mismatches += 1
            if mismatches <= 10:
                print(f"Mismatch at {s}:{a} -> letters-tokens={len(tokens)}, corpus words={c_count}")
                print("  Quran tokens:", tokens)

print(f"Total checked. Mismatches with letter filter: {mismatches}")
