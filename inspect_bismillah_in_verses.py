import json

with open("app/src/main/assets/quran_complete.json", "r", encoding="utf-8") as f:
    quran = json.load(f)

embedded = []
for s_str, verses in quran.items():
    s = int(s_str)
    if s == 1: continue # Al-Fatihah 1:1 IS Bismillah
    for v in verses:
        if v["verseNumber"] == 1:
            t = v["textArabic"].strip().replace("\ufeff", "")
            if "بِسْمِ" in t or "بِّسْمِ" in t or "بِسمِ" in t:
                embedded.append(s)

print("Surahs with Bismillah prefix in verse 1:", len(embedded), embedded)
