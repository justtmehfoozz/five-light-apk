import json

with open("app/src/main/assets/quran_complete.json", "r", encoding="utf-8") as f:
    quran = json.load(f)

for ref in ['37', '95', '97']:
    for v in quran[ref]:
        if v['verseNumber'] == 1:
            print(f"{ref}:1 ->", repr(v['textArabic']))
