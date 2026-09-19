import json
import unicodedata

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
    return "".join(BW_MAP.get(c, c) for c in s if c in BW_MAP)

# Combine segments of each word in corpus
corpus_words = {} # (s, a, w) -> {"form_ar": ..., "root": ..., "lem": ..., "pos": ...}
with open("quranic-corpus-morphology-0.4.txt", "r", encoding="utf-8") as f:
    for line in f:
        if not line or line[0] != '(':
            continue
        parts = line.strip().split('\t')
        loc = parts[0][1:-1]
        s, a, w, seg = map(int, loc.split(':'))
        key = (s, a, w)
        if key not in corpus_words:
            corpus_words[key] = {
                "forms": [],
                "pos": "",
                "root": "",
                "lem": "",
                "stem_pos": ""
            }
        form_bw = parts[1]
        tag = parts[2]
        features = parts[3]
        corpus_words[key]["forms"].append(form_bw)
        
        # Check if stem
        if "STEM" in features or not corpus_words[key]["pos"]:
            corpus_words[key]["pos"] = tag
        
        import re
        m_r = re.search(r'ROOT:([^|]+)', features)
        if m_r:
            corpus_words[key]["root"] = bw_to_ar(m_r.group(1))
        m_l = re.search(r'LEM:([^|]+)', features)
        if m_l:
            corpus_words[key]["lem"] = bw_to_ar(m_l.group(1))

# Compare for 1:1, 1:2, 2:2
with open("app/src/main/assets/quran_complete.json", "r", encoding="utf-8") as f:
    quran = json.load(f)

for ref in [(1,1), (1,2), (2,2)]:
    s, a = ref
    v = next(x for x in quran[str(s)] if x['verseNumber'] == a)
    q_tokens = [t for t in v['textArabic'].replace('\ufeff','').split() if any(unicodedata.category(c).startswith('L') for c in t)]
    print(f"\n--- Verse {s}:{a} ---")
    for w_idx, q_tok in enumerate(q_tokens, 1):
        c_info = corpus_words.get((s, a, w_idx), {})
        reconstructed_bw = "".join(c_info.get("forms", []))
        reconstructed_ar = bw_to_ar(reconstructed_bw)
        print(f"  Word {w_idx}: Quran=[{q_tok}] Corpus=[{reconstructed_ar}] Root=[{c_info.get('root')}] Lem=[{c_info.get('lem')}] Pos=[{c_info.get('pos')}]")
