import json

with open("app/src/main/assets/quran_complete.json", "r", encoding="utf-8") as f:
    quran = json.load(f)

v2_2 = quran['2'][1]['textArabic'].strip().replace("\ufeff", "")
print("2:2 raw text:", repr(v2_2))
print("2:2 split tokens:", v2_2.split())

# Let's see what Quranic Arabic Corpus has for 2:2
with open("quranic-corpus-morphology-0.4.txt", "r", encoding="utf-8") as f:
    for line in f:
        if line.startswith("(2:2:"):
            print(line.strip())
