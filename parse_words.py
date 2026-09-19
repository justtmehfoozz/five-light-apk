import re
import json

BW_MAP = {
    "'": "ء", ">": "أ", "&": "ؤ", "<": "إ", "}": "ئ",
    "A": "ا", "b": "ب", "p": "ة", "t": "ت", "v": "ث",
    "j": "ج", "H": "ح", "x": "خ", "d": "د", "*": "ذ",
    "r": "ر", "z": "ز", "s": "س", "$": "ش", "S": "ص",
    "D": "ض", "T": "ط", "Z": "ظ", "E": "ع", "g": "غ",
    "_": "ـ", "f": "ف", "q": "ق", "k": "ك", "l": "ل",
    "m": "م", "n": "ن", "h": "ه", "w": "و", "Y": "ى",
    "y": "ي", "F": "ً", "N": "ٍ", "K": "ٌ", "a": "َ",
    "u": "ُ", "i": "ِ", "~": "ّ", "o": "ْ", "`": "ٰ",
    "{": "ٱ"
}

def bw_to_ar(s):
    if not s:
        return ""
    return "".join(BW_MAP.get(c, c) for c in s)

with open("app/src/main/assets/quran_complete.json", "r", encoding="utf-8") as f:
    quran = json.load(f)

# Load morphology
with open("quranic-corpus-morphology-0.4.txt", "r", encoding="utf-8") as f:
    morph_lines = [l for l in f if l.strip() and not l.startswith("#")]

# Group by (surah, ayah, word)
words_map = {} # (s, a, w) -> list of segments
for line in morph_lines[1:]: # skip header
    parts = line.strip().split("\t")
    if len(parts) < 4:
        continue
    loc = parts[0] # (s:a:w:seg)
    m = re.match(r"\((\d+):(\d+):(\d+):(\d+)\)", loc)
    if not m:
        continue
    s, a, w, seg = int(m.group(1)), int(m.group(2)), int(m.group(3)), int(m.group(4))
    form_bw = parts[1]
    tag = parts[2]
    features = parts[3]
    key = (s, a, w)
    if key not in words_map:
        words_map[key] = []
    words_map[key].append((seg, form_bw, tag, features))

print("Total words in corpus:", len(words_map))

# Verify matching with quran_complete.json verses
mismatches = 0
total_checked_verses = 0
for s_str, verses in quran.items():
    s = int(s_str)
    for v in verses:
        a = v["verseNumber"]
        text = v["textArabic"].strip().replace("\ufeff", "")
        # Quran text words split
        q_words = text.split()
        # Corpus words for this verse
        c_words = [k[2] for k in words_map if k[0] == s and k[1] == a]
        c_count = max(c_words) if c_words else 0
        total_checked_verses += 1
        if len(q_words) != c_count:
            mismatches += 1
            if mismatches <= 5:
                print(f"Mismatch at {s}:{a} -> quran words={len(q_words)}, corpus words={c_count}")

print(f"Total verses: {total_checked_verses}, Mismatches count: {mismatches}")
