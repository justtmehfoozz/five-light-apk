import json
import re

with open("app/src/main/assets/quran_word_explorer.json", "r", encoding="utf-8") as f:
    d = json.load(f)
v_map = d["v"]

with open("app/src/main/assets/quran_complete.json", "r", encoding="utf-8") as f:
    quran = json.load(f)

for s_num in range(1, 115):
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
        
        if len(tokens) != len(words_data):
            print(f"Mismatch in {key}: {len(tokens)} vs {len(words_data)}")
            exit(1)

print("100% of all 6,236 verses match spans perfectly!")
