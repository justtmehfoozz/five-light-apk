import json
import re

with open("app/src/main/assets/quran_word_explorer.json", "r", encoding="utf-8") as f:
    d = json.load(f)
v_map = d["v"]

with open("app/src/main/assets/quran_complete.json", "r", encoding="utf-8") as f:
    quran = json.load(f)

# Let's test all verses in Surah 1, 2, 36, 114
for s_num in [1, 2, 36, 114]:
    verses = quran[str(s_num)]
    for v in verses:
        v_num = v["verseNumber"]
        key = f"{s_num}:{v_num}"
        raw_text = v["textArabic"]
        words_data = v_map.get(key, [])
        
        tokens = []
        for m in re.finditer(r'\S+', raw_text):
            tok = m.group()
            if re.search(r'[\u0621-\u064A\u0671]', tok):
                tokens.append((tok, m.start(), m.end()))
        
        assert len(tokens) == len(words_data), f"Mismatch in {key}: {len(tokens)} vs {len(words_data)}"

print("Assertion passed for all tested verses!")
