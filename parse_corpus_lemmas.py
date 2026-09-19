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
    if not s: return ""
    return "".join(BW_MAP.get(c, c) for c in s if c not in ['^', ':', '+', '|', '@'])

# Extract all roots and their occurrence counts
roots = {} # root_ar -> list of "s:a"
lemmas = {} # lem_ar -> list of "s:a"

with open("quranic-corpus-morphology-0.4.txt", "r", encoding="utf-8") as f:
    for line in f:
        if not line or line[0] != '(':
            continue
        parts = line.strip().split('\t')
        if len(parts) < 4:
            continue
        loc = parts[0][1:-1]
        s, a, w, seg = loc.split(':')
        features = parts[3]
        
        # Look for ROOT:xxx
        m_root = re.search(r'ROOT:([^|]+)', features)
        if m_root:
            r_bw = m_root.group(1)
            r_ar = bw_to_ar(r_bw)
            verse_ref = f"{s}:{a}"
            if r_ar not in roots:
                roots[r_ar] = set()
            roots[r_ar].add(verse_ref)
            
        m_lem = re.search(r'LEM:([^|]+)', features)
        if m_lem:
            l_bw = m_lem.group(1)
            l_ar = bw_to_ar(l_bw)
            verse_ref = f"{s}:{a}"
            if l_ar not in lemmas:
                lemmas[l_ar] = set()
            lemmas[l_ar].add(verse_ref)

print(f"Total unique roots found: {len(roots)}")
print(f"Total unique lemmas found: {len(lemmas)}")

# Test ktb (كتب), rHm (رحم), smw (سمو)
for r in ["كتب", "رحم", "علم", "نور"]:
    if r in roots:
        print(f"Root '{r}' appears in {len(roots[r])} verses, e.g. {sorted(list(roots[r]))[:5]}")
