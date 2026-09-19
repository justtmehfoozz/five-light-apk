import re
import json

# Buckwalter to Arabic conversion
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

def bw_to_arabic(s):
    if not s:
        return ""
    # Strip diacritics / formatting like ^ etc if needed or convert directly
    res = []
    for c in s:
        if c in BW_MAP:
            res.append(BW_MAP[c])
        elif c in ['^', ':', '+', '|', '@']:
            continue
        else:
            res.append(c)
    return "".join(res)

POS_NAMES = {
    "N": "Noun",
    "PN": "Proper Noun",
    "ADJ": "Adjective",
    "IMPN": "Imperative Verbal Noun",
    "PRON": "Personal Pronoun",
    "DEM": "Demonstrative Pronoun",
    "REL": "Relative Pronoun",
    "T": "Time Adverb",
    "LOC": "Location Adverb",
    "V": "Verb",
    "P": "Preposition",
    "CONJ": "Conjunction",
    "SUB": "Subordinating Conjunction",
    "ACC": "Accusative Particle",
    "ANS": "Answer Particle",
    "CAUS": "Particle of Cause",
    "CERT": "Particle of Certainty",
    "CIRC": "Circumstantial Particle",
    "COM": "Comitative Particle",
    "COND": "Conditional Particle",
    "EQ": "Equalization Particle",
    "EXH": "Exhortation Particle",
    "EXL": "Explanation Particle",
    "EXP": "Exceptive Particle",
    "FUT": "Future Particle",
    "INC": "Inception Particle",
    "INT": "Particle of Interpretation",
    "INTG": "Interrogative Particle",
    "NEG": "Negative Particle",
    "PREV": "Preventive Particle",
    "PRO": "Prohibition Particle",
    "REM": "Resumption Particle",
    "RES": "Restriction Particle",
    "RET": "Retraction Particle",
    "RSLT": "Result Particle",
    "SUP": "Supplemental Particle",
    "SUR": "Surprise Particle",
    "VOC": "Vocative Particle",
    "INL": "Quranic Initials (Muqatta'at)"
}

print("Parsing corpus...")
# (s, a, w) -> list of segments
corpus_words = {}
roots_freq = {} # root_ar -> list of "s:a" occurrences
lemmas_freq = {} # lem_ar -> list of "s:a" occurrences

with open("quranic-corpus-morphology-0.4.txt", "r", encoding="utf-8") as f:
    for line in f:
        if not line or line[0] != '(':
            continue
        parts = line.strip().split('\t')
        if len(parts) < 4:
            continue
        loc = parts[0][1:-1] # s:a:w:seg
        s_str, a_str, w_str, seg_str = loc.split(':')
        s, a, w, seg = int(s_str), int(a_str), int(w_str), int(seg_str)
        form_bw = parts[1]
        tag = parts[2]
        features = parts[3]
        
        word_key = (s, a, w)
        if word_key not in corpus_words:
            corpus_words[word_key] = []
        corpus_words[word_key].append((seg, form_bw, tag, features))

print("Total corpus words parsed:", len(corpus_words))

# Sample 1:1:1 and 2:2:2
print("1:1:1 segments:", corpus_words[(1,1,1)])
print("2:2:2 segments:", corpus_words[(2,2,2)])
