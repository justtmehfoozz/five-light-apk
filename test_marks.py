import json
import unicodedata

with open("app/src/main/assets/quran_complete.json", "r", encoding="utf-8") as f:
    quran = json.load(f)

v2_2 = quran['2'][1]['textArabic'].strip().replace("\ufeff", "")
for tok in v2_2.split():
    # check if tok has arabic letters
    chars = [(c, unicodedata.name(c, 'UNKNOWN'), unicodedata.category(c)) for c in tok]
    has_letter = any(unicodedata.category(c).startswith('L') for c in tok)
    print(f"Token: {tok}, has_letter={has_letter}")
    if not has_letter:
        print("  Chars in standalone mark:", chars)
