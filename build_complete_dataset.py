import urllib.request
import json
import time
import os
import re
import unicodedata

# 1. Parse Quranic Arabic Corpus (morphology, roots, lemmas, pos)
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

POS_MAP = {
    "N": "Noun", "PN": "Proper Noun", "ADJ": "Adjective",
    "IMPN": "Imperative Verbal Noun", "PRON": "Pronoun",
    "DEM": "Demonstrative", "REL": "Relative Pronoun",
    "T": "Time Adverb", "LOC": "Location Adverb", "V": "Verb",
    "P": "Preposition", "CONJ": "Conjunction", "SUB": "Subordinating Conjunction",
    "ACC": "Accusative Particle", "ANS": "Answer Particle",
    "CAUS": "Particle of Cause", "CERT": "Particle of Certainty",
    "CIRC": "Circumstantial Particle", "COM": "Comitative Particle",
    "COND": "Conditional Particle", "EQ": "Equalization Particle",
    "EXH": "Exhortation Particle", "EXL": "Explanation Particle",
    "EXP": "Exceptive Particle", "FUT": "Future Particle",
    "INC": "Inception Particle", "INT": "Particle of Interpretation",
    "INTG": "Interrogative Particle", "NEG": "Negative Particle",
    "PREV": "Preventive Particle", "PRO": "Prohibition Particle",
    "REM": "Resumption Particle", "RES": "Restriction Particle",
    "RET": "Retraction Particle", "RSLT": "Result Particle",
    "SUP": "Supplemental Particle", "SUR": "Surprise Particle",
    "VOC": "Vocative Particle", "INL": "Quranic Initials"
}

print("Parsing Quranic Corpus morphology...")
corpus_data = {} # (s, a, w) -> {"root": ..., "lemma": ..., "pos": ..., "morph": ...}
root_occurrences = {} # root -> list of "s:a"
lemma_occurrences = {} # lemma -> list of "s:a"

with open("quranic-corpus-morphology-0.4.txt", "r", encoding="utf-8") as f:
    for line in f:
        if not line or line[0] != '(':
            continue
        parts = line.strip().split('\t')
        if len(parts) < 4:
            continue
        loc = parts[0][1:-1]
        s, a, w, seg = map(int, loc.split(':'))
        tag = parts[2]
        features = parts[3]
        
        key = (s, a, w)
        if key not in corpus_data:
            corpus_data[key] = {
                "root": "",
                "lemma": "",
                "pos": "",
                "tags": []
            }
        
        corpus_data[key]["tags"].append(tag)
        if "STEM" in features or not corpus_data[key]["pos"]:
            corpus_data[key]["pos"] = tag
            
        m_r = re.search(r'ROOT:([^|]+)', features)
        if m_r:
            r_ar = bw_to_ar(m_r.group(1))
            corpus_data[key]["root"] = r_ar
            ref = f"{s}:{a}"
            if r_ar not in root_occurrences:
                root_occurrences[r_ar] = set()
            root_occurrences[r_ar].add(ref)
            
        m_l = re.search(r'LEM:([^|]+)', features)
        if m_l:
            l_ar = bw_to_ar(m_l.group(1))
            corpus_data[key]["lemma"] = l_ar
            ref = f"{s}:{a}"
            if l_ar not in lemma_occurrences:
                lemma_occurrences[l_ar] = set()
            lemma_occurrences[l_ar].add(ref)

print(f"Corpus parsed: {len(corpus_data)} words, {len(root_occurrences)} roots, {len(lemma_occurrences)} lemmas.")

# Download word-by-word meanings and transliterations for all 114 chapters
print("Fetching word-by-word translations from verified Quran Foundation API...")
wbw_map = {} # (s, a, w) -> {"translit": ..., "meaning": ...}

for ch in range(1, 115):
    url = f"https://api.quran.com/api/v4/verses/by_chapter/{ch}?language=en&words=true&word_fields=translation,transliteration,location&per_page=300"
    for attempt in range(3):
        try:
            req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
            with urllib.request.urlopen(req, timeout=20) as resp:
                data = json.loads(resp.read().decode('utf-8'))
                for v in data.get('verses', []):
                    w_counter = 0
                    for word_obj in v.get('words', []):
                        if word_obj.get('char_type_name') == 'word':
                            w_counter += 1
                            s_num = ch
                            a_num = v['verse_number']
                            loc_parts = word_obj.get('location', '').split(':')
                            if len(loc_parts) == 3:
                                w_pos = int(loc_parts[2])
                            else:
                                w_pos = w_counter
                            translit = word_obj.get('transliteration', {}).get('text') or ""
                            meaning = word_obj.get('translation', {}).get('text') or ""
                            wbw_map[(s_num, a_num, w_pos)] = {
                                "translit": translit,
                                "meaning": meaning
                            }
            break
        except Exception as e:
            print(f"Retry {attempt+1} for chapter {ch}: {e}")
            time.sleep(1)
    if ch % 20 == 0 or ch == 114:
        print(f"  Processed {ch}/114 chapters (wbw words collected: {len(wbw_map)})...")

print(f"Total wbw collected: {len(wbw_map)}")

# Load existing quran_complete.json to ensure exact mapping with the app's verses
with open("app/src/main/assets/quran_complete.json", "r", encoding="utf-8") as f:
    quran = json.load(f)

# Build word exploration database
# Structure:
# {
#   "words": {
#      "s:a": [
#         {
#           "wordIndex": 0,
#           "exact": "ٱلْكِتَٰبُ",
#           "translit": "al-kitābu",
#           "meaning": "The Book",
#           "lemma": "كِتَٰب",
#           "root": "كتب",
#           "pos": "Noun",
#           "posCode": "N"
#         }, ...
#      ]
#   },
#   "rootOccurrences": {
#      "كتب": ["2:2", "2:44", ...]
#   },
#   "lemmaOccurrences": {
#      "كِتَٰب": ["2:2", ...]
#   }
# }

final_verse_words = {}

for s_str, verses in quran.items():
    s = int(s_str)
    for v in verses:
        a = v["verseNumber"]
        text = v["textArabic"].strip().replace("\ufeff", "")
        # Get individual tokens from Quran text
        raw_tokens = text.split()
        
        # Identify word tokens (tokens containing at least one Arabic letter)
        word_token_items = [] # list of (token_idx, token_str)
        for idx, tok in enumerate(raw_tokens):
            if any(unicodedata.category(c).startswith('L') for c in tok):
                word_token_items.append((idx, tok))
        
        verse_key = f"{s}:{a}"
        verse_words_list = []
        
        # Handle the special case where 95:1 and 97:1 have embedded Bismillah in textArabic
        # If (s == 95 or s == 97) and a == 1, the first 4 letter-tokens are Bismillah
        corpus_w_offset = 0
        if (s == 95 or s == 97) and a == 1 and len(word_token_items) > 4:
            # First 4 are Bismillah:
            bismillah_corpus = [(1,1,1), (1,1,2), (1,1,3), (1,1,4)]
            for b_i in range(4):
                tok_idx, tok_str = word_token_items[b_i]
                c_info = corpus_data.get(bismillah_corpus[b_i], {})
                w_info = wbw_map.get(bismillah_corpus[b_i], {})
                pos_code = c_info.get("pos", "")
                pos_name = POS_MAP.get(pos_code, pos_code)
                verse_words_list.append({
                    "tokenIndex": tok_idx,
                    "exact": tok_str,
                    "translit": w_info.get("translit", ""),
                    "meaning": w_info.get("meaning", ""),
                    "lemma": c_info.get("lemma", ""),
                    "root": c_info.get("root", ""),
                    "pos": pos_name,
                    "posCode": pos_code
                })
            corpus_w_offset = -4
            
        start_w = 4 if ((s == 95 or s == 97) and a == 1) else 0
        for w_i in range(start_w, len(word_token_items)):
            tok_idx, tok_str = word_token_items[w_i]
            corpus_w = w_i + 1 + corpus_w_offset
            c_info = corpus_data.get((s, a, corpus_w), {})
            w_info = wbw_map.get((s, a, corpus_w), {})
            pos_code = c_info.get("pos", "")
            pos_name = POS_MAP.get(pos_code, pos_code)
            verse_words_list.append({
                "tokenIndex": tok_idx,
                "exact": tok_str,
                "translit": w_info.get("translit", ""),
                "meaning": w_info.get("meaning", ""),
                "lemma": c_info.get("lemma", ""),
                "root": c_info.get("root", ""),
                "pos": pos_name,
                "posCode": pos_code
            })
            
        final_verse_words[verse_key] = verse_words_list

# Prepare serializable sets for occurrences
final_roots = {r: sorted(list(occ)) for r, occ in root_occurrences.items()}
final_lemmas = {l: sorted(list(occ)) for l, occ in lemma_occurrences.items()}

out_data = {
    "verses": final_verse_words,
    "rootOccurrences": final_roots,
    "lemmaOccurrences": final_lemmas
}

with open("app/src/main/assets/quran_word_explorer.json", "w", encoding="utf-8") as f:
    json.dump(out_data, f, ensure_ascii=False)

import os
size_mb = os.path.getsize("app/src/main/assets/quran_word_explorer.json") / (1024 * 1024)
print(f"Successfully generated app/src/main/assets/quran_word_explorer.json: {size_mb:.2f} MB")
