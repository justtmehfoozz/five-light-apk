import json
import re

with open("app/src/main/assets/quran_word_explorer.json", "r", encoding="utf-8") as f:
    d = json.load(f)

v_map = d["v"]

with open("app/src/main/assets/quran_complete.json", "r", encoding="utf-8") as f:
    quran = json.load(f)

mismatches = 0
checked = 0
for s_str, verses in quran.items():
    s_num = int(s_str)
    for v in verses:
        v_num = v["verse_number"] if "verse_number" in v else v.get("id", 0)
        key = f"{s_num}:{v_num}"
        words_data = v_map.get(key, [])
        
        raw_text = v.get("text_madani") or v.get("text") or v.get("arabic") or ""
        raw_tokens = raw_text.split()
        letter_tokens = [t for t in raw_tokens if re.search(r'[\u0621-\u064A\u0671]', t)]
        
        if len(letter_tokens) != len(words_data):
            mismatches += 1
            if mismatches <= 5:
                print(f"Mismatch in {key}: letter tokens={len(letter_tokens)}, dataset words={len(words_data)}")
        checked += 1

print(f"Total checked: {checked}, mismatches: {mismatches}")
